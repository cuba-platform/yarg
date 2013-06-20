/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.xls.caches;

import org.apache.poi.hssf.usermodel.HSSFFont;

import java.util.HashMap;
import java.util.Map;

/**
 * Font cache for XlsFormatter
 * <p>$Id: XlsFontCache.java 10854 2013-03-20 08:07:17Z artamonov $</p>
 *
 * @author artamonov
 */
public class XlsFontCache {
    private Map<HSSFFontCacheKey, HSSFFont> fonts = new HashMap<HSSFFontCacheKey, HSSFFont>();

    public HSSFFont getFontByTemplate(HSSFFont font){
        return fonts.get(new HSSFFontCacheKey(font));
    }

    public void addCachedFont(HSSFFont templateFont, HSSFFont font) {
        fonts.put(new HSSFFontCacheKey(templateFont), font);
    }
}