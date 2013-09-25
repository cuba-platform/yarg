/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Degtyarjov
 * Created: 24.09.13 18:33
 *
 * $Id$
 */
package loaders;

import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import junit.framework.Assert;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoadQueryTransformerTest extends AbstractDbDataLoader {
    @Override
    public List<Map<String, Object>> loadData(ReportQuery reportQuery, BandData parentBand, Map<String, Object> params) {
        return null;
    }

    @Test
    public void testReplaceSingleCondition() throws Exception {
        String query = "select id as id from user where id  =  ${param1}";
        HashMap<String, Object> params = new HashMap<>();
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertFalse(queryPack.getQuery().contains("${"));
        writeParams(queryPack);

        params.put("param1", "param1");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(1, StringUtils.countMatches(queryPack.getQuery(), "?"));
        writeParams(queryPack);
    }

    @Test
    public void testParentBandCondition() throws Exception {
        String query = "select id as id from user where id  =  ${Root.parentBandParam}";
        HashMap<String, Object> params = new HashMap<>();
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertFalse(queryPack.getQuery().contains("${"));
        writeParams(queryPack);

        BandData parentBand = new BandData("Root");
        parentBand.setData(new HashMap<String, Object>());
        parentBand.addData("parentBandParam", "parentBandParam");
        queryPack = prepareQuery(query, parentBand, params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(1, StringUtils.countMatches(queryPack.getQuery(), "?"));
        writeParams(queryPack);
    }

    @Test
    public void testParamReordering() throws Exception {
        String query = "select id as id from user where id  =  ${param2} or id = ${param1} and id > ${param2}";
        HashMap<String, Object> params = new HashMap<>();
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertFalse(queryPack.getQuery().contains("${"));
        writeParams(queryPack);

        params.put("param1", "param1");
        params.put("param2", "param2");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(3, StringUtils.countMatches(queryPack.getQuery(), "?"));
        writeParams(queryPack);

        Assert.assertEquals("param2", queryPack.getParams()[0].getValue());
        Assert.assertEquals("param1", queryPack.getParams()[1].getValue());
        Assert.assertEquals("param2", queryPack.getParams()[2].getValue());
    }


    private void writeParams(QueryPack queryPack) {
        QueryParameter[] params1;
        params1 = queryPack.getParams();
        for (int i = 0; i < params1.length; i++) {
            QueryParameter queryParameter = params1[i];
            System.out.println(i + ":" + queryParameter.getPosition() + ":" + queryParameter.getValue());
        }
    }
}
