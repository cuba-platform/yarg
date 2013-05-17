package com.haulmont.newreport.util.properties;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultPropertiesLoader implements PropertiesLoader {
    private String propertiesPath = "./reporting.properties";

    public DefaultPropertiesLoader() {
    }

    public DefaultPropertiesLoader(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public Properties load() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(propertiesPath));
        properties.putAll(System.getProperties());
        return properties;
    }

}
