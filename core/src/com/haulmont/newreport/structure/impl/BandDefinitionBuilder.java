package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.BandDefinition;
import com.haulmont.newreport.structure.DataSet;

import java.util.ArrayList;

public class BandDefinitionBuilder {
    BandDefinitionImpl bandDefinition = new BandDefinitionImpl(null, null, new ArrayList<BandDefinition>(), new ArrayList<DataSet>(), BandOrientation.HORIZONTAL);

    public BandDefinitionBuilder band(BandDefinition bandDefinition) {
        BandDefinitionImpl copyBand = new BandDefinitionImpl(bandDefinition);
        copyBand.setParentBandDefinition(this.bandDefinition);
        this.bandDefinition.getChildrenBandDefinitions().add(copyBand);
        return this;
    }

    public BandDefinitionBuilder dataSet(String name, String script, String loaderType) {
        bandDefinition.dataSets.add(new DataSetImpl(name, script, loaderType));
        return this;
    }

    public BandDefinitionBuilder name(String name) {
        bandDefinition.setName(name);
        return this;

    }

    public BandDefinitionBuilder parent(BandDefinition parent) {
        bandDefinition.setParentBandDefinition(parent);
        return this;

    }

    public BandDefinition build() {
        return bandDefinition;
    }
}
