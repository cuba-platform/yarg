package com.haulmont.yarg.structure;

import junit.framework.Assert;
import org.junit.Test;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class BandDataTest {
    @Test
    public void testGetChildren() throws Exception {
        BandData root = createData();

        BandData band1 = root.getChildByName("Band1");
        BandData band2 = root.getChildByName("Band2");
        BandData band3 = root.getChildByName("Band3");
        Assert.assertNotNull(band1);
        Assert.assertNotNull(band2);
        Assert.assertNotNull(band3);

        BandData band11 = band1.getChildByName("Band11");
        BandData band12 = band1.getChildByName("Band12");
        BandData band13 = band1.getChildByName("Band13");
        BandData band14 = band1.getChildByName("Band14");

        Assert.assertNotNull(band11);
        Assert.assertNotNull(band12);
        Assert.assertNotNull(band13);
        Assert.assertNotNull(band14);

        BandData alsoBand1 = root.getChildrenByName("Band1").get(0);
        BandData alsoBand14 = alsoBand1.getChildrenByName("Band14").get(0);
        Assert.assertTrue(alsoBand1 == band1);
        Assert.assertTrue(alsoBand14 == band14);
    }

    @Test
    public void testFindBandsRecursively() throws Exception {
        BandData root = createData();

        BandData band1 = root.findBandRecursively("Band1");
        BandData band2 = root.findBandRecursively("Band2");
        BandData band3 = root.findBandRecursively("Band3");
        Assert.assertNotNull(band1);
        Assert.assertNotNull(band2);
        Assert.assertNotNull(band3);

        BandData band11 = root.findBandRecursively("Band11");
        BandData band12 = root.findBandRecursively("Band12");
        BandData band13 = root.findBandRecursively("Band13");
        BandData band14 = root.findBandRecursively("Band14");

        BandData band34 = root.findBandRecursively("Band34");

        Assert.assertNotNull(band11);
        Assert.assertNotNull(band12);
        Assert.assertNotNull(band13);
        Assert.assertNotNull(band14);

        Assert.assertEquals("Band1.Band11", band11.getFullName());
        Assert.assertEquals("Band3.Band34", band34.getFullName());
    }


    private BandData createData() {
        BandData root = new BandData("Root");

        BandData band1 = new BandData("Band1", root);
        BandData band2 = new BandData("Band2", root);
        BandData band3 = new BandData("Band3", root);
        root.addChild(band1);
        root.addChild(band2);
        root.addChild(band3);

        band1.addChild(new BandData("Band11", band1));
        band1.addChild(new BandData("Band12", band1));
        band1.addChild(new BandData("Band13", band1));
        band1.addChild(new BandData("Band14", band1));

        band2.addChild(new BandData("Band21", band2));
        band2.addChild(new BandData("Band22", band2));
        band2.addChild(new BandData("Band23", band2));
        band2.addChild(new BandData("Band24", band2));

        band3.addChild(new BandData("Band31", band3));
        band3.addChild(new BandData("Band32", band3));
        band3.addChild(new BandData("Band33", band3));
        band3.addChild(new BandData("Band34", band3));
        return root;
    }
}
