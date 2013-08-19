/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import java.util.Map;

/**
 * This interface describes certain query which load some data.
 * It might be not only SQL or JPQL query but also Groovy script or smth like this
 */
public interface ReportQuery {
    String getName();

    /**
     * Sql, groovy or other script which describes logic of data loading
     */
    String getScript();

    String getLinkParameterName();

    /**
     * @return loader code.
     * See com.haulmont.yarg.loaders.factory.ReportLoaderFactory implementations and com.haulmont.yarg.loaders.factory.DefaultLoaderFactory for default values.
     */
    String getLoaderType();

    Map<String, Object> getAdditionalParams();
}
