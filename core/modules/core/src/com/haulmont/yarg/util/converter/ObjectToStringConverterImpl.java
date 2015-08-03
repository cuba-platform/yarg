package com.haulmont.yarg.util.converter;

import com.haulmont.yarg.exception.ReportingException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ObjectToStringConverterImpl extends AbstractObjectToStringConverter {
    public static final SimpleDateFormat DEFAULT_DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm");
    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    protected SimpleDateFormat dateTimeFormat = DEFAULT_DATETIME_FORMAT;
    protected SimpleDateFormat dateFormat = DEFAULT_DATE_FORMAT;

    @Override
    public String convertToString(Class parameterClass, Object paramValue) {
        if (paramValue == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return (String) paramValue;
        } else if (Date.class.isAssignableFrom(parameterClass)) {
            return dateTimeFormat.format(paramValue);
        }

        return String.valueOf(paramValue);
    }

    @Override
    public Object convertFromString(Class parameterClass, String paramValueStr) {
        if (paramValueStr == null) {
            return null;
        } else if (String.class.isAssignableFrom(parameterClass)) {
            return paramValueStr;
        } else if (java.sql.Date.class.isAssignableFrom(parameterClass)) {
            Date date = parseDate(paramValueStr);
            return new java.sql.Date(date.getTime());
        } else if (java.sql.Timestamp.class.isAssignableFrom(parameterClass)) {
            Date date = parseDate(paramValueStr);
            return new java.sql.Timestamp(date.getTime());
        } else if (Date.class.isAssignableFrom(parameterClass)) {
            return parseDate(paramValueStr);
        } else {
            return convertFromStringUnresolved(parameterClass, paramValueStr);
        }
    }

    private Date parseDate(String paramValueStr) {
        try {
            return dateTimeFormat.parse(paramValueStr);
        } catch (ParseException e) {
            try {
                return dateFormat.parse(paramValueStr);
            } catch (ParseException e1) {
                throw new ReportingException(
                        String.format("Couldn't read date from value [%s]. Date format should be [%s].",
                                paramValueStr, dateTimeFormat.toPattern()));
            }
        }
    }
}
