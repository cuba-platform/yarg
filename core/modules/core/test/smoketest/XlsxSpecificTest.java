package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Random;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class XlsxSpecificTest extends AbstractFormatSpecificTest{
    @Test
    public void testXlsxCharts() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        root.addChild(new BandData("Header", root, BandOrientation.HORIZONTAL));

        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }

        root.addChild(new BandData("Charts", root, BandOrientation.HORIZONTAL));

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Header");
        root.getFirstLevelBandDefinitionNames().add("Band");
        root.getFirstLevelBandDefinitionNames().add("Charts");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/charts.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/charts.xlsx", "./modules/core/test/smoketest/charts.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testXlsxToPdfPrintSpaces() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        Random random = new Random();
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band1", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }
        root.addChild(new BandData("Split1", root));
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band2", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }
        root.addChild(new BandData("Split2", root));
        for (int i = 1; i <= 10; i++) {
            BandData band = new BandData("Band3", root, BandOrientation.HORIZONTAL);
            band.addData("i", i);
            double value1 = 15 + i + Math.abs(random.nextDouble()) * 30;
            band.addData("value1", value1);
            double value2 = 20 + i + Math.abs(random.nextDouble()) * 60;
            band.addData("value2", value2);
            double value3 = 25 + i + Math.abs(random.nextDouble()) * 90;
            band.addData("value3", value3);
            band.addData("value4", (value1 + value2 + value3) / 3);
            root.addChild(band);
        }


        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");
        root.getFirstLevelBandDefinitionNames().add("Band3");
        root.getFirstLevelBandDefinitionNames().add("Split1");
        root.getFirstLevelBandDefinitionNames().add("Split2");

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/print-spaces.pdf");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/print-spaces.xlsx", "./modules/core/test/smoketest/print-spaces.xlsx", ReportOutputType.pdf), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

}
