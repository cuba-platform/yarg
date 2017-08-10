package com.haulmont.yarg.formatters.impl.csv;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.opencsv.CSVWriter;

import java.util.regex.Matcher;

import static com.haulmont.yarg.formatters.impl.AbstractFormatter.UNIVERSAL_ALIAS_PATTERN;

/**
 * @author birin
 * @version $Id$
 */
public class SimpleSeparatorDetector {
    public static char detectSeparator(String line) {
        StringBuffer buffer = new StringBuffer();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(line);
        while (matcher.find()) {
            matcher.appendReplacement(buffer, "");
        }
        matcher.appendTail(buffer);

        String separateDetection = buffer.toString().replaceAll("[^,;|\\t]*", "");
        if (separateDetection == null)
            throw new ReportFormattingException("Error while detecting a separator");

        if (!separateDetection.isEmpty())
            return separateDetection.charAt(0);
        else
            return CSVWriter.DEFAULT_SEPARATOR;
    }
}
