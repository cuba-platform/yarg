package sample.bookstore;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import utils.TestDatabase;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class BookStoreTest {
    @Test
    public void testBookStoreReport() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        Connection connection = testDatabase.getDs().getConnection();
        try {
            connection.createStatement().executeUpdate("drop table store;");
        } catch (SQLException e) {
            //ignore
        }
        try {
            connection.createStatement().executeUpdate("drop table book;");
        } catch (SQLException e) {
            //ignore
        }

        connection.createStatement().executeUpdate("create table store (id integer, name varchar, address varchar);");
        connection.createStatement().executeUpdate("create table book(id integer, name varchar, author varchar, price decimal, store_id integer);");

        connection.createStatement().executeUpdate("insert into store values(1, 'First shop', '1st Street');");
        connection.createStatement().executeUpdate("insert into store values(2, 'Second shop', '2nd Street');");

        connection.createStatement().executeUpdate("insert into book values(1, '1st book', '1st author', 10.0, 1);");
        connection.createStatement().executeUpdate("insert into book values(2, '1st book', '1st author', 10.0, 1);");
        connection.createStatement().executeUpdate("insert into book values(3, '2nd book', '2nd author', 20.0, 2);");
        connection.createStatement().executeUpdate("insert into book values(4, '2nd book', '2nd author', 20.0, 2);");
        connection.createStatement().executeUpdate("insert into book values(5, '2nd book', '2nd author', 20.0, 1);");
        connection.createStatement().executeUpdate("insert into book values(6, '1st book', '1st author', 10.0, 2);");

        connection.commit();

        Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./test/sample/bookstore/bookstore.xml")));
        System.out.println();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(new DefaultLoaderFactory()
                .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
                .setSqlDataLoader(new SqlDataLoader(testDatabase.getDs())));

        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report), new FileOutputStream("./result/sample/bookstore.xls"));

        testDatabase.stop();
    }
}
