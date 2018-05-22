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
package sample.incomes;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;

import static org.apache.commons.io.FileUtils.readFileToString;

public class IncomesTest {
    @Test
    public void testIncomesReport() throws Exception {
        Report report = new DefaultXmlReader()
                .parseXml(readFileToString(new File("./modules/core/test/sample/incomes/incomes.xml")));

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(
                new DefaultLoaderFactory()
                        .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

        ReportOutputDocument reportOutputDocument = reporting.runReport(
                new RunParams(report), new FileOutputStream("./result/sample/incomes.xlsx"));
    }
}