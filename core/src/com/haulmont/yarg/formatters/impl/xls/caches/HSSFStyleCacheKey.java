/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.haulmont.yarg.formatters.impl.xls.caches;

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