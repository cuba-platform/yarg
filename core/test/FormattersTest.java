import com.haulmont.newreport.formatters.factory.FormatterFactoryInput;
import com.haulmont.newreport.formatters.impl.doc.connector.OOConnector;
import com.haulmont.newreport.structure.impl.BandOrientation;
import com.haulmont.newreport.structure.ReportOutputType;
import com.haulmont.newreport.formatters.Formatter;
import com.haulmont.newreport.formatters.factory.DefaultFormatterFactory;
import com.haulmont.newreport.structure.impl.Band;
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
        defaultFormatterFactory.setOOConnectorAPI(new OOConnector("C:\\Program Files (x86)\\OpenOffice.org 3\\program", 8100));
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
        Map<String, Object> datamap = new HashMap<String, Object>();
        datamap.put("col1", 123);
        datamap.put("col2", 321);
        band1_1.setData(datamap);
        Map<String, Object> datamap2 = new HashMap<String, Object>();
        datamap2.put("col1", 456);
        datamap2.put("col2", 654);
        band1_2.setData(datamap2);

        root.addChild(band1_1);
        root.addChild(band1_2);
        root.setBandDefinitionNames(new HashSet<String>());
        root.getBandDefinitionNames().add("Band1");
        root.getBandDefinitionNames().add("Band2");
        return root;
    }


}
