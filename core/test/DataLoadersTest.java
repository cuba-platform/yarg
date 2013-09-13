import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.impl.BandOrientation;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.structure.impl.ReportQueryImpl;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import junit.framework.Assert;
import org.junit.Test;

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
                    new ReportQueryImpl("", "select login as \"login\", password as \"password\" from user where create_ts > ${startDate} and login like ${start} limit 10", "sql", null, null), rootBand, params);
            printResult(result);
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

}
