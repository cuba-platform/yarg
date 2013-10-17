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
