/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.console;

import com.haulmont.yarg.exception.InitializationException;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.util.properties.PropertiesLoader;
import org.apache.commons.dbcp.*;
import org.apache.commons.lang3.StringUtils;

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

            if (StringUtils.isBlank(driver) || StringUtils.isBlank(dbUrl)) {
                return null;
            }

            DataSource dataSource = setupDataSource(driver, dbUrl, user, password, 3, 2, 1);
            return new SqlDataLoader(dataSource);
        } catch (IOException e) {
            throw new InitializationException("An error occurred while loading properties", e);
        }
    }

    protected DataSource setupDataSource(String driver, String connectURI,
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
            return new PoolingDataSource(connectionPool);
        } catch (ClassNotFoundException e) {
            throw new InitializationException("An error occurred during creation of new datasource object", e);
        }
    }
}