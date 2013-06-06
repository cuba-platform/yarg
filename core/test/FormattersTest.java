import com.haulmont.newreport.formatters.factory.FormatterFactoryInput;
import com.haulmont.newreport.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.newreport.structure.impl.BandOrientation;
import com.haulmont.newreport.structure.ReportOutputType;
import com.haulmont.newreport.formatters.Formatter;
import com.haulmont.newreport.formatters.factory.DefaultFormatterFactory;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.impl.ReportTemplateBuilder;
import com.haulmont.newreport.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class FormattersTest {
    @Test
    public void testXlsFormatter() throws Exception {
        Band root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/result.xls");

        Formatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl(null, "test.xls", "./test/test.xls", ReportOutputType.xls), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsToPdfFormatter() throws Exception {
        Band root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/result.pdf");

        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        Formatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl(null, "test.xls", "./test/test.xls", ReportOutputType.pdf), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocx() throws Exception {
        Band root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/result.docx");
        Formatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl(null, "test.docx", "./test/test.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testHtml() throws Exception {
        Band root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/result.html");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        Formatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl(null, "test.ftl", "./test/test.ftl", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDoc() throws Exception {
        Band root = createRootBand();

        FileOutputStream outputStream = new FileOutputStream("./result/result.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
        Formatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl(null, "test.odt", "./test/test.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    private Band createRootBand() {
        Band root = new Band("Root", null, BandOrientation.HORIZONTAL);
        root.setData(Collections.<String, Object>singletonMap("param1", "AAAAAA"));
        Band band1_1 = new Band("Band1", root, BandOrientation.HORIZONTAL);
        Band band1_2 = new Band("Band1", root, BandOrientation.HORIZONTAL);
        Band band1_3 = new Band("Band1", root, BandOrientation.HORIZONTAL);
        Band footer = new Band("Footer", root, BandOrientation.HORIZONTAL);
        Band split = new Band("Split", root, BandOrientation.HORIZONTAL);

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



        Band band2_1 = new Band("Band2", root, BandOrientation.HORIZONTAL);
        Band band2_2 = new Band("Band2", root, BandOrientation.HORIZONTAL);

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
        return root;
    }


}
