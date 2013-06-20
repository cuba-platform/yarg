/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import java.util.Map;

public interface ReportQuery {
    String getName();

    String getScript();

    String getLoaderType();

    Map<String, Object> getAdditionalParams();
}
