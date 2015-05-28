package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class FormattersSmokeTest {
    public String openOfficePath = System.getenv("YARG_OPEN_OFFICE_PATH");

    public FormattersSmokeTest() {
        if (StringUtils.isBlank(openOfficePath)) {
            openOfficePath = "C:/Program Files (x86)/OpenOffice.org 3/program";
        }
    }

    @Test
    public void testXlsFormatter() throws Exception {
        BandData root = createRootBand();
        BandData date = new BandData("Date", root);
        BandData dateHeader = new BandData("DateHeader", root);
        date.addData("date", new Date());
        root.addChild(dateHeader);
        root.addChild(date);
        List<BandData> band1objects = root.getChildrenByName("Band1");
        for (int i = 0; i < band1objects.size(); i++) {
            BandData bandData = band1objects.get(i);
            if (i % 2 == 0) {
                bandData.addData("theStyle", "red");
            }
        }


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.xls");

        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.xls", "./modules/core/test/smoketest/test.xls", ReportOutputType.xls), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsToPdfFormatter() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.pdf");

        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.xls", "./modules/core/test/smoketest/test.xls", ReportOutputType.pdf), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxTableWithSplittedBandAlias() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);
        BandData ride = new BandData("ride", root, BandOrientation.HORIZONTAL);
        ride.setData(new HashMap<String, Object>());
        root.addChild(ride);


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/waybill_car.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/waybill_car.docx", "./modules/core/test/smoketest/waybill_car.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocx() throws Exception {
        BandData root = createRootBand();
        root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footerChild.addData("nestedData.withPoint", "NESTED_DATA_WITH_POINT");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.docx", "./modules/core/test/smoketest/test.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxToPdf() throws Exception {
        BandData root = createRootBand();
        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_docx.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.docx", "./modules/core/test/smoketest/test.docx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxToHtml() throws Exception {
        BandData root = createRootBand();
        root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_docx.html");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.docx", "./modules/core/test/smoketest/test.docx", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxRunMerge() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData cover = new BandData("Cover", root, BandOrientation.HORIZONTAL);
        cover.setData(new HashMap<String, Object>());
        cover.addData("index", "123");
        cover.addData("volume", "321");
        cover.addData("name", "AAA");
        BandData documents = new BandData("Documents", root, BandOrientation.HORIZONTAL);
        documents.setData(new HashMap<String, Object>());
        root.addChild(cover);
        root.addChild(documents);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/runMerge.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/runMerge.docx", "./modules/core/test/smoketest/runMerge.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testOdt() throws Exception {
        BandData root = createRootBand();
        root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.odt", "./modules/core/test/smoketest/test.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDoc() throws Exception {
        BandData root = createRootBand();
        root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result2.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testParallelDoc() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(3);
        final DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        final OfficeIntegration officeIntegrationAPI = new OfficeIntegration(openOfficePath, 8100, 8101, 8102);
        officeIntegrationAPI.setTimeoutInSeconds(10);
        officeIntegrationAPI.setTemporaryDirPath("./result/temp/");

        defaultFormatterFactory.setOfficeIntegration(officeIntegrationAPI);
        new Thread() {
            @Override
            public void run() {
                try {
                    BandData root = createRootBand();
                    root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));
                    BandData footer = root.getChildByName("Footer");
                    BandData footerChild = new BandData("FooterChild", footer);
                    footerChild.addData("nestedData", "NESTED_DATA");
                    footer.addChild(footerChild);
                    FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_parallel1.doc");
                    ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                            new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
                    formatter.renderDocument();

                    IOUtils.closeQuietly(outputStream);
                } catch (IOException e) {
                    Assert.fail();
                } finally {
                    countDownLatch.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    BandData root = createRootBand();
                    root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));
                    BandData footer = root.getChildByName("Footer");
                    BandData footerChild = new BandData("FooterChild", footer);
                    footerChild.addData("nestedData", "NESTED_DATA");
                    footer.addChild(footerChild);

                    FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_parallel2.doc");
                    ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                            new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
                    formatter.renderDocument();

                    IOUtils.closeQuietly(outputStream);
                } catch (IOException e) {
                    Assert.fail();
                } finally {
                    countDownLatch.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    BandData root = createRootBand();
                    root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));
                    BandData footer = root.getChildByName("Footer");
                    BandData footerChild = new BandData("FooterChild", footer);
                    footerChild.addData("nestedData", "NESTED_DATA");
                    footer.addChild(footerChild);

                    FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_parallel3.doc");
                    ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                            new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
                    formatter.renderDocument();

                    IOUtils.closeQuietly(outputStream);
                } catch (IOException e) {
                    Assert.fail();
                } finally {
                    countDownLatch.countDown();
                }
            }
        }.start();
        countDownLatch.await();
        System.out.println();
    }


    @Test
    public void testXlsx() throws Exception {
        BandData root = createRootBand();

        BandData band3_1 = new BandData("Band3", root, BandOrientation.VERTICAL);
        band3_1.addData("col1", 123);
        band3_1.addData("col2", 321);
        BandData band3_2 = new BandData("Band3", root, BandOrientation.VERTICAL);
        band3_2.addData("col1", 456);
        band3_2.addData("col2", 654);
        BandData band3_3 = new BandData("Band3", root, BandOrientation.VERTICAL);
        band3_3.addData("col1", 789);
        band3_3.addData("col2", 987);
        BandData second = new BandData("Second", root, BandOrientation.HORIZONTAL);


        root.addChild(band3_1);
        root.addChild(band3_2);
        root.addChild(band3_3);
        root.addChild(second);

        BandData split = new BandData("Split", root, BandOrientation.HORIZONTAL);
        split.setData(new HashMap<String, Object>());
        split.addData("image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));
        root.addChild(split);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.xlsx", "./modules/core/test/smoketest/test.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsx2() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);

        BandData header = new BandData("Header", root, BandOrientation.VERTICAL);
        BandData band = new BandData("Band", root, BandOrientation.VERTICAL);
        band.addData("number", BigDecimal.valueOf(-200015));
        band.addData("date", new Date());
        band.addData("money", -113123d);
        band.addData("text", "someText");

        root.addChild(header);
        root.addChild(band);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result2.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test2.xlsx", "./modules/core/test/smoketest/test2.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxToPdf() throws Exception {
        BandData root = createRootBand();

        BandData band3_1 = new BandData("Band3", root, BandOrientation.VERTICAL);
        band3_1.addData("col1", 123);
        band3_1.addData("col2", 321);
        BandData band3_2 = new BandData("Band3", root, BandOrientation.VERTICAL);
        band3_2.addData("col1", 456);
        band3_2.addData("col2", 654);
        BandData band3_3 = new BandData("Band3", root, BandOrientation.VERTICAL);
        band3_3.addData("col1", 789);
        band3_3.addData("col2", 987);
        BandData second = new BandData("Second", root, BandOrientation.HORIZONTAL);


        root.addChild(band3_1);
        root.addChild(band3_2);
        root.addChild(band3_3);
        root.addChild(second);

        BandData split = new BandData("Split", root, BandOrientation.HORIZONTAL);
        split.setData(new HashMap<String, Object>());
        split.addData("image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));
        root.addChild(split);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_xlsx.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.xlsx", "./modules/core/test/smoketest/test.xlsx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsx1() throws Exception {
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

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result1.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test1.xlsx", "./modules/core/test/smoketest/test1.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    /*@Test
    public void testBigXlsx() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        for (int i = 0; i < 100000; i++) {
            BandData band1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
            band1.addData("name", "Name#" + i);
            root.addChild(band1);
        }

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result3.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test3.xlsx", "./modules/core/test/smoketest/test3.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }*/

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
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/charts.xlsx", "./modules/core/test/smoketest/charts.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
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

        Random random = new Random();
        for (BandData band : bands) {
            for (int i = 1; i <= 10; i++) {
                BandData nested = new BandData("Band2", band, BandOrientation.VERTICAL);
                band.addChild(nested);
                nested.addData("income", new BigDecimal(20 + i + Math.abs(random.nextDouble()) * 60));
            }
        }

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Header");
        root.getFirstLevelBandDefinitionNames().add("DateHeader");
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/crosstab.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/crosstab.xlsx", "./modules/core/test/smoketest/crosstab.xlsx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxBreaks() throws Exception {
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

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/breaks.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/breaks.xlsx", "./modules/core/test/smoketest/breaks.xlsx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testHtml() throws Exception {
        BandData root = createRootBand();
        root.addData("date", null);
        root.addData("date2", new Date());
        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl("", "test.ftl", "./modules/core/test/smoketest/test.ftl", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testHtml2() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);

        BandData band11 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band11.setData(new RandomMap());

        BandData band12 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band12.setData(new RandomMap());

        root.addChild(band11);
        root.addChild(band12);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result2.html");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl("", "test.ftl", "./modules/core/test/smoketest/test.ftl", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxWithColontitulesAndHtmlPageBreak() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData letterTable = new BandData("letterTable", root, BandOrientation.HORIZONTAL);
        BandData creatorInfo = new BandData("creatorInfo", root, BandOrientation.HORIZONTAL);
        HashMap<String, Object> letterTableData = new HashMap<String, Object>();
        String html = "<html><body>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>123456712345671234567123456712345671234567123456712345" +
                    "67123456712345671234567123456712345671234567123456712345671234" +
                    "5671234567123456712345671234567123456712345671234567</td></tr>";
        }
        html += "</table>";
        html += "<br style=\"page-break-after: always\">";
        html += "<p>Second table</p>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>1234567</td></tr>";
        }
        html += "</table>";


        html += "</body></html>";
        letterTableData.put("html", html);
        letterTable.setData(letterTableData);
        HashMap<String, Object> creatorInfoData = new HashMap<String, Object>();
        creatorInfoData.put("name", "12345");
        creatorInfoData.put("phone", "54321");
        creatorInfo.setData(creatorInfoData);
        root.addChild(letterTable);
        root.addChild(creatorInfo);
        root.getReportFieldFormats().put("letterTable.html", new ReportFieldFormatImpl("letterTable.html", "${html}"));

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/colontitules.docx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/colontitules.docx", "./modules/core/test/smoketest/colontitules.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocWithColontitulesAndHtmlPageBreak() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData letterTable = new BandData("letterTable", root, BandOrientation.HORIZONTAL);
        BandData creatorInfo = new BandData("creatorInfo", root, BandOrientation.HORIZONTAL);
        HashMap<String, Object> letterTableData = new HashMap<String, Object>();
        String html = "<html><body>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>123456712345671234567123456712345671234567123456712345" +
                    "67123456712345671234567123456712345671234567123456712345671234" +
                    "5671234567123456712345671234567123456712345671234567</td></tr>";
        }
        html += "</table>";
        html += "<br style=\"page-break-after: always\">";
        html += "<p>Second table</p>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>1234567</td></tr>";
        }
        html += "</table>";


        html += "</body></html>";
        letterTableData.put("html", html);
        letterTable.setData(letterTableData);
        HashMap<String, Object> creatorInfoData = new HashMap<String, Object>();
        creatorInfoData.put("name", "12345");
        creatorInfoData.put("phone", "54321");
        creatorInfo.setData(creatorInfoData);
        root.addChild(letterTable);
        root.addChild(creatorInfo);
        root.getReportFieldFormats().put("letterTable.html", new ReportFieldFormatImpl("letterTable.html", "${html}"));

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/colontitules.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/colontitules.odt", "./modules/core/test/smoketest/colontitules.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    private BandData createRootBand() {
        return createRootBand(null);
    }

    private BandData createRootBand(List<BandData> bands) {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Root.param1", "%16s")));

        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData band1_1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData band1_2 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData band1_3 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData footer = new BandData("Footer", root, BandOrientation.HORIZONTAL);
        BandData split = new BandData("Split", root, BandOrientation.HORIZONTAL);
        split.setData(new HashMap<String, Object>());

        Map<String, Object> datamap = new HashMap<String, Object>();
        datamap.put("col1", 111);
        datamap.put("col2", "<html><body><b>html text</b></body></html>");
        datamap.put("col3", 333);
        datamap.put("col.nestedCol", null);
        datamap.put("col.nestedBool", null);
        datamap.put("cwidth", 10000);
        band1_1.setData(datamap);

        Map<String, Object> datamap2 = new HashMap<String, Object>();
        datamap2.put("col1", 444);
        datamap2.put("col2", "<html><body><b>html text</b></body></html>");
        datamap2.put("col3", 666);
        datamap2.put("col.nestedCol", "NESTED1");
        datamap2.put("col.nestedBool", false);
        datamap2.put("cwidth", 10000);
        band1_2.setData(datamap2);

        Map<String, Object> datamap3 = new HashMap<String, Object>();
        datamap3.put("col1", 777);
        datamap3.put("col2", "<html><body><b>html text</b></body></html>");
        datamap3.put("col3", 999);
        datamap3.put("col.nestedCol", "NESTED2");
        datamap3.put("col.nestedBool", true);
        datamap3.put("cwidth", 10000);
        band1_3.setData(datamap3);

        BandData band2_1 = new BandData("Band2", root, BandOrientation.HORIZONTAL);
        BandData band2_2 = new BandData("Band2", root, BandOrientation.HORIZONTAL);

        Map<String, Object> datamap4 = new HashMap<String, Object>();
        datamap4.put("col1", 111);
        datamap4.put("col2", 222);
        datamap4.put("col3", 333);
        datamap4.put("col4", 444);
        band2_1.setData(datamap4);

        Map<String, Object> datamap5 = new HashMap<String, Object>();
        datamap5.put("col1", 555);
        datamap5.put("col2", 666);
        datamap5.put("col3", 777);
        datamap5.put("col4", 888);
        band2_2.setData(datamap5);

        Map<String, Object> datamap6 = new HashMap<String, Object>();
        datamap6.put("col1", 123);
        datamap6.put("col2", 456);
        datamap6.put("col3", 789);
        footer.setData(datamap6);

        if (bands != null) {
            for (BandData band : bands) {
                root.addChild(band);
                band.setParentBand(root);
            }
        }

        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(band1_3);
        root.addChild(band2_1);
        root.addChild(band2_2);
        root.addChild(split);
        root.addChild(footer);
        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");
        root.getFirstLevelBandDefinitionNames().add("Split");
        root.getFirstLevelBandDefinitionNames().add("Footer");


        root.setReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Root.image", "${bitmap:100x100}"), new ReportFieldFormatImpl("Split.image", "${bitmap:100x100}")));
        try {
            root.addData("image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));
            split.addData("image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));
            split.addData("date", new Date());
            split.addData("theStyle", "redDate");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;
    }


}
