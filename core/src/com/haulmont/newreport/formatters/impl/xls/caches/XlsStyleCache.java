/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.newreport.formatters.impl.xls.caches;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>$Id: XlsStyleCache.java 10854 2013-03-20 08:07:17Z artamonov $</p>
 *
 * @author artamonov
 */
public class XlsStyleCache {

    private Map<HSSFStyleCacheKey, HSSFCellStyle> cellStyles = new HashMap<HSSFStyleCacheKey, HSSFCellStyle>();

    private Map<String, HSSFCellStyle> styleMap = new HashMap<String, HSSFCellStyle>();

    public HSSFCellStyle processCellStyle(HSSFCellStyle cellStyle) {
        HSSFCellStyle cachedCellStyle = cellStyles.get(new HSSFStyleCacheKey(cellStyle));
        if (cachedCellStyle == null)
            cellStyles.put(new HSSFStyleCacheKey(cellStyle), cellStyle);
        else
            cellStyle = cachedCellStyle;

        return cellStyle;
    }

    public HSSFCellStyle getCellStyleByTemplate(HSSFCellStyle templateCellStyle) {
        return cellStyles.get(new HSSFStyleCacheKey(templateCellStyle));
    }

    public void addCachedStyle(HSSFCellStyle templateCellStyle, HSSFCellStyle cellStyle) {
        cellStyles.put(new HSSFStyleCacheKey(templateCellStyle), cellStyle);
    }

    public void addNamedStyle(HSSFCellStyle cellStyle) {
        styleMap.put(cellStyle.getUserStyleName(), cellStyle);
    }

    public HSSFCellStyle getStyleByName(String styleName) {
        return styleMap.get(styleName);
    }
}