/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import com.haulmont.yarg.structure.impl.BandOrientation;

import java.util.List;

/**
 * This interface describes Band abstraction. Band is description of some data.
 * Bands have tree structure - parent has several children, they also can have children, etc.
 */
public interface ReportBand {
    String getName();

    ReportBand getParent();

    List<ReportBand> getChildren();

    List<ReportQuery> getReportQueries();

    /**
     * @return band orientation. Relevant only for Xls and Xlsx templates.
     */
    BandOrientation getBandOrientation();
}
