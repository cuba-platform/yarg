package com.haulmont.yarg.formatters.impl.jasper;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.functions.annotations.FunctionCategory;

import java.io.ByteArrayInputStream;

@FunctionCategory()
public class CubaJRFunction {
    protected JRBandDataDataSource mainDs;

    public CubaJRFunction(JRDataSource ds) {
        mainDs = (JRBandDataDataSource) ds;
    }

    public JRDataSource dataset(String name){
        return mainDs.subDataSource(name);
    }
    public ByteArrayInputStream bitmap(Object imageByteArr){
        return new java.io.ByteArrayInputStream((byte[]) imageByteArr);
    }
}