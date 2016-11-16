package utils;

import com.haulmont.yarg.util.db.DatasourceCreator;
import org.hsqldb.Server;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TestDatabase {
    private DataSource ds;
    private Server hsqlServer;

    public void setUpDatabase() throws Exception {
        hsqlServer = new Server();

        hsqlServer.setLogWriter(null);
        hsqlServer.setSilent(true);

        hsqlServer.setDatabaseName(0, "reportingDb");
        hsqlServer.setDatabasePath(0, "file:./db/testdb");

        hsqlServer.start();
        ds = DatasourceCreator.setupDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:hsql://localhost/reportingDb", "sa", "", 10, 10, 0);

        Connection connection = ds.getConnection();
        try {
            connection.createStatement().executeUpdate("drop table user;");
        } catch (SQLException e) {
            //ignore
        }
        connection.createStatement().executeUpdate("create table user (login varchar, password varchar, create_ts timestamp);");
        connection.createStatement().executeUpdate("insert into user (login, password, create_ts) values ('login1', 'passwd', TIMESTAMP '2050-01-01 00:00:00');");
        connection.createStatement().executeUpdate("insert into user (login, password, create_ts) values ('login2', 'passwd', TIMESTAMP '2050-01-01 00:00:00');");
        connection.createStatement().executeUpdate("insert into user (login, password, create_ts) values ('login3', 'passwd', TIMESTAMP '2050-01-01 00:00:00');");
        connection.commit();
    }

    public DataSource getDs() {
        return ds;
    }

    public void stop() {
        try {
            hsqlServer.shutdown();
        } catch (Exception e) {
            //ignore
        }
    }
}