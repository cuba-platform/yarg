package com.haulmont.yarg.formatters.impl.xls.hints;

import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author kozyaikin
 * @version $Id: CustomWidthHint.java 10710 2013-03-01 12:40:55Z kozyaikin $
 */
public class CustomWidthHint implements XlsHint {

    private HSSFSheet resultSheet;

    private int resultColumnIndex;
    private int width;

    public CustomWidthHint(HSSFSheet resultSheet, int resultColumnIndex, int width) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
        this.width=width;
    }

    @Override
    public void apply() {
        resultSheet.setColumnWidth(resultColumnIndex, width);
    }
}
