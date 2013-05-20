package com.haulmont.newreport.util.properties;

import java.io.IOException;
import java.util.Properties;

public interface PropertiesLoader {
    String CUBA_REPORTING_SQL_DRIVER = "cuba.reporting.sql.driver";
    String CUBA_REPORTING_SQL_DB_URL = "cuba.reporting.sql.dbUrl";
    String CUBA_REPORTING_SQL_USER = "cuba.reporting.sql.user";
    String CUBA_REPORTING_SQL_PASSWORD = "cuba.reporting.sql.password";
    String CUBA_REPORTING_OPENOFFICE_PATH = "cuba.reporting.openoffice.path";
    String CUBA_REPORTING_OPENOFFICE_PORTS = "cuba.reporting.openoffice.ports";
    String CUBA_REPORTING_OPENOFFICE_TIMEOUT = "cuba.reporting.openoffice.timeout";

    Properties load() throws IOException;
}
