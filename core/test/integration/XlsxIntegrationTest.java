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
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.BandOrientation;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.junit.Test;
import org.xlsx4j.sml.Cell;
import org.xlsx4j.sml.Row;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
                new ReportTemplateImpl(null, "./test/integration/test.xlsx", "./test/integration/test.xlsx", ReportOutputType.xlsx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);

        compareFiles("./result/integration/result.xlsx", "./test/integration/etalon.xlsx");
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
                        Assert.assertEquals(resultCell.getF().getValue(), etalonCell.getF().getValue());
                    } else {
                        Assert.assertEquals(resultCell.getV(), etalonCell.getV());
                    }
                }
            }
        }

    }
}
