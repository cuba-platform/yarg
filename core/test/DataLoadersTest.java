import com.haulmont.newreport.structure.impl.BandOrientation;
import com.haulmont.newreport.loaders.impl.GroovyDataLoader;
import com.haulmont.newreport.loaders.impl.SqlDataDataLoader;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.impl.DataSetImpl;
import com.haulmont.newreport.util.groovy.DefaultScriptingImpl;
import junit.framework.Assert;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */

public class DataLoadersTest {
    @org.junit.Test
    public void testSqlLoader() throws Exception {
        TestDatabase testDatabase = new TestDatabase();
        testDatabase.setUpDatabase();

        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("startDate", new Timestamp(new Date().getTime()));
            params.put("start", "login%");
            SqlDataDataLoader sqlDataDataLoader = new SqlDataDataLoader(testDatabase.getDs());
            Band rootBand = new Band("band1", null, BandOrientation.HORIZONTAL);
            rootBand.setData(Collections.<String, Object>emptyMap());

            List<Map<String, Object>> result = sqlDataDataLoader.loadData(
                    new DataSetImpl("", "select login, password from user where create_ts > ${startDate} and login like ${start} limit 10", "sql"), rootBand, params);
            printResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
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
        Band rootBand = new Band("band1", null, BandOrientation.HORIZONTAL);
        rootBand.setData(Collections.<String, Object>emptyMap());

        List<Map<String, Object>> result = groovyDataLoader.loadData(
                new DataSetImpl("", "return [['a':123, 'b':321], ['a':456, 'b':654]]", "groovy")
                , rootBand, Collections.<String, Object>emptyMap());
        printResult(result);
    }

}
