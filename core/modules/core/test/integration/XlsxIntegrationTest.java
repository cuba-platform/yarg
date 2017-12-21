package integration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.xlsx.Document;
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
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.junit.*;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;
import smoketest.ConstantMap;
import utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XlsxIntegrationTest {
    static TestDatabase database = new TestDatabase();
    static DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory();
    DefaultExtractionControllerFactory controllerFactory
            = new DefaultExtractionControllerFactory(loaderFactory);

    ExtractionContextFactory contextFactory = new DefaultExtractionContextFactory(ExtractionUtils.emptyExtractor());

    @BeforeClass
    public static void construct() throws Exception {
        database.setUpDatabase();

        loaderFactory.setSqlDataLoader(new SqlDataLoader(database.getDs()));
        loaderFactory.setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()));
        loaderFactory.setJsonDataLoader(new JsonDataLoader());

        FixtureUtils.loadDb(database.getDs(), "integration/fixture/xlsx_integration_test.sql");
    }
    @AfterClass
    public static void destroy() throws SQLException, IOException, URISyntaxException {
        database.stop();
    }


    @Before
    public void configure() {
        controllerFactory.register(BandOrientation.CROSS, CrossTabExtractionController::new);
    }

    @Test
    public void testXlsx() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData band1_1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1_1.addData("col1", 1);
        band1_1.addData("col2", 2);
        band1_1.addData("col3", 3);
        band1_1.addData("col4", 4);
        band1_1.addData("col5", 5);
        band1_1.addData("col6", 6);

        BandData band12_1 = new BandData("Band12", band1_1, BandOrientation.HORIZONTAL);
        band12_1.addData("col1", 10);
        band12_1.addData("col2", 20);
        band12_1.addData("col3", 30);

        BandData band12_2 = new BandData("Band12", band1_1, BandOrientation.HORIZONTAL);
        band12_2.addData("col1", 100);
        band12_2.addData("col2", 200);
        band12_2.addData("col3", 300);

        BandData band13_1 = new BandData("Band13", band1_1, BandOrientation.VERTICAL);
        band13_1.addData("col1", 190);
        band13_1.addData("col2", 290);

        BandData band13_2 = new BandData("Band13", band1_1, BandOrientation.VERTICAL);
        band13_2.addData("col1", 390);
        band13_2.addData("col2", 490);

        BandData band14_1 = new BandData("Band14", band1_1, BandOrientation.VERTICAL);
        band14_1.addData("col1", "v5");
        band14_1.addData("col2", "v6");

        BandData band14_2 = new BandData("Band14", band1_1, BandOrientation.VERTICAL);
        band14_2.addData("col1", "v7");
        band14_2.addData("col2", "v8");

        BandData band1_2 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1_2.addData("col1", 11);
        band1_2.addData("col2", 22);
        band1_2.addData("col3", 33);
        band1_2.addData("col4", 44);
        band1_2.addData("col5", 55);
        band1_2.addData("col6", 66);

        BandData band12_3 = new BandData("Band12", band1_2, BandOrientation.HORIZONTAL);
        band12_3.addData("col1", 40);
        band12_3.addData("col2", 50);
        band12_3.addData("col3", 60);

        BandData band12_4 = new BandData("Band12", band1_2, BandOrientation.HORIZONTAL);
        band12_4.addData("col1", 400);
        band12_4.addData("col2", 500);
        band12_4.addData("col3", 600);

        band1_1.addChild(band12_1);
        band1_1.addChild(band12_2);
        band1_1.addChild(band13_1);
        band1_1.addChild(band13_2);
        band1_1.addChild(band14_1);
        band1_1.addChild(band14_2);

        band1_2.addChild(band12_3);
        band1_2.addChild(band12_4);

        root.addChild(band1_1);
        root.addChild(band1_2);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test.xlsx", "./modules/core/test/integration/test.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result.xlsx", "./modules/core/test/integration/etalon.xlsx");
    }

    @Test
    public void testXlsxToCsv() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData band1_1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1_1.addData("col1", 1);
        band1_1.addData("col2", 2);
        band1_1.addData("col3", 3);
        band1_1.addData("col4", 4);
        band1_1.addData("col5", 5);
        band1_1.addData("col6", 6);

        BandData band12_1 = new BandData("Band12", band1_1, BandOrientation.HORIZONTAL);
        band12_1.addData("col1", 10);
        band12_1.addData("col2", 20);
        band12_1.addData("col3", 30);

        BandData band12_2 = new BandData("Band12", band1_1, BandOrientation.HORIZONTAL);
        band12_2.addData("col1", 100);
        band12_2.addData("col2", 200);
        band12_2.addData("col3", 300);

        BandData band13_1 = new BandData("Band13", band1_1, BandOrientation.VERTICAL);
        band13_1.addData("col1", 190);
        band13_1.addData("col2", 290);

        BandData band13_2 = new BandData("Band13", band1_1, BandOrientation.VERTICAL);
        band13_2.addData("col1", 390);
        band13_2.addData("col2", 490);

        BandData band14_1 = new BandData("Band14", band1_1, BandOrientation.VERTICAL);
        band14_1.addData("col1", "v5");
        band14_1.addData("col2", "v6");

        BandData band14_2 = new BandData("Band14", band1_1, BandOrientation.VERTICAL);
        band14_2.addData("col1", "v7");
        band14_2.addData("col2", "v8");

        BandData band1_2 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1_2.addData("col1", 11);
        band1_2.addData("col2", 22);
        band1_2.addData("col3", 33);
        band1_2.addData("col4", 44);
        band1_2.addData("col5", 55);
        band1_2.addData("col6", 66);

        BandData band12_3 = new BandData("Band12", band1_2, BandOrientation.HORIZONTAL);
        band12_3.addData("col1", 40);
        band12_3.addData("col2", 50);
        band12_3.addData("col3", 60);

        BandData band12_4 = new BandData("Band12", band1_2, BandOrientation.HORIZONTAL);
        band12_4.addData("col1", 400);
        band12_4.addData("col2", 500);
        band12_4.addData("col3", 600);

        band1_1.addChild(band12_1);
        band1_1.addChild(band12_2);
        band1_1.addChild(band13_1);
        band1_1.addChild(band13_2);
        band1_1.addChild(band14_1);
        band1_1.addChild(band14_2);

        band1_2.addChild(band12_3);
        band1_2.addChild(band12_4);

        root.addChild(band1_1);
        root.addChild(band1_2);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result_xlsx.csv");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test.xlsx", "./modules/core/test/integration/test.xlsx", ReportOutputType.csv), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        File sample = new File("./modules/core/test/integration/ethalon_xlsx.csv");
        File result = new File("./result/integration/result_xlsx.csv");
        boolean isTwoEqual = FileUtils.contentEqualsIgnoreEOL(sample, result, null);

        assertTrue("Files are not equal", isTwoEqual);
    }

    @Test
    public void testAlignmentXlsx() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData band1_1 = createBand("Band1", 1, root, BandOrientation.HORIZONTAL);
        BandData band1_2 = createBand("Band1", 2, root, BandOrientation.HORIZONTAL);
        BandData band2_1 = createBand("Band2", 1, root, BandOrientation.HORIZONTAL);
        BandData band2_2 = createBand("Band2", 2, root, BandOrientation.HORIZONTAL);
        BandData band3_1 = createBand("Band3", 1, root, BandOrientation.VERTICAL);
        BandData band3_2 = createBand("Band3", 2, root, BandOrientation.VERTICAL);
        BandData band4_1 = createBand("Band4", 1, root, BandOrientation.VERTICAL);
        BandData band4_2 = createBand("Band4", 2, root, BandOrientation.VERTICAL);

        BandData split = new BandData("Split", root, BandOrientation.HORIZONTAL);
        BandData split2 = new BandData("Split2", root, BandOrientation.HORIZONTAL);
        BandData split3 = new BandData("Split3", root, BandOrientation.HORIZONTAL);

        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(split);
        root.addChild(band2_1);
        root.addChild(band2_2);
        root.addChild(split2);
        root.addChild(band3_1);
        root.addChild(band3_2);
        root.addChild(split3);
        root.addChild(band4_1);
        root.addChild(band4_2);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-align.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test.xlsx", "./modules/core/test/integration/test-align.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result-align.xlsx", "./modules/core/test/integration/etalon-align.xlsx");
    }

    @Test
    public void testXlsxCrosstab() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData header = new BandData("Header", root, BandOrientation.HORIZONTAL);
        root.addChild(header);

        for (int i = 1; i <= 10; i++) {
            BandData dateHeader = new BandData("DateHeader", root, BandOrientation.VERTICAL);
            dateHeader.addData("date", "2014/04/" + i);
            root.addChild(dateHeader);
        }

        BandData dateHeader = new BandData("DateHeader", root, BandOrientation.VERTICAL);
        dateHeader.addData("date", "...");
        root.addChild(dateHeader);

        BandData band11 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band11.addData("name", "Stanley");

        BandData band12 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band12.addData("name", "Kyle");

        BandData band13 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band13.addData("name", "Eric");

        BandData band14 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band14.addData("name", "Kenney");

        BandData band15 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band15.addData("name", "Craig");

        List<BandData> bands = Arrays.asList(band11, band12, band13, band14, band15);
        root.addChildren(bands);

        for (int i = 0, bandsSize = bands.size(); i < bandsSize; i++) {
            BandData band = bands.get(i);
            for (int j = 1; j <= 10; j++) {
                BandData nested = new BandData("Band2", band, BandOrientation.VERTICAL);
                band.addChild(nested);
                nested.addData("income", new BigDecimal((i + 1) * j));
            }
        }

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Header");
        root.getFirstLevelBandDefinitionNames().add("DateHeader");
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-crosstab.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-crosstab.xlsx", "./modules/core/test/integration/test-crosstab.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
        compareFiles("./result/integration/result-crosstab.xlsx", "./modules/core/test/integration/etalon-crosstab.xlsx");
    }

    @Test
    public void testXlsxCrosstabFeature() throws Exception {
        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-crosstab-feature.xlsx");
        ReportBand band = YmlDataUtil.bandFrom(FileLoader.load("integration/fixture/cross_sql_report_band.yml"));
        BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.setData(new HashMap<>());
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<>());

        for (ReportBand definition : band.getChildren()) {
            List<BandData> data = controllerFactory.controllerBy(definition.getBandOrientation())
                    .extract(contextFactory.context(definition, rootBand, new HashMap<>()));

            assertNotNull(data);

            data.forEach(b-> {
                assertNotNull(b);
                assertTrue(StringUtils.isNotEmpty(b.getName()));
            });

            rootBand.addChildren(data);
            rootBand.getFirstLevelBandDefinitionNames().add(definition.getName());
        }

        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(
                new FormatterFactoryInput("xlsx", rootBand,
                    new ReportTemplateImpl("", "./modules/core/test/integration/test-crosstab-feature.xlsx",
                            "./modules/core/test/integration/test-crosstab-feature.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
        compareFiles("./result/integration/result-crosstab-feature.xlsx", "./modules/core/test/integration/etalon-crosstab-feature.xlsx");
    }

    @Ignore("Fails on Travis CI")
    @Test
    public void testXlsxFormats() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);

        BandData header = new BandData("Header", root, BandOrientation.VERTICAL);
        BandData band = new BandData("Band", root, BandOrientation.VERTICAL);
        band.addData("number", BigDecimal.valueOf(-200015));
        band.addData("date", new Date(1440747161585L));
        band.addData("money", -113123d);
        band.addData("text", "someText");

        root.addChild(header);
        root.addChild(band);

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-formats.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-formats.xlsx",
                        "./modules/core/test/integration/test-formats.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
        compareFiles("./result/integration/result-formats.xlsx", "./modules/core/test/integration/etalon-formats.xlsx");
    }

    @Test
    public void testXlsxFormulas() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);

        BandData mainHeader = new BandData("MainHeader", root);
        mainHeader.setData(rootData);
        root.addChild(mainHeader);

        BandData header1 = new BandData("Header", root, BandOrientation.HORIZONTAL);
        header1.setData(new HashMap<String, Object>());
        header1.addData("service", "IT support");
        BandData details11 = new BandData("Details", header1, BandOrientation.HORIZONTAL);
        details11.setData(new HashMap<String, Object>());
        details11.addData("client", "Google");
        details11.addData("volume", 900);
        details11.addData("price", 114);
        BandData details12 = new BandData("Details", header1, BandOrientation.HORIZONTAL);
        details12.setData(new HashMap<String, Object>());
        details12.addData("client", "Yandex");
        details12.addData("volume", 300);
        details12.addData("price", 171);

        header1.addChild(details11);
        header1.addChild(details12);
        header1.addChild(new BandData("Total", header1, BandOrientation.HORIZONTAL));

        BandData header2 = new BandData("Header", root, BandOrientation.HORIZONTAL);
        header2.setData(new HashMap<String, Object>());
        header2.addData("service", "Technical support");
        BandData details21 = new BandData("Details", header2, BandOrientation.HORIZONTAL);
        details21.setData(new HashMap<String, Object>());
        details21.addData("client", "Google");
        details21.addData("volume", 110);
        details21.addData("price", 600);
        BandData details22 = new BandData("Details", header2, BandOrientation.HORIZONTAL);
        details22.setData(new HashMap<String, Object>());
        details22.addData("client", "Yandex");
        details22.addData("volume", 60);
        details22.addData("price", 800);

        header2.addChild(details21);
        header2.addChild(details22);
        header2.addChild(new BandData("Total", header2, BandOrientation.HORIZONTAL));

        root.addChild(header1);
        root.addChild(header2);

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-formulas.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-formulas.xlsx", "./modules/core/test/integration/test-formulas.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
        compareFiles("./result/integration/result-formulas.xlsx", "./modules/core/test/integration/etalon-formulas.xlsx");
    }

    @Test
    public void testXlsxHorizontalBandAfterVertical() throws Exception {
        BandData root = new BandData(BandData.ROOT_BAND_NAME);

        BandData horizontal11 = createBand("Horizontal", root, BandOrientation.HORIZONTAL);
        BandData horizontal111 = createBand("Horizontal1", horizontal11, BandOrientation.HORIZONTAL);
        BandData horizontal112 = createBand("Horizontal1", horizontal11, BandOrientation.HORIZONTAL);
        BandData vertical11 = createBand("Vertical", horizontal11, BandOrientation.VERTICAL);
        BandData vertical12 = createBand("Vertical", horizontal11, BandOrientation.VERTICAL);
        BandData vertical13 = createBand("Vertical", horizontal11, BandOrientation.VERTICAL);
        horizontal11.addChild(horizontal111);
        horizontal11.addChild(horizontal112);
        horizontal11.addChildren(Arrays.asList(vertical11, vertical12, vertical13));
        BandData horizontal21 = createBand("Horizontal2", horizontal11, BandOrientation.HORIZONTAL);
        BandData horizontal31 = createBand("Horizontal2", horizontal11, BandOrientation.HORIZONTAL);
        BandData horizontal41 = createBand("Horizontal2", horizontal11, BandOrientation.HORIZONTAL);
        BandData horizontal51 = createBand("Horizontal2", horizontal11, BandOrientation.HORIZONTAL);
        horizontal11.addChildren(Arrays.asList(horizontal21, horizontal31, horizontal41, horizontal51));

        BandData horizontal12 = createBand("Horizontal", root, BandOrientation.HORIZONTAL);
        horizontal111 = createBand("Horizontal1", horizontal12, BandOrientation.HORIZONTAL);
        horizontal112 = createBand("Horizontal1", horizontal12, BandOrientation.HORIZONTAL);
        BandData vertical21 = createBand("Vertical", horizontal12, BandOrientation.VERTICAL);
        BandData vertical22 = createBand("Vertical", horizontal12, BandOrientation.VERTICAL);
        horizontal12.addChild(horizontal111);
        horizontal12.addChild(horizontal112);
        horizontal12.addChildren(Arrays.asList(vertical21, vertical22));
        BandData horizontal22 = createBand("Horizontal2", horizontal12, BandOrientation.HORIZONTAL);
        horizontal12.addChild(horizontal22);

        BandData horizontal13 = createBand("Horizontal", root, BandOrientation.HORIZONTAL);
        BandData vertical31 = createBand("Vertical", horizontal13, BandOrientation.VERTICAL);
        BandData vertical32 = createBand("Vertical", horizontal13, BandOrientation.VERTICAL);
        BandData vertical33 = createBand("Vertical", horizontal13, BandOrientation.VERTICAL);
        horizontal13.addChildren(Arrays.asList(vertical31, vertical32, vertical33));
        BandData horizontal23 = createBand("Horizontal2", horizontal13, BandOrientation.HORIZONTAL);
        horizontal13.addChild(horizontal23);

        root.addChildren(Arrays.asList(horizontal11, horizontal12, horizontal13));

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-horizontal-after-vertical.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-horizontal-after-vertical.xlsx",
                        "./modules/core/test/integration/test-horizontal-after-vertical.xlsx",
                        ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result-horizontal-after-vertical.xlsx",
                "./modules/core/test/integration/etalon-horizontal-after-vertical.xlsx");
    }

    @Test
    public void testXlsxSecondLevelVertical() throws Exception {
        BandData root = createBand("Root", null, BandOrientation.HORIZONTAL);
        root.addChild(createBand("Header", root, BandOrientation.HORIZONTAL));

        for (int i = 1; i <= 10; i++) {
            BandData band1 = createBand("Band1", root, BandOrientation.HORIZONTAL);
            band1.addData("name", i);
            root.addChild(band1);
            for (int j = 0; j < 10; j++) {
                BandData band2 = createBand("Band2", band1, BandOrientation.HORIZONTAL);
                band1.addChild(band2);

                for (int k = 0; k < 3; k++) {
                    BandData band3 = createBand("Band3", band2, BandOrientation.VERTICAL);
                    band2.addChild(band3);
                }
            }
        }

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-second-level-vertical.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-second-level-vertical.xlsx",
                        "./modules/core/test/integration/test-second-level-vertical.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result-second-level-vertical.xlsx",
                "./modules/core/test/integration/etalon-second-level-vertical.xlsx");
    }


    private BandData createBand(String name, BandData root, BandOrientation horizontal) {
        BandData hor11 = new BandData(name, root, horizontal);
        hor11.setData(new ConstantMap(name));
        return hor11;
    }

    private BandData createBand(String name, int multiplier, BandData root, BandOrientation childOrient) {
        BandData band1_1 = new BandData(name, root, BandOrientation.HORIZONTAL);
        band1_1.addData("col1", 1 * multiplier);
        band1_1.addData("col2", 2 * multiplier);

        BandData band11_1 = new BandData(name + "1", band1_1, childOrient);
        band11_1.addData("col1", 10 * multiplier);
        band11_1.addData("col2", 20 * multiplier);

        BandData band11_2 = new BandData(name + "1", band1_1, childOrient);
        band11_2.addData("col1", 100 * multiplier);
        band11_2.addData("col2", 200 * multiplier);

        band1_1.addChild(band11_1);
        band1_1.addChild(band11_2);
        return band1_1;
    }

    private void compareFiles(String resultPath, String etalonPath) throws Docx4JException {
        Document result = Document.create(SpreadsheetMLPackage.load(new File(resultPath)));
        Document etalon = Document.create(SpreadsheetMLPackage.load(new File(etalonPath)));

        List<Document.SheetWrapper> resultWorksheets = result.getWorksheets();
        List<Document.SheetWrapper> etalonWorksheets = etalon.getWorksheets();

        for (int i = 0; i < resultWorksheets.size(); i++) {
            Document.SheetWrapper resultWorksheet = resultWorksheets.get(i);
            Document.SheetWrapper etalonWorksheet = etalonWorksheets.get(i);

            List<Row> resultRows = resultWorksheet.getWorksheet().getContents().getSheetData().getRow();
            List<Row> etalonRows = etalonWorksheet.getWorksheet().getContents().getSheetData().getRow();
            for (int j = 0, rowSize = resultRows.size(); j < rowSize; j++) {
                Row resultRow = resultRows.get(j);
                Row etalonRow = etalonRows.get(j);
                List<Cell> resultCells = resultRow.getC();
                List<Cell> etalonCells = etalonRow.getC();
                for (int i1 = 0, cSize = etalonCells.size(); i1 < cSize; i1++) {
                    Cell resultCell = resultCells.get(i1);
                    Cell etalonCell = etalonCells.get(i1);
                    if (resultCell.getF() != null) {
                        Assert.assertEquals(etalonCell.getF().getValue(), resultCell.getF().getValue());
                    } else {
                        Assert.assertEquals(etalonCell.getV(), resultCell.getV());
                    }
                }
            }
        }
    }
}