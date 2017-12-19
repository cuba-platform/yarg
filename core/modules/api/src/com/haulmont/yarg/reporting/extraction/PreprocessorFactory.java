package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.loaders.QueryLoaderPreprocessor;

/**
 * <p>This interface implementation should holding relation between name of data loader type (<b>ex: sql</b>)
 * and custom params preprocessor
 * if relation not set, implementation should present default params preprocessor</p>
 * <p><b>ex:</b> {@code (query, params, consumer)-> consumer.apply(query, params) }</p>
 *
 * <p>The default implementation is <b>com.haulmont.yarg.reporting.extraction.DefaultPreprocessorFactory</b></p>
 */
public interface PreprocessorFactory {
    /**
     * Method for registering query preprocessing by loader type
     *
     * @param loaderType loader type ex: sql
     * @param preprocessor preprocessor implementation
     */
    void register(String loaderType, QueryLoaderPreprocessor preprocessor);

    /**
     * @param loaderType loader type ex: sql
     * @return preprocessor instance
     */
    QueryLoaderPreprocessor processorBy(String loaderType);
}
