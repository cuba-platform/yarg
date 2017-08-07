/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.console;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.factory.PropertiesSqlLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.reporting.DataExtractorImpl;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.reporting.RunParams;
import com.haulmont.yarg.structure.Report;
import com.haulmont.yarg.structure.ReportParameter;
import com.haulmont.yarg.structure.ReportTemplate;
import com.haulmont.yarg.structure.xml.XmlReader;
import com.haulmont.yarg.structure.xml.impl.DefaultXmlReader;
import com.haulmont.yarg.util.converter.ObjectToStringConverter;
import com.haulmont.yarg.util.converter.ObjectToStringConverterImpl;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import com.haulmont.yarg.util.properties.DefaultPropertiesLoader;
import com.haulmont.yarg.util.properties.PropertiesLoader;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class ConsoleRunner {
    public static final String PROPERTIES_PATH = "prop";
    public static final String REPORT_PATH = "rp";
    public static final String OUTPUT_PATH = "op";
    public static final String TEMPLATE_CODE = "tc";
    public static final String REPORT_PARAMETER = "P";
    public static volatile boolean doExitWhenFinished = true;

    protected static ObjectToStringConverter converter = new ObjectToStringConverterImpl();

    public static void main(String[] args) {
        Options options = createOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);


            if (!cmd.hasOption(REPORT_PATH) || !cmd.hasOption(OUTPUT_PATH)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("report", options);
                System.exit(-1);
            }

            String templateCode = cmd.getOptionValue(TEMPLATE_CODE, ReportTemplate.DEFAULT_TEMPLATE_CODE);
            PropertiesLoader propertiesLoader = new DefaultPropertiesLoader(
                    cmd.getOptionValue(PROPERTIES_PATH, DefaultPropertiesLoader.DEFAULT_PROPERTIES_PATH));

            Reporting reporting = createReportingEngine(propertiesLoader);

            XmlReader xmlReader = new DefaultXmlReader();
            Report report = xmlReader.parseXml(FileUtils.readFileToString(new File(cmd.getOptionValue(REPORT_PATH))));
            Map<String, Object> params = parseReportParams(cmd, report);

            reporting.runReport(new RunParams(report)
                            .templateCode(templateCode)
                            .params(params),
                    new FileOutputStream(cmd.getOptionValue(OUTPUT_PATH)));
            if (doExitWhenFinished) {
                System.exit(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (doExitWhenFinished) {
                System.exit(-1);
            }
        }
    }

    private static Map<String, Object> parseReportParams(CommandLine cmd, Report report) {
        if (cmd.hasOption(REPORT_PARAMETER)) {
            Map<String, Object> params = new HashMap<String, Object>();
            Properties optionProperties = cmd.getOptionProperties(REPORT_PARAMETER);
            for (ReportParameter reportParameter : report.getReportParameters()) {
                String paramValueStr = optionProperties.getProperty(reportParameter.getAlias());
                if (paramValueStr != null) {
                    params.put(reportParameter.getAlias(),
                            converter.convertFromString(reportParameter.getParameterClass(), paramValueStr));
                }
            }

            return params;
        } else {
            return Collections.emptyMap();
        }
    }

    private static Reporting createReportingEngine(PropertiesLoader propertiesLoader) throws IOException {
        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();

        Reporting reporting = new Reporting();
        Properties properties = propertiesLoader.load();
        String openOfficePath = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_PATH);
        String openOfficePorts = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_PORTS);
        if (StringUtils.isNotBlank(openOfficePath) && StringUtils.isNotBlank(openOfficePorts)) {
            String[] portsStr = openOfficePorts.split("[,|]");
            Integer[] ports = new Integer[portsStr.length];
            for (int i = 0, portsStrLength = portsStr.length; i < portsStrLength; i++) {
                String str = portsStr[i];
                ports[i] = Integer.valueOf(str);
            }

            OfficeIntegration officeIntegration = new OfficeIntegration(openOfficePath, ports);
            formatterFactory.setOfficeIntegration(officeIntegration);

            String openOfficeTimeout = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_TIMEOUT);
            if (StringUtils.isNotBlank(openOfficeTimeout)) {
                officeIntegration.setTimeoutInSeconds(Integer.valueOf(openOfficeTimeout));
            }

            String displayDeviceAvailable = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_DISPLAY_DEVICE_AVAILABLE);
            if (StringUtils.isNotBlank(displayDeviceAvailable)) {
                officeIntegration.setDisplayDeviceAvailable(Boolean.valueOf(displayDeviceAvailable));
            }
        }

        reporting.setFormatterFactory(formatterFactory);
        SqlDataLoader sqlDataLoader = new PropertiesSqlLoaderFactory(propertiesLoader).create();
        GroovyDataLoader groovyDataLoader = new GroovyDataLoader(new DefaultScriptingImpl());
        JsonDataLoader jsonDataLoader = new JsonDataLoader();

        DefaultLoaderFactory loaderFactory = new DefaultLoaderFactory()
                .setSqlDataLoader(sqlDataLoader)
                .setGroovyDataLoader(groovyDataLoader)
                .setJsonDataLoader(jsonDataLoader);
        reporting.setLoaderFactory(loaderFactory);

        String putEmptyRowIfNoDataSelected = properties.getProperty(PropertiesLoader.CUBA_REPORTING_PUT_EMPTY_ROW_IF_NO_DATA_SELECTED);
        DataExtractorImpl dataExtractor = new DataExtractorImpl(loaderFactory);
        dataExtractor.setPutEmptyRowIfNoDataSelected(Boolean.parseBoolean(putEmptyRowIfNoDataSelected));
        reporting.setDataExtractor(dataExtractor);

        if (sqlDataLoader != null) {
            DatasourceHolder.dataSource = sqlDataLoader.getDataSource();
        }
        return reporting;
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(PROPERTIES_PATH, true, "reporting properties path");
        options.addOption(REPORT_PATH, true, "target report path");
        options.addOption(OUTPUT_PATH, true, "output document path");
        options.addOption(TEMPLATE_CODE, true, "template code");
        OptionBuilder
                .withArgName("parameter=value")
                .hasOptionalArgs()
                .withValueSeparator()
                .withDescription("report parameter");
        Option reportParam = OptionBuilder.create(REPORT_PARAMETER);
        options.addOption(reportParam);
        return options;
    }
}