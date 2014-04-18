package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandOrientation;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class FormattersSmokeTest {
    @Test
    public void testXlsFormatter() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.xls");

        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl("", "./test/smoketest/test.xls", "./test/smoketest/test.xls", ReportOutputType.xls), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsToPdfFormatter() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.pdf");

        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl("", "./test/smoketest/test.xls", "./test/smoketest/test.xls", ReportOutputType.pdf), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocx() throws Exception {
        BandData root = createRootBand();
        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footerChild.addData("nestedData.withPoint", "NESTED_DATA_WITH_POINT");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./test/smoketest/test.docx", "./test/smoketest/test.docx", ReportOutputType.docx), outputStream));
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
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./test/smoketest/test.docx", "./test/smoketest/test.docx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxToHtml() throws Exception {
        BandData root = createRootBand();
        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_docx.html");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./test/smoketest/test.docx", "./test/smoketest/test.docx", ReportOutputType.html), outputStream));
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
                new ReportTemplateImpl("", "./test/smoketest/runMerge.docx", "./test/smoketest/runMerge.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testOdt() throws Exception {
        BandData root = createRootBand();
        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl("", "./test/smoketest/test.odt", "./test/smoketest/test.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDoc() throws Exception {
        BandData root = createRootBand();
        BandData footer = root.getChildByName("Footer");
        BandData footerChild = new BandData("FooterChild", footer);
        footerChild.addData("nestedData", "NESTED_DATA");
        footer.addChild(footerChild);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result2.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                new ReportTemplateImpl("", "./test/smoketest/test.doc", "./test/smoketest/test.doc", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
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
        split.addData("image", FileUtils.readFileToByteArray(new File("./test/yarg.png")));
        root.addChild(split);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./test/smoketest/test.xlsx", "./test/smoketest/test.xlsx", ReportOutputType.xlsx), outputStream));
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
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./test/smoketest/test1.xlsx", "./test/smoketest/test1.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testHtml() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.html");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl("", "test.ftl", "./test/smoketest/test.ftl", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testHtml2() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);

        class StrangeMap implements Map {
            Random random = new Random();

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public boolean containsKey(Object key) {
                return false;
            }

            @Override
            public boolean containsValue(Object value) {
                return false;
            }

            @Override
            public Object get(Object key) {
                return random.nextInt();
            }

            @Override
            public Object put(Object key, Object value) {
                return null;
            }

            @Override
            public Object remove(Object key) {
                return null;
            }

            @Override
            public void putAll(Map m) {

            }

            @Override
            public void clear() {

            }

            @Override
            public Set keySet() {
                return null;
            }

            @Override
            public Collection values() {
                return null;
            }

            @Override
            public Set<Entry> entrySet() {
                return null;
            }
        }

        BandData band11 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band11.setData(new StrangeMap());

        BandData band12 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band12.setData(new StrangeMap());

        root.addChild(band11);
        root.addChild(band12);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result2.html");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl("", "test.ftl", "./test/smoketest/test.ftl", ReportOutputType.html), outputStream));
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
            html += "<tr><td>1234567</td></tr>";
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
                new ReportTemplateImpl("", "./test/smoketest/colontitules.docx", "./test/smoketest/colontitules.docx", ReportOutputType.docx), outputStream));
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
            html += "<tr><td>1234567</td></tr>";
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
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl("", "./test/smoketest/colontitules.odt", "./test/smoketest/colontitules.odt", ReportOutputType.doc), outputStream));
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
        datamap.put("col2", 222);
        datamap.put("col3", 333);
        datamap.put("col.nestedCol", null);
        datamap.put("col.nestedBool", null);
        datamap.put("cwidth", 10000);
        band1_1.setData(datamap);

        Map<String, Object> datamap2 = new HashMap<String, Object>();
        datamap2.put("col1", 444);
        datamap2.put("col2", 555);
        datamap2.put("col3", 666);
        datamap2.put("col.nestedCol", "NESTED1");
        datamap2.put("col.nestedBool", false);
        datamap2.put("cwidth", 10000);
        band1_2.setData(datamap2);

        Map<String, Object> datamap3 = new HashMap<String, Object>();
        datamap3.put("col1", 777);
        datamap3.put("col2", 888);
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
            root.addData("image", FileUtils.readFileToByteArray(new File("./test/yarg.png")));
            split.addData("image", FileUtils.readFileToByteArray(new File("./test/yarg.png")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;
    }


}
