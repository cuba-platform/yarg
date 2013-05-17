import com.haulmont.newreport.formatters.factory.DefaultFormatterFactory;
import com.haulmont.newreport.loaders.factory.DefaultLoaderFactory;
import com.haulmont.newreport.reporting.Reporting;
import com.haulmont.newreport.reporting.RunParams;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportOutputType;
import com.haulmont.newreport.structure.impl.BandDefinitionBuilder;
import com.haulmont.newreport.structure.impl.ReportBuilder;
import com.haulmont.newreport.structure.impl.ReportTemplateImpl;
import com.haulmont.newreport.util.properties.DefaultPropertiesLoader;
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
        reporting.setLoaderFactory(new DefaultLoaderFactory(new DefaultPropertiesLoader()));

        reporting.runReport(new RunParams(report).templateName("XLS"), new FileOutputStream("./result/result.xls"));
    }

    private Report createReport() throws IOException {
        ReportBuilder report = new ReportBuilder()
                .band(new BandDefinitionBuilder()
                        .name("Band1")
                        .dataSet("", "select login as col1, password as col2 from user", "sql")
                        .build()
                );
        report.template(new ReportTemplateImpl("XLS", "result.xls", "./test/test.xls", ReportOutputType.xls));
        report.name("report");

        return report.build();
    }
}
