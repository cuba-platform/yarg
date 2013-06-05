/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.reporting;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.formatters.Formatter;
import com.haulmont.newreport.formatters.factory.FormatterFactoryInput;
import com.haulmont.newreport.formatters.factory.FormatterFactory;
import com.haulmont.newreport.loaders.DataLoader;
import com.haulmont.newreport.loaders.factory.LoaderFactory;
import com.haulmont.newreport.structure.BandDefinition;
import com.haulmont.newreport.structure.DataSet;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportTemplate;
import com.haulmont.newreport.structure.impl.Band;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

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
    public void runReport(RunParams runParams, OutputStream outputStream) {
        runReport(runParams.report, runParams.reportTemplate, runParams.params, outputStream);
    }

    @Override
    public byte[] runReport(RunParams runParams) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        runReport(runParams.report, runParams.reportTemplate, runParams.params, result);
        return result.toByteArray();
    }

    private void runReport(Report report, ReportTemplate reportTemplate, Map<String, Object> params, OutputStream outputStream) {
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
        for (BandDefinition definition : report.getRootBandDefinition().getChildren()) {
            List<Band> bands = createBands(definition, rootBand, params);
            rootBand.addChildren(bands);
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
        }

        formatter.renderDocument();
    }

    private List<Band> createBands(BandDefinition definition, Band parentBand, Map<String, Object> params) {
        List<Map<String, Object>> outputData = getBandData(definition, parentBand, params);
        return createBandsList(definition, parentBand, outputData, params);
    }

    private List<Band> createBandsList(BandDefinition definition, Band parentBand, List<Map<String, Object>> outputData, Map<String, Object> params) {
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

    private List<Map<String, Object>> getBandData(BandDefinition definition, Band parentBand, Map<String, Object> params) {
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

    private List<Map<String, Object>> getDataSetData(Band parentBand, DataSet dataSet, Map<String, Object> paramsMap) {
        DataLoader dataLoader = loaderFactory.createDataLoader(dataSet.getLoaderType());
        return dataLoader.loadData(dataSet, parentBand, paramsMap);
    }
}
