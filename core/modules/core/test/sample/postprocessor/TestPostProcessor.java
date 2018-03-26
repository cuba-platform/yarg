/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sample.postprocessor;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.ReportPostProcessor;
import com.haulmont.yarg.structure.BandData;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.io3.Save;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.WorksheetPart;
import org.xlsx4j.jaxb.Context;
import org.xlsx4j.sml.*;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class TestPostProcessor implements ReportPostProcessor<SpreadsheetMLPackage> {

    @Override
    public byte[] postProcess(SpreadsheetMLPackage source, BandData rootBand) {

        try {
            WorksheetPart sheetCreatedByPostProcessor = source.createWorksheetPart(new PartName("/xl/worksheets/sheet5.xml"),
                                                                                "Post Process Sheet", 4);
            Worksheet sheet = sheetCreatedByPostProcessor.getContents();
            SheetData sheetData = sheet.getSheetData();

            // Create cells using parameters from rootBand
            for (Map.Entry<String, Object> param : rootBand.getData().entrySet()) {
                // Create a new row
                Row row = Context.getsmlObjectFactory().createRow();

                Cell keyCell = newCellWithInlineString(param.getKey());
                Cell valueCell = newCellWithInlineString(String.valueOf(param.getValue()));

                // Add the cell to the row
                row.getC().add(keyCell);
                row.getC().add(valueCell);

                // Add the row to the sheet
                sheetData.getRow().add(row);
            }

        } catch (InvalidFormatException | JAXBException e) {
            throw new ReportingException("Cannot create works sheet part", e);
        } catch (Docx4JException e) {
            throw new ReportingException("Cannot get works sheet", e);
        }

        Save saver = new Save(source);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            saver.save(os);
        } catch (Docx4JException e) {
            throw new ReportingException("Cannot save SpreadsheetMLPackage", e);
        }

        return os.toByteArray();
    }

    @Override
    public SpreadsheetMLPackage fromByteArray(byte[] reportContent) {
        SpreadsheetMLPackage mlPackage;
        try {
            mlPackage = SpreadsheetMLPackage.load(new ByteArrayInputStream(reportContent));
        } catch (Docx4JException e) {
            throw new ReportingException("Cannot load SpreadsheetMLPackage from byte array", e);
        }

        return mlPackage;
    }

    private Cell newCellWithInlineString(String string) {

        CTXstringWhitespace ctx = Context.getsmlObjectFactory().createCTXstringWhitespace();
        ctx.setValue(string);

        CTRst ctrst = new CTRst();
        ctrst.setT(ctx);

        Cell newCell = Context.getsmlObjectFactory().createCell();
        newCell.setIs(ctrst);
        newCell.setT(STCellType.INLINE_STR);

        return newCell;
    }
}
