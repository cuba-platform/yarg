package integration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;

public class HtmlGroovyIntegrationTest {
    @Test
    public void testHtmlFormatter() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData userBand = new BandData("User", root, BandOrientation.HORIZONTAL);
        userBand.addData("active", true);
        userBand.addData("login", "admin");
        root.addChild(userBand);
        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("User");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/html-groovy-test-result.html");

        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("html", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/html-groovy-test-template.html",
                        "./modules/core/test/integration/html-groovy-test-template.html", ReportOutputType.html, true), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        File sample = new File("./modules/core/test/integration/html-groovy-test-template-result.html");
        File result = new File("./result/integration/html-groovy-test-result.html");
        boolean isTwoEqual = FileUtils.contentEqualsIgnoreEOL(sample, result, null);

        Assert.assertTrue("Files are not equal", isTwoEqual);
    }
}
