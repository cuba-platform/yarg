import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.factory.PropertiesSqlLoaderFactory;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import com.haulmont.yarg.util.properties.DefaultPropertiesLoader;
import junit.framework.Assert;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class ReportingTest {

    @Test
    public void testReporting() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();
        Report report = createReport();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(new DefaultLoaderFactory().setSqlDataLoader(new PropertiesSqlLoaderFactory(new DefaultPropertiesLoader()).create()));

        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report).templateCode("XLS"), new FileOutputStream("./result/smoke/result.xls"));

        Assert.assertEquals("myFileName.xls", reportOutputDocument.getDocumentName());

        testDatabase.stop();
    }

    private Report createReport() throws IOException {
        ReportBuilder report = new ReportBuilder()
                .band(new BandBuilder()
                        .name("Band1")
                        .query("", "select 'myFileName.txt' as file_name,login as col1, password as col2 from user", "sql")
                        .build()
                );
        report.template(
                new ReportTemplateBuilder()
                        .code("XLS")
                        .documentName("result.xls")
                        .documentPath("./test/smoketest/test.xls").readFileFromPath()
                        .outputType(ReportOutputType.xls)
                        .outputNamePattern("${Band1.FILE_NAME}")
                        .build())
                .name("report");

        return report.build();
    }
}
