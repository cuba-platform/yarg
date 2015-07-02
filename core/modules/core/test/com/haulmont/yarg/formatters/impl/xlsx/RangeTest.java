package com.haulmont.yarg.formatters.impl.xlsx;

import junit.framework.Assert;
import org.junit.Test;
import org.xlsx4j.sml.CTCellFormula;
import org.xlsx4j.sml.Cell;

import java.util.Set;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RangeTest {
    @Test
    public void testRangeFromFormula() throws Exception {
        Cell cellWithFormula = new Cell();
        CTCellFormula value = new CTCellFormula();
        value.setValue("A1*C1");
        cellWithFormula.setF(value);
        Set<Range> ranges = Range.fromCellFormula("", cellWithFormula);
        System.out.println(ranges);
        Assert.assertEquals(2, ranges.size());
        Assert.assertTrue(ranges.contains(Range.fromRange("", "A1:A1")));
        Assert.assertTrue(ranges.contains(Range.fromRange("", "C1:C1")));


        value.setValue("SUM(A1:C1)");
        ranges = Range.fromCellFormula("", cellWithFormula);
        System.out.println(ranges);
        System.out.println(ranges);
        Assert.assertEquals(1, ranges.size());
        Assert.assertTrue(ranges.contains(Range.fromRange("", "A1:C1")));
    }
}
