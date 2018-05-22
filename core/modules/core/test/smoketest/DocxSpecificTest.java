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
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import com.haulmont.yarg.structure.impl.ReportTemplateImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.FileOutputStream;
import java.util.HashMap;

public class DocxSpecificTest extends AbstractFormatSpecificTest {
    @Test
    public void testTableInTable() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);
        BandData ride = new BandData("Customer", root, BandOrientation.HORIZONTAL);
        ride.setData(new RandomMap());
        BandData cc1 = new BandData("CustomerContacts", root, BandOrientation.HORIZONTAL);
        cc1.setData(new RandomMap());
        BandData cc2 = new BandData("CustomerContacts", root, BandOrientation.HORIZONTAL);
        cc2.setData(new RandomMap());

        root.addChild(ride);
        root.addChild(cc1);
        root.addChild(cc2);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/table-in-table.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/table-in-table.docx", "./modules/core/test/smoketest/table-in-table.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxTableWithSplittedBandAlias() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);
        BandData ride = new BandData("ride", root, BandOrientation.HORIZONTAL);
        ride.setData(new RandomMap());
        root.addChild(ride);


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/splitted-aliases-in-table.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/splitted-aliases-in-table.docx", "./modules/core/test/smoketest/splitted-aliases-in-table.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testControlTables1() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);

        BandData band1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        band1.setData(new RandomMap());
        root.addChild(band1);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/control-table-hide.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/control-tables-1.docx", "./modules/core/test/smoketest/control-tables-1.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);

        BandData control = new BandData("Control1", root, BandOrientation.HORIZONTAL);
        control.setData(new RandomMap());
        root.addChild(control);

        outputStream = new FileOutputStream("./result/smoke/control-table-show.docx");
        formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/control-tables-1.docx", "./modules/core/test/smoketest/control-tables-1.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);

    }

    @Test
    public void testControlTables2() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);

        BandData mainControl = band("MainControl", root);
        BandData portfolio = randomBand("Portfolio", root);
        BandData participationsControl = band("ParticipationsControl", root);
        BandData participations = randomBand("Participations", root);
        BandData priorLearningPlaces = randomBand("PriorLearningPlaces", root);//the table should be hidden
        BandData projectActivitiesControl = band("ProjectActivitiesControl", root);
        BandData projectActivities = randomBand("ProjectActivities", root);
        BandData additionalEducations = randomBand("AdditionalEducations", root);//the table should be hidden

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/control-tables-2.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/control-tables-2.docx", "./modules/core/test/smoketest/control-tables-2.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testTableOfContents() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);

        BandData project = randomBand("Project", root);
        for (int i = 0; i < 30; i++) {
            BandData options = randomBand("Options", root);
        }

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/table-of-contents.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/table-of-contents.docx", "./modules/core/test/smoketest/table-of-contents.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);

    }

    private BandData randomBand(String name, BandData root) {
        BandData band1 = new BandData(name, root, BandOrientation.HORIZONTAL);
        band1.setData(new RandomMap());
        root.addChild(band1);
        return band1;
    }

    private BandData band(String name, BandData root) {
        BandData band1 = new BandData(name, root, BandOrientation.HORIZONTAL);
        band1.setData(new HashMap<>());
        root.addChild(band1);
        return band1;
    }

    @Test
    public void testUrl() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        rootData.put("url", "https://www.google.ru/#newwindow=1&q=YARG");
        rootData.put("urlCaption", "URL");
        root.setData(rootData);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/url.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/url.docx", "./modules/core/test/smoketest/url.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();
        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxTableWithAliasInHeader() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        root.setData(rootData);
        BandData price = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price.setData(new RandomMap());
        root.addChild(price);
        BandData price2 = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price2.setData(new RandomMap());
        root.addChild(price2);
        BandData price3 = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price3.setData(new RandomMap());
        root.addChild(price3);
        BandData price4 = new BandData("Price", root, BandOrientation.HORIZONTAL);
        price4.setData(new RandomMap());
        root.addChild(price4);
        BandData info = new BandData("Info", root, BandOrientation.HORIZONTAL);
        info.setData(new RandomMap());
        root.addChild(info);


        FileOutputStream outputStream = new FileOutputStream("./result/smoke/TemplateRateBook.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/TemplateRateBook.docx", "./modules/core/test/smoketest/TemplateRateBook.docx", ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }


    @Test
    public void testDocxWithSplittedAlias() throws Exception {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        HashMap<String, Object> rootData = new HashMap<>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData cover = new BandData("Cover", root, BandOrientation.HORIZONTAL);
        cover.setData(new HashMap<>());
        cover.addData("index", "123");
        cover.addData("volume", "321");
        cover.addData("name", "AAA");
        BandData documents = new BandData("Documents", root, BandOrientation.HORIZONTAL);
        documents.setData(new HashMap<>());
        root.addChild(cover);
        root.addChild(documents);

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/splitted-aliases.docx");
        ReportFormatter formatter = new DefaultFormatterFactory().createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/splitted-aliases.docx", "./modules/core/test/smoketest/splitted-aliases.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }

    @Test
    public void testDocxWithColontitulesAndHtmlPageBreak() throws Exception {
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

        FileOutputStream outputStream = new FileOutputStream("./result/smoke/colontitules.docx");
        DefaultFormatterFactory defaultFormatterFactory = new DefaultFormatterFactory();
        ReportFormatter formatter = defaultFormatterFactory.createFormatter(new FormatterFactoryInput("docx", root,
                new ReportTemplateImpl("", "./modules/core/test/smoketest/colontitules.docx", "./modules/core/test/smoketest/colontitules.docx",
                        ReportOutputType.docx), outputStream));
        formatter.renderDocument();

        IOUtils.closeQuietly(outputStream);
    }
}