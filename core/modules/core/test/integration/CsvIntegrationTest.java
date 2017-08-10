package integration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * @author birin
 * @version $Id$
 */
public class CsvIntegrationTest {

    @Test
    public void testCsv() throws Exception {
        BandData root = createRootCsvTree();

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result.csv");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("csv", root,
                new ReportTemplateImpl("", "test.csv", "./modules/core/test/integration/test.csv", ReportOutputType.csv), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        File sample = new File("./modules/core/test/integration/ethalon.csv");
        File result = new File("./result/integration/result.csv");
        boolean isTwoEqual = FileUtils.contentEquals(sample, result);

        Assert.assertTrue("Files are not equal", isTwoEqual);
    }

    @Test
    public void testCsvQuote() throws Exception {
        BandData root = createRootCsvTree();

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-quote.csv");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("csv", root,
                new ReportTemplateImpl("", "test-quote.csv", "./modules/core/test/integration/test-quote.csv", ReportOutputType.csv), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        File sample = new File("./modules/core/test/integration/ethalon-quote.csv");
        File result = new File("./result/integration/result-quote.csv");
        boolean isTwoEqual = FileUtils.contentEquals(sample, result);

        Assert.assertTrue("Files are not equal", isTwoEqual);
    }

    protected BandData createRootCsvTree() throws Exception{
        BandData root = new BandData("Root");
        BandData header = new BandData("Header", root);
        SimpleDateFormat ft = new SimpleDateFormat("dd.MM.yyyy");

        String amountFormat = "###,###.###";
        String dateFormat = "dd.MM.yyyy HH:mm:ss";
        root.addReportFieldFormats(Arrays.asList(
                new ReportFieldFormatImpl("date", dateFormat),
                new ReportFieldFormatImpl("double", amountFormat),
                new ReportFieldFormatImpl("amount", amountFormat)));

        BandData first = new BandData("First", root);
        first.addData("firstName", "first");
        first.addData("lastName", "last");
        first.addData("amount", 24132432);
        first.addData("date", ft.parse("10.08.2017"));
        first.addData("bigdc", BigDecimal.valueOf(23897428374324L));
        first.addData("double", 3478213324.231232);

        BandData second = new BandData("Second", root);
        second.addData("firstName", "second");
        second.addData("lastName", "last,last");
        second.addData("amount", 12432434);
        second.addData("date", ft.parse("11.08.2017"));
        second.addData("bigdc", BigDecimal.valueOf(32748237487238L));
        second.addData("double", 94875746.3248736);


        root.addChildren(Arrays.asList(header, first, second));
        return root;
    }
}
