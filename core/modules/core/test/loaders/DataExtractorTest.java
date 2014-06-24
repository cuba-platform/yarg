/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package loaders;

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
        DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory();
        loaderFactory.setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()));

        DataExtractorImpl dataExtractor = new DataExtractorImpl(loaderFactory) {
            {
                emptyMap = EMPTY_MAP;
            }
        };
        dataExtractor.setPutEmptyRowIfNoDataSelected(true);
        Report report = createReport();
        BandData rootBand = rootBand();

        dataExtractor.extractData(report, new HashMap<String, Object>(), rootBand);
        System.out.println(rootBand);
        Assert.assertEquals(1, rootBand.getChildrenList().size());

        BandData band1 = rootBand.getChildrenList().get(0);
        Assert.assertEquals(emptyMap, band1.getData());

        Assert.assertEquals(1, band1.getChildrenList().size());
        Assert.assertEquals(emptyMap, band1.getChildrenList().get(0).getData());


        dataExtractor.setPutEmptyRowIfNoDataSelected(false);
        rootBand = rootBand();
        dataExtractor.extractData(report, new HashMap<String, Object>(), rootBand);
        System.out.println(rootBand);

        Assert.assertEquals(0, rootBand.getChildrenList().size());
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
}
