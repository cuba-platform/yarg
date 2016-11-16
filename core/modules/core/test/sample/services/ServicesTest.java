package sample.services;

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
public class ServicesTest {
    @Test
    public void testBreakdownReport() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        try {
            Connection connection = testDatabase.getDs().getConnection();
            try {
                connection.createStatement().executeUpdate("drop table service;");
            } catch (SQLException e) {
                //ignore
            }
            try {
                connection.createStatement().executeUpdate("drop table sold_item;");
            } catch (SQLException e) {
                //ignore
            }

            connection.createStatement().executeUpdate("create table service (name varchar(50));");
            connection.createStatement().executeUpdate("create table sold_item (service varchar(50), client varchar(50), volume integer, price decimal);");

            connection.createStatement().executeUpdate("insert into service values('IT Support');");
            connection.createStatement().executeUpdate("insert into service values('Technical support');");

            connection.createStatement().executeUpdate("insert into sold_item values('IT Support', 'Google', 10, 100);");
            connection.createStatement().executeUpdate("insert into sold_item values('IT Support', 'Yandex', 25, 100);");

            connection.createStatement().executeUpdate("insert into sold_item values('Technical support', 'Google', 20, 50);");
            connection.createStatement().executeUpdate("insert into sold_item values('Technical support', 'Yandex', 35, 75);");

            connection.commit();

            Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./modules/core/test/sample/services/services.xml")));
            System.out.println();

            Reporting reporting = new Reporting();
            reporting.setFormatterFactory(new DefaultFormatterFactory());
            reporting.setLoaderFactory(new DefaultLoaderFactory()
                    .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
                    .setSqlDataLoader(new SqlDataLoader(testDatabase.getDs())));

            ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report), new FileOutputStream("./result/sample/services.xlsx"));
        } finally {
            testDatabase.stop();
        }
    }
}