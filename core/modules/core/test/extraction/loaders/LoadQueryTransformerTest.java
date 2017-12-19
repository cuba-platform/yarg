/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Degtyarjov
 * Created: 24.09.13 18:33
 *
 * $Id$
 */
package extraction.loaders;

import com.haulmont.yarg.loaders.impl.AbstractDbDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import junit.framework.Assert;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
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
        HashMap<String, Object> params = new HashMap<String, Object>();
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
        HashMap<String, Object> params = new HashMap<String, Object>();
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
        String query = "select id as id from user where id  =  ${param1} or id = ${param2} and id > ${param1}";
        HashMap<String, Object> params = new HashMap<String, Object>();
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

        Assert.assertEquals("param1", queryPack.getParams()[0].getValue());
        Assert.assertEquals("param2", queryPack.getParams()[1].getValue());
        Assert.assertEquals("param1", queryPack.getParams()[2].getValue());
    }

    @Test
    public void testTemplate() throws Exception {
        String query = "select id as id from user where <% if (id != null) {%>id = \\${id}<%} else {%>id is null<%}%>";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", "id");
        String newQuery = processQueryTemplate(query, null, params);
        Assert.assertEquals("select id as id from user where id = ${id}", newQuery);

        params.put("id", null);
        newQuery = processQueryTemplate(query, null, params);
        Assert.assertEquals("select id as id from user where id is null", newQuery);

        query = "select id as id from user where <% if (Root.name != null) {%>id = \\${id}<%} else {%>id is null<%}%>";
        BandData parentBand = new BandData("Root");
        Map<String, Object> bandData = new HashMap<String, Object>();
        parentBand.setData(bandData);
        bandData.put("name", "name");

        newQuery = processQueryTemplate(query, parentBand, params);
        Assert.assertEquals("select id as id from user where id = ${id}", newQuery);

        bandData.put("name", null);
        newQuery = processQueryTemplate(query, parentBand, params);
        Assert.assertEquals("select id as id from user where id is null", newQuery);
    }

    @Test
    public void testParamReordering2() throws Exception {
        String query = "select \n" +
                "u.id as initiatorId,\n" +
                "u.name as initiator,\n" +
                "count(*) as tasks,\n" +
                "count(case when c.state = ',InWork,' or c.state = ',Assigned,' then 1 end) as tasksInWork,\n" +
                "count(case when c.state = ',InControl,' or c.state = ',Completed,' then 1 end) as tasksOnControl,\n" +
                "count(case when ((c.state = ',InControl,' or c.state = ',InWork,' or c.state = ',Assigned,' or c.state = ',Completed,') \n" +
                "    and t.finish_date_plan <= ${date}) then 1 end) as overdue\n" +
                "\n" +
                "from (select distinct r.card_id from wf_card_role r where r.delete_ts is null \n" +
                "and (r.code='10-Initiator' or r.code='20-Executor' or r.code='30-Controller' or r.code='90-Observer')\n" +
                "and r.user_id = ${runs}) as r\n" +
                "join tm_task t on t.card_id = r.card_id \n" +
                "join wf_card c on t.card_id = c.id\n" +
                "join  (select card_id, user_id from wf_card_role where delete_ts is null and code='20-Executor') as execut on execut.card_id = t.card_id\n" +
                "join df_employee e on execut.user_id = e.user_id\n" +
                "join sec_user u on t.initiator_id = u.id\n" +
                "\n" +
                "where c.delete_ts is null\n" +
                "and (c.state = ',InControl,' or c.state = ',InWork,' or c.state = ',Completed,' or c.state = ',Assigned,')\n" +
                "and t.create_date <= ${date}\n" +
                "and e.user_id = ${executorId}\n" +
                "group by u.id, u.name\n" +
                "order by u.name";
        HashMap<String, Object> params = new HashMap<String, Object>();
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertFalse(queryPack.getQuery().contains("${"));
        writeParams(queryPack);

        params.put("date", "param1");
        params.put("executorId", "param2");
        params.put("runs", null);
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals(3, StringUtils.countMatches(queryPack.getQuery(), "?"));
        writeParams(queryPack);

        Assert.assertEquals("param1", queryPack.getParams()[0].getValue());
        Assert.assertEquals("param1", queryPack.getParams()[1].getValue());
        Assert.assertEquals("param2", queryPack.getParams()[2].getValue());
    }

    @Test
    public void testParamsReplacing() throws Exception {
        String query = " where id=${param1} and id = ${param2}";
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("param1", null);
        params.put("param2", "param2");
        params.put("param3", null);
        AbstractDbDataLoader.QueryPack queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where 1=1 and id = ?", queryPack.getQuery());

        query = "where id=${param1} or id = ${param2}";
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where 1=0 or id = ?", queryPack.getQuery());

        query = "where (id=${param1}         or          id = ${param2}) and id = ${param3}";
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where ( 1=0 or id = ?) and 1=1", queryPack.getQuery());

        query = "where (id like ${param1}         or          id in ${param2}) and id = ${param3}";
        params.put("param2", Arrays.asList("1", "2"));
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where ( 1=0 or id in (?,?)) and 1=1", queryPack.getQuery());

        params.put("param2", null);
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where ( 1=0 or 1=0 ) and 1=1", queryPack.getQuery());

        query = "where (${param1} like '123'         or          id in ${param2}) and id = ${param3}";
        params.put("param1", "1");
        params.put("param2", Arrays.asList("1", "2"));
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where (? like '123' or id in (?,?)) and 1=1", queryPack.getQuery());

        query = "where (${param1} like 'A' and ${param2} in ('A', 'B')) or field_name=${param3}";
        params.put("param1", "A");
        params.put("param2", null);
        params.put("param3", "1234");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where (? like 'A' and 1=1 ) or field_name=?", queryPack.getQuery());


        query = "where (${param1} like 'A' and ${param2} in (select 1 from a where a = ${param2})) or field_name=${param3}";
        params.put("param1", "param1");
        params.put("param2", null);
        params.put("param3", "param3");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where (? like 'A' and 1=1 ) or field_name=?", queryPack.getQuery());

        query = "where (${param1} like 'A' and ${param2} in (select 1 from a where a = ${param2})) or field_name=${param3}";
        params.put("param2", "param2");
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where (? like 'A' and ? in (select 1 from a where a = ?)) or field_name=?", queryPack.getQuery());

        Assert.assertEquals("param1", queryPack.getParams()[0].getValue());
        Assert.assertEquals("param2", queryPack.getParams()[1].getValue());
        Assert.assertEquals("param2", queryPack.getParams()[2].getValue());
        Assert.assertEquals("param3", queryPack.getParams()[3].getValue());

        query = "where (${param1} like 'A' and ${param2} in (1,2,3)) or field_name=${param3}";
        params.put("param1", null);
        params.put("param3", null);
        queryPack = prepareQuery(query, new BandData(""), params);
        System.out.println(queryPack.getQuery());
        Assert.assertEquals("where ( 1=1 and ? in (1,2,3)) or 1=0", queryPack.getQuery());
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
