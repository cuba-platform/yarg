/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package integration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.impl.BandOrientation;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class XlsIntegrationTest {

    @Test
    public void testFormulas() throws Exception {
        BandData root = createRootBandForFormulas();

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-with-formulas.xls");

        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl("", "smoketest/test.xls", "./test/integration/test-with-formulas.xls", ReportOutputType.xls), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./test/integration/etalon-with-formulas.xls", "./result/integration/result-with-formulas.xls");
    }

    @Test
    public void testAggregations() throws Exception {
        BandData root = createRootBandForAggregation();

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-with-aggregation.xls");

        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xls", root,
                new ReportTemplateImpl("", "smoketest/test.xls", "./test/integration/test-with-aggregation.xls", ReportOutputType.xls), outputStream));

        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./test/integration/etalon-with-aggregation.xls", "./result/integration/result-with-aggregation.xls");
    }

    private void compareFiles(String etalonFile, String resultFile) throws IOException {
        HSSFWorkbook result = new HSSFWorkbook(FileUtils.openInputStream(new File(etalonFile)));
        HSSFWorkbook etalon = new HSSFWorkbook(FileUtils.openInputStream(new File(resultFile)));

        HSSFSheet resultSheet = result.getSheetAt(0);
        HSSFSheet etalonSheet = etalon.getSheetAt(0);

        for (int row = 0; row < 10; row++) {
            HSSFRow resultRow = resultSheet.getRow(row);
            HSSFRow etalonRow = etalonSheet.getRow(row);
            if (resultRow == null && etalonRow == null) {
                continue;
            } else if ((resultRow == null) || (etalonRow == null)) {
                Assert.fail("fail on row [" + row + "]");
            }

            for (int cell = 0; cell < 10; cell++) {
                HSSFCell resultCell = resultRow.getCell(cell);
                HSSFCell etalonCell = etalonRow.getCell(cell);

                if (resultCell != null && etalonCell != null) {
                    Assert.assertEquals(String.format("fail on cell [%d,%d]", row, cell), etalonCell.getNumericCellValue(), resultCell.getNumericCellValue());
                } else if ((resultCell == null && etalonCell != null) || (resultCell != null)) {
                    Assert.fail(String.format("fail on cell [%d,%d]", row, cell));
                }
            }
        }
    }

    private BandData createRootBandForFormulas() {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);
        BandData band1_1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData band1_2 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData band1_3 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData footer = new BandData("Footer", root, BandOrientation.HORIZONTAL);

        Map<String, Object> datamap = new HashMap<String, Object>();
        datamap.put("col1", 1);
        datamap.put("col2", 2);
        datamap.put("col3", 3);
        band1_1.setData(datamap);

        Map<String, Object> datamap2 = new HashMap<String, Object>();
        datamap2.put("col1", 4);
        datamap2.put("col2", 5);
        datamap2.put("col3", 6);
        band1_2.setData(datamap2);

        Map<String, Object> datamap3 = new HashMap<String, Object>();
        datamap3.put("col1", 7);
        datamap3.put("col2", 8);
        datamap3.put("col3", 9);
        band1_3.setData(datamap3);

        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(band1_3);
        root.addChild(footer);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");

        return root;
    }

    private BandData createRootBandForAggregation() {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);

        BandData band1_1 = band(1, 2, BandOrientation.HORIZONTAL, null, "Band1");
        BandData band2_1 = band(11, 22, BandOrientation.HORIZONTAL, null, "Band2");
        BandData band2_2 = band(12, 23, BandOrientation.HORIZONTAL, null, "Band2");
        band1_1.addChildren(Arrays.asList(band2_1, band2_2));

        BandData band1_2 = band(2, 3, BandOrientation.HORIZONTAL, null, "Band1");
        BandData band2_3 = band(13, 24, BandOrientation.HORIZONTAL, null, "Band2");
        BandData band3_1 = band(111, null, BandOrientation.VERTICAL, band2_3, "Band3");
        BandData band3_2 = band(222, null, BandOrientation.VERTICAL, band2_3, "Band3");
        band1_2.addChildren(Arrays.asList(band2_3));
        band2_3.addChildren(Arrays.asList(band3_1, band3_2));

        BandData band1_3 = band(3, 4, BandOrientation.HORIZONTAL, null, "Band1");


        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(band1_3);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");

        return root;
    }

    private BandData band(int col1, Integer col2, BandOrientation orientation, BandData parentBand, String name) {
        BandData band1_1 = new BandData(name, parentBand, orientation);
        Map<String, Object> datamap = new HashMap<String, Object>();
        datamap.put("col1", col1);
        datamap.put("col2", col2);
        band1_1.setData(datamap);
        return band1_1;
    }

}
