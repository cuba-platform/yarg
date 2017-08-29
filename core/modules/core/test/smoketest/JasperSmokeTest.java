package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;


public class JasperSmokeTest {

    @Test
    public void jasperTestPdf() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jasper-result.pdf");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jasper", root,
                new ReportTemplateImpl("", "test.jasper", "./modules/core/test/smoketest/test.jasper", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void jasperTestCsv() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jasper-result.csv");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jasper", root,
                new ReportTemplateImpl("", "test.jasper", "./modules/core/test/smoketest/test.jasper", ReportOutputType.csv), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void jasperTestDoc() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jasper-result.doc");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jasper", root,
                new ReportTemplateImpl("", "test.jasper", "./modules/core/test/smoketest/test.jasper", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void jasperTestXls() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jasper-result.xls");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jasper", root,
                new ReportTemplateImpl("", "test.jasper", "./modules/core/test/smoketest/test.jasper", ReportOutputType.xls), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void jrxmlTestHtml() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jrxml-result.html");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jrxml", root,
                new ReportTemplateImpl("", "test.jrxml", "./modules/core/test/smoketest/test.jrxml", ReportOutputType.html), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void jrxmlTestXlsx() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jrxml-result.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jrxml", root,
                new ReportTemplateImpl("", "test.jrxml", "./modules/core/test/smoketest/test.jrxml", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void jrxmlTestDocx() throws Exception {
        BandData root = createRootTree();

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/jrxml-result.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("jrxml", root,
                new ReportTemplateImpl("", "test.jrxml", "./modules/core/test/smoketest/test.jrxml", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    protected BandData createRootTree() throws Exception {
        BandData root = new BandData("Root");
        BandData header = new BandData("Header", root);

        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy");

        BandData first = new BandData("First", root);
        first.addData("name", "first");
        first.addData("vin", 8475834);
        first.addData("date", ft.parse("10.08.2017"));
        first.addData("bigdc", BigDecimal.valueOf(23897428374324L));

        BandData second = new BandData("Second", root);
        second.addData("name", "second");
        second.addData("vin", 4738534);
        second.addData("date", ft.parse("11.08.2017"));
        second.addData("bigdc", BigDecimal.valueOf(32748237487238L));

        BandData first11 = new BandData("First11", first);
        first11.addData("name", "first 11");
        first11.addData("vin", 11847583);
        first11.addData("date", ft.parse("10.08.2017"));
        first11.addData("bigdc", BigDecimal.valueOf(23897428374324L));

        BandData first12 = new BandData("First12", first);
        first12.addData("name", "first 12");
        first12.addData("vin", 12847584);
        first12.addData("date", ft.parse("10.08.2017"));
        first12.addData("bigdc", BigDecimal.valueOf(23897428374324L));

        BandData second21 = new BandData("Second21", second);
        second21.addData("name", "second 21");
        second21.addData("vin", 21473853);
        second21.addData("date", ft.parse("11.08.2017"));
        second21.addData("bigdc", BigDecimal.valueOf(32748237487238L));


        BandData second22 = new BandData("Second22", second);
        second22.addData("name", "second 22");
        second22.addData("vin", 22473853);
        second22.addData("date", ft.parse("11.08.2017"));
        second22.addData("bigdc", BigDecimal.valueOf(32748237487238L));

        root.addChildren(Arrays.asList(header, first, second));
        first.addChildren(Arrays.asList(first11, first12));
        second.addChildren(Arrays.asList(second21, second22));

        return root;
    }
}
