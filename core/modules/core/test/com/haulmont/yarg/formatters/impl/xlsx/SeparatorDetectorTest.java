package com.haulmont.yarg.formatters.impl.xlsx;

import org.junit.Assert;
import org.junit.Test;

import static com.haulmont.yarg.formatters.impl.csv.SimpleSeparatorDetector.detectSeparator;

/**
 * @author birin
 * @version $Id$
 */
public class SeparatorDetectorTest {
    @Test
    public void detectNoSeparatorTest(){
        String template = "asdasd";
        char separator = detectSeparator(template);
        Assert.assertEquals("Unexpected separator", ',', separator);
    }

    @Test
    public void detectCommaTest(){
        String template = "${asdas},asdasd";
        char separator = detectSeparator(template);
        Assert.assertEquals("Unexpected separator", ',', separator);
    }

    @Test
    public void detectSemicolonTest(){
        String template = "${asdas};asdasd";
        char separator = detectSeparator(template);
        Assert.assertEquals("Unexpected separator", ';', separator);
    }

    @Test
    public void detectTabTest(){
        String template = "${asdas}\tasdasd";
        char separator = detectSeparator(template);
        Assert.assertEquals("Unexpected separator", '\t', separator);
    }

    @Test
    public void detectPipeTest(){
        String template = "${asdas}|asdasd";
        char separator = detectSeparator(template);
        Assert.assertEquals("Unexpected separator", '|', separator);
    }
}
