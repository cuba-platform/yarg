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

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.PropertiesSqlLoaderFactory;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportParameterImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import com.haulmont.yarg.util.properties.DefaultPropertiesLoader;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import utils.TestDatabase;

import java.io.FileOutputStream;
import java.io.IOException;

public class ReportingTest {
    private Reporting reporting;
    private TestDatabase testDatabase;

    @Before
    public void setup()  throws Exception {
        testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(new DefaultLoaderFactory()
                .setSqlDataLoader(new PropertiesSqlLoaderFactory(new DefaultPropertiesLoader("./modules/core/test/reporting.properties")).create()));
    }

    @After
    public void tearDown() {
        testDatabase.stop();
    }

    @Test
    public void testReporting() throws Exception {
        Report report = createReport(false, null);
        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report).templateCode("XLS"), new FileOutputStream("./result/smoke/result.xls"));
        Assert.assertEquals("myFileName.xls", reportOutputDocument.getDocumentName());
    }

    @Test
    public void testReportWithDefaultParameters() throws Exception {
        ReportOutputDocument reportOutputDocument = null;
        try {
            Report report = createReport(true, null);
            reportOutputDocument = reporting.runReport(new RunParams(report).templateCode("XLS"), new FileOutputStream("./result/smoke/result.xls"));
            Assert.fail("Should fail without required parameter");
        } catch (IllegalArgumentException e) {
            //do nothing
        }

        try {
            Report report = createReport(true, "abc");
            reportOutputDocument = reporting.runReport(
                    new RunParams(report)
                            .templateCode("XLS"),
                    new FileOutputStream("./result/smoke/result.xls"));
            Assert.fail("Should fail with parse exception");
        } catch (Exception e) {
            //do nothing
        }

        try {
            Report report = createReport(true, "1");
            reportOutputDocument = reporting.runReport(
                    new RunParams(report)
                            .templateCode("XLS"),
                    new FileOutputStream("./result/smoke/result.xls"));
        } catch (Exception e) {
            Assert.fail("Should not fail");
        }

        Assert.assertEquals("myFileName.xls", reportOutputDocument.getDocumentName());
    }

    private Report createReport(boolean hasParameter, String defaultParameter) throws IOException {
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
                        .documentPath("./modules/core/test/smoketest/test.xls").readFileFromPath()
                        .outputType(ReportOutputType.xls)
                        .outputNamePattern("${Band1.FILE_NAME}")
                        .build())
                .name("report");
        if (hasParameter) {
            if (defaultParameter != null) {
                report.parameter(new ReportParameterImpl("myParam", "myParam", true, Integer.class, defaultParameter));
            } else {
                report.parameter(new ReportParameterImpl("myParam", "myParam", true, Integer.class));
            }
        }

        return report.build();
    }
}