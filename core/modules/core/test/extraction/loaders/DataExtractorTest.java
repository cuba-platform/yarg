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
package extraction.loaders;

import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.reporting.DataExtractorImpl;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.structure.impl.ReportTemplateBuilder;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DataExtractorTest {
    private Map<String, Object> emptyMap;

    @Test
    public void testSelectingEmptyBands() throws Exception {
        DefaultLoaderFactory loaderFactory = initLoaderFactory();

        DataExtractorImpl dataExtractor = new DataExtractorImpl(loaderFactory) {
            {
                emptyMap = EMPTY_MAP;
            }
        };
        dataExtractor.setPutEmptyRowIfNoDataSelected(true);
        Report report = createReport();
        BandData rootBand = rootBand();

        dataExtractor.extractData(report, new HashMap<>(), rootBand);
        System.out.println(rootBand);
        Assert.assertEquals(1, rootBand.getChildrenList().size());

        BandData band1 = rootBand.getChildrenList().get(0);
        Assert.assertEquals(emptyMap, band1.getData());

        Assert.assertEquals(1, band1.getChildrenList().size());
        Assert.assertEquals(emptyMap, band1.getChildrenList().get(0).getData());


        dataExtractor.setPutEmptyRowIfNoDataSelected(false);
        rootBand = rootBand();
        dataExtractor.extractData(report, new HashMap<>(), rootBand);
        System.out.println(rootBand);

        Assert.assertEquals(0, rootBand.getChildrenList().size());
    }

    @Test
    public void returnImmutableMapShouldNotThrowException() throws Exception {
        DefaultLoaderFactory loaderFactory = initLoaderFactory();
        DataExtractorImpl dataExtractor = new DataExtractorImpl(loaderFactory);
        Report report = createReportReturnImmutableMapAsQuery();
        BandData rootBand = rootBand();
        Map<String, Object> dummyParams = new HashMap<>();
        dummyParams.put("param1", "val1");
        dummyParams.put("param2", "val2");
        dataExtractor.extractData(report, dummyParams, rootBand);
        Assert.assertEquals(1, rootBand.getChildrenList().size());
    }

    private DefaultLoaderFactory initLoaderFactory() {
        DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory();
        loaderFactory.setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()));
        return loaderFactory;
    }

    private BandData rootBand() {
        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<String, Object>());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<String>());
        return rootBand;
    }

    private Report createReport() throws IOException {
        ReportBuilder report = new ReportBuilder()
                .band(new BandBuilder()
                        .name("Band1")
                        .query("", "return null", "groovy")
                        .child(new BandBuilder().name("Band11").query("", "return null", "groovy").build())
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

        return report.build();
    }

    private Report createReportReturnImmutableMapAsQuery() throws IOException {
        String query = "return [['var1':'val1'].asImmutable()]";

        ReportBuilder report = new ReportBuilder()
                .band(new BandBuilder()
                        .name("Band1")
                        .query("", query, "groovy")
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

        return report.build();
    }
}
