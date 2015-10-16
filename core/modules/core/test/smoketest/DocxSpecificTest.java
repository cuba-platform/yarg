package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.HashMap;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class DocxSpecificTest extends AbstractFormatSpecificTest {
    @Test
    public void testDocxTableWithSplittedBandAlias() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<String, Object>();
        root.setData(rootData);
        BandData ride = new BandData("ride", root, BandOrientation.HORIZONTAL);
        ride.setData(new RandomMap());
        root.addChild(ride);


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/splitted-aliases-in-table.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/splitted-aliases-in-table.docx", "./modules/core/test/smoketest/splitted-aliases-in-table.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }


    @Test
    public void testDocxWithSplittedAlias() throws Exception {
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

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/splitted-aliases.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/splitted-aliases.docx", "./modules/core/test/smoketest/splitted-aliases.docx", ReportOutputType.docx), outputStream));
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
}
