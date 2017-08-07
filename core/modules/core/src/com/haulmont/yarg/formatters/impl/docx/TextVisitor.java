package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import org.docx4j.wml.Text;

import java.util.HashSet;
import java.util.Set;

public class TextVisitor extends AliasVisitor {
    protected Set<TextWrapper> textWrappers = new HashSet<TextWrapper>();

    public TextVisitor(DocxFormatterDelegate docxFormatter) {
        super(docxFormatter);
    }

    @Override
    protected void handle(Text text) {
        textWrappers.add(new TextWrapper(docxFormatter, text));
    }
}
