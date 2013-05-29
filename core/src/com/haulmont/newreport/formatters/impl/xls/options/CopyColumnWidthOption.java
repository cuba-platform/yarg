/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.newreport.formatters.impl.xls.options;

import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author artamonov
 * @version $Id: CopyColumnWidthOption.java 9328 2012-10-18 15:28:32Z artamonov $
 */
public class CopyColumnWidthOption implements StyleOption {
    private int width;

    private int resultColumnIndex;

    private HSSFSheet resultSheet;

    public CopyColumnWidthOption(HSSFSheet resultSheet, int resultColumnIndex, int width) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
        this.width = width;
    }

    @Override
    public void apply() {
        resultSheet.setColumnWidth(resultColumnIndex, width);
    }
}