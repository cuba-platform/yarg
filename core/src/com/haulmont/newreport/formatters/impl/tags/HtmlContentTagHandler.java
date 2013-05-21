/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 10.03.11 17:11
 *
 * $Id: HtmlContentTagHandler.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl.tags;

import com.haulmont.newreport.exception.ReportingException;
import com.haulmont.newreport.formatters.impl.doc.OfficeComponent;
import com.sun.star.beans.PropertyValue;
import com.sun.star.document.XDocumentInsertable;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextRange;
import org.apache.commons.lang.StringUtils;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.contenttype.ContentType;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.WordprocessingML.AlternativeFormatInputPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.CTAltChunk;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.newreport.formatters.impl.doc.UnoConverter.asXDocumentInsertable;

/**
 * Handle HTML with format string: ${html}
 */
public class HtmlContentTagHandler implements TagHandler {

    public final static String REGULAR_EXPRESSION = "\\$\\{html\\}";

    private static final String ENCODING_HEADER = "<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=utf-8\">";

    private static final String OPEN_HTML_TAGS = "<html> <head> </head> <body>";
    private static final String CLOSE_HTML_TAGS = "</body> </html>";

    private Pattern tagPattern;

    public HtmlContentTagHandler() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    public void handleTag(OfficeComponent officeComponent,
                          XText destination, XTextRange textRange,
                          Object paramValue, Matcher matcher) throws Exception {
        boolean inserted = false;
        if (paramValue != null) {
            String htmlContent = paramValue.toString();
            if (!StringUtils.isEmpty(htmlContent)) {
                try {
                    insertHTML(destination, textRange, htmlContent);
                    inserted = true;
                } catch (Exception ignored) {
                }
            }
        }
        if (!inserted)
            destination.getText().insertString(textRange, "", true);
    }

    public void handleTag(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher matcher) {
        try {
            AlternativeFormatInputPart afiPart = new AlternativeFormatInputPart(new PartName("/" + UUID.randomUUID().toString() + ".html"));
            afiPart.setBinaryData(paramValue.toString().getBytes());
            afiPart.setContentType(new ContentType("text/html"));
            Relationship altChunkRel = wordPackage.getMainDocumentPart().addTargetPart(afiPart);
            CTAltChunk ac = Context.getWmlObjectFactory().createCTAltChunk();
            ac.setId(altChunkRel.getId());
            R run = (R) text.getParent();
            run.getContent().add(ac);
            text.setValue("");
            wordPackage.getContentTypeManager().addDefaultContentType("html", "text/html");
        } catch (InvalidFormatException e) {
            throw new ReportingException("An error occured while inserting html to docx file", e);
        }
    }

    private void insertHTML(XText destination, XTextRange textRange, String htmlContent)
            throws Exception {
        File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".htm");

        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder.append(ENCODING_HEADER);
        contentBuilder.append(OPEN_HTML_TAGS);
        contentBuilder.append(htmlContent);
        contentBuilder.append(CLOSE_HTML_TAGS);

        FileOutputStream fileOutput = new FileOutputStream(tempFile);
        try {
            fileOutput.write(contentBuilder.toString().getBytes());
        } finally {
            fileOutput.close();
        }

        try {
            String filePath = tempFile.getCanonicalPath().replace("\\", "/");
            StringBuffer sUrl = new StringBuffer("file:///");
            sUrl.append(filePath);

            XTextCursor textCursor = destination.createTextCursorByRange(textRange);
            XDocumentInsertable docInsertable = asXDocumentInsertable(textCursor);

            docInsertable.insertDocumentFromURL(sUrl.toString(), new PropertyValue[0]);
        } finally {
            tempFile.delete();
        }
    }
}