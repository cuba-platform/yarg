import com.haulmont.newreport.formatters.factory.DefaultFormatterFactory;
import com.haulmont.newreport.loaders.factory.DefaultLoaderFactory;
import com.haulmont.newreport.loaders.factory.PropertiesSqlLoaderFactory;
import com.haulmont.newreport.reporting.ReportOutputDocument;
import com.haulmont.newreport.reporting.Reporting;
import com.haulmont.newreport.reporting.RunParams;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportOutputType;
import com.haulmont.newreport.structure.impl.BandDefinitionBuilder;
import com.haulmont.newreport.structure.impl.ReportBuilder;
import com.haulmont.newreport.structure.impl.ReportTemplateImpl;
import com.haulmont.newreport.util.properties.DefaultPropertiesLoader;
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

        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report).templateCode("XLS"), new FileOutputStream("./result/result.xls"));

        Assert.assertEquals("myFileName.txt", reportOutputDocument.getDocumentName());

        testDatabase.stop();
    }

    private Report createReport() throws IOException {
        ReportBuilder report = new ReportBuilder()
                .band(new BandDefinitionBuilder()
                        .name("Band1")
                        .dataSet("", "select 'myFileName.txt' as file_name,login as col1, password as col2 from user", "sql")
                        .build()
                );
        report.template(new ReportTemplateImpl("XLS", "result.xls", "./test/test.xls", ReportOutputType.xls, "${Band1.FILE_NAME}"))
                .name("report");

        return report.build();
    }
}
