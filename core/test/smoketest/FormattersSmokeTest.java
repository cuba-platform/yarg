package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.impl.BandData;
import com.haulmont.yarg.structure.impl.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
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
                new ReportTemplateImpl(null, "./test/smoketest/test.xls", "./test/smoketest/test.xls", ReportOutputType.xls), outputStream));

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
                new ReportTemplateImpl(null, "./test/smoketest/test.xls", "./test/smoketest/test.xls", ReportOutputType.pdf), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocx() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl(null, "./test/smoketest/test.docx", "./test/smoketest/test.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testHtml() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.html");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl(null, "test.ftl", "./test/smoketest/test.ftl", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDoc() throws Exception {
        BandData root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/result.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl(null, "./test/smoketest/test.odt", "./test/smoketest/test.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    private BandData createRootBand() {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
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
        datamap.put("cwidth", 10000);
        band1_1.setData(datamap);

        Map<String, Object> datamap2 = new HashMap<String, Object>();
        datamap2.put("col1", 444);
        datamap2.put("col2", 555);
        datamap2.put("col3", 666);
        datamap2.put("cwidth", 10000);
        band1_2.setData(datamap2);

        Map<String, Object> datamap3 = new HashMap<String, Object>();
        datamap3.put("col1", 1);
        datamap3.put("col2", 2);
        datamap3.put("col3", 3);
        datamap3.put("cwidth", 10000);
        band1_3.setData(datamap3);

        BandData band2_1 = new BandData("Band2", root, BandOrientation.HORIZONTAL);
        BandData band2_2 = new BandData("Band2", root, BandOrientation.HORIZONTAL);

        Map<String, Object> datamap4 = new HashMap<String, Object>();
        datamap4.put("col1", 111);
        datamap4.put("col2", 222);
        datamap4.put("col3", 333);
        datamap4.put("col4", 4444);
        band2_1.setData(datamap4);

        Map<String, Object> datamap5 = new HashMap<String, Object>();
        datamap5.put("col1", 111);
        datamap5.put("col2", 222);
        datamap5.put("col3", 333);
        datamap5.put("col4", 444);
        band2_2.setData(datamap5);


        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(band1_3);
        root.addChild(footer);
        root.addChild(split);
        root.addChild(band2_1);
        root.addChild(band2_2);
        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");

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
