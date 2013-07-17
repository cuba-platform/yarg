/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import java.util.List;
import java.util.Map;

/**
 * This interface describes main report object. Contains all data about report: bands, parameters, formats.
 */
public interface Report {
    String getName();

    /**
     * @return map with report templates <templateCode, template>
     */
    Map<String, ReportTemplate> getReportTemplates();

    /**
     * @return root band which contains all others bands
     */
    ReportBand getRootBand();

    List<ReportParameter> getReportParameters();

    List<ReportFieldFormat> getReportFieldFormats();
}
