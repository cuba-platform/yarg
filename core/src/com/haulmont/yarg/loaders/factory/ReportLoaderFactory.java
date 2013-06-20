/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders.factory;

import com.haulmont.yarg.loaders.ReportDataLoader;

public interface ReportLoaderFactory {
    ReportDataLoader createDataLoader(String loaderType);
}
