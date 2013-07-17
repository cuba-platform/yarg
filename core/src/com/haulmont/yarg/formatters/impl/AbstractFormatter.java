/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 25.06.2010 14:07:49
 *
 * $Id: AbstractFormatter.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.yarg.formatters.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.impl.inline.BitmapContentInliner;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.formatters.impl.inline.HtmlContentContentInliner;
import com.haulmont.yarg.formatters.impl.inline.ImageContentInliner;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportTemplate;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractFormatter implements ReportFormatter {
    public static final String UNIVERSAL_ALIAS_REGEXP = "\\$\\{[A-z0-9_\\.#]+?\\}";
    public static final String ALIAS_WITH_BAND_NAME_REGEXP = "\\$\\{([A-z0-9_\\.]+?#?[A-z0-9_\\.]+?)\\}";

    public static final Pattern UNIVERSAL_ALIAS_PATTERN = Pattern.compile(UNIVERSAL_ALIAS_REGEXP, Pattern.CASE_INSENSITIVE);
    public static final Pattern ALIAS_WITH_BAND_NAME_PATTERN = Pattern.compile(ALIAS_WITH_BAND_NAME_REGEXP);
    public static final Pattern BAND_NAME_DECLARATION_PATTERN = Pattern.compile("##band=([A-z_0-9]+)");


    protected BandData rootBand;
    protected ReportTemplate reportTemplate;
    protected OutputStream outputStream;

    /**
     * Chain of responsibility for content inliners
     */
    protected List<ContentInliner> contentInliners = new ArrayList<ContentInliner>();

    protected AbstractFormatter(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        Preconditions.checkNotNull("\"rootBand\" parameter can not be null", reportTemplate);
        Preconditions.checkNotNull("\"reportTemplate\" parameter can not be null", reportTemplate);

        this.rootBand = rootBand;
        this.reportTemplate = reportTemplate;
        this.outputStream = outputStream;

        this.contentInliners.add(new BitmapContentInliner());
        this.contentInliners.add(new HtmlContentContentInliner());
        this.contentInliners.add(new ImageContentInliner());
    }

    @Override
    public byte[] createDocument() {
        outputStream = new ByteArrayOutputStream();
        renderDocument();
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    public List<ContentInliner> getContentInliners() {
        return contentInliners;
    }

    public void setContentInliners(List<ContentInliner> contentInliners) {
        this.contentInliners = contentInliners;
    }

    protected String unwrapParameterName(String nameWithAlias) {
        return nameWithAlias.replaceAll("[\\$|\\{|\\}]", "");
    }

    protected String formatValue(Object value, String valueName) {
        String valueString = "";
        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldConverters();
        if (value != null) {
            if (formats != null && formats.containsKey(valueName)) {
                String formatString = formats.get(valueName).getFormat();
                if (value instanceof Number) {
                    DecimalFormat decimalFormat = new DecimalFormat(formatString);
                    valueString = decimalFormat.format(value);
                } else if (value instanceof Date) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat(formatString);
                    valueString = dateFormat.format(value);
                } else {
                    valueString = value.toString();
                }
            } else {
                valueString = defaultFormat(value);
            }
        }

        return valueString;
    }

    private String defaultFormat(Object value) {
        return value != null ? value.toString() : null;
    }

    protected String insertBandDataToString(BandData band, String resultStr) {
        List<String> parametersToInsert = new ArrayList<String>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(resultStr);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            Object value = band.getData().get(parameterName);
            String valueStr = formatValue(value, parameterName);
            resultStr = inlineParameterValue(resultStr, parameterName, valueStr);
        }
        return resultStr;
    }

    protected String inlineParameterValue(String template, String parameterName, String value) {
        return template.replaceAll("\\$\\{" + parameterName + "\\}", value);
    }

    protected BandData findBandByPath(BandData rootBand, String path) {
        if (rootBand.getName().equals(path)) return rootBand;

        String[] pathParts = path.split("\\.");
        BandData currentBand = rootBand;
        for (String pathPart : pathParts) {
            if (currentBand == null) return null;
            currentBand = currentBand.getChildByName(pathPart);
        }

        return currentBand;
    }

    protected static class BandPathAndParameterName {
        final String bandPath;
        final String parameterName;

        public BandPathAndParameterName(String bandPath, String parameterName) {
            this.bandPath = bandPath;
            this.parameterName = parameterName;
        }
    }

    protected BandPathAndParameterName separateBandNameAndParameterName(String alias) {
        String bandPathPart;
        String paramNamePart;
        if (alias.indexOf("#") > 0) {
            bandPathPart = StringUtils.substringBeforeLast(alias, "#");
            paramNamePart = StringUtils.substringAfterLast(alias, "#");
        } else {
            bandPathPart = StringUtils.substringBefore(alias, ".");
            paramNamePart = StringUtils.substringAfter(alias, ".");
        }

        return new BandPathAndParameterName(bandPathPart, paramNamePart);
    }

    protected ReportingException wrapWithReportingException(String message, Exception e) {
        return new ReportingException(message + " Template name [" + reportTemplate.getDocumentName() + "]", e);
    }

    protected ReportingException wrapWithReportingException(String message) {
        return new ReportingException(message + " Template name [" + reportTemplate.getDocumentName() + "]");
    }
}
