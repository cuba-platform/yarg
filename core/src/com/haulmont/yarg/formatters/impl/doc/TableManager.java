/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 12.10.2010 19:21:36
 *
 * $Id$
 */
package com.haulmont.yarg.formatters.impl.doc;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XNameAccess;
import com.sun.star.frame.XController;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
import com.sun.star.table.XCell;
import com.sun.star.table.XCellRange;
import com.sun.star.table.XTableRows;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.haulmont.yarg.formatters.impl.doc.UnoHelper.copy;
import static com.haulmont.yarg.formatters.impl.doc.UnoHelper.paste;
import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.*;

public class TableManager {
    protected XTextTable xTextTable;

    public TableManager(XComponent xComponent, String tableName) throws NoSuchElementException, WrappedTargetException {
        xTextTable = getTableByName(xComponent, tableName);
    }

    public XTextTable getXTextTable() {
        return xTextTable;
    }

    public static List<String> getTablesNames(XComponent xComponent) {
        XNameAccess tables = asXTextTablesSupplier(xComponent).getTextTables();
        return new ArrayList<String>(Arrays.asList(tables.getElementNames()));
    }

    protected static XTextTable getTableByName(XComponent xComponent, String tableName) throws NoSuchElementException, WrappedTargetException {
        XNameAccess tables = asXTextTablesSupplier(xComponent).getTextTables();
        return (XTextTable) ((Any) tables.getByName(tableName)).getObject();
    }

    public boolean hasValueExpressions() {
        XTextTable xTextTable = getXTextTable();
        int lastRow = xTextTable.getRows().getCount() - 1;
        try {
            for (int i = 0; i < xTextTable.getColumns().getCount(); i++) {
                String templateText = asXText(getXCell(i, lastRow)).getString();
                if (AbstractFormatter.UNIVERSAL_ALIAS_PATTERN.matcher(templateText).find()) {
                    return true;
                }
            }
        } catch (Exception e) {
            throw new ReportingException(e);
        }
        return false;
    }

    public XCell getXCell(int col, int row) throws IndexOutOfBoundsException {
        return asXCellRange(xTextTable).getCellByPosition(col, row);
    }

    public void selectRow(XController xController, int row) throws com.sun.star.uno.Exception {
        String[] cellNames = xTextTable.getCellNames();
        int colCount = xTextTable.getColumns().getCount();
        String firstCellName = cellNames[row * colCount];
        String lastCellName = cellNames[row * colCount + colCount - 1];
        XTextTableCursor xTextTableCursor = xTextTable.createCursorByCellName(firstCellName);
        xTextTableCursor.gotoCellByName(lastCellName, true);
        // stupid shit. It works only if XCellRange was created via cursor. why????
        // todo: refactor this if possible
        if (firstCellName.equalsIgnoreCase(lastCellName)) {
            XCell cell = asXCellRange(xTextTable).getCellByPosition(0, row);
            asXSelectionSupplier(xController).select(new Any(new Type(XCell.class), cell));
        } else {
            XCellRange xCellRange = asXCellRange(xTextTable).getCellRangeByName(xTextTableCursor.getRangeName());
            // and why do we need Any here?
            asXSelectionSupplier(xController).select(new Any(new Type(XCellRange.class), xCellRange));
        }
    }

    public void deleteLastRow() {
        XTableRows xTableRows = xTextTable.getRows();
        xTableRows.removeByIndex(xTableRows.getCount() - 1, 1);
    }

    public void insertRowToEnd() {
        XTableRows xTableRows = xTextTable.getRows();
        xTableRows.insertByIndex(xTableRows.getCount(), 1);
    }

    public void duplicateLastRow(XDispatchHelper xDispatchHelper, XController xController) throws com.sun.star.uno.Exception, IllegalArgumentException {
        int lastRowNum = xTextTable.getRows().getCount() - 1;
        selectRow(xController, lastRowNum);
        XDispatchProvider xDispatchProvider = asXDispatchProvider(xController.getFrame());
        copy(xDispatchHelper, xDispatchProvider);
        insertRowToEnd();
        selectRow(xController, ++lastRowNum);
        paste(xDispatchHelper, xDispatchProvider);
    }
}
