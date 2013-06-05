/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.loaders.factory;

import com.haulmont.newreport.exception.InitializationException;
import com.haulmont.newreport.exception.UnsupportedLoaderException;
import com.haulmont.newreport.loaders.DataLoader;
import com.haulmont.newreport.loaders.impl.GroovyDataLoader;
import com.haulmont.newreport.loaders.impl.SqlDataLoader;
import com.haulmont.newreport.util.groovy.DefaultScriptingImpl;
import com.haulmont.newreport.util.properties.PropertiesLoader;
import org.apache.commons.dbcp.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultLoaderFactory implements LoaderFactory {
    protected Map<String, DataLoader> dataLoaders = new HashMap<String, DataLoader>();

    public DefaultLoaderFactory setDataLoaders(Map<String, DataLoader> dataLoaders) {
        this.dataLoaders.putAll(dataLoaders);
        return this;
    }

    public DefaultLoaderFactory setGroovyDataLoader(DataLoader dataLoader) {
        return registerDataLoader("groovy", dataLoader);
    }

    public DefaultLoaderFactory setSqlDataLoader(DataLoader dataLoader) {
        return registerDataLoader("sql", dataLoader);
    }

    public DefaultLoaderFactory registerDataLoader(String key, DataLoader dataLoader) {
        dataLoaders.put(key, dataLoader);
        return this;
    }

    @Override
    public DataLoader createDataLoader(String loaderType) {
        DataLoader dataLoader = dataLoaders.get(loaderType);
        if (dataLoader == null) {
            throw new UnsupportedLoaderException(String.format("Unsupported loader type [%s]", loaderType));
        } else {
            return dataLoader;
        }
    }

    public static DataSource setupDataSource(String driver, String connectURI,
                                             String username,
                                             String password,
                                             Integer maxActive,
                                             Integer maxIdle,
                                             Integer maxWait) {
        try {
            Class.forName(driver);
            final AbandonedConfig config = new AbandonedConfig();
            config.setLogAbandoned(true);

            AbandonedObjectPool connectionPool = new AbandonedObjectPool(null, config);

            connectionPool.setMaxIdle(maxIdle);
            connectionPool.setMaxActive(maxActive);
            if (maxWait != null) {
                connectionPool.setMaxWait(maxWait);
            }

            ConnectionFactory connectionFactory =
                    new DriverManagerConnectionFactory(connectURI, username, password);

            PoolableConnectionFactory poolableConnectionFactory =
                    new PoolableConnectionFactory(
                            connectionFactory, connectionPool, null, null, false, true);

            connectionPool.setFactory(poolableConnectionFactory);
            PoolingDataSource dataSource =
                    new PoolingDataSource(connectionPool);

            return dataSource;
        } catch (ClassNotFoundException e) {
            throw new InitializationException("An error occurred during creation of new datasource object", e);
        }
    }
}
