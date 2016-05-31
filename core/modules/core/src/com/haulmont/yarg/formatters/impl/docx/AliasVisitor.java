package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Text;
import org.jvnet.jaxb2_commons.ppp.Child;

import java.util.List;
import java.util.Set;

/**
* @author degtyarjov
* @version $Id$
*/
public abstract class AliasVisitor extends TraversalUtil.CallbackImpl {
    protected DocxFormatterDelegate docxFormatter;

    public AliasVisitor(DocxFormatterDelegate docxFormatter) {
        this.docxFormatter = docxFormatter;
    }

    @Override
    public List<Object> apply(Object o) {
        if (o instanceof P || o instanceof P.Hyperlink) {
            String paragraphText = docxFormatter.getElementText(o);

            if (AbstractFormatter.UNIVERSAL_ALIAS_PATTERN.matcher(paragraphText).find()) {
                Set<Text> mergedTexts = new TextMerger((ContentAccessor) o, AbstractFormatter.UNIVERSAL_ALIAS_REGEXP).mergeMatchedTexts();
                for (Text text : mergedTexts) {
                    handle(text);
                }
            }
        }

        return null;
    }

    protected abstract void handle(Text text);

    public void walkJAXBElements(Object parent) {
        List children = getChildren(parent);
        if (children != null) {

            for (Object object : children) {
                object = XmlUtils.unwrap(object);

                if (object instanceof Child) {
                    ((Child) object).setParent(parent);
                }

                this.apply(object);

                if (this.shouldTraverse(object)) {
                    walkJAXBElements(object);
                }
            }
        }
    }
}