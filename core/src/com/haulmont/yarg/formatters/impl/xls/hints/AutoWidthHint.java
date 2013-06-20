/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.xls.hints;

import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author artamonov
 * @version $Id: AutoWidthHint.java 9328 2012-10-18 15:28:32Z artamonov $
 */
public class AutoWidthHint implements XlsHint {

    private HSSFSheet resultSheet;

    private int resultColumnIndex;

    public AutoWidthHint(HSSFSheet resultSheet, int resultColumnIndex) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
    }

    @Override
    public void apply() {
        resultSheet.autoSizeColumn(resultColumnIndex);
    }
}
