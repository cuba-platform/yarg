/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.reporting;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.exception.ReportingException;
import com.haulmont.newreport.formatters.Formatter;
import com.haulmont.newreport.formatters.factory.FormatterFactoryInput;
import com.haulmont.newreport.formatters.factory.FormatterFactory;
import com.haulmont.newreport.loaders.DataLoader;
import com.haulmont.newreport.loaders.factory.LoaderFactory;
import com.haulmont.newreport.structure.*;
import com.haulmont.newreport.structure.impl.Band;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reporting implements ReportingAPI {
    protected FormatterFactory formatterFactory;

    protected LoaderFactory loaderFactory;

    public void setFormatterFactory(FormatterFactory formatterFactory) {
        this.formatterFactory = formatterFactory;
    }

    public void setLoaderFactory(LoaderFactory loaderFactory) {
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
        Preconditions.checkNotNull(report, "\"report\" parameter can not be null");
        Preconditions.checkNotNull(reportTemplate, "\"reportTemplate\" can not be null");
        Preconditions.checkNotNull(params, "\"params\" can not be null");
        Preconditions.checkNotNull(outputStream, "\"outputStream\" can not be null");

        String extension = StringUtils.substringAfterLast(reportTemplate.getDocumentName(), ".");
        Band rootBand = new Band(Band.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<String, Object>(params));
        FormatterFactoryInput factoryInput = new FormatterFactoryInput(extension, rootBand, reportTemplate, outputStream);
        Formatter formatter = formatterFactory.createFormatter(factoryInput);

        rootBand.setReportValueFormats(report.getReportValueFormats());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<String>());

        List<Map<String, Object>> rootBandData = getBandData(report.getRootBandDefinition(), null, params);
        if (CollectionUtils.isNotEmpty(rootBandData)) {
            rootBand.getData().putAll(rootBandData.get(0));
        }

        for (BandDefinition definition : report.getRootBandDefinition().getChildren()) {
            List<Band> bands = createBands(definition, rootBand, params);
            rootBand.addChildren(bands);
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
        }

        formatter.renderDocument();
        String outputName = resolveOutputFileName(report, reportTemplate, rootBand);
        return new ReportOutputDocument(report, null, outputName, reportTemplate.getOutputType());
    }

    protected String resolveOutputFileName(Report report, ReportTemplate reportTemplate, Band rootBand) {
        String outputNamePattern = reportTemplate.getOutputNamePattern();
        String outputName = reportTemplate.getDocumentName();
        Pattern pattern = Pattern.compile("\\$\\{([A-z0-9_]+)\\.([A-z0-9_]+)\\}");
        if (outputNamePattern != null) {
            Matcher matcher = pattern.matcher(outputNamePattern);
            if (matcher.find()) {
                String bandName = matcher.group(1);
                String paramName = matcher.group(2);

                Band bandWithFileName = null;
                if (Band.ROOT_BAND_NAME.equals(bandName)) {
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

    protected List<Band> createBands(BandDefinition definition, Band parentBand, Map<String, Object> params) {
        List<Map<String, Object>> outputData = getBandData(definition, parentBand, params);
        return createBandsList(definition, parentBand, outputData, params);
    }

    protected List<Band> createBandsList(BandDefinition definition, Band parentBand, List<Map<String, Object>> outputData, Map<String, Object> params) {
        List<Band> bandsList = new ArrayList<Band>();
        for (Map<String, Object> data : outputData) {
            Band band = new Band(definition.getName(), parentBand, definition.getBandOrientation());
            band.setData(data);
            Collection<BandDefinition> childrenBandDefinitions = definition.getChildren();
            for (BandDefinition childDefinition : childrenBandDefinitions) {
                List<Band> childBands = createBands(childDefinition, band, params);
                band.addChildren(childBands);
            }
            bandsList.add(band);
        }
        return bandsList;
    }

    protected List<Map<String, Object>> getBandData(BandDefinition definition, Band parentBand, Map<String, Object> params) {
        Collection<DataSet> dataSets = definition.getInnerDataSets();
        //add input params to band
        if (CollectionUtils.isEmpty(dataSets))
            return Collections.singletonList(params);

        Iterator<DataSet> dataSetIterator = dataSets.iterator();
        DataSet firstDataSet = dataSetIterator.next();

        //gets data from first dataset
        List<Map<String, Object>> result = getDataSetData(parentBand, firstDataSet, params);

        //adds data from second and following datasets to result
        while (dataSetIterator.hasNext()) {//todo reimplement
            List<Map<String, Object>> dataSetData = getDataSetData(parentBand, dataSetIterator.next(), params);
            for (int j = 0; (j < result.size()) && (j < dataSetData.size()); j++) {
                result.get(j).putAll(dataSetData.get(j));
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

    protected List<Map<String, Object>> getDataSetData(Band parentBand, DataSet dataSet, Map<String, Object> paramsMap) {
        DataLoader dataLoader = loaderFactory.createDataLoader(dataSet.getLoaderType());
        return dataLoader.loadData(dataSet, parentBand, paramsMap);
    }
}
