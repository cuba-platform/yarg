/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 26.02.11 12:11
 *
 * $Id: ImageTagHandler.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.yarg.formatters.impl.inline;

import com.haulmont.yarg.exception.ReportingException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Handle images with format string: ${image:100x100}
 */
public class ImageContentInliner extends AbstractInliner {
    private final static String REGULAR_EXPRESSION = "\\$\\{image:([0-9]+?)x([0-9]+?)\\}";

    public ImageContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    protected byte[] getContent(Object paramValue) {
        try {
            return IOUtils.toByteArray(new URL(paramValue.toString()).openStream());
        } catch (IOException e) {
            throw new ReportingException("Unable to get image from " + paramValue, e);
        }
    }
}