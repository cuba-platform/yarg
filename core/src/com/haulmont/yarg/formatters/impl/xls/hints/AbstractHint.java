/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.xls.hints;

import com.haulmont.yarg.structure.BandData;
import org.apache.poi.hssf.usermodel.HSSFCell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractHint implements XlsHint {
    protected static class DataObject {
        protected final HSSFCell resultCell;
        protected final HSSFCell templateCell;
        protected final BandData bandData;

        public DataObject(HSSFCell resultCell, HSSFCell templateCell, BandData bandData) {
            this.resultCell = resultCell;
            this.templateCell = templateCell;
            this.bandData = bandData;
        }
    }

    protected List<DataObject> data = new ArrayList<>();

    protected String patternStr;
    protected Pattern pattern;


    protected AbstractHint(String patternStr) {
        this.patternStr = patternStr;
        this.pattern = Pattern.compile(patternStr);
    }

    @Override
    public void add(HSSFCell templateCell, HSSFCell resultCell, BandData bandData) {
        data.add(new DataObject(resultCell, templateCell, bandData));
    }

    @Override
    public CheckResult check(String templateCellValue) {
        Matcher matcher = pattern.matcher(templateCellValue);
        if (matcher.find()) {
            return new CheckResult(true, templateCellValue.replaceAll(patternStr, ""));
        } else {
            return CheckResult.NEGATIVE;
        }
    }
}
