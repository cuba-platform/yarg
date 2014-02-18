package loaders;

import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.impl.BandOrientation;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.structure.impl.ReportQueryImpl;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import com.jayway.jsonpath.JsonPath;
import junit.framework.Assert;
import net.minidev.json.JSONObject;
import org.junit.Test;
import utils.TestDatabase;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class DataLoadersTest {

    @Test
    public void testSqlLoader() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("startDate", new Timestamp(new Date().getTime()));
            params.put("start", "login%");
            SqlDataLoader sqlDataLoader = new SqlDataLoader(testDatabase.getDs());
            BandData rootBand = new BandData("band1", null, BandOrientation.HORIZONTAL);
            rootBand.setData(Collections.<String, Object>emptyMap());

            List<Map<String, Object>> result = sqlDataLoader.loadData(
                    new ReportQueryImpl("", "select login as \"Login\", password as \"Password\" from user where create_ts > ${startDate} and login like ${start} limit 10", "sql", null, null), rootBand, params);
            printResult(result);
            Assert.assertNotNull(result.get(0).get("Login"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        testDatabase.stop();
    }

    @Test
    public void testCaseSensitiveSqlLoader() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("startDate", new Timestamp(new Date().getTime()));
            params.put("start", "login%");
            SqlDataLoader sqlDataLoader = new SqlDataLoader(testDatabase.getDs());
            BandData rootBand = new BandData("band1", null, BandOrientation.HORIZONTAL);
            rootBand.setData(Collections.<String, Object>emptyMap());

            List<Map<String, Object>> result = sqlDataLoader.loadData(
                    new ReportQueryImpl("", "select login as Login, password as Password from user where create_ts > ${startDate} and login like ${start} limit 10", "sql", null, null), rootBand, params);
            printResult(result);
            Assert.assertNotNull(result.get(0).get("Login"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        testDatabase.stop();
    }

    private void printResult(List<Map<String, Object>> result) {
        for (Map<String, Object> stringObjectMap : result) {
            for (Map.Entry<String, Object> entry : stringObjectMap.entrySet()) {
                System.out.print("(" + entry.getKey() + ":" + entry.getValue() + ") ");
            }
            System.out.println();
        }
    }

    @Test
    public void testGroovyLoader() throws Exception {
        GroovyDataLoader groovyDataLoader = new GroovyDataLoader(new DefaultScriptingImpl());
        BandData rootBand = new BandData("band1", null, BandOrientation.HORIZONTAL);
        rootBand.setData(Collections.<String, Object>emptyMap());

        List<Map<String, Object>> result = groovyDataLoader.loadData(
                new ReportQueryImpl("", "return [['a':123, 'b':321], ['a':456, 'b':654]]", "groovy", null, null)
                , rootBand, Collections.<String, Object>emptyMap());
        printResult(result);
    }

    @Test
    public void testLinksInQueries() throws Exception {

    }

    @Test
    public void testJson() throws Exception {
        JsonDataLoader jsonDataLoader = new JsonDataLoader();
        BandData rootBand = new BandData("band1", null, BandOrientation.HORIZONTAL);
        rootBand.setData(Collections.<String, Object>emptyMap());
        ReportQueryImpl reportQuery = new ReportQueryImpl("", "parameter=param1 $.store.book[*]", "json", null, null);

        String json = "{ \"store\": {\n" +
                "    \"book\": [ \n" +
                "      { \"category\": \"reference\",\n" +
                "        \"author\": \"Nigel Rees\",\n" +
                "        \"title\": \"Sayings of the Century\",\n" +
                "        \"price\": 8.95\n" +
                "      },\n" +
                "      { \"category\": \"fiction\",\n" +
                "        \"author\": \"Evelyn Waugh\",\n" +
                "        \"title\": \"Sword of Honour\",\n" +
                "        \"price\": 12.99,\n" +
                "        \"isbn\": \"0-553-21311-3\"\n" +
                "      }\n" +
                "    ],\n" +
                "    \"bicycle\": {\n" +
                "      \"color\": \"red\",\n" +
                "      \"price\": 19.95\n" +
                "    }\n" +
                "  }\n" +
                "}";

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("param1", json);

        List<Map<String, Object>> maps = jsonDataLoader.loadData(reportQuery, rootBand, params);
        System.out.println(maps);
        Assert.assertEquals(2, maps.size());

        Map<String, Object> book1 = maps.get(0);
        Assert.assertEquals("Sayings of the Century", book1.get("title"));
    }
}
