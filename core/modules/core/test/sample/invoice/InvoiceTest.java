package sample.invoice;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.*;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Date;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class InvoiceTest {
    @Test
    public void testInvoiceReport() throws Exception {
        Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./modules/core/test/sample/invoice/invoice-groovy.xml")));

        Reporting reporting = new Reporting();
        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();
        reporting.setFormatterFactory(formatterFactory);
        reporting.setLoaderFactory(new DefaultLoaderFactory().setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report), new FileOutputStream("./result/sample/invoice-groovy.pdf"));
    }

    @Test
    public void testInvoiceJsonReport() throws Exception {
        Report report = new DefaultXmlReader().parseXml(FileUtils.readFileToString(new File("./modules/core/test/sample/invoice/invoice-json.xml")));

        Reporting reporting = new Reporting();
        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();
        reporting.setFormatterFactory(formatterFactory);
        reporting.setLoaderFactory(new DefaultLoaderFactory()
                .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl()))
                .setJsonDataLoader(new JsonDataLoader()));

        String json = FileUtils.readFileToString(new File("./modules/core/test/sample/invoice/invoice-data.json"));
        ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report).param("param1", json),
                new FileOutputStream("./result/sample/invoice-json.pdf"));
    }


    @Test
    public void testInvoiceReportRaw() throws Exception {
        ReportBuilder reportBuilder = new ReportBuilder();
        ReportTemplateBuilder reportTemplateBuilder = new ReportTemplateBuilder()
                .documentPath("./modules/core/test/sample/invoice/invoice.docx")
                .documentName("invoice.docx")
                .outputType(ReportOutputType.docx)
                .readFileFromPath();
        reportBuilder.template(reportTemplateBuilder.build());
        BandBuilder bandBuilder = new BandBuilder();
        ReportBand main = bandBuilder.name("Main").query("Main", "return [\n" +
                "                              [\n" +
                "                               'invoiceNumber':99987,\n" +
                "                               'client' : 'Google Inc.',\n" +
                "                               'date' : new Date(),\n" +
                "                               'addLine1': '1600 Amphitheatre Pkwy',\n" +
                "                               'addLine2': 'Mountain View, USA',\n" +
                "                               'addLine3':'CA 94043',\n" +
                "                               'signature': '<html><body><span style=\"color:red\">Mr. Yarg</span></body></html>',\n" +
                "                               'footer' : '<html><body><b><span style=\"color:green;font-weight:bold;\">The invoice footer</span></b></body></html>' \n" +
                "                            ]]", "groovy").build();


        bandBuilder = new BandBuilder();
        ReportBand items = bandBuilder.name("Items").query("Items", "return [\n" +
                "                                ['name':'Java Concurrency in practice', 'price' : 15000],\n" +
                "                                ['name':'Clear code', 'price' : 13000],\n" +
                "                                ['name':'Scala in action', 'price' : 12000]\n" +
                "                            ]", "groovy").build();

        reportBuilder.band(main);
        reportBuilder.band(items);
        reportBuilder.format(new ReportFieldFormatImpl("Main.signature", "${html}"));
        reportBuilder.format(new ReportFieldFormatImpl("Main.footer", "${html}"));

        Report report = reportBuilder.build();

        Reporting reporting = new Reporting();
        reporting.setFormatterFactory(new DefaultFormatterFactory());
        reporting.setLoaderFactory(
                new DefaultLoaderFactory().setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

        ReportOutputDocument reportOutputDocument = reporting.runReport(
                new RunParams(report), new FileOutputStream("./result/sample/invoice-groovy-raw.docx"));
    }

    @Test
    public void testJrxmlInvoice() throws Exception {
        BandData root = createJasperRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/sample/jasper-invoice-result.pdf");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jrxml", root,
                new ReportTemplateImpl("", "invoice.jrxml", "./modules/core/test/sample/invoice/invoice.jrxml", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    protected BandData createJasperRootTree() throws Exception{
        BandData root = new BandData("Root");
        BandData header = new BandData("Header", root);

        BandData main = new BandData("Main", root);
        main.addData("Main.client", "Google Inc.");
        main.addData("Main.date", new Date());
        main.addData("Main.addLine1", "1600 Amphitheatre Pkwy");
        main.addData("Main.addLine2", "Mountain View, USA");
        main.addData("Main.invoiceNumber", 99987);
        main.addData("Main.signature", "Mr. Yarg");
        main.addData("Main.footer", "The invoice footer");
        main.addData("Main.image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));


        BandData dataset2 = new BandData("Dataset2", root);
        BandData dataset3 = new BandData("Dataset3", root);

        root.addChildren(Arrays.asList(header, main, dataset2, dataset3));

        BandData row1 = new BandData("row1", dataset2);
        row1.addData("name", "Java Concurrency in practice");
        row1.addData("price", 15000);

        BandData row2 = new BandData("row2", dataset2);
        row2.addData("name", "Clean code");
        row2.addData("price", 13000);

        BandData row3 = new BandData("row3", dataset2);
        row3.addData("name", "Scala in action");
        row3.addData("price", 12000);

        dataset2.addChildren(Arrays.asList(row1, row2, row3));

        BandData row11 = new BandData("row1", dataset3);
        row11.addData("author", "Brian Göetz");
        row11.addData("count", 1);

        BandData row21 = new BandData("row2", dataset3);
        row21.addData("author", "Robert \"Uncle Bob\" Martin");
        row21.addData("count", 1);

        BandData row31 = new BandData("row3", dataset3);
        row31.addData("author", "Nilanjan Raychaudhuri");
        row31.addData("count", 1);

        dataset3.addChildren(Arrays.asList(row11, row21, row31));

        return root;
    }

}
