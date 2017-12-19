package com.haulmont.yarg.reporting.extraction.controller;

import com.google.common.collect.Multimap;
import com.haulmont.yarg.exception.DataLoadingException;
import com.haulmont.yarg.exception.ValidationException;
import com.haulmont.yarg.loaders.ReportDataLoader;
import com.haulmont.yarg.loaders.factory.ReportLoaderFactory;
import com.haulmont.yarg.reporting.extraction.*;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default data extraction logic implementation
 */
public class DefaultExtractionController implements ExtractionController {

    protected ReportLoaderFactory loaderFactory;
    protected ExtractionControllerFactory controllerRegistry;
    protected PreprocessorFactory preprocessorFactory;

    public DefaultExtractionController(ExtractionControllerFactory controllerRegistry, ReportLoaderFactory loaderFactory) {
        checkNotNull(controllerRegistry);
        checkNotNull(loaderFactory);

        this.loaderFactory = loaderFactory;
        this.controllerRegistry = controllerRegistry;
        this.preprocessorFactory = createPreprocessorFactory();
    }

    protected PreprocessorFactory createPreprocessorFactory() {
        return new DefaultPreprocessorFactory();
    }

    @Override
    public List<BandData> extract(ExtractionContext context) {
        List<Map<String, Object>> outputData = extractData(context);
        return traverseData(context, outputData);
    }

    @Override
    public List<Map<String, Object>> extractData(ExtractionContext context) {
        checkNotNull(context);

        if (CollectionUtils.isEmpty(context.getBand().getReportQueries())) {
            return Collections.singletonList(context.getParams());
        }

        List<Map<String, Object>> result = null;
        if (!isEmptyBand(context.getParentBandData())) {
            result = getQueriesResult(context);

            if (result != null) {
                //add input params to band
                //todo eude - probably we need to get rid of the following logic, because leads to errors while logging report
                for (Map<String, Object> map : result) {
                    map = new HashMap<>(map);
                    for (Map.Entry<String, Object> paramEntry : context.getParams().entrySet()) {
                        if ( !(paramEntry.getValue() instanceof Collection)
                                && !(paramEntry.getValue() instanceof  Map)
                                && !(paramEntry.getValue() instanceof Multimap)) {
                            map.put(paramEntry.getKey(), paramEntry.getValue());
                        }
                    }
                }
            }
        }

        if (result == null) {
            result = Collections.emptyList();
        }

        if (context.putEmptyRowIfNoDataSelected() && CollectionUtils.isEmpty(result)) {
            result = new ArrayList<>();
            result.add(Collections.emptyMap());
        }

        return result;
    }

    public void setPreprocessorFactory(PreprocessorFactory preprocessorFactory) {
        checkNotNull(preprocessorFactory);

        this.preprocessorFactory = preprocessorFactory;
    }

    public PreprocessorFactory getPreprocessorFactory() {
        return preprocessorFactory;
    }

    protected List<BandData> traverseData(ExtractionContext context, List<Map<String, Object>> outputData) {
        return outputData.stream()
                .map(data-> wrapData(context, data))
                .collect(Collectors.toList());
    }

    protected BandData wrapData(ExtractionContext context, Map<String, Object> data) {
        BandData band = new BandData(context.getBand().getName(),
                context.getParentBandData(), context.getBand().getBandOrientation());
        band.setData(data);
        Collection<ReportBand> childrenBandDefinitions = context.getBand().getChildren();
        if (childrenBandDefinitions != null) {
            for (ReportBand childDefinition : childrenBandDefinitions) {
                List<BandData> childBands = controllerRegistry
                        .controllerBy(childDefinition.getBandOrientation())
                        .extract(context
                                .withBand(childDefinition, band));
                band.addChildren(childBands);
            }
        }
        return band;
    }

    protected Stream<ReportQuery> getQueries(ExtractionContext context) {
        return context.getBand().getReportQueries().stream();
    }

    protected List<Map<String, Object>> getQueriesResult(ExtractionContext context) {
        return getQueriesResult(getQueries(context).iterator(), context);
    }

    protected List<Map<String, Object>> getQueriesResult(Iterator<ReportQuery> queryIterator, ExtractionContext context) {
        ReportQuery firstReportQuery = queryIterator.next();

        //gets data from first dataset
        List<Map<String, Object>> result = getQueryData(context, firstReportQuery);

        //adds data from second and following datasets to result
        while (queryIterator.hasNext()) {
            ReportQuery reportQuery = queryIterator.next();
            List<Map<String, Object>> currentQueryData = getQueryData(context, reportQuery);
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
                                        " Query defines link parameter [%s] but result does not contain such field. Query [%s].",
                                        context.getBand().getName(), link, firstReportQuery.getName()));
                            }
                        }
                    } else {
                        throw new DataLoadingException(String.format("An error occurred while loading data for band [%s]." +
                                " Query defines link parameter [%s] but result does not contain such field. Query [%s].",
                                context.getBand().getName(), link, reportQuery.getName()));
                    }
                }
            } else {
                for (int j = 0; (j < result.size()) && (j < currentQueryData.size()); j++) {
                    result.get(j).putAll(currentQueryData.get(j));
                }
            }
        }

        return result;
    }

    protected List<Map<String, Object>>  getQueryData(ExtractionContext context, ReportQuery reportQuery) {
        try {
            ReportDataLoader dataLoader = loaderFactory.createDataLoader(reportQuery.getLoaderType());
            return preprocessorFactory.processorBy(reportQuery.getLoaderType())
                    .preprocess(reportQuery, new HashMap<>(context.getParams()), (processedQuery, processedParams)-> {
                        //fixme: ugly params overloading support, needs to push context object for dependent logic
                        List<Map<String, Object>> result = dataLoader.loadData(processedQuery,
                                context.getParentBandData(), processedParams);
                        context.extendParams(processedParams);
                        return result;
                    });
        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DataLoadingException(String.format("An error occurred while loading data for band [%s] and query [%s].",
                    context.getBand().getName(), reportQuery.getName()), e);
        }
    }

    protected boolean isEmptyBand(BandData parentBand) {
        return parentBand != null && parentBand.getData() == Collections.EMPTY_MAP;
    }
}
