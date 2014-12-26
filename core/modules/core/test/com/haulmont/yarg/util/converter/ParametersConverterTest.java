package com.haulmont.yarg.util.converter;

import com.haulmont.yarg.structure.BandOrientation;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class ParametersConverterTest {
    ParametersConverter parametersConverter = new ParametersConverterImpl();

    @Test
    public void testNumeric() throws Exception {
        Object converted = convertFromString(Short.class, convertToString(Short.class, 42));
        Assert.assertEquals((short) 42, converted);
        converted = convertFromString(Integer.class, convertToString(Integer.class, 42));
        Assert.assertEquals(42, converted);
        converted = convertFromString(Long.class, convertToString(Long.class, 42));
        Assert.assertEquals(42L, converted);
        converted = convertFromString(Float.class, convertToString(Float.class, 42));
        Assert.assertEquals(42.0f, converted);
        converted = convertFromString(Double.class, convertToString(Double.class, 42));
        Assert.assertEquals(42.0, converted);
        converted = convertFromString(BigInteger.class, convertToString(BigInteger.class, 42));
        Assert.assertEquals(new BigInteger("42"), converted);
        converted = convertFromString(BigDecimal.class, convertToString(BigDecimal.class, 42));
        Assert.assertEquals(new BigDecimal("42"), converted);
    }

    @Test
    public void testEnum() throws Exception {
        Object converted = convertFromString(BandOrientation.class, convertToString(BandOrientation.class, BandOrientation.HORIZONTAL));
        Assert.assertEquals(BandOrientation.HORIZONTAL, converted);
    }

    @Test
    public void testDate() throws Exception {
        Date date = ParametersConverterImpl.DEFAULT_DATE_FORMAT.parse("01/01/2014 00:00");
        Object converted = convertFromString(Date.class, convertToString(Date.class, date));
        Assert.assertEquals(date, converted);
    }

    private String convertToString(Class aClass, Object value) {return parametersConverter.convertToString(aClass, value);}

    private Object convertFromString(Class aClass, String value) {return parametersConverter.convertFromString(aClass, value);}
}
