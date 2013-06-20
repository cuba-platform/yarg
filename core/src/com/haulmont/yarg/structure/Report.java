/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import java.util.List;
import java.util.Map;

public interface Report {
    String getName();

    Map<String, ReportTemplate> getReportTemplates();

    ReportBand getRootBandDefinition();

    List<ReportParameter> getReportParameters();

    List<ReportFieldFormat> getReportFieldFormats();
}
