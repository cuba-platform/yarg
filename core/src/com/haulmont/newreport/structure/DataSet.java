/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import java.util.Map;

public interface DataSet {
    String getName();

    String getScript();

    String getLoaderType();

    Map<String, Object> getAdditionalParams();
}
