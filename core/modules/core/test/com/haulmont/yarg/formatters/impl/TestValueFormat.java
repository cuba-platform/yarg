package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.formatters.impl.ValueFormat;

/**
 * Created by degtyarjov on 31.01.2018.
 */
public class TestValueFormat implements ValueFormat {
    @Override
    public String format(Object o) {
        return "Test";
    }
}
