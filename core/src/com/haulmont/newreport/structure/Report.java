/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import java.util.List;
import java.util.Map;

public interface Report {
    String getName();

    Map<String, ReportTemplate> getReportTemplates();

    BandDefinition getRootBandDefinition();

    List<ReportParameter> getReportParameters();

    List<ReportValueFormat> getReportValueFormats();
}
