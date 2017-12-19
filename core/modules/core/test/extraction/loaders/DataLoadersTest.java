package extraction.loaders;

import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.BandOrientation;
import com.haulmont.yarg.structure.impl.ReportQueryImpl;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import junit.framework.Assert;
import org.junit.Test;
import utils.TestDatabase;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author degtyarjov
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
                    new ReportQueryImpl("", "select login as \"Login\", password as \"Password\" " +
                            "from user " +
                            "where create_ts > ${startDate} and login like ${start} limit 10", "sql", null, null), rootBand, params);
            printResult(result);
            Assert.assertNotNull(result.get(0).get("Login"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            testDatabase.stop();
        }
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
                    new ReportQueryImpl("", "select login as Login, password as Password " +
                            "from user " +
                            "where create_ts > ${startDate} and login like ${start} limit 10", "sql", null, null), rootBand, params);
            printResult(result);
            Assert.assertNotNull(result.get(0).get("Login"));
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        } finally {
            testDatabase.stop();
        }
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

//    @Test todo
    public void testLinksInQueries() throws Exception {

    }

    @Test
    public void testJson() throws Exception {
        JsonDataLoader jsonDataLoader = new JsonDataLoader();
        BandData rootBand = new BandData("band1", null, BandOrientation.HORIZONTAL);
        rootBand.setData(new HashMap<String, Object>());
        rootBand.getData().put("searchParameter", "fiction");

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

        System.out.println(json);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("param1", json);

        List<Map<String, Object>> maps = jsonDataLoader.loadData(reportQuery, rootBand, params);
        Assert.assertEquals(2, maps.size());

        Map<String, Object> book1 = maps.get(0);
        Assert.assertEquals("Sayings of the Century", book1.get("title"));

        reportQuery = new ReportQueryImpl("", "parameter=param1 $", "json", null, null);
        maps = jsonDataLoader.loadData(reportQuery, rootBand, params);

        Map<String, Object> map = maps.get(0);

        Assert.assertEquals("red", map.get("store.bicycle.color"));

        params.put("searchParameter", "reference");
        reportQuery = new ReportQueryImpl("", "parameter=param1 $.store.book[?(@.category=='${searchParameter}')]", "json", null, null);
        maps = jsonDataLoader.loadData(reportQuery, rootBand, params);
        map = maps.get(0);
        Assert.assertEquals("reference", map.get("category"));

        reportQuery = new ReportQueryImpl("", "parameter=param1 $.store.book[?(@.category=='${band1.searchParameter}')]", "json", null, null);
        maps = jsonDataLoader.loadData(reportQuery, rootBand, params);
        map = maps.get(0);
        Assert.assertEquals("fiction", map.get("category"));

        reportQuery = new ReportQueryImpl("", "parameter=param1 $.some.not.existing", "json", null, null);
        maps = jsonDataLoader.loadData(reportQuery, rootBand, params);
        Assert.assertEquals(0, maps.size());
    }
}
