/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.xlsx;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CellReference {
    public static final Pattern CELL_COORDINATES_PATTERN = Pattern.compile("([A-z]+)([0-9]+)");
    public final int column;
    public final int row;
    public final String sheet;

    public CellReference(String sheet, int column, int row) {
        this.column = column;
        this.row = row;
        this.sheet = sheet;
    }

    public CellReference(String sheet, String cellRef) {
        Matcher matcher = CELL_COORDINATES_PATTERN.matcher(cellRef);
        if (matcher.find()) {
            column = XlsxUtils.getNumberFromColumnReference(matcher.group(1));
            row = Integer.valueOf(matcher.group(2));
            this.sheet = sheet;
        } else {
            throw new RuntimeException("Wrong cell " + cellRef);
        }
    }
}
