/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.xls.hints;

import com.haulmont.yarg.structure.BandData;
import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * @author artamonov
 * @version $Id: XlsHint.java 9328 2012-10-18 15:28:32Z artamonov $
 */
public interface XlsHint {
    class CheckResult {
        public final String cellValue;
        public final boolean result;

        public CheckResult(boolean result, String cellValue) {
            this.cellValue = cellValue;
            this.result = result;
        }

        public static CheckResult NEGATIVE = new CheckResult(false, null);
    }

    CheckResult check(String templateCellValue);

    void add(HSSFCell templateCell, HSSFCell resultCell, BandData bandData);

    void apply();
}