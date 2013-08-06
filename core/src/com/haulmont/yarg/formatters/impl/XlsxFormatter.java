/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.LinkedHashMultimap;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.formatters.impl.xlsx.CellReference;
import com.haulmont.yarg.formatters.impl.xlsx.Document;
import com.haulmont.yarg.formatters.impl.xlsx.Range;
import com.haulmont.yarg.formatters.impl.xlsx.XlsxUtils;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandVisitor;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportTemplate;
import com.haulmont.yarg.structure.impl.BandOrientation;
import org.apache.commons.collections.CollectionUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.*;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.*;

import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;

public class XlsxFormatter extends AbstractFormatter {
    protected Document template;
    protected Document result;

    protected long currentRow = 0;

    protected ArrayListMultimap<Range, Range> rangeDependencies = ArrayListMultimap.create();
    protected LinkedHashMultimap<Range, Range> rangeVerticalIntersections = LinkedHashMultimap.create();
    protected BiMap<BandData, Range> bandsToTemplateRanges = HashBiMap.create();
    protected BiMap<BandData, Range> bandsToResultRanges = HashBiMap.create();

    protected Set<Cell> innerFormulas = new HashSet<>();
    protected Set<Cell> outerFormulas = new HashSet<>();
    private int previousRangesRightOffset;

    public XlsxFormatter(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        super(rootBand, reportTemplate, outputStream);
    }

    @Override
    public void renderDocument() {
        init();

        findVerticalDependencies();

        result.clearWorkbook();

        for (BandData childBand : rootBand.getChildrenList()) {
            writeBand(childBand);
        }

        updateMergeRegions();
        updateCharts();
        updateFormulas();

        try {
            SaveToZipFile saver = new SaveToZipFile(result.getPackage());
            saver.save(outputStream);
        } catch (Docx4JException e) {
            throw new ReportingException("An error occurred during save result document", e);
        }
    }

    private void init() {
        try {
            template = Document.create((SpreadsheetMLPackage) SpreadsheetMLPackage.load(reportTemplate.getDocumentContent()));

//            SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
//            WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/sheet1.xml"), "data", 1);

//            result = create(pkg);
            result = Document.create((SpreadsheetMLPackage) SpreadsheetMLPackage.load(reportTemplate.getDocumentContent()));
            result.getWorkbook().getCalcPr().setCalcMode(STCalcMode.AUTO);
            result.getWorkbook().getCalcPr().setFullCalcOnLoad(true);
        } catch (Exception e) {
            throw new ReportingException(String.format("An error occurred while loading template [%s]", reportTemplate.getDocumentName()), e);
        }
    }

    private void findVerticalDependencies() {
        List<CTDefinedName> definedName = template.getWorkbook().getDefinedNames().getDefinedName();
        for (CTDefinedName name1 : definedName) {
            for (CTDefinedName name2 : definedName) {
                if (!name1.equals(name2)) {
                    Range range1 = Range.fromFormula(name1.getValue());
                    Range range2 = Range.fromFormula(name2.getValue());
                    if (range1.firstRow >= range2.firstRow && range1.firstRow <= range2.lastRow ||
                            range1.lastRow >= range2.firstRow && range1.lastRow <= range2.lastRow ||
                            range2.firstRow >= range1.firstRow && range2.firstRow <= range1.lastRow ||
                            range2.lastRow >= range1.firstRow && range2.lastRow <= range1.lastRow
                            ) {
                        rangeVerticalIntersections.put(range1, range2);
                        rangeVerticalIntersections.put(range2, range1);
                    }
                }
            }
        }
    }

    private void updateCharts() {
        for (Map.Entry<Range, Document.ChartPair> entry : result.getChartSpaces().entrySet()) {
            for (Range templateRange : rangeDependencies.keySet()) {
                if (templateRange.contains(entry.getKey())) {
                    List<Range> chartBandResultRanges = rangeDependencies.get(templateRange);
                    if (chartBandResultRanges.size() > 0) {
                        Range firstResultRange = getFirst(chartBandResultRanges);

                        shiftChart(entry.getValue(), templateRange, firstResultRange);

                        CTChart chart = entry.getValue().getChartSpace().getChart();
                        CTPlotArea plotArea = chart.getPlotArea();
                        List<Object> areaChartOrArea3DChartOrLineChart = plotArea.getAreaChartOrArea3DChartOrLineChart();
                        for (Object o : areaChartOrArea3DChartOrLineChart) {
                            if (o instanceof ListSer) {
                                ListSer series = (ListSer) o;
                                List<SerContent> ser = series.getSer();
                                for (SerContent ctBarSer : ser) {
                                    CTAxDataSource captions = ctBarSer.getCat();
                                    CTNumDataSource data = ctBarSer.getVal();

                                    Range temlpateCaptionsRange = Range.fromFormula(captions.getStrRef().getF());
                                    Range templateDataRange = Range.fromFormula(data.getNumRef().getF());

                                    //todo separate handling for data and captions
                                    for (Range range : rangeDependencies.keySet()) {
                                        if (range.contains(temlpateCaptionsRange)) {
                                            List<Range> seriesResultRanges = rangeDependencies.get(range);

                                            Range seriesFirstRange = getFirst(seriesResultRanges);
                                            Range seriesLastRange = getLast(seriesResultRanges);

                                            Offset offset = calculateOffset(temlpateCaptionsRange, seriesFirstRange);
                                            temlpateCaptionsRange = temlpateCaptionsRange.shift(offset.downOffset, offset.rightOffset);
                                            templateDataRange = templateDataRange.shift(offset.downOffset, offset.rightOffset);

                                            int grow = seriesLastRange.firstRow - seriesFirstRange.firstRow;
                                            temlpateCaptionsRange = temlpateCaptionsRange.growDown(grow);
                                            templateDataRange = templateDataRange.growDown(grow);

                                            captions.getStrRef().setF(temlpateCaptionsRange.toFormula());
                                            data.getNumRef().setF(templateDataRange.toFormula());
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }

    private void shiftChart(Document.ChartPair chart, Range templateRange, Range firstResultRange) {
        Offset offset = calculateOffset(templateRange, firstResultRange);

        CTTwoCellAnchor anchor = (CTTwoCellAnchor) chart.getDrawing().getJaxbElement().getEGAnchor().get(0);
        anchor.getFrom().setRow(anchor.getFrom().getRow() + offset.downOffset);
        anchor.getFrom().setCol(anchor.getFrom().getCol() + offset.rightOffset);
        anchor.getTo().setRow(anchor.getTo().getRow() + offset.downOffset);
        anchor.getTo().setCol(anchor.getTo().getCol() + offset.rightOffset);
    }

    private void updateFormulas() {
        //todo support growing formulas inside 1 parent band
        for (Cell cellWithFormula : innerFormulas) {
            Row row = (Row) cellWithFormula.getParent();
            SheetData sheetData = (SheetData) row.getParent();
            Worksheet worksheet = (Worksheet) sheetData.getParent();
            Range formulaRange = Range.fromCellFormula(result.getSheetName(worksheet), cellWithFormula);
            Range originalFormulaRange = formulaRange;
            for (Range templateRange : rangeDependencies.keySet()) {
                if (templateRange.contains(formulaRange)) {
                    List<Range> resultRanges = rangeDependencies.get(templateRange);

                    CellReference cellReference = new CellReference(cellWithFormula.getR());
                    for (Range resultRange : resultRanges) {
                        if (resultRange.contains(cellReference)) {
                            Offset offset = calculateOffset(templateRange, resultRange);

                            formulaRange = formulaRange.shift(offset.downOffset, offset.rightOffset);
                            updateFormula(cellWithFormula, originalFormulaRange, formulaRange);
                            break;
                        }
                    }
                }
            }
        }

        for (Cell cellWithFormula : outerFormulas) {
            Row row = (Row) cellWithFormula.getParent();
            SheetData sheetData = (SheetData) row.getParent();
            Worksheet worksheet = (Worksheet) sheetData.getParent();
            Range formulaRange = Range.fromCellFormula(result.getSheetName(worksheet), cellWithFormula);
            Range originalFormulaRange = formulaRange;
            for (Range templateRange : rangeDependencies.keySet()) {
                if (templateRange.contains(formulaRange)) {
                    List<Range> resultRanges = rangeDependencies.get(templateRange);

                    Range firstResultRange = getFirst(resultRanges);
                    Range lastResultRange = getLast(resultRanges);

                    Offset offset = calculateOffset(templateRange, firstResultRange);
                    formulaRange = formulaRange.shift(offset.downOffset, offset.rightOffset);

                    int grow = lastResultRange.firstRow - firstResultRange.firstRow;
                    formulaRange = formulaRange.growDown(grow);
                    updateFormula(cellWithFormula, originalFormulaRange, formulaRange);
                    break;
                }
            }
        }
    }

    private void updateFormula(Cell cellWithFormula, Range originalFormulaRange, Range formulaRange) {
        CTCellFormula formula = cellWithFormula.getF();
        formula.setValue(formula.getValue().replace(originalFormulaRange.toRange(), formulaRange.toRange()));
    }

    private Offset calculateOffset(Range from, Range to) {
        int downOffset = to.firstRow - from.firstRow;
        int rightOffset = to.firstColumn - from.firstColumn;
        return new Offset(downOffset, rightOffset);
    }

    private void updateMergeRegions() {
        for (Range templateRange : rangeDependencies.keySet()) {
            Worksheet templateSheet = template.getSheetByName(templateRange.sheet);
            Worksheet resultSheet = result.getSheetByName(templateRange.sheet);

            if (templateSheet.getMergeCells() != null) {
                if (resultSheet.getMergeCells() == null) {
                    CTMergeCells resultMergeCells = new CTMergeCells();
                    resultMergeCells.setParent(resultSheet);
                    resultSheet.setMergeCells(resultMergeCells);
                }
            }

            for (Range resultRange : rangeDependencies.get(templateRange)) {
                if (templateSheet.getMergeCells() != null && templateSheet.getMergeCells().getMergeCell() != null) {
                    for (CTMergeCell templateMergeRegion : templateSheet.getMergeCells().getMergeCell()) {
                        Range mergeRange = Range.fromRange(templateRange.sheet, templateMergeRegion.getRef());
                        if (templateRange.contains(mergeRange)) {
                            Offset offset = calculateOffset(templateRange, resultRange);
                            Range resultMergeRange = mergeRange.shift(offset.downOffset, offset.rightOffset);
                            CTMergeCell resultMergeRegion = new CTMergeCell();
                            resultMergeRegion.setRef(resultMergeRange.toRange());
                            resultMergeRegion.setParent(resultSheet.getMergeCells());
                            resultSheet.getMergeCells().getMergeCell().add(resultMergeRegion);
                        }
                    }
                }
            }
        }
    }

    private void writeBand(BandData childBand) {
        if (BandOrientation.HORIZONTAL == childBand.getOrientation()) {
            writeHBand(childBand);
        } else {
            writeVBand(childBand);
        }
    }

    private void writeHBand(BandData band) {
        Range templateRange = getBandRange(band);
        Worksheet resultSheet = result.getSheetByName(templateRange.sheet);
        List<Row> resultSheetRows = resultSheet.getSheetData().getRow();

        Row firstRow = findNextRowForHBand(band, templateRange, resultSheetRows);
        firstRow = ensureNecessaryRowsCreated(templateRange, resultSheet, firstRow);

        List<Cell> resultCells = copyCells(band, templateRange, resultSheetRows, firstRow);

        updateRangeMappings(band, templateRange, resultCells);

        //render children
        if (CollectionUtils.isNotEmpty(resultCells)) {
            for (BandData child : band.getChildrenList()) {
                writeBand(child);
            }
        }
    }

    private void writeVBand(BandData band) {
        Range templateRange = getBandRange(band);
        Worksheet resultSheet = result.getSheetByName(templateRange.sheet);
        List<Row> resultSheetRows = resultSheet.getSheetData().getRow();

        Row firstRow = findNextRowForVBand(band, templateRange, resultSheetRows);
        firstRow = ensureNecessaryRowsCreated(templateRange, resultSheet, firstRow);

        List<Cell> resultCells = copyCells(band, templateRange, resultSheetRows, firstRow);

        //shift cells right
        for (Cell resultCell : resultCells) {
            CellReference cellReference = new CellReference(resultCell.getR());
            String newColumn = XlsxUtils.getColumnReferenceFromNumber(cellReference.column + previousRangesRightOffset);
            resultCell.setR(newColumn + cellReference.row);
        }

        updateRangeMappings(band, templateRange, resultCells);
    }

    private void updateRangeMappings(BandData band, Range templateRange, List<Cell> resultCells) {
        if (CollectionUtils.isNotEmpty(resultCells)) {
            Range resultRange = Range.fromCells(templateRange.sheet, getFirst(resultCells).getR(), resultCells.get(resultCells.size() - 1).getR());
            rangeDependencies.put(templateRange, resultRange);
            bandsToTemplateRanges.forcePut(band, templateRange);
            bandsToResultRanges.forcePut(band, resultRange);
        }
    }

    private Row findNextRowForHBand(BandData band, Range templateRange, List<Row> resultSheetRows) {
        Row firstRow = null;

        boolean isFirstLevelBand = BandData.ROOT_BAND_NAME.equals(band.getParentBand().getName());

        List<Range> alreadyRenderedRanges = findAlreadyRenderedRanges(band);

        if (CollectionUtils.isNotEmpty(alreadyRenderedRanges)) {//this band has been already rendered at least once
            Range lastRenderedRange = alreadyRenderedRanges.get(alreadyRenderedRanges.size() - 1);
            BandData lastRenderedBand = bandsToResultRanges.inverse().get(lastRenderedRange);
            LastRowBandVisitor bandVisitor = new LastRowBandVisitor();
            lastRenderedBand.visit(bandVisitor);

            if (resultSheetRows.size() > bandVisitor.lastRow) {//get next row
                firstRow = resultSheetRows.get(bandVisitor.lastRow);
            }
        } else if (!isFirstLevelBand) {
            LastRowBandVisitor bandVisitor = new LastRowBandVisitor();
            band.getParentBand().visit(bandVisitor);
            if (resultSheetRows.size() > bandVisitor.lastRow) {//get next row
                firstRow = resultSheetRows.get(bandVisitor.lastRow);
            }
        } else {//this is the first render
            Collection<Range> templateNeighbours = rangeVerticalIntersections.get(templateRange);
            for (Range templateNeighbour : templateNeighbours) {
                Collection<Range> resultRanges = rangeDependencies.get(templateNeighbour);
                if (resultRanges.size() > 0) {
                    Range firstResultRange = resultRanges.iterator().next();
                    firstRow = resultSheetRows.get(firstResultRange.firstRow - 1);//get current  row
                    break;
                }
            }
        }
        return firstRow;
    }

    private Row findNextRowForVBand(BandData band, Range templateRange, List<Row> thisSheetRows) {
        Row firstRow = null;
        boolean isFirstLevelBand = BandData.ROOT_BAND_NAME.equals(band.getParentBand().getName());

        List<Range> alreadyRenderedRanges = findAlreadyRenderedRanges(band);

        previousRangesRightOffset = 0;
        if (CollectionUtils.isNotEmpty(alreadyRenderedRanges)) {//this band has been already rendered at least once
            Range previousRange = getLast(alreadyRenderedRanges);
            previousRangesRightOffset = previousRange.firstColumn - templateRange.firstColumn + 1;
            if (thisSheetRows.size() > previousRange.firstRow - 1) {//get current row
                firstRow = thisSheetRows.get(previousRange.firstRow - 1);
            }
        } else if (!isFirstLevelBand) {
            BandData parentBand = band.getParentBand();
            Range resultParentRange = bandsToResultRanges.get(parentBand);
            Range templateParentRange = bandsToTemplateRanges.get(parentBand);

            if (resultParentRange != null && templateParentRange != null) {
                if (templateParentRange.firstRow == templateRange.firstRow) {
                    if (thisSheetRows.size() > resultParentRange.firstRow - 1) {//get current row
                        firstRow = thisSheetRows.get(resultParentRange.firstRow - 1);
                    }
                } else {
                    LastRowBandVisitor bandVisitor = new LastRowBandVisitor();
                    band.getParentBand().visit(bandVisitor);
                    if (thisSheetRows.size() > bandVisitor.lastRow) {//get next row
                        firstRow = thisSheetRows.get(bandVisitor.lastRow);
                    }
                }
            }
        } else {//this is the first render
            Collection<Range> templateNeighbours = rangeVerticalIntersections.get(templateRange);
            for (Range templateNeighbour : templateNeighbours) {
                Collection<Range> resultRanges = rangeDependencies.get(templateNeighbour);
                if (resultRanges.size() > 0) {
                    Range firstResultRange = resultRanges.iterator().next();
                    firstRow = thisSheetRows.get(firstResultRange.firstRow - 1);//get current  row
                    break;
                }
            }
        }
        return firstRow;
    }

    private Row ensureNecessaryRowsCreated(Range templateRange, Worksheet resultSheet, Row firstRow) {
        if (firstRow == null) {
            firstRow = createNewRow(resultSheet);
        }

        if (resultSheet.getSheetData().getRow().size() < firstRow.getR() + templateRange.lastRow - templateRange.firstRow) {
            for (int i = 0; i < templateRange.lastRow - templateRange.firstRow; i++) {
                Row row = createNewRow(resultSheet);
            }
        }
        return firstRow;
    }

    private List<Cell> copyCells(BandData band, Range templateRange, List<Row> resultSheetRows, Row firstRow) {
        List<Cell> resultCells = new ArrayList<>();
        for (int i = 0; i <= templateRange.lastRow - templateRange.firstRow; i++) {
            Range oneRowRange = new Range(templateRange.sheet, templateRange.firstColumn, templateRange.firstRow + i, templateRange.lastColumn, templateRange.firstRow + i);
            List<Cell> templateCells = template.getCellsByRange(oneRowRange);
            Row resultRow = resultSheetRows.get((int) (firstRow.getR() + i - 1));
            List<Cell> currentRowResultCells = copyCells(templateRange, band, resultRow, templateCells);
            resultCells.addAll(currentRowResultCells);
        }
        return resultCells;
    }

    private List<Range> findAlreadyRenderedRanges(BandData band) {
        List<Range> alreadyRenderedRanges = new ArrayList<>();
        List<BandData> sameLevelBands = band.getParentBand().getChildrenByName(band.getName());
        for (BandData sameLevelBand : sameLevelBands) {
            Range range = bandsToResultRanges.get(sameLevelBand);
            if (range != null) {
                alreadyRenderedRanges.add(range);
            }
        }

        return alreadyRenderedRanges;
    }

    private Range getBandRange(BandData childBand) {
        CTDefinedName targetRange = template.getDefinedName(childBand.getName());
        return Range.fromFormula(targetRange.getValue());
    }

    private Row createNewRow(Worksheet resultSheet) {
        Row newRow = Context.getsmlObjectFactory().createRow();
        currentRow++;
        newRow.setR(currentRow);
        resultSheet.getSheetData().getRow().add(newRow);
        newRow.setParent(resultSheet.getSheetData());

        return newRow;
    }

    private List<Cell> copyCells(Range templateRange, BandData childBand, Row newRow, List<Cell> templateCells) {
        List<Cell> resultCells = new ArrayList<>();
        for (Cell templateCell : templateCells) {
            copyRowSettings((Row) templateCell.getParent(), newRow);

            Cell newCell = XmlUtils.deepCopy(templateCell, Context.jcSML);

            if (newCell.getF() != null) {
                addFormulaForPostProcessing(templateRange, newRow, templateCell, newCell);
            }

            resultCells.add(newCell);

            newCell.setR(newCell.getR().replaceAll("[0-9]+", String.valueOf(newRow.getR())));
            newRow.getC().add(newCell);
            newCell.setParent(newRow);

            SheetData sheetData = (SheetData) newRow.getParent();
            Worksheet worksheet = (Worksheet) sheetData.getParent();
            WorksheetPart worksheetPart = null;
            for (Document.SheetWrapper sheetWrapper : result.getWorksheets()) {
                if (sheetWrapper.getWorksheet().getJaxbElement() == worksheet) {
                    worksheetPart = sheetWrapper.getWorksheet();
                }
            }

            updateCell(worksheetPart, childBand, newCell);
        }
        return resultCells;
    }

    private void addFormulaForPostProcessing(Range templateRange, Row newRow, Cell templateCell, Cell newCell) {
        SheetData sheetData = (SheetData) newRow.getParent();
        Worksheet worksheet = (Worksheet) sheetData.getParent();
        Range formulaRange = Range.fromCellFormula(result.getSheetName(worksheet), templateCell);
        if (templateRange.contains(formulaRange)) {
            innerFormulas.add(newCell);
        } else {
            outerFormulas.add(newCell);
        }
    }

    private void copyRowSettings(Row templateRow, Row newRow) {
        newRow.setHt(templateRow.getHt());
        newRow.setCustomHeight(true);
    }

    private void updateCell(WorksheetPart worksheetPart, BandData childBand, Cell newCell) {
        String cellValue = template.getCellValue(newCell);

        if (cellValue == null) {
            newCell.setV("");
            return;
        }

        if (UNIVERSAL_ALIAS_PATTERN.matcher(cellValue).matches()) {
            String paramName = unwrapParameterName(cellValue);
            String paramFullName = childBand.getName() + "." + paramName;
            Object value = childBand.getData().get(paramName);

            boolean handled = false;
            Map<String, ReportFieldFormat> converters = rootBand.getReportFieldConverters();
            if (value != null && converters != null && converters.containsKey(paramFullName)) {
                String format = converters.get(paramFullName).getFormat();
                // Handle doctags
                for (ContentInliner contentInliner : contentInliners) {
                    Matcher matcher = contentInliner.getTagPattern().matcher(format);
                    if (matcher.find()) {
                        contentInliner.inlineToXlsx(result.getPackage(), worksheetPart, newCell, value, matcher);
                        handled = true;
                    }
                }
            }

            if (!handled) {
                if (value != null) {
                    if (value instanceof Number) {
                        newCell.setT(STCellType.N);
                    } else {
                        newCell.setT(STCellType.STR);
                    }
                    newCell.setV(value.toString());

                } else {
                    newCell.setV("");
                }
            }
        } else {
            String value = insertBandDataToString(childBand, cellValue);
            newCell.setV(value);

            if (newCell.getT() == STCellType.S) {
                newCell.setT(STCellType.STR);
            }
        }
    }

    private <T> T getFirst(List<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    private <T> T getLast(List<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(list.size() - 1);
        }

        return null;
    }

    private static class Offset {
        int downOffset;
        int rightOffset;

        private Offset(int downOffset, int rightOffset) {
            this.downOffset = downOffset;
            this.rightOffset = rightOffset;
        }
    }

    private class LastRowBandVisitor implements BandVisitor {
        private int lastRow = 0;

        @Override
        public boolean visit(BandData band) {
            Range range = bandsToResultRanges.get(band);
            if (range != null && range.lastRow > lastRow) {
                lastRow = range.lastRow;
            }
            return false;
        }
    }
}
