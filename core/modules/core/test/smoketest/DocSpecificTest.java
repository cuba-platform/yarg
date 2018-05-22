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
package smoketest;

import com.haulmont.yarg.formatters.ReportFormatter;
import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

public class DocSpecificTest extends AbstractFormatSpecificTest{
    @Test
    public void testTableOdt() throws Exception {
        BandData root = new BandData("Root");
        root.setData(new RandomMap());
        BandData ship = new BandData("Ship", root);
        ship.setData(new RandomMap());
        BandData corrosion = new BandData("Corrosion", root);
        corrosion.setData(new RandomMap());
        BandData custom = new BandData("Custom", root);
        custom.setData(new RandomMap());
        root.addChild(ship);
        root.addChild(corrosion);
        root.addChild(custom);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/table.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/table.odt", "./modules/core/test/smoketest/table.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocWithColontitulesAndHtmlPageBreak() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData letterTable = new BandData("letterTable", root, BandOrientation.HORIZONTAL);
        BandData creatorInfo = new BandData("creatorInfo", root, BandOrientation.HORIZONTAL);
        HashMap<String, Object> letterTableData = new HashMap<>();
        String html = "<html><body>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>123456712345671234567123456712345671234567123456712345" +
                    "67123456712345671234567123456712345671234567123456712345671234" +
                    "5671234567123456712345671234567123456712345671234567</td></tr>";
        }
        html += "</table>";
        html += "<br style=\"page-break-after: always\">";
        html += "<p>Second table</p>";
        html += "<table border=\"2px\">";
        for (int i = 0; i < 5; i++) {
            html += "<tr><td>1234567</td></tr>";
        }
        html += "</table>";


        html += "</body></html>";
        letterTableData.put("html", html);
        letterTable.setData(letterTableData);
        HashMap<String, Object> creatorInfoData = new HashMap<>();
        creatorInfoData.put("name", "12345");
        creatorInfoData.put("phone", "54321");
        creatorInfo.setData(creatorInfoData);
        root.addChild(letterTable);
        root.addChild(creatorInfo);
        root.getReportFieldFormats().put("letterTable.html", new ReportFieldFormatImpl("letterTable.html", "${html}"));

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/colontitules.doc");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        defaultFormatterFactory.setOfficeIntegration(new OfficeIntegration(openOfficePath, 8100));
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("odt", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/colontitules.odt", "./modules/core/test/smoketest/colontitules.odt", ReportOutputType.doc), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testParallelDoc() throws Exception {
        final CountDownLatch countDownLatch = new CountDownLatch(3);
        final DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        final OfficeIntegration officeIntegrationAPI = new OfficeIntegration(openOfficePath, 8100, 8101, 8102);
        officeIntegrationAPI.setTimeoutInSeconds(10);
        officeIntegrationAPI.setTemporaryDirPath("./result/temp/");

        defaultFormatterFactory.setOfficeIntegration(officeIntegrationAPI);
        new Thread() {
            @Override
            public void run() {
                try {
                    BandData root = createRootBand();
                    root.addReportFieldFormats(Arrays.asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));
                    BandData footer = root.getChildByName("Footer");
                    BandData footerChild = new BandData("FooterChild", footer);
                    footerChild.addData("nestedData", "NESTED_DATA");
                    footer.addChild(footerChild);
                    FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_parallel1.doc");
                    ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                            new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
                    formatter.renderDocument();

                    IOUtils.closeQuietly(outputStream);
                } catch (IOException e) {
                    Assert.fail();
                } finally {
                    countDownLatch.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    BandData root = createRootBand();
                    root.addReportFieldFormats(Arrays.asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));
                    BandData footer = root.getChildByName("Footer");
                    BandData footerChild = new BandData("FooterChild", footer);
                    footerChild.addData("nestedData", "NESTED_DATA");
                    footer.addChild(footerChild);

                    FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_parallel2.doc");
                    ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                            new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
                    formatter.renderDocument();

                    IOUtils.closeQuietly(outputStream);
                } catch (IOException e) {
                    Assert.fail();
                } finally {
                    countDownLatch.countDown();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    BandData root = createRootBand();
                    root.addReportFieldFormats(Arrays.asList(new ReportFieldFormatImpl("Band1.col2", "${html}")));
                    BandData footer = root.getChildByName("Footer");
                    BandData footerChild = new BandData("FooterChild", footer);
                    footerChild.addData("nestedData", "NESTED_DATA");
                    footer.addChild(footerChild);

                    FileOutputStream outputStream = new FileOutputStream("./result/smoke/result_parallel3.doc");
                    ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("doc", root,
                            new ReportTemplateImpl("", "./modules/core/test/smoketest/test.doc", "./modules/core/test/smoketest/test.doc", ReportOutputType.doc), outputStream));
                    formatter.renderDocument();

                    IOUtils.closeQuietly(outputStream);
                } catch (IOException e) {
                    Assert.fail();
                } finally {
                    countDownLatch.countDown();
                }
            }
        }.start();
        countDownLatch.await();
        System.out.println();
    }
}