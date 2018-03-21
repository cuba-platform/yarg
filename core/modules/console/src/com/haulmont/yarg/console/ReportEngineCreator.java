package com.haulmont.yarg.console;

import com.haulmont.yarg.formatters.factory.DefaultFormatterFactory;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.yarg.loaders.factory.DefaultLoaderFactory;
import com.haulmont.yarg.loaders.impl.GroovyDataLoader;
import com.haulmont.yarg.loaders.impl.JsonDataLoader;
import com.haulmont.yarg.loaders.impl.SqlDataLoader;
import com.haulmont.yarg.reporting.DataExtractorImpl;
import com.haulmont.yarg.reporting.Reporting;
import com.haulmont.yarg.util.groovy.DefaultScriptingImpl;
import com.haulmont.yarg.util.properties.PropertiesLoader;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Properties;

public class ReportEngineCreator {
    public Reporting createReportingEngine(PropertiesLoader propertiesLoader) throws IOException {
        DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();

        Reporting reporting = new Reporting();
        Properties properties = propertiesLoader.load();
        String openOfficePath = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_PATH);
        String openOfficePorts = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_PORTS);
        String fontsDirectory = properties.getProperty(PropertiesLoader.CUBA_REPORTING_FONTS_DIRECTORY);
        boolean openHtmlForPdfConversion = BooleanUtils.toBoolean(
                properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPEN_HTML_FOR_PDF_CONVERSION));

        formatterFactory.setFontsDirectory(fontsDirectory);
        formatterFactory.setOpenHtmlForPdfConversion(openHtmlForPdfConversion);
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
}
