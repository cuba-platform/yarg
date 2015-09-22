package com.haulmont.yarg.util.db;

import com.haulmont.yarg.exception.InitializationException;
import org.apache.commons.dbcp.*;

import javax.sql.DataSource;

/**
 * @author degtyarjov
 * @version $Id$
 */
public final class DatasourceCreator {
    private DatasourceCreator() {
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
