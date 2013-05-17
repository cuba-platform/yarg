/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import java.util.Collection;
import java.util.Map;

public interface Report {
    String getName();

    Map<String, ReportTemplate> getReportTemplates();

    BandDefinition getRootBandDefinition();

    Collection<ReportParameter> getReportParameters();
}
