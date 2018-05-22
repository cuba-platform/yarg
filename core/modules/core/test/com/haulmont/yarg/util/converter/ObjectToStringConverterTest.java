/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.util.converter;

import com.haulmont.yarg.structure.BandOrientation;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import static junit.framework.Assert.assertEquals;

public class ObjectToStringConverterTest {
    ObjectToStringConverter objectToStringConverter = new ObjectToStringConverterImpl();

    @Test
    public void testNumeric() throws Exception {
        Object converted = convertFromString(Short.class, convertToString(Short.class, 42));
        assertEquals((short) 42, converted);
        converted = convertFromString(Integer.class, convertToString(Integer.class, 42));
        assertEquals(42, converted);
        converted = convertFromString(Long.class, convertToString(Long.class, 42));
        assertEquals(42L, converted);
        converted = convertFromString(Float.class, convertToString(Float.class, 42));
        assertEquals(42.0f, converted);
        converted = convertFromString(Double.class, convertToString(Double.class, 42));
        assertEquals(42.0, converted);
        converted = convertFromString(BigInteger.class, convertToString(BigInteger.class, 42));
        assertEquals(new BigInteger("42"), converted);
        converted = convertFromString(BigDecimal.class, convertToString(BigDecimal.class, 42));
        assertEquals(new BigDecimal("42"), converted);
    }

    @Test
    public void testEnum() throws Exception {
        Object converted = convertFromString(BandOrientation.class, convertToString(BandOrientation.class, BandOrientation.HORIZONTAL));
        assertEquals(BandOrientation.HORIZONTAL, converted);
    }

    @Test
    public void testDate() throws Exception {
        Date date = ObjectToStringConverterImpl.DEFAULT_DATETIME_FORMAT.parse("01/01/2014 00:00");
        Object converted = convertFromString(Date.class, convertToString(Date.class, date));
        assertEquals(date, converted);

        date = ObjectToStringConverterImpl.DEFAULT_DATE_FORMAT.parse("01/01/2014");
        converted = convertFromString(Date.class, convertToString(Date.class, date));
        assertEquals(date, converted);
    }

    @Test
    public void testSqlDate() throws Exception {
        Date date = new java.sql.Date(ObjectToStringConverterImpl.DEFAULT_DATETIME_FORMAT.parse("01/01/2014 00:00").getTime());
        Object converted = convertFromString(java.sql.Date.class, convertToString(java.sql.Date.class, date));
        Assert.assertTrue(converted instanceof java.sql.Date);
        assertEquals(date, converted);

        date = new java.sql.Date(ObjectToStringConverterImpl.DEFAULT_DATE_FORMAT.parse("01/01/2014").getTime());
        converted = convertFromString(java.sql.Date.class, convertToString(java.sql.Date.class, date));
        Assert.assertTrue(converted instanceof java.sql.Date);
        assertEquals(date, converted);
    }

    @Test
    public void testSqlTimestamp() throws Exception {
        Date date = new java.sql.Timestamp(ObjectToStringConverterImpl.DEFAULT_DATETIME_FORMAT.parse("01/01/2014 00:00").getTime());
        Object converted = convertFromString(java.sql.Timestamp.class, convertToString(java.sql.Timestamp.class, date));
        Assert.assertTrue(converted instanceof java.sql.Timestamp);
        assertEquals(date, converted);

        date = new java.sql.Timestamp(ObjectToStringConverterImpl.DEFAULT_DATE_FORMAT.parse("01/01/2014").getTime());
        converted = convertFromString(java.sql.Timestamp.class, convertToString(java.sql.Timestamp.class, date));
        Assert.assertTrue(converted instanceof java.sql.Timestamp);
        assertEquals(date, converted);
    }

    private String convertToString(Class aClass, Object value) {return objectToStringConverter.convertToString(aClass, value);}

    private Object convertFromString(Class aClass, String value) {return objectToStringConverter.convertFromString(aClass, value);}
}