package com.haulmont.yarg.formatters.impl.xlsx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range {
    public static Pattern FORMULA_RANGE_PATTERN = Pattern.compile("(.*)!\\$(.*)\\$(.*):\\$(.*)\\$(.*)");
    public static Pattern SINGLE_CELL_RANGE_PATTERN = Pattern.compile("(.*)!\\$(.*)\\$(.*)");
    public static Pattern RANGE_PATTERN = Pattern.compile("([A-z0-9]*):([A-z0-9]*)");

    public final String sheet;
    public final int firstColumn;
    public final int firstRow;
    public final int lastColumn;
    public final int lastRow;

    public Range(String sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
    }

    public static Range fromCells(String sheetName, String firstCellRef, String lastCellRef) {
        int startColumn, startRow, lastColumn, lastRow;

        CellReference firstCell = new CellReference(firstCellRef);
        CellReference lastCell = new CellReference(lastCellRef);
        startColumn = firstCell.column;
        startRow = firstCell.row;
        lastColumn = lastCell.column;
        lastRow = lastCell.row;

        Range result = new Range(sheetName, startColumn, startRow, lastColumn, lastRow);
        return result;
    }

    public static Range fromFormula(String range) {
        Matcher matcher = FORMULA_RANGE_PATTERN.matcher(range);
        Matcher matcher2 = SINGLE_CELL_RANGE_PATTERN.matcher(range);
        if (matcher.find()) {
            String sheet = matcher.group(1);

            String startColumnStr = matcher.group(2);
            String startRowStr = matcher.group(3);
            String endColumnStr = matcher.group(4);
            String endRowStr = matcher.group(5);
            int startColumn = XlsxUtils.getNumberFromColumnReference(startColumnStr);
            int startRow = Integer.valueOf(startRowStr);
            int lastColumn = XlsxUtils.getNumberFromColumnReference(endColumnStr);
            int lastRow = Integer.valueOf(endRowStr);
            Range result = new Range(sheet, startColumn, startRow, lastColumn, lastRow);
            return result;
        } else if (matcher2.find()) {
            String sheet = matcher2.group(1);

            String startColumnStr = matcher2.group(2);
            String startRowStr = matcher2.group(3);
            int startColumn = XlsxUtils.getNumberFromColumnReference(startColumnStr);
            int startRow = Integer.valueOf(startRowStr);
            Range result = new Range(sheet, startColumn, startRow, startColumn, startRow);
            return result;
        } else {
            throw new RuntimeException("Wrong range value " + range);
        }
    }

    public static Range fromRange(String sheet, String range) {
        Matcher matcher = RANGE_PATTERN.matcher(range);
        if (matcher.find()) {
            String firstCell = matcher.group(1);
            String lastCell = matcher.group(2);

            return fromCells(sheet, firstCell, lastCell);
        } else {
            throw new RuntimeException("Wrong range value " + range);
        }
    }

    public boolean contains(CellReference cellReference) {
        return firstColumn <= cellReference.column && firstRow <= cellReference.row
                && lastColumn >= cellReference.column && lastRow >= cellReference.row;
    }

    public boolean contains(Range range) {
        return firstColumn <= range.firstColumn && firstRow <= range.firstRow
                && lastColumn >= range.lastColumn && lastRow >= range.lastRow;
    }

    public Range shiftLeftRight(int shift) {
        return new Range(sheet, firstColumn + shift, firstRow, lastColumn + shift, lastRow);
    }

    public Range shiftUpDown(int shift) {
        return new Range(sheet, firstColumn, firstRow + shift, lastColumn, lastRow + shift);
    }

    public Range growUpDown(int grow) {
        return new Range(sheet, firstColumn, firstRow, lastColumn, lastRow + grow);
    }

    @Override
    public String toString() {
        return toFormula();
    }

    public String toFormula() {
        return String.format("%s!$%s$%d:$%s$%d", sheet, XlsxUtils.getColumnReferenceFromNumber(firstColumn), firstRow, XlsxUtils.getColumnReferenceFromNumber(lastColumn), lastRow);
    }

    public String toRange() {
        return String.format("%s%d:%s%d", XlsxUtils.getColumnReferenceFromNumber(firstColumn), firstRow, XlsxUtils.getColumnReferenceFromNumber(lastColumn), lastRow);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (lastColumn != range.lastColumn) return false;
        if (lastRow != range.lastRow) return false;
        if (firstColumn != range.firstColumn) return false;
        if (firstRow != range.firstRow) return false;
        if (sheet != null ? !sheet.equals(range.sheet) : range.sheet != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sheet != null ? sheet.hashCode() : 0;
        result = 31 * result + firstColumn;
        result = 31 * result + firstRow;
        result = 31 * result + lastColumn;
        result = 31 * result + lastRow;
        return result;
    }
}