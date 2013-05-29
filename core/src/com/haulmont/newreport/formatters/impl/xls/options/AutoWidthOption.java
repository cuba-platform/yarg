/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.newreport.formatters.impl.xls.options;

import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author artamonov
 * @version $Id: AutoWidthOption.java 9328 2012-10-18 15:28:32Z artamonov $
 */
public class AutoWidthOption implements StyleOption {

    private HSSFSheet resultSheet;

    private int resultColumnIndex;

    public AutoWidthOption(HSSFSheet resultSheet, int resultColumnIndex) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
    }

    @Override
    public void apply() {
        resultSheet.autoSizeColumn(resultColumnIndex);
    }
}
