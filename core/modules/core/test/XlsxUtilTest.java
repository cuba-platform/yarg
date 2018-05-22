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

import com.haulmont.yarg.formatters.impl.xlsx.Range;
import com.haulmont.yarg.formatters.impl.xlsx.XlsxUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XlsxUtilTest {
    @Test
    public void testGetNumberFromColumnValue() {
        checkReference("A", 1);
        checkReference("B", 2);
        checkReference("Z", 26);
        checkReference("AA", 27);
        checkReference("AB", 28);
    }

    private void checkReference(String ref, int num) {
        int numberFromColumnReference = XlsxUtils.getNumberFromColumnReference(ref);
        System.out.println(numberFromColumnReference);
        Assert.assertEquals(num, numberFromColumnReference);
        String columnReferenceFromNumber = XlsxUtils.getColumnReferenceFromNumber(num);
        System.out.println(columnReferenceFromNumber);
        Assert.assertEquals(ref, columnReferenceFromNumber);
    }

    @Test
    public void testRange() throws Exception {
        String rangeStr = "'Лист1'!$A$1:$C$1";
        Range range = Range.fromFormula(rangeStr);
        Assert.assertEquals(rangeStr, range.toString());
    }

    @Test
    public void testName() throws Exception {
        String str = "${col1}##width=cwidth";
        Pattern pattern = Pattern.compile("##width=([A-z0-9]+)");
        Matcher matcher = pattern.matcher(str);
        System.out.println(matcher.find());
    }
}