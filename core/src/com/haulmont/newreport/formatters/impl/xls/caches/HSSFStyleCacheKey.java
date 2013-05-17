/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.newreport.formatters.impl.xls.caches;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.io.Serializable;

/**
 * @author artamonov
 * @version $Id: HSSFStyleCacheKey.java 10854 2013-03-20 08:07:17Z artamonov $
 */
public class HSSFStyleCacheKey implements Serializable {

    private static final long serialVersionUID = 3327348050407288508L;

    protected final HSSFCellStyle style;

    public HSSFStyleCacheKey(HSSFCellStyle style) {
        this.style = style;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HSSFStyleCacheKey)
            return style.formatEquals(((HSSFStyleCacheKey) obj).style);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return style.formatHashCode();
    }
}