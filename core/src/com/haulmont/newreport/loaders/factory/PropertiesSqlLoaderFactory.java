/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.loaders.factory;

import com.haulmont.newreport.exception.InitializationException;
import com.haulmont.newreport.loaders.impl.SqlDataLoader;
import com.haulmont.newreport.util.properties.PropertiesLoader;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;

public class PropertiesSqlLoaderFactory {
    protected PropertiesLoader propertiesLoader;

    public PropertiesSqlLoaderFactory(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
    }

    public SqlDataLoader create() {
        try {
            Properties properties = propertiesLoader.load();
            String driver = properties.getProperty(PropertiesLoader.CUBA_REPORTING_SQL_DRIVER);
            String dbUrl = properties.getProperty(PropertiesLoader.CUBA_REPORTING_SQL_DB_URL);
            String user = properties.getProperty(PropertiesLoader.CUBA_REPORTING_SQL_USER);
            String password = properties.getProperty(PropertiesLoader.CUBA_REPORTING_SQL_PASSWORD);

            DataSource dataSource = DefaultLoaderFactory.setupDataSource(driver, dbUrl, user, password, 3, 2, 1);
            SqlDataLoader sqlDataLoader = new SqlDataLoader(dataSource);
            return sqlDataLoader;
        } catch (IOException e) {
            throw new InitializationException("An error occurred while loading properties", e);
        }
    }
}
