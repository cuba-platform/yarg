import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportTemplate;
import com.haulmont.yarg.structure.impl.*;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlWriter;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
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
                .band(new BandBuilder()
                        .name("Band1")
                        .query("Data_set_1", "return [['col1':123, 'col2':321], ['col1':456, 'col2':654]]", "groovy")
                        .build()
                ).parameter(new ReportParameterImpl("parameterName", "parameterAlias", true, String.class));
        report.template(
                new ReportTemplateBuilder()
                        .code(ReportTemplate.DEFAULT_TEMPLATE_CODE)
                        .documentName("test.xls")
                        .documentPath("./test/test.xls").readFileFromPath()
                        .outputType(ReportOutputType.xls)
                        .outputNamePattern( "outputNamePattern")
                        .build());

        report.name("report");
        report.format(new ReportFieldFormatImpl("formatArgumentName", "format"));

        return report.build();
    }
}
