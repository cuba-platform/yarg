package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.TraversalUtil;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public abstract class AbstractRegexpFinder<T> extends TraversalUtil.CallbackImpl {
    protected DocxFormatterDelegate docxFormatter;
    protected Class<T> classToHandle;
    protected Pattern regularExpression;

    public AbstractRegexpFinder(DocxFormatterDelegate docxFormatter, Pattern regularExpression, Class<T> classToHandle) {
        this.docxFormatter = docxFormatter;
        this.regularExpression = regularExpression;
        this.classToHandle = classToHandle;
    }

    @Override
    public List<Object> apply(Object o) {
        if (skipFind()) {
            return null;
        }
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

    protected abstract void onFind(T o, Matcher matcher);

    protected boolean skipFind() {
        return false;
    }
}
