package com.haulmont.yarg.server;

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
import com.haulmont.yarg.util.properties.PropertiesLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import static spark.Spark.internalServerError;

public class Server {
    protected String reportsPath;
    protected Integer port;
    protected PropertiesLoader propertiesLoader;

    protected static ObjectToStringConverter converter = new ObjectToStringConverterImpl();
    protected Logger logger = LoggerFactory.getLogger(getClass());

    public Server reportsPath(String reportsPath) {
        this.reportsPath = reportsPath;
        return this;
    }

    public Server port(int port) {
        this.port = port;
        return this;
    }

    public Server propertiesLoader(PropertiesLoader propertiesLoader) {
        this.propertiesLoader = propertiesLoader;
        return this;
    }

    public void init() throws IOException {
        if (port != null) {
            Spark.port(port);
        }

        initPing();

        initGenerate();
    }

    protected void initPing() {
        get("/ping", (req, res) -> "pong");
    }

    protected void initGenerate() throws IOException {
        Reporting reporting = new ReportEngineCreator().createReportingEngine(propertiesLoader);

        get("/generate", (req, res) -> {
            try {
                Report report = loadReport(req);
                if (report == null) {
                    res.type("application/json");
                    res.status(400);
                    return "{\"errorMessage\": " +
                            "\"Report name is not provided or could not find the report.\"}";
                }

                Map<String, Object> params = parseParameters(req, report);
                String templateCode = req.queryParams("templateCode");
                RunParams reportParams = new RunParams(report).params(params);
                if (StringUtils.isNotBlank(templateCode)) {
                    reportParams.templateCode(templateCode);
                }
                ReportOutputDocument reportOutputDocument = reporting.runReport(reportParams);
                writeResult(res, reportOutputDocument);
                return "Ok";
            } catch (Exception e) {
                logger.error(String.format("An error occurred while generating report [%s]", req.queryParams("report")), e);
                throw new RuntimeException(e);
            }
        });

        internalServerError((req, res) -> {
            res.type("application/json");
            res.status(500);
            return "{\"errorMessage\": " +
                    "\"An exception occurred while generating the report. Please see the server logs for the detailed information.\"}";
        });
    }

    protected Report loadReport(Request req) throws IOException {
        String reportName = req.queryParams("report");
        if (StringUtils.isBlank(reportName)) {
            return null;
        } else {
            XmlReader xmlReader = new DefaultXmlReader();
            return xmlReader.parseXml(FileUtils.readFileToString(new File(String.format("%s/%s.xml", reportsPath, reportName))));
        }
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
