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

package com.haulmont.yarg.formatters.impl.xlsx;

import org.xlsx4j.sml.Cell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range {
    public static Pattern FORMULA_RANGE_PATTERN = Pattern.compile("(.*)!\\$(.*)\\$(.*):\\$(.*)\\$(.*)");
    public static Pattern SINGLE_CELL_RANGE_PATTERN = Pattern.compile("(.*)!\\$(.*)\\$(.*)");
    public static Pattern RANGE_PATTERN = Pattern.compile("([A-z0-9]*):([A-z0-9]*)");

    private String sheet;
    private int firstColumn;
    private int firstRow;
    private int lastColumn;
    private int lastRow;

    public Range(String sheet, int firstColumn, int firstRow, int lastColumn, int lastRow) {
        this.sheet = sheet;
        this.firstColumn = firstColumn;
        this.firstRow = firstRow;
        this.lastColumn = lastColumn;
        this.lastRow = lastRow;
    }

    public static Range fromCells(String sheetName, String firstCellRef, String lastCellRef) {
        int startColumn, startRow, lastColumn, lastRow;

        CellReference firstCell = new CellReference(sheetName, firstCellRef);
        CellReference lastCell = new CellReference(sheetName, lastCellRef);
        startColumn = firstCell.getColumn();
        startRow = firstCell.getRow();
        lastColumn = lastCell.getColumn();
        lastRow = lastCell.getRow();

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

    public static Range fromCellFormula(String sheet, Cell cellWithFormula) {
        Matcher matcher = Range.RANGE_PATTERN.matcher(cellWithFormula.getF().getValue());
        if (matcher.find()) {
            String rangeStr = matcher.group();
            Range formulaRange = Range.fromRange(sheet, rangeStr);
            return formulaRange;
        }

        return null;
    }


    public boolean contains(CellReference cellReference) {
        return cellReference.getSheet().equals(getSheet()) && getFirstColumn() <= cellReference.getColumn() && getFirstRow() <= cellReference.getRow()
                && getLastColumn() >= cellReference.getColumn() && getLastRow() >= cellReference.getRow();
    }

    public boolean contains(Range range) {
        return range.getSheet().equals(getSheet()) && getFirstColumn() <= range.getFirstColumn() && getFirstRow() <= range.getFirstRow()
                && getLastColumn() >= range.getLastColumn() && getLastRow() >= range.getLastRow();
    }

    public boolean intersectsByVertical(Range range) {
        return this.getSheet().equals(range.getSheet()) && (
                this.getFirstRow() >= range.getFirstRow() && this.getFirstRow() <= range.getLastRow() ||
                        this.getLastRow() >= range.getFirstRow() && this.getLastRow() <= range.getLastRow() ||
                        range.getFirstRow() >= this.getFirstRow() && range.getFirstRow() <= this.getLastRow() ||
                        range.getLastRow() >= this.getFirstRow() && range.getLastRow() <= this.getLastRow()
        );
    }


    public Range shift(int downShift, int rightShift) {
        firstColumn += rightShift;
        firstRow += downShift;
        lastColumn += rightShift;
        lastRow += downShift;

        return this;
    }

    public Range shiftDown(int shift) {
        return shift(shift, 0);
    }

    public Range shiftRight(int shift) {
        return shift(0, shift);
    }

    public Range grow(int downGrow, int rightGrow) {
        lastColumn += rightGrow;
        lastRow += downGrow;

        return this;
    }

    public Range growDown(int grow) {
        return grow(grow, 0);
    }

    public Range growRight(int grow) {
        return grow(0, grow);
    }

    public Range copy() {
        return new Range(sheet, firstColumn, firstRow, lastColumn, lastRow);
    }

    @Override
    public String toString() {
        return toFormula();
    }

    public String toFormula() {
        return String.format("%s!$%s$%d:$%s$%d", getSheet(), XlsxUtils.getColumnReferenceFromNumber(getFirstColumn()), getFirstRow(), XlsxUtils.getColumnReferenceFromNumber(getLastColumn()), getLastRow());
    }

    public String toRange() {
        return String.format("%s%d:%s%d", XlsxUtils.getColumnReferenceFromNumber(getFirstColumn()), getFirstRow(), XlsxUtils.getColumnReferenceFromNumber(getLastColumn()), getLastRow());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (getLastColumn() != range.getLastColumn()) return false;
        if (getLastRow() != range.getLastRow()) return false;
        if (getFirstColumn() != range.getFirstColumn()) return false;
        if (getFirstRow() != range.getFirstRow()) return false;
        if (getSheet() != null ? !getSheet().equals(range.getSheet()) : range.getSheet() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = getSheet() != null ? getSheet().hashCode() : 0;
        result = 31 * result + getFirstColumn();
        result = 31 * result + getFirstRow();
        result = 31 * result + getLastColumn();
        result = 31 * result + getLastRow();
        return result;
    }

    public String getSheet() {
        return sheet;
    }

    public int getFirstColumn() {
        return firstColumn;
    }

    public int getFirstRow() {
        return firstRow;
    }

    public int getLastColumn() {
        return lastColumn;
    }

    public int getLastRow() {
        return lastRow;
    }
}