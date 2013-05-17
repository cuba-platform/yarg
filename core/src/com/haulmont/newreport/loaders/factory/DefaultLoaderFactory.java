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
import com.haulmont.newreport.loaders.impl.SqlDataDataLoader;
import com.haulmont.newreport.util.groovy.DefaultScriptingImpl;
import com.haulmont.newreport.util.properties.PropertiesLoader;
import org.apache.commons.dbcp.*;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class DefaultLoaderFactory implements LoaderFactory {
    protected GroovyDataLoader groovyDataLoader;
    protected SqlDataDataLoader sqlDataDataLoader;
    protected PropertiesLoader propertiesLoader;

    public DefaultLoaderFactory(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
        try {
            groovyDataLoader = new GroovyDataLoader(new DefaultScriptingImpl());

            Properties properties = propertiesLoader.load();
            String driver = properties.getProperty("cuba.reporting.sql.driver");
            String dbUrl = properties.getProperty("cuba.reporting.sql.dbUrl");
            String user = properties.getProperty("cuba.reporting.sql.user");
            String password = properties.getProperty("cuba.reporting.sql.password");

            DataSource dataSource = setupDataSource(driver, dbUrl, user, password, 3, 2, 1);
            sqlDataDataLoader = new SqlDataDataLoader(dataSource);
        } catch (IOException e) {
            throw new InitializationException("An error occurred while loading properties", e);
        }
    }

    @Override
    public DataLoader createDataLoader(String loaderType) {
        if ("groovy".equalsIgnoreCase(loaderType)) {
            return groovyDataLoader;
        } else if ("sql".equalsIgnoreCase(loaderType)) {
            return sqlDataDataLoader;

        }
        throw new UnsupportedLoaderException(String.format("Unsupported loader type [%s]", loaderType));
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
            throw new InitializationException("An error occurred during creation of new datasource object",e);
        }
    }
}
