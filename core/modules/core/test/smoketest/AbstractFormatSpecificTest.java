package smoketest;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.impl.ReportFieldFormatImpl;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public abstract class AbstractFormatSpecificTest {
    protected String openOfficePath = System.getenv("YARG_OPEN_OFFICE_PATH");

    public AbstractFormatSpecificTest() {
        if (StringUtils.isBlank(openOfficePath)) {
            openOfficePath = "C:/Program Files (x86)/OpenOffice 4/program";
        }
    }

    protected BandData createRootBand() {
        return createRootBand(null);
    }

    protected BandData createRootBand(List<BandData> bands) {
        BandData root = new BandData("Root", null, BandOrientation.HORIZONTAL);
        root.addReportFieldFormats(Arrays.<ReportFieldFormat>asList(new ReportFieldFormatImpl("Root.param1", "%16s")));

        HashMap<String, Object> rootData = new HashMap<String, Object>();
        rootData.put("param1", "AAAAAA");
        root.setData(rootData);
        BandData band1_1 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData band1_2 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData band1_3 = new BandData("Band1", root, BandOrientation.HORIZONTAL);
        BandData footer = new BandData("Footer", root, BandOrientation.HORIZONTAL);
        BandData split = new BandData("Split", root, BandOrientation.HORIZONTAL);
        split.setData(new HashMap<String, Object>());

        Map<String, Object> datamap = new HashMap<String, Object>();
        datamap.put("col1", 111);
        datamap.put("col2", "<html><body><b>html text</b></body></html>");
        datamap.put("col3", 333);
        datamap.put("col.nestedCol", null);
        datamap.put("col.nestedBool", null);
        datamap.put("cwidth", 10000);
        band1_1.setData(datamap);

        Map<String, Object> datamap2 = new HashMap<String, Object>();
        datamap2.put("col1", 444);
        datamap2.put("col2", "<html><body><b>html text</b></body></html>");
        datamap2.put("col3", 666);
        datamap2.put("col.nestedCol", "NESTED1");
        datamap2.put("col.nestedBool", false);
        datamap2.put("cwidth", 10000);
        band1_2.setData(datamap2);

        Map<String, Object> datamap3 = new HashMap<String, Object>();
        datamap3.put("col1", 777);
        datamap3.put("col2", "<html><body><b>html text</b></body></html>");
        datamap3.put("col3", 999);
        datamap3.put("col.nestedCol", "NESTED2");
        datamap3.put("col.nestedBool", true);
        datamap3.put("cwidth", 10000);
        band1_3.setData(datamap3);

        BandData band2_1 = new BandData("Band2", root, BandOrientation.HORIZONTAL);
        BandData band2_2 = new BandData("Band2", root, BandOrientation.HORIZONTAL);

        Map<String, Object> datamap4 = new HashMap<String, Object>();
        datamap4.put("col1", 111);
        datamap4.put("col2", 222);
        datamap4.put("col3", 333);
        datamap4.put("col4", 444);
        band2_1.setData(datamap4);

        Map<String, Object> datamap5 = new HashMap<String, Object>();
        datamap5.put("col1", 555);
        datamap5.put("col2", 666);
        datamap5.put("col3", 777);
        datamap5.put("col4", 888);
        band2_2.setData(datamap5);

        Map<String, Object> datamap6 = new HashMap<String, Object>();
        datamap6.put("col1", 123);
        datamap6.put("col2", 456);
        datamap6.put("col3", 789);
        footer.setData(datamap6);

        if (bands != null) {
            for (BandData band : bands) {
                root.addChild(band);
                band.setParentBand(root);
            }
        }

        root.addChild(band1_1);
        root.addChild(band1_2);
        root.addChild(band1_3);
        root.addChild(band2_1);
        root.addChild(band2_2);
        root.addChild(split);
        root.addChild(footer);
        root.setFirstLevelBandDefinitionNames(new HashSet<String>());
        root.getFirstLevelBandDefinitionNames().add("Band1");
        root.getFirstLevelBandDefinitionNames().add("Band2");
        root.getFirstLevelBandDefinitionNames().add("Split");
        root.getFirstLevelBandDefinitionNames().add("Footer");


        root.addReportFieldFormats(Arrays.<ReportFieldFormat>asList(
                new ReportFieldFormatImpl("Root.html", "${html}"),
                new ReportFieldFormatImpl("Root.image", "${bitmap:100x100}"),
                new ReportFieldFormatImpl("Split.image", "${bitmap:100x100}")));
        try {
            root.addData("html", "<html><body><a href=\"http://localhost:8080/app\">localhost</a></body></html>");
            root.addData("image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));
            split.addData("image", FileUtils.readFileToByteArray(new File("./modules/core/test/yarg.png")));
            split.addData("date", new Date());
            split.addData("theStyle", "redDate");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return root;
    }
}
