package extraction.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.reporting.extraction.DefaultExtractionContextFactory;
import com.haulmont.yarg.reporting.extraction.DefaultExtractionControllerFactory;
import com.haulmont.yarg.reporting.extraction.ExtractionContextFactory;
import com.haulmont.yarg.reporting.extraction.controller.CrossTabExtractionController;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import org.apache.commons.lang.StringUtils;
import org.junit.*;
import utils.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static utils.ExtractionUtils.checkHeader;
import static utils.ExtractionUtils.checkMasterData;

public class CrosstabControllerTest {

    static TestDatabase database = new TestDatabase();
    static DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory();
    DefaultExtractionControllerFactory controllerFactory
            = new DefaultExtractionControllerFactory(loaderFactory);

    ExtractionContextFactory contextFactory = new DefaultExtractionContextFactory(ExtractionUtils.emptyExtractor());

    @BeforeClass
    public static void construct() throws Exception {
        database.setUpDatabase();
        FixtureUtils.loadDb(database.getDs(), "extraction/fixture/controller_test.sql");

        loaderFactory.setSqlDataLoader(new SqlDataLoader(database.getDs()));
        loaderFactory.setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()));
        loaderFactory.setJsonDataLoader(new JsonDataLoader());
    }

    @AfterClass
    public static void destroy() throws Exception {
        database.stop();
    }

    @Before
    public void configure() {
        controllerFactory.register(BandOrientation.CROSS, CrossTabExtractionController::new);
    }

    @Test
    public void testSqlExtractionForCrosstabBand() throws IOException, URISyntaxException {
        ReportBand band = YmlDataUtil.bandFrom(FileLoader.load("extraction/fixture/cross_sql_report_band.yml"));
        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        Multimap<String, BandData> reportBandMap = HashMultimap.create();

        for (ReportBand definition : band.getChildren()) {
            List<BandData> data = controllerFactory.controllerBy(definition.getBandOrientation())
                    .extract(contextFactory.context(definition, rootBand, new HashMap<>()));

            Assert.assertNotNull(data);

            data.forEach(b-> {
                Assert.assertNotNull(b);
                Assert.assertTrue(StringUtils.isNotEmpty(b.getName()));

                reportBandMap.put(b.getName(), b);
            });

            rootBand.addChildren(data);
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
        }

        checkHeader(reportBandMap.get("crosstab_header"), 12, "MONTH_NAME", "MONTH_ID");
        checkMasterData(reportBandMap.get("crosstab_master_data"), 3, 12,
                "USER_ID", "LOGIN", "HOURS");
    }

    @Test
    public void testGroovyExtractionForBand() throws IOException, URISyntaxException {
        ReportBand band = YmlDataUtil.bandFrom(FileLoader.load("extraction/fixture/cross_groovy_report_band.yml"));

        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        Multimap<String, BandData> reportBandMap = HashMultimap.create();

        for (ReportBand definition : band.getChildren()) {
            List<BandData> data = controllerFactory.controllerBy(definition.getBandOrientation())
                    .extract(contextFactory.context(definition, rootBand, new HashMap<>()));

            Assert.assertNotNull(data);

            data.forEach(b-> {
                Assert.assertNotNull(b);
                Assert.assertTrue(StringUtils.isNotEmpty(b.getName()));

                reportBandMap.put(b.getName(), b);
            });

            rootBand.addChildren(data);
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
        }

        checkHeader(reportBandMap.get("crosstab_header"), 2, "name", "id");
        checkMasterData(reportBandMap.get("crosstab_master_data"), 2, 2,
                "id", "name", "crosstab_dynamic_header_id", "crosstab_master_data_id", "value");
    }

    @Test
    public void testJsonExtractionForBand() throws IOException, URISyntaxException {
        ReportBand band = YmlDataUtil.bandFrom(FileLoader.load("extraction/fixture/cross_json_report_band.yml"));

        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        Multimap<String, BandData> reportBandMap = HashMultimap.create();

        for (ReportBand definition : band.getChildren()) {
            List<BandData> data = controllerFactory.controllerBy(definition.getBandOrientation())
                    .extract(contextFactory.context(definition, rootBand, ExtractionUtils.getParams(definition)));

            Assert.assertNotNull(data);

            data.forEach(b-> {
                Assert.assertNotNull(b);
                Assert.assertTrue(StringUtils.isNotEmpty(b.getName()));

                reportBandMap.put(b.getName(), b);
            });

            rootBand.addChildren(data);
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
        }

        checkHeader(reportBandMap.get("crosstab_header"), 2, "name", "id");
        checkMasterData(reportBandMap.get("crosstab_master_data"), 2, 2,
                "id", "name", "crosstab_dynamic_header_id", "crosstab_master_data_id", "value");
    }
}
