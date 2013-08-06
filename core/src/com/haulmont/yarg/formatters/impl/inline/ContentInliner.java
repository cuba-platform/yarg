/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 26.02.11 12:11
 *
 * $Id: TagHandler.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.yarg.formatters.impl.inline;

import com.haulmont.yarg.formatters.impl.doc.OfficeComponent;
import com.sun.star.text.XText;
import com.sun.star.text.XTextRange;
import org.apache.poi.hssf.usermodel.*;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.docx4j.wml.Text;
import org.xlsx4j.sml.Cell;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handle tags in format strings
 */
public interface ContentInliner {

    /**
     * Get Regexp Pattern for match format string
     *
     * @return Pattern
     */
    Pattern getTagPattern();

    /**
     * Inline content to xlsx template
     */
    void inlineToXlsx(SpreadsheetMLPackage pkg, WorksheetPart worksheetPart, Cell newCell, Object paramValue, Matcher matcher);

    /**
     * Inline content into doc template
     */
    void inlineToDoc(OfficeComponent officeComponent, XTextRange textRange, XText destination, Object paramValue, Matcher paramsMatcher)
            throws Exception;

    /**
     * Inline content into docx template
     */
    void inlineToDocx(WordprocessingMLPackage wordPackage, Text destination, Object paramValue, Matcher paramsMatcher);

    /**
     * Inline content into xls template
     */
    void inlineToXls(HSSFPatriarch patriarch, HSSFCell destination, Object paramValue, Matcher paramsMatcher);
}