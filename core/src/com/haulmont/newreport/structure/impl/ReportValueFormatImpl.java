package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.ReportValueFormat;

public class ReportValueFormatImpl implements ReportValueFormat {
    protected String name;
    protected String format;

    public ReportValueFormatImpl(String name, String format) {
        this.name = name;
        this.format = format;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getFormat() {
        return format;
    }
}
