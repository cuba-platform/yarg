/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.reporting;

import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class DataExtractorImpl implements DataExtractor {
    protected ReportLoaderFactory loaderFactory;

    protected boolean putEmptyRowIfNoDataSelected = true;

    public DataExtractorImpl(ReportLoaderFactory loaderFactory) {
        this.loaderFactory = loaderFactory;
    }

    public void extractData(Report report, Map<String, Object> params, BandData rootBand) {
        List<Map<String, Object>> rootBandData = getBandData(report.getRootBand(), null, params);
        if (CollectionUtils.isNotEmpty(rootBandData)) {
            rootBand.getData().putAll(rootBandData.get(0));
        }

        List<ReportBand> firstLevelBands = report.getRootBand().getChildren();
        if (firstLevelBands != null) {
            for (ReportBand definition : firstLevelBands) {
                List<BandData> bands = createBands(definition, rootBand, params);
                rootBand.addChildren(bands);
                rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
            }
        }
    }

    public void setPutEmptyRowIfNoDataSelected(boolean putEmptyRowIfNoDataSelected) {
        this.putEmptyRowIfNoDataSelected = putEmptyRowIfNoDataSelected;
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
            if (childrenBandDefinitions != null) {
                for (ReportBand childDefinition : childrenBandDefinitions) {
                    List<BandData> childBands = createBands(childDefinition, band, params);
                    band.addChildren(childBands);
                }
            }
            bandsList.add(band);
        }
        return bandsList;
    }

    protected List<Map<String, Object>> getBandData(ReportBand band, BandData parentBand, Map<String, Object> params) {
        Collection<ReportQuery> reportQueries = band.getReportQueries();
        //add input params to band
        if (CollectionUtils.isEmpty(reportQueries))
            return Collections.singletonList(params);

        Iterator<ReportQuery> queryIterator = reportQueries.iterator();
        ReportQuery firstReportQuery = queryIterator.next();

        //gets data from first dataset
        List<Map<String, Object>> result = getQueryData(parentBand, band, firstReportQuery, params);

        //adds data from second and following datasets to result
        while (queryIterator.hasNext()) {
            ReportQuery reportQuery = queryIterator.next();
            List<Map<String, Object>> currentQueryData = getQueryData(parentBand, band, reportQuery, params);
            String link = reportQuery.getLinkParameterName();
            if (StringUtils.isNotBlank(link)) {
                for (Map<String, Object> currentRow : currentQueryData) {
                    Object linkObj = currentRow.get(link);
                    if (linkObj != null) {
                        for (Map<String, Object> resultRow : result) {
                            Object linkObj2 = resultRow.get(link);
                            if (linkObj2 != null) {
                                if (linkObj.equals(linkObj2)) {
                                    resultRow.putAll(currentRow);
                                    break;
                                }
                            } else {
                                throw new DataLoadingException(String.format("An error occurred while loading data for band [%s]." +
                                        " Query defines link parameter [%s] but result does not contain such field. Query [%s].", band.getName(), link, firstReportQuery.getName()));
                            }
                        }
                    } else {
                        throw new DataLoadingException(String.format("An error occurred while loading data for band [%s]." +
                                " Query defines link parameter [%s] but result does not contain such field. Query [%s].", band.getName(), link, reportQuery.getName()));
                    }
                }
            } else {
                for (int j = 0; (j < result.size()) && (j < currentQueryData.size()); j++) {
                    result.get(j).putAll(currentQueryData.get(j));
                }
            }
        }

        if (CollectionUtils.isEmpty(result) && putEmptyRowIfNoDataSelected){
            result.add(new HashMap<String, Object>());
        }

        if (result != null) {
            //add output params to band
            for (Map<String, Object> map : result) {
                map.putAll(params);
            }
        }

        return result;
    }

    protected List<Map<String, Object>> getQueryData(BandData parentBand, ReportBand band, ReportQuery reportQuery, Map<String, Object> paramsMap) {
        try {
            ReportDataLoader dataLoader = loaderFactory.createDataLoader(reportQuery.getLoaderType());
            return dataLoader.loadData(reportQuery, parentBand, paramsMap);
        } catch (Exception e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for band [%s] and query [%s].", band.getName(), reportQuery.getName()), e);
        }
    }
}
