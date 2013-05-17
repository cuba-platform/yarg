package com.haulmont.newreport.util.properties;

import java.io.IOException;
import java.util.Properties;

public interface PropertiesLoader {
    Properties load() throws IOException;
}
