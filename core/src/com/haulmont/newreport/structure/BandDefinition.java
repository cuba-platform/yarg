/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import com.haulmont.newreport.structure.impl.BandOrientation;

import java.util.Collection;
import java.util.List;

public interface BandDefinition {
    String getName();

    BandDefinition getParent();

    List<BandDefinition> getChildren();

    List<DataSet> getInnerDataSets();

    BandOrientation getBandOrientation();
}
