package sample.nestedband;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TestDatabase;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author degtyarjov
 */
public class NestedBandTest {
    private TestDatabase testDatabase;

    @Before
    public void init() throws Exception {
        testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        Connection connection = testDatabase.getDs().getConnection();
        try {
            connection.createStatement().executeUpdate("drop table items;");
        } catch (SQLException e) {
            //ignore
        }

        connection.createStatement().executeUpdate("create table items (id integer, number integer, barcode varchar);");

        connection.createStatement().executeUpdate("insert into items values(1, 1, '12345678');");

        connection.commit();
    }

    @After
    public void tearDown() {
        testDatabase.stop();
    }

    @Test
    public void testNestedBandRaw() throws Exception {
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportTemplateBuilder reportTemplateBuilder = new ReportTemplateBuilder()
                .documentPath("./modules/core/test/sample/nestedband/template.docx")
                .documentName("template.docx")
                .outputType(ReportOutputType.docx)
                .readFileFromPath();
        reportBuilder.template(reportTemplateBuilder.build());
        BandBuilder bandBuilder = new BandBuilder();
        ReportBand serial = bandBuilder.name("Item")
                .query("Item", "select id as \"id\", " +
                        "number as \"number\", " +
                        "barcode as \"barcode\" from items", "sql")
                .child(new BandBuilder().name("Barcode")
                        .query("Barcode",
                                "[['imageCode' : parentBand.getParameterValue('barcode'), " +
                                        "'image' : org.apache.commons.io.FileUtils.readFileToByteArray(new File('./modules/core/test/yarg.png'))]]",
                                "groovy")
                        .build())
                .build();

        reportBuilder.band(serial);
        reportBuilder.format(new ReportFieldFormatImpl("Barcode.image", "${bitmap:100x100}"));
        Report report = reportBuilder.build();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(
                new DefaultLoaderFactory()
                        .setSqlDataLoader(new SqlDataLoader(testDatabase.getDs()))
                        .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

        ReportOutputDocument reportOutputDocument = reporting.runReport(
                new RunParams(report), new FileOutputStream("./result/sample/nestedband.docx"));
    }
}