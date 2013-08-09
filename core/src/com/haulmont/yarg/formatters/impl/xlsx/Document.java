/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.xlsx;

import org.docx4j.dml.chart.CTChartSpace;
import org.docx4j.dml.spreadsheetdrawing.CTDrawing;
import org.docx4j.dml.spreadsheetdrawing.CTMarker;
import org.docx4j.dml.spreadsheetdrawing.CTTwoCellAnchor;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.DrawingML.Drawing;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.SpreadsheetML.SharedStrings;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.xlsx4j.sml.*;

import java.util.*;

public class Document {
    protected SpreadsheetMLPackage thePackage;
    protected List<SheetWrapper> worksheets = new ArrayList<>();

    protected Map<Range, ChartPair> chartSpaces = new HashMap<>();
    protected Workbook workbook;
    protected SharedStrings sharedStrings;
    protected HashSet<Part> handled = new HashSet<Part>();


    public static Document create(SpreadsheetMLPackage thePackage) {
        Document document = new Document();
        document.thePackage = thePackage;
        RelationshipsPart rp = thePackage.getRelationshipsPart();
        document.traverse(null, rp);

        return document;
    }

    public SpreadsheetMLPackage getPackage() {
        return thePackage;
    }

    public Map<Range, ChartPair> getChartSpaces() {
        return chartSpaces;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public List<SheetWrapper> getWorksheets() {
        return worksheets;
    }

    public Worksheet getSheetByName(String name) {
        for (Document.SheetWrapper sheetWrapper : worksheets) {
            if (sheetWrapper.getName().equals(name)) {
                return sheetWrapper.getWorksheet().getJaxbElement();
            }
        }

        return null;
    }


    public String getSheetName(Worksheet worksheet) {
        for (Document.SheetWrapper sheetWrapper : worksheets) {
            if (worksheet == sheetWrapper.getWorksheet().getJaxbElement()) {
                return sheetWrapper.getName();
            }
        }

        return null;
    }

    public String getCellValue(Cell cell) {
        if (cell.getV() == null) return null;
        if (cell.getT().equals(STCellType.S)) {
            return sharedStrings.getJaxbElement().getSi().get(Integer.parseInt(cell.getV())).getT().getValue();
        } else {
            return cell.getV();
        }
    }

    public CTDefinedName getDefinedName(String name) {
        List<CTDefinedName> definedName = workbook.getDefinedNames().getDefinedName();
        CTDefinedName targetRange = null;
        for (CTDefinedName namedRange : definedName) {
            if (name.equals(namedRange.getName())) {
                targetRange = namedRange;
            }
        }

        return targetRange;
    }

    public List<Cell> getCellsByRange(Range range) {
        Worksheet sheet = getSheetByName(range.sheet);
        SheetData data = sheet.getSheetData();

        List<Cell> result = new ArrayList<>();
        for (int i = 1; i <= data.getRow().size(); i++) {
            Row row = data.getRow().get(i - 1);
            if (range.firstRow <= row.getR() && row.getR() <= range.lastRow) {
                List<Cell> c = row.getC();

                for (Cell cell : c) {
                    CellReference cellReference = new CellReference(range.sheet, cell.getR());
                    if (range.firstColumn <= cellReference.column && cellReference.column <= range.lastColumn) {
                        result.add(cell);
                    }
                }
            }
        }
        return result;
    }

    private void traverse(Part parent, RelationshipsPart rp) {
        int chartNum = 0;
        for (Relationship r : rp.getRelationships().getRelationship()) {
            Part part = rp.getPart(r);
            if (handled.contains(part)) {
                continue;
            }

            if (part instanceof JaxbXmlPart) {
                Object o = ((JaxbXmlPart) part).getJaxbElement();

                if (o instanceof CTChartSpace) {
                    Drawing drawing = (Drawing) parent;
                    CTDrawing ctDrawing = drawing.getJaxbElement();
                    Object anchorObj = ctDrawing.getEGAnchor().get(chartNum++);

                    Range range = null;
                    if (anchorObj instanceof CTTwoCellAnchor) {
                        CTTwoCellAnchor ctTwoCellAnchor = (CTTwoCellAnchor) anchorObj;
                        CTMarker from = ctTwoCellAnchor.getFrom();
                        CTMarker to = ctTwoCellAnchor.getTo();
                        String sheetName = worksheets.get(worksheets.size() - 1).name;
                        range = new Range(sheetName, from.getCol() + 1, from.getRow() + 1, to.getCol() + 1, to.getRow() + 1);
                    }

                    chartSpaces.put(range, new ChartPair((CTChartSpace) o, drawing));
                }

                if (o instanceof Workbook) {
                    workbook = (Workbook) o;
                }
            }

            if (part instanceof WorksheetPart) {
                for (Relationship relationship : part.getSourceRelationships()) {
                    if (relationship.getType().endsWith("worksheet")) {
                        String sheetId = relationship.getId();
                        for (Sheet sheet : workbook.getSheets().getSheet()) {
                            if (sheet.getId().equals(sheetId)) {
                                worksheets.add(new SheetWrapper((WorksheetPart) part, sheet.getName()));
                            }
                        }

                    }
                }
            } else if (part instanceof SharedStrings) {
                sharedStrings = (SharedStrings) part;
            }

            handled.add(part);

            if (part.getRelationshipsPart() != null) {
                traverse(part, part.getRelationshipsPart());
            }
        }
    }

    public void clearWorkbook() {
        for (SheetWrapper sheet : worksheets) {
            sheet.worksheet.getJaxbElement().getSheetData().getRow().clear();
            CTMergeCells mergeCells = sheet.worksheet.getJaxbElement().getMergeCells();
            if (mergeCells != null && mergeCells.getMergeCell() != null) {
                mergeCells.getMergeCell().clear();
            }
        }
        workbook.getDefinedNames().getDefinedName().clear();
    }

    public static class SheetWrapper {
        private WorksheetPart worksheet;
        private String name;

        public SheetWrapper(WorksheetPart worksheet, String name) {
            this.worksheet = worksheet;
            this.name = name;
        }

        public WorksheetPart getWorksheet() {
            return worksheet;
        }

        public String getName() {
            return name;
        }
    }

    public static class ChartPair {
        private final CTChartSpace chartSpace;
        private final Drawing drawing;

        public ChartPair(CTChartSpace chartSpace, Drawing drawing) {
            this.chartSpace = chartSpace;
            this.drawing = drawing;
        }

        public CTChartSpace getChartSpace() {
            return chartSpace;
        }

        public Drawing getDrawing() {
            return drawing;
        }
    }
}