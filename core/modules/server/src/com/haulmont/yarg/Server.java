package com.haulmont.yarg;

import com.haulmont.yarg.console.ReportEngineCreator;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportParameter;
import com.haulmont.yarg.structure.xml.XmlReader;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import com.haulmont.yarg.util.converter.ObjectToStringConverterImpl;
import com.haulmont.yarg.util.properties.DefaultPropertiesLoader;
import com.haulmont.yarg.util.properties.PropertiesLoader;
import org.apache.commons.io.FileUtils;
import spark.QueryParamsMap;
import spark.Request;
import spark.Response;
import spark.Spark;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.get;

public class Server {
    protected String reportsPath;
    protected Integer port;

    protected static ObjectToStringConverter converter = new ObjectToStringConverterImpl();

    public Server reportsPath(String reportsPath) {
        this.reportsPath = reportsPath;
        return this;
    }

    public Server port(int port) {
        this.port = port;
        return this;
    }

    public void init() throws IOException {
        if (port != null) {
            Spark.port(port);
        }

        //todo set log dir
        //todo exception handling

        initPing();

        initGenerate();
    }

    protected void initPing() {
        get("/ping", (req, res) -> "pong");
    }

    protected void initGenerate() throws IOException {
        PropertiesLoader propertiesLoader = new DefaultPropertiesLoader(DefaultPropertiesLoader.DEFAULT_PROPERTIES_PATH);
        Reporting reporting = new ReportEngineCreator().createReportingEngine(propertiesLoader);

        get("/generate", (req, res) -> {
            Report report = loadReport(req);
            Map<String, Object> params = parseParameters(req, report);
            ReportOutputDocument reportOutputDocument = reporting.runReport(new RunParams(report).params(params));
            writeResult(res, reportOutputDocument);
            return "Ok";
        });
    }

    protected Report loadReport(Request req) throws IOException {
        String reportName = req.queryParams("report");
        XmlReader xmlReader = new DefaultXmlReader();
        return xmlReader.parseXml(FileUtils.readFileToString(new File(String.format("%s/%s.xml", reportsPath, reportName))));
    }

    protected Map<String, Object> parseParameters(Request req, Report report) {
        QueryParamsMap queryParams = req.queryMap("params");
        Map<String, Object> params = new HashMap<>();

        for (ReportParameter reportParameter : report.getReportParameters()) {
            java.lang.String paramValueStr = queryParams.value(reportParameter.getAlias());
            if (paramValueStr != null) {
                params.put(reportParameter.getAlias(),
                        converter.convertFromString(reportParameter.getParameterClass(), paramValueStr));
            }
        }

        return params;
    }

    protected void writeResult(Response res, ReportOutputDocument reportOutputDocument) throws IOException {
        HttpServletResponse raw = res.raw();
        raw.setHeader("Content-Disposition", String.format("attachment; filename=\"%s\"", reportOutputDocument.getDocumentName()));
        raw.setContentLength(reportOutputDocument.getContent().length);
        raw.getOutputStream().write(reportOutputDocument.getContent());
        res.status(200);
    }
}
