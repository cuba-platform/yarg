/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.loaders.factory;

import com.haulmont.yarg.loaders.ReportDataLoader;
/**
 * This interface describes a factory which spawns data loaders. The default implementation is com.haulmont.yarg.loaders.factory.DefaultLoaderFactory
 */
public interface ReportLoaderFactory {
    ReportDataLoader createDataLoader(String loaderType);
}
