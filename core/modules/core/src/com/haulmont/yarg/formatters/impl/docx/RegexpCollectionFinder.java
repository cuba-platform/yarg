package com.haulmont.yarg.formatters.impl.docx;


import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexpCollectionFinder<T> extends AbstractRegexpFinder<T> {
    protected List<String> values = new ArrayList<String>();

    public RegexpCollectionFinder(DocxFormatterDelegate docxFormatter, Pattern regularExpression, Class<T> classToHandle) {
        super(docxFormatter, regularExpression, classToHandle);
    }

    protected void onFind(T o, Matcher matcher) {
        values.add(matcher.group(0));
    }

    public List<String> getValues() {
        return values;
    }
}
