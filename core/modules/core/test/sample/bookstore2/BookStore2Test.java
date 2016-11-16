package sample.bookstore2;

import com.haulmont.yarg.console.DatasourceHolder;
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
 */
public class BookStore2Test {
    @Test
    public void testBookStoreReport() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        try {
            Connection connection = testDatabase.getDs().getConnection();
            DatasourceHolder.dataSource = testDatabase.getDs();
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

            connection.createStatement().executeUpdate("create table store (id integer, name varchar(200), address varchar(200));");
            connection.createStatement().executeUpdate("create table book(id integer, name varchar(200), author varchar(200), price decimal, store_id integer);");

            connection.createStatement().executeUpdate("insert into store values(1, 'Main store', 'Some street');");
            connection.createStatement().executeUpdate("insert into store values(2, 'Secondary store', 'Another street');");

            connection.createStatement().executeUpdate("insert into book values(1, 'Concurrency in practice', 'Brian Goetz', 10.0, 1);");
            connection.createStatement().executeUpdate("insert into book values(2, 'Concurrency in practice', 'Brian Goetz', 10.0, 1);");
            connection.createStatement().executeUpdate("insert into book values(2, 'Concurrency in practice', 'Brian Goetz', 10.0, 1);");
            connection.createStatement().executeUpdate("insert into book values(3, 'Effective Java', 'Josh Bloch', 20.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(4, 'Effective Java', 'Josh Bloch', 20.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(4, 'Effective Java', 'Josh Bloch', 20.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(5, 'Effective Java', 'Josh Bloch', 20.0, 1);");
            connection.createStatement().executeUpdate("insert into book values(5, 'Effective Java', 'Josh Bloch', 20.0, 1);");
            connection.createStatement().executeUpdate("insert into book values(6, 'Concurrency in practice', 'Brian Goetz', 10.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(7, 'Refactoring', 'Martin Fowler', 15.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(8, 'Refactoring', 'Martin Fowler', 15.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(8, 'Refactoring', 'Martin Fowler', 15.0, 2);");
            connection.createStatement().executeUpdate("insert into book values(9, 'Refactoring', 'Martin Fowler', 15.0, 1);");

            connection.commit();

            Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./modules/core/test/sample/bookstore2/bookstore2.xml")));
            System.out.println();

            Reporting reporting = new Reporting();
            reporting.setFormatterFactory(new DefaultFormatterFactory());
            reporting.setLoaderFactory(new DefaultLoaderFactory()
                    .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
                    .setSqlDataLoader(new SqlDataLoader(testDatabase.getDs())));

            ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report), new FileOutputStream("./result/sample/bookstore2.xls"));
        } finally {
            testDatabase.stop();
        }
    }
}
