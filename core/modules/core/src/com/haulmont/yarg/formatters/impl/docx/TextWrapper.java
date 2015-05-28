package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.AbstractFormatter;
import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import com.haulmont.yarg.structure.BandData;
import org.docx4j.wml.Text;

import java.util.regex.Matcher;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
* @author degtyarjov
* @version $Id$
*/
public class TextWrapper {
    private DocxFormatterDelegate docxFormatter;
    protected Text text;

    protected TextWrapper(DocxFormatterDelegate docxFormatter, Text text) {
        this.docxFormatter = docxFormatter;
        this.text = text;
    }

    public void fillTextWithBandData() {
        Matcher matcher = AbstractFormatter.ALIAS_WITH_BAND_NAME_PATTERN.matcher(text.getValue());
        while (matcher.find()) {
            String alias = matcher.group(1);
            String stringFunction = matcher.group(2);

            AbstractFormatter.BandPathAndParameterName bandAndParameter = docxFormatter.separateBandNameAndParameterName(alias);

            if (isBlank(bandAndParameter.getBandPath()) || isBlank(bandAndParameter.getParameterName())) {
                if (alias.matches("[A-z0-9_\\.]+?")) {//skip aliases in tables
                    continue;
                }

                throw docxFormatter.wrapWithReportingException("Bad alias : " + text.getValue());
            }

            BandData band = docxFormatter.findBandByPath(bandAndParameter.getBandPath());

            if (band == null) {
                throw docxFormatter.wrapWithReportingException(String.format("No band for alias [%s] found", alias));
            }

            String fullParameterName = band.getName() + "." + bandAndParameter.getParameterName();
            Object parameterValue = band.getParameterValue(bandAndParameter.getParameterName());

            if (docxFormatter.tryToApplyInliners(fullParameterName, parameterValue, text)) return;

            text.setValue(docxFormatter.inlineParameterValue(text.getValue(), alias,
                    docxFormatter.formatValue(parameterValue, bandAndParameter.getParameterName(), fullParameterName, stringFunction)));
            text.setSpace("preserve");
        }
    }
}
