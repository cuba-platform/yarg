package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class XlsxSpecificTest extends AbstractFormatSpecificTest {
    @Test
    public void testXlsxCharts() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));

        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }

        root.addChild(new BandData("Charts", root, BandOrientation.HORIZONTAL));

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Header");
        root.getFirstLevelBandDefinitionNames().add("Band");
        root.getFirstLevelBandDefinitionNames().add("Charts");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/charts.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/charts.xlsx", "./modules/core/test/smoketest/charts.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxRowBreaks() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));

        Random random = new Random();
        for (int i = 1; i <= 100; i++) {
            BandData band = new BandData("Band", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
            if (i % 10 == 0) {
                band.addChild(new BandData("Footer", band));
            }
        }

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Header");
        root.getFirstLevelBandDefinitionNames().add("Band");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/row_breaks.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/row_breaks.xlsx", "./modules/core/test/smoketest/row_breaks.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxStyles() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));

        Random random = new Random();
        for (int i = 1; i <= 100000; i++) {
            BandData band = new BandData("Band", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            if (i % 3 == 0) {
                band.addData("theStyle1", "TheStyle1");
            } else {
                band.addData("theStyle2", "TheStyle2");
            }
            root.addChild(band);
        }

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/xslx_styles.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/xslx_styles.xlsx", "./modules/core/test/smoketest/xslx_styles.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxHeader() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));
        BandData header1 = new BandData("Header1", root, BandOrientation.HORIZONTAL);
        header1.addData("company", "TestCompany");
        root.addChild(header1);

        Random random = new Random();
        for (int i = 1; i <= 1000; i++) {
            BandData band = new BandData("Band", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/xslx_header.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/xslx_header.xlsx", "./modules/core/test/smoketest/xslx_header.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxToPdfPrintSpaces() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band1", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }
        root.addChild(new BandData("Split1", root));
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band2", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }
        root.addChild(new BandData("Split2", root));
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band3", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }


        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");
        root.getFirstLevelBandDefinitionNames().add("Band3");
        root.getFirstLevelBandDefinitionNames().add("Split1");
        root.getFirstLevelBandDefinitionNames().add("Split2");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/print-spaces.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/print-spaces.xlsx", "./modules/core/test/smoketest/print-spaces.xlsx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    //todo eude move to integration tests
    @Test
    public void testLongColumnXlsxWithAggregatedFormulas() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);

        BandData header = new BandData("header", root, BandOrientation.HORIZONTAL);
        header.setData(new RandomMap());

        BandData project1 = new BandData("project", root, BandOrientation.HORIZONTAL);
        project1.setData(new RandomMap(10));

        BandData project2 = new BandData("project", root, BandOrientation.HORIZONTAL);
        project2.setData(new RandomMap(10));

        BandData project3 = new BandData("project", root, BandOrientation.HORIZONTAL);
        project3.setData(new RandomMap(10));

        BandData time11 = new BandData("time", project1, BandOrientation.HORIZONTAL);
        time11.setData(new RandomMap(10));
        BandData time12 = new BandData("time", project1, BandOrientation.HORIZONTAL);
        time12.setData(new RandomMap(10));
        project1.addChild(time11);
        project1.addChild(time12);

        BandData time21 = new BandData("time", project2, BandOrientation.HORIZONTAL);
        time21.setData(new RandomMap(10));
        BandData time22 = new BandData("time", project2, BandOrientation.HORIZONTAL);
        time22.setData(new RandomMap(10));
        project2.addChild(time21);
        project2.addChild(time22);

        BandData time31 = new BandData("time", project3, BandOrientation.HORIZONTAL);
        time31.setData(new RandomMap(10));
        BandData time32 = new BandData("time", project3, BandOrientation.HORIZONTAL);
        time32.setData(new RandomMap(10));
        project3.addChild(time31);
        project3.addChild(time32);

        root.addChild(header);
        root.addChild(project1);
        root.addChild(project2);
        root.addChild(project3);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/longColumnXlsxWithAggregatedFormulas.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/longColumnXlsxWithAggregatedFormulas.xlsx", "./modules/core/test/smoketest/longColumnXlsxWithAggregatedFormulas.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }


    @Test
    public void testCollapsedGroups() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));

        for (int i = 1; i <= 10; i++) {
            BandData band1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
            band1.addData("name", i);
            root.addChild(band1);
            for (int j = 0; j < 10; j++) {
                BandData band2 = new BandData("Band2", band1, BandOrientation.HORIZONTAL);
                band2.setData(new RandomMap());
                band1.addChild(band2);
            }

            for (int k = 0; k < 3; k++) {
                BandData band3 = new BandData("Band3", band1, BandOrientation.VERTICAL);
                band3.setData(new RandomMap());
                band1.addChild(band3);
            }
        }

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/collapsed_groups.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/collapsed_groups.xlsx", "./modules/core/test/smoketest/collapsed_groups.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }


    @Test
    public void testPivot() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));

        for (int i = 1; i <= 10; i++) {
            BandData band1 = new BandData("Data", root, BandOrientation.HORIZONTAL);
            band1.addData("number", "Item #" + i);
            band1.addData("count", i);
            band1.addData("price", i * 100);
            root.addChild(band1);
        }

        root.addChild(new BandData("Header2", root, BandOrientation.HORIZONTAL));

        for (int i = 1; i <= 5; i++) {
            BandData band1 = new BandData("Data2", root, BandOrientation.HORIZONTAL);
            band1.addData("number", "Item #" + i);
            band1.addData("count", i);
            band1.addData("price", i * 100);
            root.addChild(band1);
        }

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/pivot.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/pivot.xlsx", "./modules/core/test/smoketest/pivot.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }
}
