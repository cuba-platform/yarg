/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.formatters.CustomReport;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.xlsx.Range;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.ReportTemplate;
import org.junit.Assert;
import org.junit.Test;
import org.xlsx4j.sml.CTCellFormula;
import org.xlsx4j.sml.Cell;

import java.io.InputStream;

public class XlsxFormulaUpdateTest {
    private ReportTemplate reportTemplate = new ReportTemplate() {
        @Override
        public String getCode() {
            return null;
        }

        @Override
        public String getDocumentName() {
            return null;
        }

        @Override
        public String getDocumentPath() {
            return null;
        }

        @Override
        public InputStream getDocumentContent() {
            return null;
        }

        @Override
        public ReportOutputType getOutputType() {
            return null;
        }

        @Override
        public String getOutputNamePattern() {
            return null;
        }

        @Override
        public boolean isGroovy() {
            return false;
        }

        @Override
        public boolean isCustom() {
            return false;
        }

        @Override
        public CustomReport getCustomReport() {
            return null;
        }
    };

    //todo test formulas grows
    @Test
    public void testFormulaShifts() throws Exception {
        XlsxFormatter xlsxFormatter = new XlsxFormatter(
                new FormatterFactoryInput("xls", new BandData(""), reportTemplate, null));

        Cell cellWithFormula = cellWithFormula("SUM(A9:A9)/B9");
        xlsxFormatter.updateFormula(cellWithFormula, Range.fromRange("Sheet", "A9:A9"), Range.fromRange("Sheet", "A90:A90"));
        xlsxFormatter.updateFormula(cellWithFormula, Range.fromRange("Sheet", "B9:B9"), Range.fromRange("Sheet", "B90:B90"));
        Assert.assertEquals("SUM(A90:A90)/B90", cellWithFormula.getF().getValue());

        cellWithFormula = cellWithFormula("SUM(A9:B9)");
        xlsxFormatter.updateFormula(cellWithFormula, Range.fromRange("Sheet", "A9:B9"), Range.fromRange("Sheet", "A90:B90"));
        Assert.assertEquals("SUM(A90:B90)", cellWithFormula.getF().getValue());

        cellWithFormula = cellWithFormula("A9*SUM(A9:A9)");
        xlsxFormatter.updateFormula(cellWithFormula, Range.fromRange("Sheet", "A9:A9"), Range.fromRange("Sheet", "A90:A90"));
        Assert.assertEquals("A90*SUM(A90:A90)", cellWithFormula.getF().getValue());

        cellWithFormula = cellWithFormula("CA9*SUM(A9:A9)");
        xlsxFormatter.updateFormula(cellWithFormula, Range.fromRange("Sheet", "A9:A9"), Range.fromRange("Sheet", "A90:A90"));
        Assert.assertEquals("CA9*SUM(A90:A90)", cellWithFormula.getF().getValue());
    }

    private Cell cellWithFormula(String formula) {
        Cell cellWithFormula = new Cell();
        CTCellFormula ctCellFormula = new CTCellFormula();
        ctCellFormula.setValue(formula);
        cellWithFormula.setF(ctCellFormula);
        return cellWithFormula;
    }
}