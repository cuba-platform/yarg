package com.haulmont.yarg.server;

import com.haulmont.yarg.util.properties.DefaultPropertiesLoader;
import com.haulmont.yarg.util.properties.PropertiesLoader;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static com.haulmont.yarg.console.ConsoleRunner.PROPERTIES_PATH;

public class ServerRunner {
    private static final String SERVER_PORT = "port";
    private static final String REPORTS_DIRECTORY = "dir";

    protected static Logger logger = LoggerFactory.getLogger(ServerRunner.class);

    public static void main(String[] args) throws IOException {
        Options options = createOptions();

        CommandLineParser parser = new PosixParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);

            PropertiesLoader propertiesLoader = new DefaultPropertiesLoader(
                    cmd.getOptionValue(PROPERTIES_PATH, DefaultPropertiesLoader.DEFAULT_PROPERTIES_PATH));
            int port = Integer.valueOf(cmd.getOptionValue(SERVER_PORT, "4567"));
            String reportsDirectory = cmd.getOptionValue(REPORTS_DIRECTORY, "./");


            Server server = new Server()
                    .propertiesLoader(propertiesLoader)
                    .port(port)
                    .reportsPath(reportsDirectory);
            server.init();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("An error occurred while starting the reporting server", e);
            System.exit(1);
        }
    }

    private static Options createOptions() {
        Options options = new Options();
        options.addOption(PROPERTIES_PATH, true, "reporting properties path");
        options.addOption(SERVER_PORT, true, "reporting server port");
        options.addOption(REPORTS_DIRECTORY, true, "reports directory");
        return options;
    }
}
