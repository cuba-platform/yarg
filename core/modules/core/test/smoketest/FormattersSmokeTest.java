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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class FormattersSmokeTest extends AbstractFormatSpecificTest {
    @Test
    public void testXls() throws Exception {
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
    public void testXlsToPdf() throws Exception {
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
    public void testDocx() throws Exception {
        BandData root = createRootBand();
        root.addReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

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
        root.addReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

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
    public void testOdt() throws Exception {
        BandData root = createRootBand();
        root.addReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

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
        root.addReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));

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
    public void testXlsm() throws Exception {
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

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.xlsm");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsm", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/test.xlsx", "./modules/core/test/smoketest/test.xlsm", ReportOutputType.xlsx), outputStream));
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
    public void testHtml() throws Exception {
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

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.html");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl("", "test.ftl", "./modules/core/test/smoketest/test.ftl", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }


    @Test
    public void testHtmlToPdf() throws Exception {
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
}
