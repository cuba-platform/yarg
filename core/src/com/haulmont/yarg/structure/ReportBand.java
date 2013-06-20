/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure;

import com.haulmont.yarg.structure.impl.BandOrientation;

import java.util.List;

public interface ReportBand {
    String getName();

    ReportBand getParent();

    List<ReportBand> getChildren();

    List<ReportQuery> getInnerDataSets();

    BandOrientation getBandOrientation();
}
