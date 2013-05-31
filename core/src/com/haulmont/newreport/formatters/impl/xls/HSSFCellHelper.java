/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 18.01.2011 11:10:15
 *
 * $Id: HSSFCellHelper.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl.xls;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellReference;

public final class HSSFCellHelper {
    private HSSFCellHelper() {
    }

    /**
     * Detects if cell contains only one template to inline value
     *
     * @param cell - cell
     * @return -
     */
    public static boolean isOneValueCell(HSSFCell cell, String value) {
        boolean result = true;
        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            if (value.lastIndexOf("${") != 0) {
                result = false;
            } else {
                result = value.indexOf("}") == value.length() - 1;
            }
        }
        return result;
    }

    public static HSSFCell getCellFromReference(CellReference cref, HSSFSheet templateSheet) {
        return getCellFromReference(templateSheet, cref.getCol(), cref.getRow());
    }

    public static HSSFCell getCellFromReference(HSSFSheet templateSheet, int colIndex, int rowIndex) {
        HSSFRow row = templateSheet.getRow(rowIndex);
        row = row == null ? templateSheet.createRow(rowIndex) : row;
        HSSFCell cell = row.getCell(colIndex);
        cell = cell == null ? row.createCell(colIndex) : cell;
        return cell;
    }
}
