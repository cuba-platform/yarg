/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure;

import com.haulmont.newreport.structure.impl.BandOrientation;

import java.util.Collection;

public interface BandDefinition {
    String getName();

    BandDefinition getParentBandDefinition();

    Collection<BandDefinition> getChildrenBandDefinitions();

    Collection<DataSet> getDataSets();

    BandOrientation getOrientation();
}
