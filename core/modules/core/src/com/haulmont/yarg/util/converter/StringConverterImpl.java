package com.haulmont.yarg.util.converter;

import com.haulmont.yarg.exception.ReportingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class StringConverterImpl extends AbstractStringConverter {
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    protected SimpleDateFormat dateFormat = DEFAULT_DATE_FORMAT;

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        if (paramValue == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return (String) paramValue;
        } else if (Date.class.isAssignableFrom(parameterClass)) {
            return dateFormat.format(paramValue);
        }

        return String.valueOf(paramValue);
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        if (String.class.isAssignableFrom(parameterClass)) {
            return paramValueStr;
        } else if (Date.class.isAssignableFrom(parameterClass)) {
            try {
                return dateFormat.parse(paramValueStr);
            } catch (ParseException e) {
                throw new ReportingException(
                        String.format("Couldn't read date from value [%s]. Date format should be [%s].",
                                paramValueStr, dateFormat.toPattern()));
            }
        } else {
            return convertFromStringUnresolved(parameterClass, paramValueStr);
        }
    }
}
