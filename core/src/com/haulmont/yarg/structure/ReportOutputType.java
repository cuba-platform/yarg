/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 12.05.2010 10:01:06
 *
 * $Id: ReportOutputType.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.yarg.structure;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReportOutputType implements Serializable {
    public final static ReportOutputType xls = new ReportOutputType("xls");
    public final static ReportOutputType doc = new ReportOutputType("doc");
    public final static ReportOutputType docx = new ReportOutputType("docx");
    public final static ReportOutputType html = new ReportOutputType("html");
    public final static ReportOutputType pdf = new ReportOutputType("pdf");
    public final static ReportOutputType csv = new ReportOutputType("csv");
    public final static ReportOutputType custom = new ReportOutputType("custom");

    private static Map<String, ReportOutputType> typeMap = new ConcurrentHashMap<String, ReportOutputType>();

    static {
        typeMap.put(xls.id, xls);
        typeMap.put(doc.id, doc);
        typeMap.put(docx.id, docx);
        typeMap.put(html.id, html);
        typeMap.put(pdf.id, pdf);
        typeMap.put(csv.id, csv);
        typeMap.put(custom.id, custom);
    }

    public static void registerOutputType(ReportOutputType outputType) {
        typeMap.put(outputType.id, outputType);

    }

    public static ReportOutputType getOutputTypeById(String id) {
        return typeMap.get(id);
    }

    public ReportOutputType(String id) {
        this.id = id;
    }

    private final String id;

    public String getId() {
        return id;
    }
}