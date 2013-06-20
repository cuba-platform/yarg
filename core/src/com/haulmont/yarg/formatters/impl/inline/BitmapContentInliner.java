/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.inline;

import java.util.regex.Pattern;

/**
 * Handle images with format string: ${bitmap:100x100}
 */
public class BitmapContentInliner extends AbstractInliner {
    private final static String REGULAR_EXPRESSION = "\\$\\{bitmap:([0-9]+?)x([0-9]+?)\\}";

    public BitmapContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    protected byte[] getContent(Object paramValue) {
        return (byte[]) paramValue;
    }
}