/*
 * Copyright 2024 Haulmont
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
import com.haulmont.yarg.structure.ReportTemplate;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;

public class XlsxFormaterTest {

    public static String TEMPLATE_FILE_NAME = "template.xlsx";

    @Test
    public void testRenderDocumentWithImages() throws Exception {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        BandData rootBand = new BandData("Root");
        rootBand.setFirstLevelBandDefinitionNames(new HashSet<String>() {{
            add("Root");
            add("Users");
            add("Users2");
        }});
        BandData user1Band = new BandData("Users", rootBand);
        user1Band.setData(new HashMap<String, Object>() {{
            put("id", "1");
            put("email", "mail1@example.com");
        }});
        rootBand.addChild(user1Band);
        BandData user2Band = new BandData("Users", rootBand);
        user2Band.setData(new HashMap<String, Object>() {{
            put("id", "2");
            put("email", "mail2@example.com");
        }});
        rootBand.addChild(user2Band);
        BandData user3Band = new BandData("Users", rootBand);
        user3Band.setData(new HashMap<String, Object>() {{
            put("id", "3");
            put("email", "mail3@example.com");
        }});
        rootBand.addChild(user3Band);
        BandData user4Band = new BandData("Users", rootBand);
        user4Band.setData(new HashMap<String, Object>() {{
            put("id", "4");
            put("email", "mail4@example.com");
        }});
        rootBand.addChild(user4Band);
        BandData user5Band = new BandData("Users2", rootBand);
        user5Band.setData(new HashMap<String, Object>() {{
            put("id", "5");
            put("email", "mail5@example.com");
        }});
        rootBand.addChild(user5Band);
        BandData user6Band = new BandData("Users2", rootBand);
        user6Band.setData(new HashMap<String, Object>() {{
            put("id", "6");
            put("email", "mail6@example.com");
        }});
        rootBand.addChild(user6Band);
        BandData user7Band = new BandData("Users2", rootBand);
        user7Band.setData(new HashMap<String, Object>() {{
            put("id", "7");
            put("email", "mail7@example.com");
        }});
        rootBand.addChild(user7Band);
        ReportTemplate template = new ReportTemplateImpl("XLSX", "report", "", new ByteArrayInputStream(readFile(TEMPLATE_FILE_NAME)), ReportOutputType.xlsx);
        XlsxFormatter formatter = new XlsxFormatter(new FormatterFactoryInput("xlsx", rootBand, template, ReportOutputType.xlsx, os));
        formatter.renderDocument();
        byte[] byteArray = os.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        XSSFWorkbook workbook = new XSSFWorkbook(bis);
        int cnt = 0;
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet srcSH = workbook.getSheetAt(i);
            XSSFDrawing srcDraw = srcSH.createDrawingPatriarch();
            cnt += srcDraw.getShapes().size();
        }
        Assert.assertEquals(12, cnt);
    }


    protected byte[] readFile(String fileName) throws IOException, URISyntaxException {
        URL resource = XlsxFormaterTest.class
                .getResource(fileName);
        assert resource != null;
        byte[] encoded = Files.readAllBytes(Paths.get(resource.toURI()));
        return encoded;
    }
}

