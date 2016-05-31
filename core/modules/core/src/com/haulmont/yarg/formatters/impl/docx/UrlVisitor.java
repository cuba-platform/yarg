package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.TraversalUtil;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.relationships.Relationships;
import org.docx4j.wml.P;

import java.net.URLDecoder;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class UrlVisitor extends TraversalUtil.CallbackImpl {
    protected DocxFormatterDelegate docxFormatter;
    protected MainDocumentPart mainDocumentPart;

    public UrlVisitor(DocxFormatterDelegate docxFormatter, MainDocumentPart mainDocumentPart) {
        this.docxFormatter = docxFormatter;
        this.mainDocumentPart = mainDocumentPart;
    }

    @Override
    public List<Object> apply(Object o) {
        if (o instanceof P.Hyperlink) {
            P.Hyperlink hyperlink = (P.Hyperlink) o;
            try {
                Relationships contents = mainDocumentPart.getRelationshipsPart().getContents();
                List<Relationship> relationships = contents.getRelationship();
                for (Relationship relationship : relationships) {
                    if (relationship.getId().equals(hyperlink.getId())) {
                        relationship.setTarget(docxFormatter.handleStringWithAliases(URLDecoder.decode(relationship.getTarget(), "UTF-8")));
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("An error occurred while processing URL with aliases",e);
            }
        }
        return null;
    }
}
