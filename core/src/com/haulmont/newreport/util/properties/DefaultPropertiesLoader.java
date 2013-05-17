package com.haulmont.newreport.util.properties;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class DefaultPropertiesLoader implements PropertiesLoader {
    protected String propertiesPath = "./reporting.properties";
    protected Properties properties;
    protected final Object lock = new Object();

    public DefaultPropertiesLoader() {
    }

    public DefaultPropertiesLoader(String propertiesPath) {
        this.propertiesPath = propertiesPath;
    }

    public Properties load() throws IOException {
        synchronized (lock) {
            if (properties == null) {
                properties = new Properties();
                properties.load(new FileInputStream(propertiesPath));
                properties.putAll(System.getProperties());
                return properties;
            } else {
                return properties;
            }
        }
    }
}
