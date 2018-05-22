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

import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class FormatTest {
    @Test
    public void testNull() {
        AbstractFormatter abstractFormatter = createFormatter("a.number", "##.##");
        Assert.assertEquals("", abstractFormatter.formatValue(null, "number", "a.number"));
    }

    @Test
    public void testNumber() {
        AbstractFormatter abstractFormatter = createFormatter("a.number", "##.##");
        Assert.assertEquals("5,57", abstractFormatter.formatValue(5.5678, "number", "a.number").replace(".", ","));
    }

    @Test
    public void testDate() throws ParseException {
        AbstractFormatter abstractFormatter = createFormatter("a.number", "yyyy-mm-dd");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy");
        Assert.assertEquals("2009-09-01", abstractFormatter.formatValue(simpleDateFormat.parse("01-09-2009"), "number", "a.number"));
    }

    @Test
    public void testClass() throws ParseException {
        AbstractFormatter abstractFormatter = createFormatter("a.number", "class:com.haulmont.yarg.formatters.impl.TestValueFormat");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-mm-yyyy");
        Assert.assertEquals("Test", abstractFormatter.formatValue(null, "number", "a.number"));
        Assert.assertEquals("Test", abstractFormatter.formatValue(12345, "number", "a.number"));
        Assert.assertEquals("Test", abstractFormatter.formatValue(simpleDateFormat.parse("01-09-2009"), "number", "a.number"));
    }


    private AbstractFormatter createFormatter(String formatName, String formatValue) {
        final BandData rootBand = new BandData(BandData.ROOT_BAND_NAME);
        rootBand.addReportFieldFormats(Arrays.asList(new ReportFieldFormatImpl(formatName, formatValue)));
        final FormatterFactoryInput formatterFactoryInput = new FormatterFactoryInput("xlsx", rootBand, null, ReportOutputType.xlsx, null);
        return new AbstractFormatter(formatterFactoryInput) {
            @Override
            public void renderDocument() {

            }
        };
    }
}
