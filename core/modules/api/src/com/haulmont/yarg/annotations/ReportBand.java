package com.haulmont.yarg.annotations;

import com.haulmont.yarg.structure.BandOrientation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReportBand {

    String value() default "";

    String name() default "";

    String parent() default "";

    BandOrientation orientation() default BandOrientation.HORIZONTAL;
}

