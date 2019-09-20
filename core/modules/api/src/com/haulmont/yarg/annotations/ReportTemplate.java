package com.haulmont.yarg.annotations;

public @interface ReportTemplate {
    String code() default "";

    String path() default "";

    String outputType();

    boolean isDefault() default false;
}
