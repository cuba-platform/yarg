package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpFinder<T> extends AbstractRegexpFinder<T> {
    protected String value;

    public RegexpFinder(DocxFormatterDelegate docxFormatter, Pattern regularExpression, Class<T> classToHandle) {
        super(docxFormatter, regularExpression, classToHandle);
    }

    protected void onFind(T o, Matcher matcher) {
        if (value == null) {
            value = matcher.group(0);
        }
    }

    @Override
    protected boolean skipFind() {
        return value != null;
    }

    public String getValue() {
        return value;
    }
}
