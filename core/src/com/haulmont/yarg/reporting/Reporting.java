/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.reporting;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.factory.ReportFormatterFactory;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reporting implements ReportingAPI {
    protected ReportFormatterFactory formatterFactory;

    protected ReportLoaderFactory loaderFactory;

    public void setFormatterFactory(ReportFormatterFactory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    public void setLoaderFactory(ReportLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
    }

    @Override
    public ReportOutputDocument runReport(RunParams runParams, OutputStream outputStream) {
        return runReport(runParams.report, runParams.reportTemplate, runParams.params, outputStream);
    }

    @Override
    public ReportOutputDocument runReport(RunParams runParams) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ReportOutputDocument reportOutputDocument = runReport(runParams.report, runParams.reportTemplate, runParams.params, result);
        reportOutputDocument.content = result.toByteArray();
        return reportOutputDocument;
    }

    protected ReportOutputDocument runReport(Report report, ReportTemplate reportTemplate, Map<String, Object> params, OutputStream outputStream) {
        try {
            Preconditions.checkNotNull(report, "\"report\" parameter can not be null");
            Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" can not be null");
            Preconditions.checkNotNull(params, "\"params\" can not be null");
            Preconditions.checkNotNull(outputStream, "\"outputStream\" can not be null");

            String extension = StringUtils.substringAfterLast(reportTemplate.getDocumentName(), ".");
            BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
            rootBand.setData(new HashMap<String, Object>(params));
            rootBand.setReportFieldFormats(report.getReportFieldFormats());
            rootBand.setFirstLevelBandDefinitionNames(new HashSet<String>());

            new DataExtractor(loaderFactory).extractData(report, params, rootBand);

            if (reportTemplate.isCustom()) {
                try {
                    byte[] bytes = reportTemplate.getCustomReport().createReport(report, rootBand, params);
                    IOUtils.write(bytes, outputStream);
                } catch (IOException e) {
                    throw new ReportingException(String.format("An error occurred while processing custom template [%s].", reportTemplate.getDocumentName()), e);
                }
            } else {
                FormatterFactoryInput factoryInput = new FormatterFactoryInput(extension, rootBand, reportTemplate, outputStream);
                ReportFormatter formatter = formatterFactory.createFormatter(factoryInput);
                formatter.renderDocument();
            }
            String outputName = resolveOutputFileName(report, reportTemplate, rootBand);
            return new ReportOutputDocument(report, null, outputName, reportTemplate.getOutputType());
        } catch (ReportingException e) {
            throw new ReportingException(String.format("%s Report name [%s]", e.getMessage(), report.getName()), e.getCause());
        }
    }

    protected String resolveOutputFileName(Report report, ReportTemplate reportTemplate, BandData rootBand) {
        String outputNamePattern = reportTemplate.getOutputNamePattern();
        String outputName = reportTemplate.getDocumentName();
        Pattern pattern = Pattern.compile("\\$\\{([A-z0-9_]+)\\.([A-z0-9_]+)\\}");
        if (StringUtils.isNotBlank(outputNamePattern)) {
            Matcher matcher = pattern.matcher(outputNamePattern);
            if (matcher.find()) {
                String bandName = matcher.group(1);
                String paramName = matcher.group(2);

                BandData bandWithFileName = null;
                if (BandData.ROOT_BAND_NAME.equals(bandName)) {
                    bandWithFileName = rootBand;
                } else {
                    bandWithFileName = rootBand.findBandRecursively(bandName);
                }

                if (bandWithFileName != null) {
                    Object fileName = bandWithFileName.getData().get(paramName);

                    if (fileName == null) {
                        throw new ReportingException(String.format("No data in band [%s] parameter [%s] found.This band and parameter is used for output file name generation.", bandWithFileName, paramName));
                    } else {
                        outputName = fileName.toString();
                    }
                } else {
                    throw new ReportingException(String.format("No data in band [%s] found.This band is used for output file name generation.", bandWithFileName));
                }
            } else {
                return outputNamePattern;
            }
        }
        return outputName;
    }
}
