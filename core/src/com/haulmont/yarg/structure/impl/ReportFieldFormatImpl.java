package com.haulmont.yarg.structure.impl;

import com.haulmont.yarg.structure.ReportFieldFormat;

public class ReportFieldFormatImpl implements ReportFieldFormat {
    protected String name;
    protected String format;

    public ReportFieldFormatImpl(String name, String format) {
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
