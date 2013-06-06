/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 25.06.2010 14:07:49
 *
 * $Id: AbstractFormatter.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.exception.ReportingException;
import com.haulmont.newreport.formatters.Formatter;
import com.haulmont.newreport.structure.ReportValueFormat;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.ReportTemplate;
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

public abstract class AbstractFormatter implements Formatter {
    public static final String UNIVERSAL_ALIAS_REGEXP = "\\$\\{[A-z0-9_\\.#]+?\\}";
    public static final String ALIAS_WITH_BAND_NAME_REGEXP = "\\$\\{([A-z0-9_\\.]+?#?[A-z0-9_\\.]+?)\\}";

    public static final Pattern UNIVERSAL_ALIAS_PATTERN = Pattern.compile(UNIVERSAL_ALIAS_REGEXP, Pattern.CASE_INSENSITIVE);
    public static final Pattern ALIAS_WITH_BAND_NAME_PATTERN = Pattern.compile(ALIAS_WITH_BAND_NAME_REGEXP);
    public static final Pattern BAND_NAME_DECLARATION_PATTERN = Pattern.compile("##band=([A-z_0-9]+)");


    protected Band rootBand;
    protected ReportTemplate reportTemplate;
    protected OutputStream outputStream;

    protected AbstractFormatter(Band rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        Preconditions.checkNotNull("\"rootBand\" parameter can not be null", reportTemplate);
        Preconditions.checkNotNull("\"reportTemplate\" parameter can not be null", reportTemplate);

        this.rootBand = rootBand;
        this.reportTemplate = reportTemplate;
        this.outputStream = outputStream;
    }

    @Override
    public byte[] createDocument() {
        outputStream = new ByteArrayOutputStream();
        renderDocument();
        return ((ByteArrayOutputStream) outputStream).toByteArray();
    }

    public static String unwrapParameterName(String nameWithAlias) {
        return nameWithAlias.replaceAll("[\\$|\\{|\\}]", "");
    }

    protected String formatValue(Object value, String valueName) {
        String valueString = "";
        Map<String, ReportValueFormat> formats = rootBand.getReportValuesFormats();
        if (value != null) {
            if (formats != null) {
                if (formats.containsKey(valueName)) {
                    String formatString = formats.get(valueName).getFormat();
                    if (value instanceof Number) {
                        DecimalFormat decimalFormat = new DecimalFormat(formatString);
                        valueString = decimalFormat.format(value);
                    } else if (value instanceof Date) {
                        SimpleDateFormat dateformat = new SimpleDateFormat(formatString);
                        valueString = dateformat.format(value);
                    } else
                        valueString = value.toString();
                } else {
                    if (value instanceof Date) {
                        valueString = defaultFormat(value);
                    } else {
                        valueString = value.toString();
                    }
                }
            } else if (value instanceof Date) {
                valueString = defaultFormat(value);
            } else {
                valueString = value.toString();
            }
        }
        return valueString;
    }

    private String defaultFormat(Object value) {
        return value != null ? value.toString() : null;
    }

    protected String insertBandDataToString(Band band, String resultStr) {
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

    protected Band findBandByPath(Band rootBand, String path) {
        if (rootBand.getName().equals(path)) return rootBand;

        String[] pathParts = path.split("\\.");
        Band currentBand = rootBand;
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
