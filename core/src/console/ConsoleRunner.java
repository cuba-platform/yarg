/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package console;

import com.haulmont.newreport.formatters.factory.DefaultFormatterFactory;
import com.haulmont.newreport.formatters.impl.doc.connector.OfficeIntegration;
import com.haulmont.newreport.loaders.factory.DefaultLoaderFactory;
import com.haulmont.newreport.loaders.factory.PropertiesSqlLoaderFactory;
import com.haulmont.newreport.loaders.impl.GroovyDataLoader;
import com.haulmont.newreport.reporting.Reporting;
import com.haulmont.newreport.reporting.RunParams;
import com.haulmont.newreport.structure.Report;
import com.haulmont.newreport.structure.ReportTemplate;
import com.haulmont.newreport.structure.xml.XmlReader;
import com.haulmont.newreport.structure.xml.impl.DefaultXmlReader;
import com.haulmont.newreport.util.groovy.DefaultScriptingImpl;
import com.haulmont.newreport.util.properties.DefaultPropertiesLoader;
import com.haulmont.newreport.util.properties.PropertiesLoader;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

public class ConsoleRunner {
    public static final String PROPERTIES_PATH = "prop";
    public static final String REPORT_PATH = "rp";
    public static final String OUTPUT_PATH = "op";
    public static final String TEMPLATE_CODE = "tc";

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption(PROPERTIES_PATH, true, "reporting properties path");
        options.addOption(REPORT_PATH, true, "target report path");
        options.addOption(OUTPUT_PATH, true, "output document path");
        options.addOption(TEMPLATE_CODE, true, "template code");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);


            if (!cmd.hasOption(REPORT_PATH) || !cmd.hasOption(OUTPUT_PATH)) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("report", options);
                System.exit(-1);
            }

            PropertiesLoader propertiesLoader = cmd.hasOption(PROPERTIES_PATH) ? new DefaultPropertiesLoader(cmd.getOptionValue(PROPERTIES_PATH)) : new DefaultPropertiesLoader();
            String templateCode = cmd.hasOption(TEMPLATE_CODE) ? cmd.getOptionValue(TEMPLATE_CODE) : ReportTemplate.DEFAULT_TEMPLATE_CODE;

            XmlReader xmlReader = new DefaultXmlReader();
            Report report = xmlReader.parseXml(FileUtils.readFileToString(new File(cmd.getOptionValue(REPORT_PATH))));

            Reporting reporting = new Reporting();
            DefaultFormatterFactory formatterFactory = new DefaultFormatterFactory();
            Properties properties = propertiesLoader.load();
            String openOfficePath = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_PATH);
            String openOfficePorts = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_PORTS);
            if (StringUtils.isNotBlank(openOfficePath) && StringUtils.isNotBlank(openOfficePorts)) {
                String[] portsStr = openOfficePorts.split(",");
                Integer[] ports = new Integer[portsStr.length];
                for (int i = 0, portsStrLength = portsStr.length; i < portsStrLength; i++) {
                    String str = portsStr[i];
                    ports[i] = Integer.valueOf(str);
                }

                OfficeIntegration officeIntegration = new OfficeIntegration(openOfficePath, ports);
                formatterFactory.setOfficeIntegration(officeIntegration);

                String openOfficeTimeout = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_DISPLAY_DEVICE_AVAILABLE);
                if (StringUtils.isNotBlank(openOfficeTimeout)) {
                    officeIntegration.setTimeoutInSeconds(Integer.valueOf(openOfficeTimeout));
                }

                String displayDeviceAvailable = properties.getProperty(PropertiesLoader.CUBA_REPORTING_OPENOFFICE_DISPLAY_DEVICE_AVAILABLE);
                if (StringUtils.isNotBlank(displayDeviceAvailable)) {
                    officeIntegration.setDisplayDeviceAvailable(Boolean.valueOf(displayDeviceAvailable));
                }
            }

            reporting.setFormatterFactory(formatterFactory);
            reporting.setLoaderFactory(
                    new DefaultLoaderFactory()
                            .setSqlDataLoader(new PropertiesSqlLoaderFactory(propertiesLoader).create())
                            .setGroovyDataLoader(new GroovyDataLoader(new DefaultScriptingImpl())));

            reporting.runReport(new RunParams(report).templateCode(templateCode), new FileOutputStream(cmd.getOptionValue(OUTPUT_PATH)));
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
