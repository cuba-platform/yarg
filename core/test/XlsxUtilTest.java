import com.haulmont.yarg.formatters.impl.xlsx.Range;
import com.haulmont.yarg.formatters.impl.xlsx.XlsxUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class XlsxUtilTest {
    @Test
    public void testGetNumberFromColumnValue() throws Exception {
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
