/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.postprocessor;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.io.FileUtils.readFileToString;

/**
 * @author bondarchuk
 * @version $Id$
 */

public class PostProcessorTest {

    @Test
    public void testReportPostprocessor() throws Exception {
        Report report = new DefaultXmlReader()
                .parseXml(readFileToString(new File("./modules/core/test/sample/postprocessor/postProcessor.xml")));

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(
                new DefaultLoaderFactory()
                        .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

        Map<String, Object> params = new HashMap<>();
        params.put("key 1","value 1");
        params.put("key 2","value 2");
        RunParams runParams = new RunParams(report);
        runParams.params(params);

        reporting.runReport(runParams, new FileOutputStream("./result/sample/postProcessor.xlsx"));
    }
}
