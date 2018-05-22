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

package com.haulmont.yarg.formatters.impl.xlsx;

import junit.framework.Assert;
import org.junit.Test;
import org.xlsx4j.sml.CTCellFormula;
import org.xlsx4j.sml.Cell;

import java.util.Set;

public class RangeTest {
    @Test
    public void testRangeFromFormula() {
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