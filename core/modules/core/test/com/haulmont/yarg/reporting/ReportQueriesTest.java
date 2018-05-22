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

package com.haulmont.yarg.reporting;

import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.impl.BandBuilder;
import com.haulmont.yarg.structure.impl.ReportBuilder;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ReportQueriesTest {

    @Test
    public void testDataLinkage() {
        Report report = createReport();

        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>());
        rootBand.addReportFieldFormats(report.getReportFieldFormats());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        new DataExtractorImpl(new DefaultLoaderFactory().setGroovyDataLoader(
                new GroovyDataLoader(new DefaultScriptingImpl()))).extractData(report, new HashMap<>(), rootBand);

        List<BandData> bands = rootBand.getChildrenByName("Band1");
        for (BandData band : bands) {
            if (((Integer)1).equals(band.getParameterValue("link"))) {
                Assert.assertEquals(1, band.getParameterValue("col1"));
                Assert.assertEquals(10, band.getParameterValue("col2"));
                Assert.assertEquals(100, band.getParameterValue("col3"));
            } else if (((Integer)2).equals(band.getParameterValue("link"))) {
                Assert.assertEquals(2, band.getParameterValue("col1"));
                Assert.assertEquals(20, band.getParameterValue("col2"));
                Assert.assertEquals(200, band.getParameterValue("col3"));
            }
        }
    }

    private Report createReport() {
        ReportBuilder report = new ReportBuilder()
                .band(new BandBuilder()
                        .name("Band1")
                        .query("q1", " return [['col1':1, 'link': 1], ['col1':2, 'link': 2]]", "groovy", "link")
                        .query("q2", " return [['col2':10, 'link': 1], ['col2':20, 'link': 2]]", "groovy", "link")
                        .query("q3", " return [['col3':100, 'link': 1], ['col3':200, 'link': 2]]", "groovy", "link")
                        .build()
                ).name("report");

        return report.build();
    }
}