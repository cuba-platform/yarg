/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.loaders.factory;

import com.haulmont.newreport.loaders.DataLoader;

public interface LoaderFactory {
    DataLoader createDataLoader(String loaderType);
}
