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

package utils;

import com.haulmont.yarg.util.DatasourceCreator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabase {
    private DataSource ds;

    public void setUpDatabase() throws Exception {
        ds = DatasourceCreator.setupDataSource(
                "org.h2.Driver",
                "jdbc:h2:mem:reportingDb;MODE=PostgreSQL;AUTO_RECONNECT=TRUE", "sa", "", 10, 10, 0);

        try(Connection connection = ds.getConnection()) {
            try {
                connection.createStatement().executeUpdate("drop table user;");
            } catch (SQLException e) {
                //ignore
            }
            connection.createStatement().executeUpdate("create table user (login varchar(50), password varchar(50), create_ts timestamp);");
            connection.createStatement().executeUpdate("insert into user (login, password, create_ts) values ('login1', 'passwd', TIMESTAMP '2050-01-01 00:00:00');");
            connection.createStatement().executeUpdate("insert into user (login, password, create_ts) values ('login2', 'passwd', TIMESTAMP '2050-01-01 00:00:00');");
            connection.createStatement().executeUpdate("insert into user (login, password, create_ts) values ('login3', 'passwd', TIMESTAMP '2050-01-01 00:00:00');");
            connection.commit();
        }
    }

    public DataSource getDs() {
        return ds;
    }

    public void stop() {
        //maybe later for server version
    }
}