/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.impl;

import com.haulmont.newreport.structure.BandDefinition;
import com.haulmont.newreport.structure.DataSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class BandDefinitionImpl implements BandDefinition {

    protected String name;
    protected BandDefinition parentBandDefinition;
    protected List<BandDefinition> childrenBandDefinitions = new ArrayList<BandDefinition>();
    protected List<DataSet> dataSets = new ArrayList<DataSet>();
    protected BandOrientation orientation;

    public BandDefinitionImpl(String name, BandDefinition parentBandDefinition, Collection<BandDefinition> childrenBandDefinitions, Collection<DataSet> dataSets, BandOrientation orientation) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.childrenBandDefinitions = new ArrayList<BandDefinition>(childrenBandDefinitions);
        this.dataSets = new ArrayList<DataSet>(dataSets);
        this.orientation = orientation;
    }

    public BandDefinitionImpl(String name, BandDefinition parentBandDefinition, BandOrientation orientation) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.orientation = orientation;
    }

    public BandDefinitionImpl(String name, BandDefinition parentBandDefinition) {
        this.name = name;
        this.parentBandDefinition = parentBandDefinition;
        this.orientation = BandOrientation.HORIZONTAL;
    }

    public BandDefinitionImpl(BandDefinition instanceToCopy) {
        name = instanceToCopy.getName();
        parentBandDefinition = instanceToCopy.getParent();
        childrenBandDefinitions = new ArrayList<BandDefinition>(instanceToCopy.getChildren());
        dataSets = new ArrayList<DataSet>(instanceToCopy.getInnerDataSets());
        orientation = instanceToCopy.getBandOrientation();
    }


    public String getName() {
        return name;
    }

    public BandDefinition getParent() {
        return parentBandDefinition;
    }

    public List<BandDefinition> getChildren() {
        return Collections.unmodifiableList(childrenBandDefinitions);
    }

    public List<DataSet> getInnerDataSets() {
        return Collections.unmodifiableList(dataSets);
    }

    public BandOrientation getBandOrientation() {
        return orientation;
    }

    void setParentBandDefinition(BandDefinition parentBandDefinition) {
        this.parentBandDefinition = parentBandDefinition;
    }

    void setName(String name) {
        this.name = name;
    }

    void setOrientation(BandOrientation orientation) {
        this.orientation = orientation;
    }
}
