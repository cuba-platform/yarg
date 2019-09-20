import com.haulmont.yarg.annotations.*;

import java.text.SimpleDateFormat;
import java.util.*;
import com.haulmont.yarg.structure.BandRow;

@Report("SampleReport")
@ReportTemplate(path = "templates/template.docx", outputType = "docx", isDefault = true)
public class SampleReport {
    //REPORTS specific begin
    @Autowire
    protected Persistence persistence;
    //REPORTS specific end


    //YARG specific begin
    @Autowire
    protected JdbcTemplate jdbcTemplate;
    //YARG specific end

    @ReportParameter(name = "User", required = true)
    protected String user;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    @ReportFormat("date")
    protected String format(Date date) {
        SimpleDateFormat sd = new SimpleDateFormat("dd.MM.YYYY");
        return sd.format(date);
    }

    @ReportBand("header")
    public Map<String, Object> header() {
        Map<String, Object> result = new HashMap<>();
        result.put("header", "Header");
        result.put("user", user);

        return result;
    }

    @ReportBand("profits")
    public List<Map<String, Object>> profitByMonths() {
        List<Map<String, Object>> result = new ArrayList<>();

        Map<String, Object> map1 = new HashMap<>();
        map1.put("month", "October");
        map1.put("profit", 5466);

        Map<String, Object> map2 = new HashMap<>();
        map2.put("month", "March");
        map2.put("profit", 4687);

        result.add(map1);
        result.add(map2);

        return result;
    }

    @ReportBand("authors")
    public List<Entity> authors() {
        return persistence.getEntityManager().createQuery("select e from lib$Authors e").getResultList();
    }

    @ReportBand(name = "books", parent = "authors")
    public List<Entity> books(BandRow<Author> authorRow) {
        return persistence.getEntityManager().createQuery("select e from lib$Book e where e.author.id = :author")
                .setParameter("author", authorRow.getOriginData().getId())
                .getResultList();
    }

    @ReportBand("users")
    public List<Map<String, Object>> users() {
        return jdbcTemplate.queryForList("select NAME, LOGIN from SEC_USER WHERE LOGIN = ?", new Object[]{user});
    }

    //perhaps in the feature
    @ReportBand("users1")
    public SqlRowSet users1() {
        return jdbcTemplate.queryForRowSet("select NAME, LOGIN from SEC_USER");
    }


    public static void main(String[] argv) {
        SampleReport report = new SampleReport();
        report.setUser(user);
        result = reportingAPI.runReport(report);
    }
}