/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Degtyarjov
 * Created: 20.09.13 15:08
 *
 * $Id$
 */
package com.haulmont.yarg.reporting;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.Report;

import java.util.Map;

/**
 * This class should load data using ReportQuery objects, convert data onto BandData object and build BandData object tree (link children and parent bands)
 * The default implementation is com.haulmont.yarg.reporting.DataExtractorImpl
 * !Attention! Please make sure if you really need to change this behaviour against default implementation cause it might crash report generation logic
 */
public interface DataExtractor {
    void extractData(Report report, Map<String, Object> params, BandData rootBand);
}
