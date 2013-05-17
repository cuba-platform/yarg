/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.newreport.formatters.impl.xls.caches;

import org.apache.poi.hssf.usermodel.HSSFFont;

import java.io.Serializable;

/**
 * @author artamonov
 * @version $Id: HSSFFontCacheKey.java 10854 2013-03-20 08:07:17Z artamonov $
 */
public class HSSFFontCacheKey implements Serializable {

    private static final long serialVersionUID = 7503724004378911912L;

    protected final HSSFFont font;

    public HSSFFontCacheKey(HSSFFont font) {
        this.font = font;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof HSSFFontCacheKey)
            return font.fontEquals(((HSSFFontCacheKey) obj).font);
        else
            return false;
    }

    @Override
    public int hashCode() {
        return font.fontHashCode();
    }
}