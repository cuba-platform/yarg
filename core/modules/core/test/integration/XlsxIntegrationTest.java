/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package integration;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.xlsx.Document;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.junit.Test;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.util.*;

public class XlsxIntegrationTest {
    @Test
    public void testXlsx() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData band1_1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1_1.addData("col1", 1);
        band1_1.addData("col2", 2);
        band1_1.addData("col3", 3);
        band1_1.addData("col4", 4);
        band1_1.addData("col5", 5);
        band1_1.addData("col6", 6);

        BandData band12_1 = new BandData("Band12", band1_1, BandOrientation.HORIZONTAL);
        band12_1.addData("col1", 10);
        band12_1.addData("col2", 20);
        band12_1.addData("col3", 30);

        BandData band12_2 = new BandData("Band12", band1_1, BandOrientation.HORIZONTAL);
        band12_2.addData("col1", 100);
        band12_2.addData("col2", 200);
        band12_2.addData("col3", 300);

        BandData band13_1 = new BandData("Band13", band1_1, BandOrientation.VERTICAL);
        band13_1.addData("col1", 190);
        band13_1.addData("col2", 290);

        BandData band13_2 = new BandData("Band13", band1_1, BandOrientation.VERTICAL);
        band13_2.addData("col1", 390);
        band13_2.addData("col2", 490);

        BandData band14_1 = new BandData("Band14", band1_1, BandOrientation.VERTICAL);
        band14_1.addData("col1", "v5");
        band14_1.addData("col2", "v6");

        BandData band14_2 = new BandData("Band14", band1_1, BandOrientation.VERTICAL);
        band14_2.addData("col1", "v7");
        band14_2.addData("col2", "v8");

        BandData band1_2 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1_2.addData("col1", 11);
        band1_2.addData("col2", 22);
        band1_2.addData("col3", 33);
        band1_2.addData("col4", 44);
        band1_2.addData("col5", 55);
        band1_2.addData("col6", 66);

        BandData band12_3 = new BandData("Band12", band1_2, BandOrientation.HORIZONTAL);
        band12_3.addData("col1", 40);
        band12_3.addData("col2", 50);
        band12_3.addData("col3", 60);

        BandData band12_4 = new BandData("Band12", band1_2, BandOrientation.HORIZONTAL);
        band12_4.addData("col1", 400);
        band12_4.addData("col2", 500);
        band12_4.addData("col3", 600);

        band1_1.addChild(band12_1);
        band1_1.addChild(band12_2);
        band1_1.addChild(band13_1);
        band1_1.addChild(band13_2);
        band1_1.addChild(band14_1);
        band1_1.addChild(band14_2);

        band1_2.addChild(band12_3);
        band1_2.addChild(band12_4);

        root.addChild(band1_1);
        root.addChild(band1_2);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test.xlsx", "./modules/core/test/integration/test.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result.xlsx", "./modules/core/test/integration/etalon.xlsx");
    }

    @Test
    public void testAlignmentXlsx() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData band1_1 = createBand("Band1", 1, root, BandOrientation.HORIZONTAL);
        BandData band1_2 = createBand("Band1", 2, root, BandOrientation.HORIZONTAL);
        BandData band2_1 = createBand("Band2", 1, root, BandOrientation.HORIZONTAL);
        BandData band2_2 = createBand("Band2", 2, root, BandOrientation.HORIZONTAL);
        BandData band3_1 = createBand("Band3", 1, root, BandOrientation.VERTICAL);
        BandData band3_2 = createBand("Band3", 2, root, BandOrientation.VERTICAL);
        BandData band4_1 = createBand("Band4", 1, root, BandOrientation.VERTICAL);
        BandData band4_2 = createBand("Band4", 2, root, BandOrientation.VERTICAL);

        BandData split = new BandData("Split", root, BandOrientation.HORIZONTAL);
        BandData split2 = new BandData("Split2", root, BandOrientation.HORIZONTAL);
        BandData split3 = new BandData("Split3", root, BandOrientation.HORIZONTAL);

        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(split);
        root.addChild(band2_1);
        root.addChild(band2_2);
        root.addChild(split2);
        root.addChild(band3_1);
        root.addChild(band3_2);
        root.addChild(split3);
        root.addChild(band4_1);
        root.addChild(band4_2);

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-align.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test.xlsx", "./modules/core/test/integration/test-align.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result-align.xlsx", "./modules/core/test/integration/etalon-align.xlsx");
    }

    @Test
    public void testXlsxCrosstab() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);

        BandData header = new BandData("Header", root, BandOrientation.HORIZONTAL);
        root.addChild(header);

        for (int i = 1; i <= 10; i++) {
            BandData dateHeader = new BandData("DateHeader", root, BandOrientation.VERTICAL);
            dateHeader.addData("date", "2014/04/" + i);
            root.addChild(dateHeader);
        }

        BandData dateHeader = new BandData("DateHeader", root, BandOrientation.VERTICAL);
        dateHeader.addData("date", "...");
        root.addChild(dateHeader);

        BandData band11 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band11.addData("name", "Stanley");

        BandData band12 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band12.addData("name", "Kyle");

        BandData band13 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band13.addData("name", "Eric");

        BandData band14 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band14.addData("name", "Kenney");

        BandData band15 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band15.addData("name", "Craig");

        List<BandData> bands = Arrays.asList(band11, band12, band13, band14, band15);
        root.addChildren(bands);

        for (int i = 0, bandsSize = bands.size(); i < bandsSize; i++) {
            BandData band = bands.get(i);
            for (int j = 1; j <= 10; j++) {
                BandData nested = new BandData("Band2", band, BandOrientation.VERTICAL);
                band.addChild(nested);
                nested.addData("income", new BigDecimal((i + 1) * j));
            }
        }

        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Header");
        root.getFirstLevelBandDefinitionNames().add("DateHeader");
        root.getFirstLevelBandDefinitionNames().add("Band1");

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-crosstab.xlsx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-crosstab.xlsx", "./modules/core/test/integration/test-crosstab.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
        compareFiles("./result/integration/result-crosstab.xlsx", "./modules/core/test/integration/etalon-crosstab.xlsx");
    }

    @Test
    public void testXlsxFormats() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);

        BandData header = new BandData("Header", root, BandOrientation.VERTICAL);
        BandData band = new BandData("Band", root, BandOrientation.VERTICAL);
        band.addData("number", BigDecimal.valueOf(-200015));
        band.addData("date", new Date(1440747161585l));
        band.addData("money", -113123d);
        band.addData("text", "someText");

        root.addChild(header);
        root.addChild(band);

        FileOutputStream outputStream = new FileOutputStream("./result/integration/result-formats.xlsx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("xlsx", root,
                new ReportTemplateImpl("", "./modules/core/test/integration/test-formats.xlsx",
                        "./modules/core/test/integration/test-formats.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
        compareFiles("./result/integration/result-formats.xlsx", "./modules/core/test/integration/etalon-formats.xlsx");
    }


    private BandData createBand(String name, int multiplier, BandData root, BandOrientation childOrient) {
        BandData band1_1 = new BandData(name, root, BandOrientation.HORIZONTAL);
        band1_1.addData("col1", 1 * multiplier);
        band1_1.addData("col2", 2 * multiplier);

        BandData band11_1 = new BandData(name + "1", band1_1, childOrient);
        band11_1.addData("col1", 10 * multiplier);
        band11_1.addData("col2", 20 * multiplier);

        BandData band11_2 = new BandData(name + "1", band1_1, childOrient);
        band11_2.addData("col1", 100 * multiplier);
        band11_2.addData("col2", 200 * multiplier);

        band1_1.addChild(band11_1);
        band1_1.addChild(band11_2);
        return band1_1;
    }

    private void compareFiles(String resultPath, String etalonPath) throws Docx4JException {
        Document result = Document.create(SpreadsheetMLPackage.load(new File(resultPath)));
        Document etalon = Document.create(SpreadsheetMLPackage.load(new File(etalonPath)));

        List<Document.SheetWrapper> resultWorksheets = result.getWorksheets();
        List<Document.SheetWrapper> etalonWorksheets = etalon.getWorksheets();

        for (int i = 0; i < resultWorksheets.size(); i++) {
            Document.SheetWrapper resultWorksheet = resultWorksheets.get(i);
            Document.SheetWrapper etalonWorksheet = etalonWorksheets.get(i);

            List<Row> resultRows = resultWorksheet.getWorksheet().getJaxbElement().getSheetData().getRow();
            List<Row> etalonRows = etalonWorksheet.getWorksheet().getJaxbElement().getSheetData().getRow();
            for (int j = 0, rowSize = resultRows.size(); j < rowSize; j++) {
                Row resultRow = resultRows.get(j);
                Row etalonRow = etalonRows.get(j);
                List<Cell> resultCells = resultRow.getC();
                List<Cell> etalonCells = etalonRow.getC();
                for (int i1 = 0, cSize = etalonCells.size(); i1 < cSize; i1++) {
                    Cell resultCell = resultCells.get(i1);
                    Cell etalonCell = etalonCells.get(i1);
                    if (resultCell.getF() != null) {
                        Assert.assertEquals(etalonCell.getF().getValue(), resultCell.getF().getValue());
                    } else {
                        Assert.assertEquals(etalonCell.getV(), resultCell.getV());
                    }
                }
            }
        }

    }
}
