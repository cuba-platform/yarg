package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.TraversalUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
* @author degtyarjov
* @version $Id$
*/
public class RegexpFinder<T> extends TraversalUtil.CallbackImpl {
    private DocxFormatterDelegate docxFormatter;
    protected Class<T> classToHandle;
    protected Pattern regularExpression;
    protected String value;

    public RegexpFinder(DocxFormatterDelegate docxFormatter, Pattern regularExpression, Class<T> classToHandle) {
        this.docxFormatter = docxFormatter;
        this.regularExpression = regularExpression;
        this.classToHandle = classToHandle;
    }

    @Override
    public List<Object> apply(Object o) {
        if (classToHandle.isAssignableFrom(o.getClass())) {
            @SuppressWarnings("unchecked")
            T currentElement = (T) o;
            String currentElementText = docxFormatter.getElementText(currentElement);
            if (isNotBlank(currentElementText)) {
                Matcher matcher = regularExpression.matcher(currentElementText);
                if (matcher.find()) {
                    onFind(currentElement, matcher);
                }
            }
        }

        return null;
    }

    protected void onFind(T o, Matcher matcher) {
        value = matcher.group(0);
    }

    public String getValue() {
        return value;
    }
}
