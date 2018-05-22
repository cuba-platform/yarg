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

import org.junit.Assert;
import org.junit.Test;

import static com.haulmont.yarg.formatters.impl.csv.SimpleSeparatorDetector.detectSeparator;

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