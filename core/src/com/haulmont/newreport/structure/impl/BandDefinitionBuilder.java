package com.haulmont.newreport.structure.impl;

import com.google.common.base.Preconditions;
import com.haulmont.newreport.structure.BandDefinition;
import com.haulmont.newreport.structure.DataSet;

import java.util.ArrayList;

public class BandDefinitionBuilder {
    BandDefinitionImpl bandDefinition = new BandDefinitionImpl(null, null, new ArrayList<BandDefinition>(), new ArrayList<DataSet>(), BandOrientation.HORIZONTAL);

    public BandDefinitionBuilder band(BandDefinition bandDefinition) {
        Preconditions.checkNotNull(bandDefinition, "\"bandDefinition\" parameter can not be null");
        BandDefinitionImpl copyBand = new BandDefinitionImpl(bandDefinition);
        copyBand.parentBandDefinition = this.bandDefinition;
        this.bandDefinition.childrenBandDefinitions.add(copyBand);
        return this;
    }

    public BandDefinitionBuilder dataSet(String name, String script, String loaderType) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        Preconditions.checkNotNull(script, "\"script\" parameter can not be null");
        Preconditions.checkNotNull(loaderType, "\"loaderType\" parameter can not be null");
        bandDefinition.dataSets.add(new DataSetImpl(name, script, loaderType));
        return this;
    }

    public BandDefinitionBuilder name(String name) {
        Preconditions.checkNotNull(name, "\"name\" parameter can not be null");
        bandDefinition.name = name;
        return this;

    }

    public BandDefinitionBuilder orientation(BandOrientation orientation) {
        Preconditions.checkNotNull(orientation, "\"orientation\" parameter can not be null");
        bandDefinition.orientation = orientation;
        return this;

    }

    public BandDefinition build() {
        return bandDefinition;
    }
}
