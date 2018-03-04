package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import freemarker.template.SimpleDate;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * Created by degtyarjov on 30.01.2018.
 */
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
