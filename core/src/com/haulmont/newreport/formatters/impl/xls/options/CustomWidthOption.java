package com.haulmont.newreport.formatters.impl.xls.options;

import org.apache.poi.hssf.usermodel.HSSFSheet;

/**
 * @author kozyaikin
 * @version $Id: CustomWidthOption.java 10710 2013-03-01 12:40:55Z kozyaikin $
 */
public class CustomWidthOption implements StyleOption {

    private HSSFSheet resultSheet;

    private int resultColumnIndex;
    private int width;

    public CustomWidthOption(HSSFSheet resultSheet, int resultColumnIndex, int width) {
        this.resultSheet = resultSheet;
        this.resultColumnIndex = resultColumnIndex;
        this.width=width;
    }

    @Override
    public void apply() {
        resultSheet.setColumnWidth(resultColumnIndex, width);
    }
}
