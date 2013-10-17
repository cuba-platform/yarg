/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
import com.haulmont.yarg.structure.*;
import com.haulmont.yarg.structure.impl.BandOrientation;
import org.apache.commons.collections.CollectionUtils;
import org.docx4j.XmlUtils;
import org.docx4j.dml.chart.*;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.CalcChain;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.*;

import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;

public class XlsxFormatter extends AbstractFormatter {
    protected Document template;
    protected Document result;

    protected ArrayListMultimap<Range, Range> rangeDependencies = ArrayListMultimap.create();
    protected LinkedHashMultimap<Range, Range> rangeVerticalIntersections = LinkedHashMultimap.create();
    protected BiMap<BandData, Range> bandsToTemplateRanges = HashBiMap.create();
    protected BiMap<BandData, Range> bandsToResultRanges = HashBiMap.create();

    protected Set<Cell> innerFormulas = new HashSet<>();
    protected Set<Cell> outerFormulas = new HashSet<>();

    private Map<Worksheet, Long> lastRowForSheet = new HashMap<>();
    private int previousRangesRightOffset;

    public XlsxFormatter(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        super(rootBand, reportTemplate, outputStream);
        supportedOutputTypes.add(ReportOutputType.xlsx);
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
            throw wrapWithReportingException("An error occurred during save result document", e);
        }
    }

    protected void init() {
        try {
            template = Document.create((SpreadsheetMLPackage) SpreadsheetMLPackage.load(reportTemplate.getDocumentContent()));

//            SpreadsheetMLPackage pkg = SpreadsheetMLPackage.createPackage();
//            WorksheetPart sheet = pkg.createWorksheetPart(new PartName("/sheet1.xml"), "data", 1);

//            result = create(pkg);
            result = Document.create((SpreadsheetMLPackage) SpreadsheetMLPackage.load(reportTemplate.getDocumentContent()));
            result.getWorkbook().getCalcPr().setCalcMode(STCalcMode.AUTO);
            result.getWorkbook().getCalcPr().setFullCalcOnLoad(true);
        } catch (Exception e) {
            throw wrapWithReportingException(String.format("An error occurred while loading template [%s]", reportTemplate.getDocumentName()), e);
        }
    }

    protected void findVerticalDependencies() {
        List<CTDefinedName> definedName = template.getWorkbook().getDefinedNames().getDefinedName();
        for (CTDefinedName name1 : definedName) {
            for (CTDefinedName name2 : definedName) {
                if (!name1.equals(name2)) {
                    Range range1 = Range.fromFormula(name1.getValue());
                    Range range2 = Range.fromFormula(name2.getValue());
                    if (range1.sheet.equals(range2.sheet) && (
                            range1.firstRow >= range2.firstRow && range1.firstRow <= range2.lastRow ||
                                    range1.lastRow >= range2.firstRow && range1.lastRow <= range2.lastRow ||
                                    range2.firstRow >= range1.firstRow && range2.firstRow <= range1.lastRow ||
                                    range2.lastRow >= range1.firstRow && range2.lastRow <= range1.lastRow
                    )
                            ) {
                        rangeVerticalIntersections.put(range1, range2);
                        rangeVerticalIntersections.put(range2, range1);
                    }
                }
            }
        }
    }

    protected void updateCharts() {
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

                                    for (Range range : rangeDependencies.keySet()) {
                                        if (range.contains(temlpateCaptionsRange)) {
                                            List<Range> seriesResultRanges = rangeDependencies.get(range);

                                            Range seriesFirstRange = getFirst(seriesResultRanges);
                                            Range seriesLastRange = getLast(seriesResultRanges);

                                            Offset offset = calculateOffset(temlpateCaptionsRange, seriesFirstRange);
                                            temlpateCaptionsRange = temlpateCaptionsRange.shift(offset.downOffset, offset.rightOffset);

                                            Offset grow = calculateOffset(seriesFirstRange, seriesLastRange);
                                            temlpateCaptionsRange = temlpateCaptionsRange.grow(grow.downOffset, grow.rightOffset);

                                            captions.getStrRef().setF(temlpateCaptionsRange.toFormula());
                                            break;
                                        }
                                    }

                                    for (Range range : rangeDependencies.keySet()) {
                                        if (range.contains(templateDataRange)) {
                                            List<Range> seriesResultRanges = rangeDependencies.get(range);

                                            Range seriesFirstRange = getFirst(seriesResultRanges);
                                            Range seriesLastRange = getLast(seriesResultRanges);

                                            Offset offset = calculateOffset(temlpateCaptionsRange, seriesFirstRange);
                                            templateDataRange = templateDataRange.shift(offset.downOffset, offset.rightOffset);

                                            Offset grow = calculateOffset(seriesFirstRange, seriesLastRange);
                                            templateDataRange = templateDataRange.grow(grow.downOffset, grow.rightOffset);

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

    protected void shiftChart(Document.ChartPair chart, Range templateRange, Range firstResultRange) {
        Offset offset = calculateOffset(templateRange, firstResultRange);

        CTTwoCellAnchor anchor = (CTTwoCellAnchor) chart.getDrawing().getJaxbElement().getEGAnchor().get(0);
        anchor.getFrom().setRow(anchor.getFrom().getRow() + offset.downOffset);
        anchor.getFrom().setCol(anchor.getFrom().getCol() + offset.rightOffset);
        anchor.getTo().setRow(anchor.getTo().getRow() + offset.downOffset);
        anchor.getTo().setCol(anchor.getTo().getCol() + offset.rightOffset);
    }

    protected void updateFormulas() {
        int formulaCount = 1;
        CTCalcChain calculationChain = getCalculationChain();

        for (Cell cellWithFormula : innerFormulas) {
            Row row = (Row) cellWithFormula.getParent();
            SheetData sheetData = (SheetData) row.getParent();
            Worksheet worksheet = (Worksheet) sheetData.getParent();
            Range formulaRange = Range.fromCellFormula(result.getSheetName(worksheet), cellWithFormula);
            Range originalFormulaRange = formulaRange;
            for (Range templateRange : rangeDependencies.keySet()) {
                if (templateRange.contains(formulaRange)) {
                    List<Range> resultRanges = rangeDependencies.get(templateRange);

                    CellReference cellReference = new CellReference(result.getSheetName(worksheet), cellWithFormula.getR());
                    for (Range resultRange : resultRanges) {
                        if (resultRange.contains(cellReference)) {
                            Offset offset = calculateOffset(templateRange, resultRange);

                            formulaRange = formulaRange.shift(offset.downOffset, offset.rightOffset);
                            updateFormula(cellWithFormula, originalFormulaRange, formulaRange, calculationChain, formulaCount++);
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
            CellReference formulaCellReference = new CellReference(result.getSheetName(worksheet), cellWithFormula.getR());

            BandData parentBand = null;
            for (Range resultRange : rangeDependencies.values()) {
                if (resultRange.contains(formulaCellReference)) {
                    BandData formulaCellBand = bandsToResultRanges.inverse().get(resultRange);
                    parentBand = formulaCellBand.getParentBand();
                }
            }

            for (Range templateRange : rangeDependencies.keySet()) {
                if (templateRange.contains(formulaRange)) {
                    List<Range> resultRanges = new ArrayList<>(rangeDependencies.get(templateRange));
                    for (Iterator<Range> iterator = resultRanges.iterator(); iterator.hasNext(); ) {
                        Range resultRange = iterator.next();
                        BandData bandData = bandsToResultRanges.inverse().get(resultRange);
                        if (!bandData.getParentBand().equals(parentBand)) {
                            iterator.remove();
                        }
                    }

                    if (resultRanges.size() > 0) {
                        Range firstResultRange = getFirst(resultRanges);
                        Range lastResultRange = getLast(resultRanges);

                        Offset offset = calculateOffset(templateRange, firstResultRange);
                        formulaRange = formulaRange.shift(offset.downOffset, offset.rightOffset);

                        Offset grow = calculateOffset(firstResultRange, lastResultRange);
                        formulaRange = formulaRange.grow(grow.downOffset, grow.rightOffset);
                        updateFormula(cellWithFormula, originalFormulaRange, formulaRange, calculationChain, formulaCount++);
                    } else {
                        cellWithFormula.setF(null);
                        cellWithFormula.setV("ERROR: Formula references to empty range");
                        cellWithFormula.setT(STCellType.STR);
                    }
                    break;
                }
            }
        }
    }

    protected CTCalcChain getCalculationChain() {
        CTCalcChain calculationChain = null;
        try {
            CalcChain part = (CalcChain) result.getPackage().getParts().get(new PartName("/xl/calcChain.xml"));
            if (part != null) {
                calculationChain = part.getJaxbElement();
                calculationChain.getC().clear();
            }
        } catch (InvalidFormatException e) {
            //do nothing
        }
        return calculationChain;
    }

    protected void updateFormula(Cell cellWithFormula, Range originalFormulaRange, Range formulaRange, CTCalcChain calculationChain, int formulaCount) {
        CTCellFormula formula = cellWithFormula.getF();
        formula.setValue(formula.getValue().replace(originalFormulaRange.toRange(), formulaRange.toRange()));

        if (calculationChain != null) {
            CTCalcCell calcCell = new CTCalcCell();
            calcCell.setR(cellWithFormula.getR());
            calcCell.setI(formulaCount);
            calculationChain.getC().add(calcCell);
        }
    }

    protected Offset calculateOffset(Range from, Range to) {
        int downOffset = to.firstRow - from.firstRow;
        int rightOffset = to.firstColumn - from.firstColumn;
        return new Offset(downOffset, rightOffset);
    }

    protected void updateMergeRegions() {
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

    protected void writeBand(BandData childBand) {
        try {
            if (BandOrientation.HORIZONTAL == childBand.getOrientation()) {
                writeHBand(childBand);
            } else {
                writeVBand(childBand);
            }
        } catch (ReportingException e) {
            throw e;
        } catch (Exception e) {
            throw wrapWithReportingException(String.format("An error occurred while rendering band [%s]", childBand.getName()), e);
        }
    }

    protected void writeHBand(BandData band) {
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

    protected void writeVBand(BandData band) {
        Range templateRange = getBandRange(band);
        Worksheet resultSheet = result.getSheetByName(templateRange.sheet);
        List<Row> resultSheetRows = resultSheet.getSheetData().getRow();

        Row firstRow = findNextRowForVBand(band, templateRange, resultSheetRows);
        firstRow = ensureNecessaryRowsCreated(templateRange, resultSheet, firstRow);

        List<Cell> resultCells = copyCells(band, templateRange, resultSheetRows, firstRow);

        //shift cells right
        for (Cell resultCell : resultCells) {
            CellReference cellReference = new CellReference(templateRange.sheet, resultCell.getR());
            String newColumn = XlsxUtils.getColumnReferenceFromNumber(cellReference.column + previousRangesRightOffset);
            resultCell.setR(newColumn + cellReference.row);
        }

        updateRangeMappings(band, templateRange, resultCells);
    }

    protected void updateRangeMappings(BandData band, Range templateRange, List<Cell> resultCells) {
        if (CollectionUtils.isNotEmpty(resultCells)) {
            Range resultRange = Range.fromCells(templateRange.sheet, getFirst(resultCells).getR(), resultCells.get(resultCells.size() - 1).getR());
            rangeDependencies.put(templateRange, resultRange);
            bandsToTemplateRanges.forcePut(band, templateRange);
            bandsToResultRanges.forcePut(band, resultRange);
        }
    }

    protected Row findNextRowForHBand(BandData band, Range templateRange, List<Row> resultSheetRows) {
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

    protected Row findNextRowForVBand(BandData band, Range templateRange, List<Row> thisSheetRows) {
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

    protected Row ensureNecessaryRowsCreated(Range templateRange, Worksheet resultSheet, Row firstRow) {
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

    protected List<Cell> copyCells(BandData band, Range templateRange, List<Row> resultSheetRows, Row firstRow) {
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

    protected List<Range> findAlreadyRenderedRanges(BandData band) {
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

    protected Range getBandRange(BandData band) {
        CTDefinedName targetRange = template.getDefinedName(band.getName());
        if (targetRange == null) {
            throw wrapWithReportingException(String.format("Could not find named range for band [%s]", band.getName()));
        }

        return Range.fromFormula(targetRange.getValue());
    }

    protected Row createNewRow(Worksheet resultSheet) {
        Row newRow = Context.getsmlObjectFactory().createRow();
        Long currentRow = lastRowForSheet.get(resultSheet);
        currentRow = currentRow != null ? currentRow : 0;
        currentRow++;
        newRow.setR(currentRow);
        lastRowForSheet.put(resultSheet, currentRow);
        resultSheet.getSheetData().getRow().add(newRow);
        newRow.setParent(resultSheet.getSheetData());

        return newRow;
    }

    protected List<Cell> copyCells(Range templateRange, BandData childBand, Row newRow, List<Cell> templateCells) {
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

    protected void addFormulaForPostProcessing(Range templateRange, Row newRow, Cell templateCell, Cell newCell) {
        SheetData sheetData = (SheetData) newRow.getParent();
        Worksheet worksheet = (Worksheet) sheetData.getParent();
        Range formulaRange = Range.fromCellFormula(result.getSheetName(worksheet), templateCell);
        if (templateRange.contains(formulaRange)) {
            innerFormulas.add(newCell);
        } else {
            outerFormulas.add(newCell);
        }
    }

    protected void copyRowSettings(Row templateRow, Row newRow) {
        newRow.setHt(templateRow.getHt());
        newRow.setCustomHeight(true);
    }

    protected void updateCell(WorksheetPart worksheetPart, BandData bandData, Cell newCell) {
        String cellValue = template.getCellValue(newCell);

        if (cellValue == null) {
            newCell.setV("");
            return;
        }

        if (UNIVERSAL_ALIAS_PATTERN.matcher(cellValue).matches()) {
            String paramName = unwrapParameterName(cellValue);
            String paramFullName = bandData.getName() + "." + paramName;
            Object value = bandData.getData().get(paramName);

            boolean handledByTags = false;
            Map<String, ReportFieldFormat> fieldFormats = rootBand.getReportFieldFormats();
            if (value != null && fieldFormats != null && fieldFormats.containsKey(paramFullName)) {
                String format = fieldFormats.get(paramFullName).getFormat();
                // Handle doctags
                for (ContentInliner contentInliner : contentInliners) {
                    Matcher matcher = contentInliner.getTagPattern().matcher(format);
                    if (matcher.find()) {
                        contentInliner.inlineToXlsx(result.getPackage(), worksheetPart, newCell, value, matcher);
                        handledByTags = true;
                    }
                }
            } else {
                value = formatValue(value, paramFullName);
            }

            if (!handledByTags) {
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
            String value = insertBandDataToString(bandData, cellValue);
            newCell.setV(value);

            if (newCell.getT() == STCellType.S) {
                newCell.setT(STCellType.STR);
            }
        }
    }

    protected <T> T getFirst(List<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;
    }

    protected <T> T getLast(List<T> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(list.size() - 1);
        }

        return null;
    }

    protected static class Offset {
        int downOffset;
        int rightOffset;

        private Offset(int downOffset, int rightOffset) {
            this.downOffset = downOffset;
            this.rightOffset = rightOffset;
        }
    }

    protected class LastRowBandVisitor implements BandVisitor {
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
