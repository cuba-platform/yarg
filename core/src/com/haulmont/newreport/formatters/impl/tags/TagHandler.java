/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 26.02.11 12:11
 *
 * $Id: TagHandler.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl.tags;

import com.haulmont.newreport.formatters.impl.doc.OfficeComponent;
import com.sun.star.text.XText;
import com.sun.star.text.XTextRange;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle tags in format strings
 */
public interface TagHandler {

    /**
     * Get Regexp Pattern for match format string
     *
     * @return Pattern
     */
    Pattern getTagPattern();

    /**
     * Handle tags for implementation using OO
     *
     * @param officeComponent OpenOffice Objects
     * @param destination     Text
     * @param textRange       Place for insert
     * @param paramValue      Parameter
     * @param matcher
     */
    void handleTag(OfficeComponent officeComponent,
                   XText destination, XTextRange textRange,
                   Object paramValue, Matcher matcher)
            throws Exception;

    /**
     * Handle tags for implementation using docx4j
     *
     * @param wordPackage
     * @param text
     * @param paramValue
     * @param matcher
     */
    void handleTag(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher matcher);
}