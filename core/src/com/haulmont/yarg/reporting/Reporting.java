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
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.structure.*;
import com.haulmont.yarg.structure.BandData;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
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

            List<Map<String, Object>> rootBandData = getBandData(report.getRootBand(), null, params);
            if (CollectionUtils.isNotEmpty(rootBandData)) {
                rootBand.getData().putAll(rootBandData.get(0));
            }

            for (ReportBand definition : report.getRootBand().getChildren()) {
                List<BandData> bands = createBands(definition, rootBand, params);
                rootBand.addChildren(bands);
                rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
            }

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
        if (outputNamePattern != null) {
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

    protected List<BandData> createBands(ReportBand definition, BandData parentBand, Map<String, Object> params) {
        List<Map<String, Object>> outputData = getBandData(definition, parentBand, params);
        return createBandsList(definition, parentBand, outputData, params);
    }

    protected List<BandData> createBandsList(ReportBand definition, BandData parentBand, List<Map<String, Object>> outputData, Map<String, Object> params) {
        List<BandData> bandsList = new ArrayList<BandData>();
        for (Map<String, Object> data : outputData) {
            BandData band = new BandData(definition.getName(), parentBand, definition.getBandOrientation());
            band.setData(data);
            Collection<ReportBand> childrenBandDefinitions = definition.getChildren();
            for (ReportBand childDefinition : childrenBandDefinitions) {
                List<BandData> childBands = createBands(childDefinition, band, params);
                band.addChildren(childBands);
            }
            bandsList.add(band);
        }
        return bandsList;
    }

    protected List<Map<String, Object>> getBandData(ReportBand definition, BandData parentBand, Map<String, Object> params) {
        Collection<ReportQuery> reportQueries = definition.getReportQueries();
        //add input params to band
        if (CollectionUtils.isEmpty(reportQueries))
            return Collections.singletonList(params);

        Iterator<ReportQuery> queryIterator = reportQueries.iterator();
        ReportQuery firstReportQuery = queryIterator.next();

        //gets data from first dataset
        List<Map<String, Object>> result = getQueryData(parentBand, firstReportQuery, params);

        //adds data from second and following datasets to result
        while (queryIterator.hasNext()) {//todo reimplement
            List<Map<String, Object>> queryData = getQueryData(parentBand, queryIterator.next(), params);
            for (int j = 0; (j < result.size()) && (j < queryData.size()); j++) {
                result.get(j).putAll(queryData.get(j));
            }
        }

        if (result != null) {
            //add output params to band
            for (Map<String, Object> map : result) {
                map.putAll(params);
            }
        }

        return result;
    }

    protected List<Map<String, Object>> getQueryData(BandData parentBand, ReportQuery reportQuery, Map<String, Object> paramsMap) {
        ReportDataLoader dataLoader = loaderFactory.createDataLoader(reportQuery.getLoaderType());
        return dataLoader.loadData(reportQuery, parentBand, paramsMap);
    }
}
