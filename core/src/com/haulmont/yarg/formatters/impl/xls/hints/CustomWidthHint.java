package com.haulmont.yarg.formatters.impl.xls.hints;

import org.apache.poi.hssf.usermodel.HSSFCell;

import java.util.regex.Matcher;

/**
 * @author kozyaikin
 * @version $Id: CustomWidthHint.java 10710 2013-03-01 12:40:55Z kozyaikin $
 */
public class CustomWidthHint extends AbstractHint {
    public CustomWidthHint() {
        super("##width=([A-z0-9]+)");
    }

    @Override
    public void apply() {
        for (DataObject dataObject : data) {
            HSSFCell resultCell = dataObject.resultCell;
            HSSFCell templateCell = dataObject.templateCell;

            String templateCellValue = templateCell.getStringCellValue();

            Matcher matcher = pattern.matcher(templateCellValue);
            if (matcher.find()) {
                String paramName = matcher.group(1);
                Integer width = (Integer) dataObject.bandData.getParameterValue(paramName);
                if (width != null) {
                    resultCell.getSheet().setColumnWidth(resultCell.getColumnIndex(), width);
                }
            }
        }
    }
}
