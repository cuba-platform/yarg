package com.haulmont.yarg.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.structure.ReportFieldFormat;

public class ReportFieldFormatImpl implements ReportFieldFormat {
    protected String name;
    protected String format;

    public ReportFieldFormatImpl(String name, String format) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(name, "\"format\" parameter can not be null");

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
