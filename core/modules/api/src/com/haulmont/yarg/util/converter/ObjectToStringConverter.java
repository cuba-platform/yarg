package com.haulmont.yarg.util.converter;

/**
 * Converts parameters from string to object and from object to string
 */
public interface ObjectToStringConverter {
    String convertToString(Class parameterClass, Object paramValue);

    Object convertFromString(Class parameterClass, String paramValueStr);
}
