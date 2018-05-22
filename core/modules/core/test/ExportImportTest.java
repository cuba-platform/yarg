/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

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
                ).parameter(new ReportParameterImpl("parameterName", "parameterAlias", false, String.class));
        report.template(
                new ReportTemplateBuilder()
                        .code(ReportTemplate.DEFAULT_TEMPLATE_CODE)
                        .documentName("smoketest/test.xls")
                        .documentPath("./modules/core/test/smoketest/test.xls").readFileFromPath()
                        .outputType(ReportOutputType.xls)
                        .outputNamePattern( "outputNamePattern")
                        .build());

        report.name("report");
        report.format(new ReportFieldFormatImpl("formatArgumentName", "format"));

        return report.build();
    }
}