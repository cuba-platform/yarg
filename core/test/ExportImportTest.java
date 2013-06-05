import com.haulmont.newreport.loaders.impl.GroovyDataLoader;
import com.haulmont.newreport.structure.ReportOutputType;
import com.haulmont.newreport.formatters.factory.DefaultFormatterFactory;
import com.haulmont.newreport.loaders.factory.DefaultLoaderFactory;
import com.haulmont.newreport.reporting.Reporting;
import com.haulmont.newreport.reporting.RunParams;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportTemplate;
import com.haulmont.newreport.structure.impl.*;
import com.haulmont.newreport.structure.xml.impl.DefaultXmlReader;
import com.haulmont.newreport.structure.xml.impl.DefaultXmlWriter;
import com.haulmont.newreport.util.groovy.DefaultScriptingImpl;
import com.haulmont.newreport.util.properties.DefaultPropertiesLoader;
import org.junit.Test;

import java.io.IOException;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class ExportImportTest {


    @Test
    public void testExport() throws Exception {
        Report report1 = createReport();
        String xml = new DefaultXmlWriter().buildXml(report1);
        System.out.println(xml);
        Report report2 = new DefaultXmlReader().parseXml(xml);
        System.out.println();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(new DefaultLoaderFactory().setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));
        reporting.runReport(new RunParams(report1));
        reporting.runReport(new RunParams(report2));
    }

    private Report createReport() throws IOException {
        ReportBuilder report = new ReportBuilder()
                .band(new BandDefinitionBuilder()
                        .name("Band1")
                        .dataSet("Data_set_1", "return [['col1':123, 'col2':321], ['col1':456, 'col2':654]]", "groovy")
                        .build()
                ).parameter(new ReportParameterImpl("1", "1", true, String.class));
        report.template(new ReportTemplateImpl(ReportTemplate.DEFAULT_TEMPLATE_CODE, "test.xls", "./test/test.xls", ReportOutputType.xls));
        report.name("report");

        return report.build();
    }
}
