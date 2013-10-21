package sample.financedetails;

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

public class BreakdownTest {
    @Test
    public void testBreakdownReport() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        Connection connection = testDatabase.getDs().getConnection();
        try {
            connection.createStatement().executeUpdate("drop table month_batch;");
        } catch (SQLException e) {
            //ignore
        }
        try {
            connection.createStatement().executeUpdate("drop table sold_item;");
        } catch (SQLException e) {
            //ignore
        }

        connection.createStatement().executeUpdate("create table month_batch (month varchar);");
        connection.createStatement().executeUpdate("create table sold_item (month varchar, name varchar, price decimal);");

        connection.createStatement().executeUpdate("insert into month_batch  values('Jan');");
        connection.createStatement().executeUpdate("insert into month_batch  values('Feb');");
        connection.createStatement().executeUpdate("insert into month_batch  values('March');");

        connection.createStatement().executeUpdate("insert into sold_item values('Jan', 'Apple', 100);");
        connection.createStatement().executeUpdate("insert into sold_item values('Jan', 'Cucumber', 200);");
        connection.createStatement().executeUpdate("insert into sold_item values('Jan', 'Tomato', 300);");

        connection.createStatement().executeUpdate("insert into sold_item values('Feb', 'Apple', 200);");
        connection.createStatement().executeUpdate("insert into sold_item values('Feb', 'Cucumber', 200);");

        connection.createStatement().executeUpdate("insert into sold_item values('March', 'Tomato', 500);");

        connection.commit();

        Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./test/sample/financedetails/breakdown.xml")));
        System.out.println();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(new DefaultLoaderFactory()
                .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
                .setSqlDataLoader(new SqlDataLoader(testDatabase.getDs())));

        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report), new FileOutputStream("./result/sample/breakdown.xls"));

        testDatabase.stop();
    }
}
