/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.formatters.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.inline.BitmapContentInliner;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.formatters.impl.inline.HtmlContentContentInliner;
import com.haulmont.yarg.formatters.impl.inline.ImageContentInliner;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.ReportTemplate;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractFormatter implements ReportFormatter {
    public static final String UNIVERSAL_ALIAS_REGEXP = "\\$\\{[A-z0-9_\\.#]+?\\}";
    public static final String ALIAS_WITH_BAND_NAME_REGEXP = "\\$\\{([A-z0-9_\\.]+?#?[A-z0-9_\\.]+?)\\}";
    public static final String BAND_NAME_DECLARATION_REGEXP = "##band=([A-z_0-9]+) *";

    public static final Pattern UNIVERSAL_ALIAS_PATTERN = Pattern.compile(UNIVERSAL_ALIAS_REGEXP, Pattern.CASE_INSENSITIVE);
    public static final Pattern ALIAS_WITH_BAND_NAME_PATTERN = Pattern.compile(ALIAS_WITH_BAND_NAME_REGEXP);
    public static final Pattern BAND_NAME_DECLARATION_PATTERN = Pattern.compile(BAND_NAME_DECLARATION_REGEXP);


    protected BandData rootBand;
    protected ReportTemplate reportTemplate;
    protected OutputStream outputStream;
    protected Set<ReportOutputType> supportedOutputTypes = new HashSet<>();
    protected DefaultFormatProvider defaultFormatProvider;

    /**
     * Chain of responsibility for content inliners
     */
    protected List<ContentInliner> contentInliners = new ArrayList<ContentInliner>();

    protected AbstractFormatter(FormatterFactoryInput formatterFactoryInput) {
        Preconditions.checkNotNull("\"rootBand\" parameter can not be null", formatterFactoryInput.getRootBand());
        Preconditions.checkNotNull("\"reportTemplate\" parameter can not be null", formatterFactoryInput.getReportTemplate());

        this.rootBand = formatterFactoryInput.getRootBand();
        this.reportTemplate = formatterFactoryInput.getReportTemplate();
        this.outputStream = formatterFactoryInput.getOutputStream();

        this.contentInliners.add(new BitmapContentInliner());
        this.contentInliners.add(new HtmlContentContentInliner());
        this.contentInliners.add(new ImageContentInliner());
    }

    @Override
    public byte[] createDocument() {
        Preconditions.checkArgument(supportedOutputTypes.contains(reportTemplate.getOutputType()), String.format("%s formatter doesn't support %s output type", getClass(), reportTemplate.getOutputType()));
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

    public void setDefaultFormatProvider(DefaultFormatProvider defaultFormatProvider) {
        this.defaultFormatProvider = defaultFormatProvider;
    }

    protected String unwrapParameterName(String nameWithAlias) {
        return nameWithAlias.replaceAll("[\\$|\\{|\\}]", "");
    }

    protected String formatValue(Object value, String fullParameterName) {
        String valueString = "";
        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldFormats();
        if (value != null) {
            if (formats != null && formats.containsKey(fullParameterName)) {
                String formatString = formats.get(fullParameterName).getFormat();
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

    protected String defaultFormat(Object value) {
        if (defaultFormatProvider != null) {
            return defaultFormatProvider.format(value);
        } else {
            return value != null ? value.toString() : null;
        }
    }

    protected String insertBandDataToString(BandData bandData, String resultStr) {
        List<String> parametersToInsert = new ArrayList<String>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(resultStr);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            Object value = bandData.getData().get(parameterName);
            String paramFullName = bandData.getName() + "." + parameterName;
            String valueStr = formatValue(value, paramFullName);
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

    protected ReportFormattingException wrapWithReportingException(String message, Exception e) {
        return new ReportFormattingException(message + ". Template name [" + reportTemplate.getDocumentName() + "]", e);
    }

    protected ReportFormattingException wrapWithReportingException(String message) {
        return new ReportFormattingException(message + ". Template name [" + reportTemplate.getDocumentName() + "]");
    }
}
