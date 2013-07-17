package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.formatters.impl.xls.Area;
import com.haulmont.yarg.formatters.impl.xls.AreaDependencyManager;
import com.haulmont.yarg.formatters.impl.xls.Cell;
import com.haulmont.yarg.formatters.impl.xls.XlsToPdfConverterAPI;
import com.haulmont.yarg.formatters.impl.xls.caches.XlsFontCache;
import com.haulmont.yarg.formatters.impl.xls.caches.XlsStyleCache;
import com.haulmont.yarg.formatters.impl.xls.hints.*;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.ReportTemplate;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.impl.BandOrientation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.model.HSSFFormulaParser;
import org.apache.poi.hssf.record.EscherAggregate;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtg;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;

import static com.haulmont.yarg.formatters.impl.xls.HSSFCellHelper.getCellFromReference;
import static com.haulmont.yarg.formatters.impl.xls.HSSFCellHelper.isOneValueCell;
import static com.haulmont.yarg.formatters.impl.xls.HSSFPicturesHelper.getAllAnchors;
import static com.haulmont.yarg.formatters.impl.xls.HSSFRangeHelper.*;

/**
 * Document formatter for '.xls' file types
 */
public class XLSFormatter extends AbstractFormatter {
    protected static final String DYNAMIC_HEIGHT_STYLE = "styleWithoutHeight";

    protected static final String CELL_DYNAMIC_STYLE_SELECTOR = "##style=";
    protected static final String COPY_COLUMN_WIDTH_SELECTOR = "##copyColumnWidth";
    protected static final String AUTO_WIDTH_SELECTOR = "##autoWidth";
    protected static final String CUSTOM_WIDTH_SELECTOR = "##width=";

    protected HSSFWorkbook templateWorkbook;
    protected HSSFWorkbook resultWorkbook;

    protected HSSFSheet currentTemplateSheet = null;

    protected XlsFontCache fontCache = new XlsFontCache();
    protected XlsStyleCache styleCache = new XlsStyleCache();

    protected int rownum = 0;
    protected int colnum = 0;
    protected int rowsAddedByVerticalBand = 0;
    protected int rowsAddedByHorizontalBand = 0;

    protected Map<String, List<SheetRange>> mergeRegionsForRangeNames = new HashMap<String, List<SheetRange>>();
    protected Map<HSSFSheet, HSSFSheet> templateToResultSheetsMapping = new HashMap<HSSFSheet, HSSFSheet>();
    protected Map<String, Bounds> templateBounds = new HashMap<String, Bounds>();

    protected AreaDependencyManager areaDependencyManager = new AreaDependencyManager();
    protected Map<Area, List<Area>> areasDependency = areaDependencyManager.getAreasDependency();

    protected List<Integer> orderedPicturesId = new ArrayList<Integer>();
    protected Map<String, EscherAggregate> sheetToEscherAggregate = new HashMap<String, EscherAggregate>();

    protected Map<HSSFSheet, HSSFPatriarch> drawingPatriarchsMap = new HashMap<HSSFSheet, HSSFPatriarch>();
    protected List<XlsHint> optionContainer = new ArrayList<XlsHint>();

    protected XlsToPdfConverterAPI xlsToPdfConverter;

    public XLSFormatter(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        super(rootBand, reportTemplate, outputStream);
    }

    public void setXlsToPdfConverter(XlsToPdfConverterAPI xlsToPdfConverter) {
        this.xlsToPdfConverter = xlsToPdfConverter;
    }

    @Override
    public void renderDocument() {
        initWorkbook();

        processDocument();

        applyStyleOptions();

        outputDocument();
    }

    protected void initWorkbook() {
        try {
            templateWorkbook = new HSSFWorkbook(reportTemplate.getDocumentContent());
            resultWorkbook = new HSSFWorkbook(reportTemplate.getDocumentContent());
        } catch (IOException e) {
            throw wrapWithReportingException("An error occurred while parsing xls template " + reportTemplate.getDocumentName(), e);
        }

        for (int sheetNumber = 0; sheetNumber < templateWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet templateSheet = templateWorkbook.getSheetAt(sheetNumber);
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);
            templateToResultSheetsMapping.put(templateSheet, resultSheet);

            initMergeRegions(templateSheet);
            copyCharts(resultSheet);
            removeMergedRegions(resultSheet);
        }

        copyPicturesToResultWorkbook();
    }

    protected void processDocument() {
        for (BandData childBand : rootBand.getChildrenList()) {
            writeBand(childBand);
        }

        updateFormulas();
        copyPictures();
    }

    protected void applyStyleOptions() {
        for (XlsHint option : optionContainer) {
            option.apply();
        }
    }

    protected void outputDocument() {
        ReportOutputType outputType = reportTemplate.getOutputType();

        if (ReportOutputType.xls.equals(outputType)) {
            try {
                resultWorkbook.write(outputStream);
            } catch (Exception e) {
                throw wrapWithReportingException("An error occurred while writing result to file.", e);
            }
        } else if (ReportOutputType.pdf.equals(outputType)) {
            if (xlsToPdfConverter != null) {
                try {
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    resultWorkbook.write(stream);
                    xlsToPdfConverter.convertXlsToPdf(stream.toByteArray(), outputStream);
                } catch (IOException e) {
                    throw wrapWithReportingException("An error occurred while converting xls to pdf.", e);
                }
            } else {
                throw new UnsupportedFormatException("Could not convert xls files to pdf because Open Office connection params not set. Please check, that \"cuba.reporting.openoffice.path\" property is set in properties file.");
            }
        }
    }

    private void copyPicturesToResultWorkbook() {
        List<HSSFPictureData> allPictures = templateWorkbook.getAllPictures();
        for (HSSFPictureData allPicture : allPictures) {
            int i = resultWorkbook.addPicture(allPicture.getData(), Workbook.PICTURE_TYPE_JPEG);
            orderedPicturesId.add(i);
        }
    }

    private void removeMergedRegions(HSSFSheet resultSheet) {
        for (int i = 0, size = resultSheet.getNumMergedRegions(); i < size; i++) {
            resultSheet.removeMergedRegion(0);//each time we remove region - they "move to left" so region 1 become region 0
        }
    }

    private void copyCharts(HSSFSheet resultSheet) {
        HSSFChart[] sheetCharts = HSSFChart.getSheetCharts(resultSheet);
        if (sheetCharts == null || sheetCharts.length == 0) {//workaround for charts. If there is charts on sheet - we can not use getDrawPatriarch as it removes all charts (because does not support them)
            HSSFPatriarch drawingPatriarch = resultSheet.createDrawingPatriarch();
            if (drawingPatriarch == null) {
                drawingPatriarch = resultSheet.createDrawingPatriarch();
            }

            drawingPatriarchsMap.put(resultSheet, drawingPatriarch);
        }
    }

    private void updateFormulas() {
        for (Map.Entry<Area, List<Area>> entry : areasDependency.entrySet()) {
            Area original = entry.getKey();

            for (Area dependent : entry.getValue()) {
                updateFormulas(original, dependent);
            }
        }
    }

    private void copyPictures() {
        for (int sheetNumber = 0; sheetNumber < templateWorkbook.getNumberOfSheets(); sheetNumber++) {
            HSSFSheet templateSheet = templateWorkbook.getSheetAt(sheetNumber);
            HSSFSheet resultSheet = resultWorkbook.getSheetAt(sheetNumber);

            copyPicturesFromTemplateToResult(templateSheet, resultSheet);
        }
    }

    private void writeBand(BandData band) {
        String rangeName = band.getName();
        HSSFSheet templateSheet = getTemplateSheetForRangeName(templateWorkbook, rangeName);

        if (templateSheet != currentTemplateSheet) { //todo: reimplement. store rownum for each sheet.
            currentTemplateSheet = templateSheet;
            rownum = 0;
        }

        HSSFSheet resultSheet = templateToResultSheetsMapping.get(templateSheet);

        if (BandOrientation.HORIZONTAL == band.getOrientation()) {
            colnum = 0;
            writeHorizontalBand(band, templateSheet, resultSheet);
        } else {
            writeVerticalBand(band, templateSheet, resultSheet);
        }
    }

    /**
     * Method writes horizontal band
     * Note: Only one band for row is supported. Now we think that many bands for row aren't usable.
     *
     * @param band          - band to write
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    private void writeHorizontalBand(BandData band, HSSFSheet templateSheet, HSSFSheet resultSheet) {
        String rangeName = band.getName();
        AreaReference templateRange = getAreaForRange(templateWorkbook, rangeName);
        if (templateRange == null) {
            throw wrapWithReportingException(String.format("No such named range in xls file: %s", rangeName));
        }
        CellReference[] crefs = templateRange.getAllReferencedCells();

        CellReference topLeft, bottomRight;
        AreaReference resultRange;

        int rowsAddedByHorizontalBandBackup = rowsAddedByHorizontalBand;
        int rownumBackup = rownum;

        if (crefs != null) {
            addRangeBounds(band, crefs);

            ArrayList<HSSFRow> resultRows = new ArrayList<HSSFRow>();

            int currentRowNum = -1;
            int currentRowCount = -1;
            int currentColumnCount = 0;
            int offset = 0;

            topLeft = new CellReference(rownum, 0);
            // no child bands - merge regions now
            if (band.getChildrenList().isEmpty()) {
                copyMergeRegions(resultSheet, rangeName, rownum + rowsAddedByHorizontalBand,
                        getCellFromReference(crefs[0], templateSheet).getColumnIndex());
            }

            for (CellReference cellRef : crefs) {
                HSSFCell templateCell = getCellFromReference(cellRef, templateSheet);
                HSSFRow resultRow;
                if (templateCell.getRowIndex() != currentRowNum) { //create new row
                    resultRow = resultSheet.createRow(rownum + rowsAddedByHorizontalBand);
                    rowsAddedByHorizontalBand += 1;

                    if (templateCell.getCellStyle().getParentStyle() != null
                            && templateCell.getCellStyle().getParentStyle().getUserStyleName() != null
                            && templateCell.getCellStyle().getParentStyle().getUserStyleName().equals(DYNAMIC_HEIGHT_STYLE)
                            ) {
                        //resultRow.setHeight(templateCell.getRow().getHeight());
                    } else {
                        resultRow.setHeight(templateCell.getRow().getHeight());
                    }
                    resultRows.add(resultRow);

                    currentRowNum = templateCell.getRowIndex();
                    currentRowCount++;
                    currentColumnCount = 0;
                    offset = templateCell.getColumnIndex();
                } else {                                          // or write cell to current row
                    resultRow = resultRows.get(currentRowCount);
                    currentColumnCount++;
                }

                copyCellFromTemplate(templateCell, resultRow, offset + currentColumnCount, band);
            }

            bottomRight = new CellReference(rownum + rowsAddedByHorizontalBand - 1, offset + currentColumnCount);
            resultRange = new AreaReference(topLeft, bottomRight);

            areaDependencyManager.addDependency(new Area(band.getName(), Area.AreaAlign.HORIZONTAL, templateRange),
                    new Area(band.getName(), Area.AreaAlign.HORIZONTAL, resultRange));
        }

        for (BandData child : band.getChildrenList()) {
            writeBand(child);
        }

        // scheduled merge regions
        if (!band.getChildrenList().isEmpty() && crefs != null) {
            copyMergeRegions(resultSheet, rangeName, rownumBackup + rowsAddedByHorizontalBandBackup,
                    getCellFromReference(crefs[0], templateSheet).getColumnIndex());
        }

        rownum += rowsAddedByHorizontalBand;
        rowsAddedByHorizontalBand = 0;
        rownum += rowsAddedByVerticalBand;
        rowsAddedByVerticalBand = 0;
    }

    /**
     * Method writes vertical band
     * Note: no child support for vertical band ;)
     *
     * @param band          - band to write
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    private void writeVerticalBand(BandData band, HSSFSheet templateSheet, HSSFSheet resultSheet) {
        String rangeName = band.getName();
        CellReference[] crefs = getRangeContent(templateWorkbook, rangeName);

        Set<Integer> addedRowNumbers = new HashSet<Integer>();

        if (crefs != null) {
            addRangeBounds(band, crefs);

            Bounds thisBounds = templateBounds.get(band.getName());
            Bounds parentBounds = templateBounds.get(band.getParentBand().getName());
            int localRowNum = parentBounds != null ? rownum + (rowsAddedByHorizontalBand - 1) + thisBounds.row0 - parentBounds.row0 : rownum;

            colnum = colnum == 0 ? getCellFromReference(crefs[0], templateSheet).getColumnIndex() : colnum;
            copyMergeRegions(resultSheet, rangeName, localRowNum, colnum);

            int firstRow = crefs[0].getRow();
            int firstColumn = crefs[0].getCol();

            for (CellReference cref : crefs) {//create necessary rows
                int currentRow = cref.getRow();
                final int rowOffset = currentRow - firstRow;
                if (!rowExists(resultSheet, localRowNum + rowOffset)) {
                    resultSheet.createRow(localRowNum + rowOffset);
                }
                addedRowNumbers.add(cref.getRow());
            }

            CellReference topLeft = null;
            CellReference bottomRight = null;
            for (CellReference cref : crefs) {
                int currentRow = cref.getRow();
                int currentColumn = cref.getCol();
                final int rowOffset = currentRow - firstRow;
                final int columnOffset = currentColumn - firstColumn;

                HSSFCell templateCell = getCellFromReference(cref, templateSheet);
                resultSheet.setColumnWidth(colnum + columnOffset, templateSheet.getColumnWidth(templateCell.getColumnIndex()));
                HSSFCell resultCell = copyCellFromTemplate(templateCell, resultSheet.getRow(localRowNum + rowOffset), colnum + columnOffset, band);
                if (topLeft == null) {
                    topLeft = new CellReference(resultCell);
                }
                bottomRight = new CellReference(resultCell);
            }

            colnum += crefs[crefs.length - 1].getCol() - firstColumn + 1;

            AreaReference templateRange = getAreaForRange(templateWorkbook, rangeName);
            AreaReference resultRange = new AreaReference(topLeft, bottomRight);
            areaDependencyManager.addDependency(new Area(band.getName(), Area.AreaAlign.VERTICAL, templateRange),
                    new Area(band.getName(), Area.AreaAlign.VERTICAL, resultRange));
        }

        //for first level vertical bands we should increase rownum by number of rows added by vertical band
        //nested vertical bands do not add rows, they use parent space
        if (BandData.ROOT_BAND_NAME.equals(band.getParentBand().getName())) {
            List<BandData> sameBands = band.getParentBand().getChildrenByName(band.getName());
            if (sameBands.size() > 0 && sameBands.get(sameBands.size() - 1) == band) {//check if this vertical band is last vertical band with same name
                rownum += addedRowNumbers.size();
                //      rowsAddedByVerticalBand = 0;
            }
        }
    }

    /**
     * <p>
     * Method creates mapping [rangeName -> List< CellRangeAddress >]. <br/>
     * List contains all merge regions for this named range
     * </p>
     * Attention: if merged regions writes wrong - look on methods isMergeRegionInsideNamedRange & isNamedRangeInsideMergeRegion
     * todo: how to recognize if merge region must be copied with named range
     *
     * @param currentSheet Sheet which contains merge regions
     */
    private void initMergeRegions(HSSFSheet currentSheet) {
        int rangeNumber = templateWorkbook.getNumberOfNames();
        for (int i = 0; i < rangeNumber; i++) {
            HSSFName aNamedRange = templateWorkbook.getNameAt(i);

            String refersToFormula = aNamedRange.getRefersToFormula();
            if (!AreaReference.isContiguous(refersToFormula)) {
                continue;
            }

            AreaReference aref = new AreaReference(refersToFormula);
            Integer rangeFirstRow = aref.getFirstCell().getRow();
            Integer rangeFirstColumn = (int) aref.getFirstCell().getCol();
            Integer rangeLastRow = aref.getLastCell().getRow();
            Integer rangeLastColumn = (int) aref.getLastCell().getCol();

            for (int j = 0; j < currentSheet.getNumMergedRegions(); j++) {
                CellRangeAddress mergedRegion = currentSheet.getMergedRegion(j);
                if (mergedRegion != null) {
                    Integer regionFirstRow = mergedRegion.getFirstRow();
                    Integer regionFirstColumn = mergedRegion.getFirstColumn();
                    Integer regionLastRow = mergedRegion.getLastRow();
                    Integer regionLastColumn = mergedRegion.getLastColumn();

                    boolean mergedInsideNamed = isMergeRegionInsideNamedRange(
                            rangeFirstRow, rangeFirstColumn, rangeLastRow, rangeLastColumn,
                            regionFirstRow, regionFirstColumn, regionLastRow, regionLastColumn);

                    boolean namedInsideMerged = isNamedRangeInsideMergeRegion(
                            rangeFirstRow, rangeFirstColumn, rangeLastRow, rangeLastColumn,
                            regionFirstRow, regionFirstColumn, regionLastRow, regionLastColumn);

                    if (mergedInsideNamed || namedInsideMerged) {
                        String name = aNamedRange.getNameName();
                        SheetRange sheetRange = new SheetRange(mergedRegion, currentSheet.getSheetName());
                        if (mergeRegionsForRangeNames.get(name) == null) {
                            ArrayList<SheetRange> list = new ArrayList<SheetRange>();
                            list.add(sheetRange);
                            mergeRegionsForRangeNames.put(name, list);
                        } else {
                            mergeRegionsForRangeNames.get(name).add(sheetRange);
                        }
                    }
                }
            }
        }
    }

    /**
     * Create new merge regions in result sheet identically to range's merge regions from template.
     * Not support copy of frames and rules
     *
     * @param resultSheet            - result sheet
     * @param rangeName              - range name
     * @param firstTargetRangeRow    - first column of target range
     * @param firstTargetRangeColumn - first column of target range
     */
    private void copyMergeRegions(HSSFSheet resultSheet, String rangeName,
                                  int firstTargetRangeRow, int firstTargetRangeColumn) {
        int rangeNameIdx = templateWorkbook.getNameIndex(rangeName);
        if (rangeNameIdx == -1) return;

        HSSFName aNamedRange = templateWorkbook.getNameAt(rangeNameIdx);
        AreaReference aref = new AreaReference(aNamedRange.getRefersToFormula());
        int column = aref.getFirstCell().getCol();
        int row = aref.getFirstCell().getRow();

        List<SheetRange> regionsList = mergeRegionsForRangeNames.get(rangeName);
        if (regionsList != null)
            for (SheetRange sheetRange : regionsList) {
                if (resultSheet.getSheetName().equals(sheetRange.getSheetName())) {
                    CellRangeAddress cra = sheetRange.getCellRangeAddress();
                    if (cra != null) {
                        int regionHeight = cra.getLastRow() - cra.getFirstRow() + 1;
                        int regionWidth = cra.getLastColumn() - cra.getFirstColumn() + 1;

                        int regionVOffset = cra.getFirstRow() - row;
                        int regionHOffset = cra.getFirstColumn() - column;

                        CellRangeAddress newRegion = cra.copy();
                        newRegion.setFirstColumn(regionHOffset + firstTargetRangeColumn);
                        newRegion.setLastColumn(regionHOffset + regionWidth - 1 + firstTargetRangeColumn);

                        newRegion.setFirstRow(regionVOffset + firstTargetRangeRow);
                        newRegion.setLastRow(regionVOffset + regionHeight - 1 + firstTargetRangeRow);

                        boolean skipRegion = false;

                        for (int mergedIndex = 0; mergedIndex < resultSheet.getNumMergedRegions(); mergedIndex++) {
                            CellRangeAddress mergedRegion = resultSheet.getMergedRegion(mergedIndex);

                            if (!intersects(newRegion, mergedRegion)) {
                                continue;
                            }

                            skipRegion = true;
                        }

                        if (!skipRegion) {
                            resultSheet.addMergedRegion(newRegion);
                        }
                    }
                }
            }
    }

    private boolean intersects(CellRangeAddress x, CellRangeAddress y) {
        return (x.getFirstColumn() <= y.getLastColumn() &&
                x.getLastColumn() >= y.getFirstColumn() &&
                x.getLastRow() >= y.getFirstRow() &&
                x.getFirstRow() <= y.getLastRow())
                // or
                || (y.getFirstColumn() <= x.getLastColumn() &&
                y.getLastColumn() >= x.getFirstColumn() &&
                y.getLastRow() >= x.getFirstRow() &&
                y.getFirstRow() <= x.getLastRow());
    }

    /**
     * copies template cell to result row into result column. Fills this cell with data from band
     *
     * @param templateCell - template cell
     * @param resultRow    - result row
     * @param resultColumn - result column
     * @param band         - band
     */
    private HSSFCell copyCellFromTemplate(HSSFCell templateCell, HSSFRow resultRow, int resultColumn, BandData band) {
        if (templateCell == null) return null;

        HSSFCell resultCell = resultRow.createCell(resultColumn);

        HSSFCellStyle templateStyle = templateCell.getCellStyle();
        HSSFCellStyle resultStyle = copyCellStyle(templateStyle);
        resultCell.setCellStyle(resultStyle);

        String templateCellValue = "";
        int cellType = templateCell.getCellType();

        if (cellType != HSSFCell.CELL_TYPE_FORMULA && cellType != HSSFCell.CELL_TYPE_NUMERIC) {
            HSSFRichTextString richStringCellValue = templateCell.getRichStringCellValue();
            templateCellValue = richStringCellValue != null ? richStringCellValue.getString() : "";

            Map<String, Object> bandData = band.getData();
            templateCellValue = extractStyles(templateCell, resultCell, templateCellValue, bandData);
        }

        if (cellType == HSSFCell.CELL_TYPE_STRING && isOneValueCell(templateCell, templateCellValue)) {
            updateValueCell(rootBand, band, templateCellValue, resultCell,
                    drawingPatriarchsMap.get(resultCell.getSheet()));
        } else {
            String cellValue = inlineBandDataToCellString(templateCell, templateCellValue, band);
            setValueToCell(resultCell, cellValue, cellType);
        }

        return resultCell;
    }

    /**
     * Copies template cell to result cell and fills it with band data
     *
     * @param band              - band
     * @param templateCellValue - template cell value
     * @param resultCell        - result cell
     */
    private void updateValueCell(BandData rootBand, BandData band, String templateCellValue, HSSFCell resultCell, HSSFPatriarch patriarch) {
        String parameterName = templateCellValue;
        parameterName = unwrapParameterName(parameterName);

        if (StringUtils.isEmpty(parameterName)) return;

        if (!band.getData().containsKey(parameterName)) {
            resultCell.setCellValue((String) null);
            return;
        }

        Object parameterValue = band.getData().get(parameterName);
        Map<String, ReportFieldFormat> valuesFormats = rootBand.getReportFieldConverters();

        if (parameterValue == null) {
            resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        } else if (parameterValue instanceof Number) {
            resultCell.setCellValue(((Number) parameterValue).doubleValue());
        } else if (parameterValue instanceof Boolean) {
            resultCell.setCellValue((Boolean) parameterValue);
        } else if (parameterValue instanceof Date) {
            resultCell.setCellValue((Date) parameterValue);
        } else {
            String bandName = band.getName();
            String fullParamName = bandName + "." + parameterName;
            if (valuesFormats.containsKey(fullParamName)) {
                String formatString = valuesFormats.get(fullParamName).getFormat();
                for (ContentInliner contentInliner : contentInliners) {
                    Matcher matcher = contentInliner.getTagPattern().matcher(formatString);
                    if (matcher.find()) {
                        contentInliner.inlineToXls(patriarch, resultCell, parameterValue, matcher);
                        return;
                    }
                }
            }
            resultCell.setCellValue(new HSSFRichTextString(parameterValue.toString()));
        }
    }

    private void setValueToCell(HSSFCell resultCell, String cellValue, int cellType) {
        if (StringUtils.isNotEmpty(cellValue)) {
            switch (cellType) {
                case HSSFCell.CELL_TYPE_FORMULA:
                    resultCell.setCellFormula(cellValue);
                    break;
                case HSSFCell.CELL_TYPE_STRING:
                    resultCell.setCellValue(new HSSFRichTextString(cellValue));
                    break;
                default:
                    resultCell.setCellValue(cellValue);
                    break;
            }

        } else {
            resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        }
    }

    private String inlineBandDataToCellString(HSSFCell cell, String templateCellValue, BandData band) {
        String resultStr = "";
        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            if (templateCellValue != null) resultStr = templateCellValue;
        } else {
            if (cell.toString() != null) resultStr = cell.toString();
        }

        if (StringUtils.isNotEmpty(resultStr)) return insertBandDataToString(band, resultStr);

        return "";
    }

    /**
     * This method adds range bounds to cache. Key is bandName
     *
     * @param band  - band
     * @param crefs - range
     */
    private void addRangeBounds(BandData band, CellReference[] crefs) {
        if (templateBounds.containsKey(band.getName()))
            return;
        Bounds bounds = new Bounds(crefs[0].getRow(), crefs[0].getCol(), crefs[crefs.length - 1].getRow(), crefs[crefs.length - 1].getCol());
        templateBounds.put(band.getName(), bounds);
    }

    private void updateFormulas(Area templateArea, Area dependentResultArea) {
        HSSFSheet templateSheet = getTemplateSheetForRangeName(templateWorkbook, templateArea.getName());
        HSSFSheet resultSheet = templateToResultSheetsMapping.get(templateSheet);

        AreaReference area = dependentResultArea.toAreaReference();
        for (CellReference cell : area.getAllReferencedCells()) {
            HSSFCell resultCell = getCellFromReference(cell, resultSheet);

            if (resultCell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
                Ptg[] ptgs = HSSFFormulaParser.parse(resultCell.getCellFormula(), resultWorkbook);

                for (Ptg ptg : ptgs) {
                    if (ptg instanceof AreaPtg) {
                        areaDependencyManager.updateAreaPtg(templateArea, dependentResultArea, (AreaPtg) ptg);
                    } else if (ptg instanceof RefPtg) {
                        areaDependencyManager.updateRefPtg(templateArea, dependentResultArea, (RefPtg) ptg);
                    }
                }

                String calculatedFormula = HSSFFormulaParser.toFormulaString(templateWorkbook, ptgs);
                resultCell.setCellFormula(calculatedFormula);
            }
        }
    }

    protected String extractStyles(HSSFCell templateCell, HSSFCell resultCell, String templateCellValue, Map<String, Object> bandData) {
        HSSFSheet resultSheet = resultCell.getSheet();

        templateCellValue = applyCustomStyleOption(resultCell, templateCellValue, bandData);

        templateCellValue = applyCopyColumnWidthOption(templateCell, resultCell, templateCellValue);

        templateCellValue = applyAutoWidthOption(resultCell, templateCellValue, resultSheet);

        templateCellValue = applyCustomWidthOption(resultCell, templateCellValue, bandData, resultSheet);

        templateCellValue = StringUtils.stripEnd(templateCellValue, null);

        return templateCellValue;
    }

    private String applyCustomWidthOption(HSSFCell resultCell, String templateCellValue, Map<String, Object> bandData, HSSFSheet resultSheet) {
        int widthPosition = StringUtils.indexOf(templateCellValue, CUSTOM_WIDTH_SELECTOR);
        if (widthPosition >= 0) {
            String stringTail = StringUtils.substring(templateCellValue, widthPosition + CUSTOM_WIDTH_SELECTOR.length());
            int styleEndIndex = StringUtils.indexOf(stringTail, " ");
            if (styleEndIndex < 0)
                styleEndIndex = templateCellValue.length() - 1;

            String widthLengthSelector = StringUtils.substring(templateCellValue, widthPosition,
                    styleEndIndex + CUSTOM_WIDTH_SELECTOR.length() + widthPosition);

            templateCellValue = StringUtils.replace(templateCellValue, widthLengthSelector, "");

            widthLengthSelector = StringUtils.substring(widthLengthSelector, CUSTOM_WIDTH_SELECTOR.length());

            if (widthLengthSelector != null && bandData.containsKey(widthLengthSelector) && bandData.get(widthLengthSelector) != null) {
                Object width = bandData.get(widthLengthSelector);

                if (width != null && width instanceof Integer) {
                    optionContainer.add(new CustomWidthHint(resultSheet, resultCell.getColumnIndex(), (Integer) width));
                }
            }
        }
        return templateCellValue;
    }

    private String applyAutoWidthOption(HSSFCell resultCell, String templateCellValue, HSSFSheet resultSheet) {
        if (StringUtils.contains(templateCellValue, AUTO_WIDTH_SELECTOR)) {
            templateCellValue = StringUtils.replace(templateCellValue, AUTO_WIDTH_SELECTOR, "");
            optionContainer.add(new AutoWidthHint(resultSheet, resultCell.getColumnIndex()));
        }
        return templateCellValue;
    }

    private String applyCopyColumnWidthOption(HSSFCell templateCell, HSSFCell resultCell, String templateCellValue) {
        HSSFSheet resultSheet = resultCell.getSheet();
        HSSFSheet templateSheet = templateCell.getSheet();

        if (StringUtils.contains(templateCellValue, COPY_COLUMN_WIDTH_SELECTOR)) {
            templateCellValue = StringUtils.replace(templateCellValue, COPY_COLUMN_WIDTH_SELECTOR, "");
            optionContainer.add(new CopyColumnHint(resultSheet,
                    resultCell.getColumnIndex(), templateSheet.getColumnWidth(templateCell.getColumnIndex())));
        }
        return templateCellValue;
    }

    private String applyCustomStyleOption(HSSFCell resultCell, String templateCellValue, Map<String, Object> bandData) {
        int stylePosition = StringUtils.indexOf(templateCellValue, CELL_DYNAMIC_STYLE_SELECTOR);
        if (stylePosition >= 0) {
            String stringTail = StringUtils.substring(templateCellValue, stylePosition + CELL_DYNAMIC_STYLE_SELECTOR.length());
            int styleEndIndex = StringUtils.indexOf(stringTail, " ");
            if (styleEndIndex < 0)
                styleEndIndex = templateCellValue.length() - 1;

            String styleSelector = StringUtils.substring(templateCellValue, stylePosition,
                    styleEndIndex + CELL_DYNAMIC_STYLE_SELECTOR.length() + stylePosition);

            templateCellValue = StringUtils.replace(templateCellValue, styleSelector, "");

            styleSelector = StringUtils.substring(styleSelector, CELL_DYNAMIC_STYLE_SELECTOR.length());

            if (styleSelector != null && bandData.get(styleSelector) != null) {
                HSSFCellStyle cellStyle = styleCache.getStyleByName((String) bandData.get(styleSelector));

                if (cellStyle != null) {
                    optionContainer.add(new CustomCellStyleHint(resultCell, cellStyle,
                            templateWorkbook, resultWorkbook, fontCache, styleCache));
                }
            }
        }
        return templateCellValue;
    }

    private HSSFCellStyle copyCellStyle(HSSFCellStyle templateStyle) {
        HSSFCellStyle style = styleCache.getCellStyleByTemplate(templateStyle);

        if (style == null) {
            HSSFCellStyle newStyle = resultWorkbook.createCellStyle();

            newStyle.cloneStyleRelationsFrom(templateStyle);
            HSSFFont templateFont = templateStyle.getFont(templateWorkbook);
            HSSFFont font = fontCache.getFontByTemplate(templateFont);
            if (font != null)
                newStyle.setFont(font);
            else {
                newStyle.cloneFontFrom(templateStyle);
                fontCache.addCachedFont(templateFont, newStyle.getFont(resultWorkbook));
            }
            styleCache.addCachedStyle(templateStyle, newStyle);
            style = newStyle;
        }

        return style;
    }

    /**
     * Returns EscherAggregate from sheet
     *
     * @param sheet - HSSFSheet
     * @return - EscherAggregate from sheet
     */
    private EscherAggregate getEscherAggregate(HSSFSheet sheet) {
        EscherAggregate agg = sheetToEscherAggregate.get(sheet.getSheetName());
        if (agg == null) {
            agg = sheet.getDrawingEscherAggregate();
            sheetToEscherAggregate.put(sheet.getSheetName(), agg);
        }
        return agg;
    }

    /**
     * Copies all pictures from template sheet to result sheet, shift picture depending on area dependencies
     *
     * @param templateSheet - template sheet
     * @param resultSheet   - result sheet
     */
    private void copyPicturesFromTemplateToResult(HSSFSheet templateSheet, HSSFSheet resultSheet) {
        List<HSSFClientAnchor> list = getAllAnchors(getEscherAggregate(templateSheet));

        int i = 0;
        if (CollectionUtils.isNotEmpty(orderedPicturesId)) {//just a shitty workaround for anchors without pictures
            for (HSSFClientAnchor anchor : list) {
                Cell topLeft = getCellFromTemplate(new Cell(anchor.getCol1(), anchor.getRow1()));
                anchor.setCol1(topLeft.getCol());
                anchor.setRow1(topLeft.getRow());

                anchor.setCol2(topLeft.getCol() + anchor.getCol2() - anchor.getCol1());
                anchor.setRow2(topLeft.getRow() + anchor.getRow2() - anchor.getRow1());

                HSSFPatriarch sheetPatriarch = drawingPatriarchsMap.get(resultSheet);
                if (sheetPatriarch != null) {
                    sheetPatriarch.createPicture(anchor, orderedPicturesId.get(i++));
                }
            }
        }
    }

    private boolean rowExists(HSSFSheet sheet, int rowNumber) {
        return sheet.getRow(rowNumber) != null;
    }

    private Cell getCellFromTemplate(Cell cell) {
        Cell newCell = new Cell(cell);
        updateCell(newCell);
        return newCell;
    }

    private void updateCell(Cell cell) {
        Area templateArea = areaDependencyManager.getTemplateAreaByCoordinate(cell.getCol(), cell.getRow());
        List<Area> resultAreas = areasDependency.get(templateArea);

        if (CollectionUtils.isNotEmpty(resultAreas)) {
            Area destination = resultAreas.get(0);

            int col = cell.getCol() - templateArea.getTopLeft().getCol() + destination.getTopLeft().getCol();
            int row = cell.getRow() - templateArea.getTopLeft().getRow() + destination.getTopLeft().getRow();

            cell.setCol(col);
            cell.setRow(row);
        }
    }

    //---------------------Utility classes------------------------

    /**
     * Cell range at sheet
     */
    private static class SheetRange {
        private CellRangeAddress cellRangeAddress;
        private String sheetName;

        private SheetRange(CellRangeAddress cellRangeAddress, String sheetName) {
            this.cellRangeAddress = cellRangeAddress;
            this.sheetName = sheetName;
        }

        public CellRangeAddress getCellRangeAddress() {
            return cellRangeAddress;
        }

        public String getSheetName() {
            return sheetName;
        }
    }

    /**
     * Bounds of region [(x,y) : (x1, y1)]
     */
    private static class Bounds {
        public final int row0;
        public final int column0;
        public final int row1;
        public final int column1;

        private Bounds(int row0, int column0, int row1, int column1) {
            this.row0 = row0;
            this.column0 = column0;
            this.row1 = row1;
            this.column1 = column1;
        }
    }
}
