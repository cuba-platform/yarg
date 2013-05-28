package com.haulmont.newreport.structure.impl;

import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */

@SuppressWarnings({"UnusedDeclaration"})
public class Band {
    public static final String ROOT_BAND_NAME = "Root";

    private Map<String, Object> data;
    private final Band parentBand;

    private Map<String, List<Band>> childrenBands = new LinkedHashMap<String, List<Band>>();

    private final String name;
    private final BandOrientation orientation;
    private Set<String> firstLevelBandDefinitionNames = null;
    private int level;

    public Band(String name) {
        this.name = name;
        this.parentBand = null;
        this.orientation = BandOrientation.HORIZONTAL;
    }

    public Band(String name, Band parentBand) {
        this.name = name;
        this.parentBand = parentBand;
        this.orientation = BandOrientation.HORIZONTAL;
    }

    public Band(String name, Band parentBand, BandOrientation orientation) {
        this.name = name;
        this.parentBand = parentBand;
        this.orientation = orientation;

        Band currentBand = this;
        while (currentBand != null) {
            level++;
            currentBand = currentBand.parentBand;
        }
    }

    public Map<String, List<Band>> getChildrenBands() {
        return childrenBands;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public void addData(String name, Object value) {
        data.put(name, value);
    }

    public Object getParameterValue(String name) {
        return data.get(name);
    }

    public void addAllParameters(Map<String, Object> parameters) {
        data.putAll(parameters);
    }

    public String getName() {
        return name;
    }

    public Band getParentBand() {
        return parentBand;
    }

    public BandOrientation getOrientation() {
        return orientation;
    }

    public int getLevel() {
        return level;
    }

    public String getFullName() {
        String fullName = name;
        Band upBand = parentBand;
        while ((upBand != null) && (upBand.level > 1)) {
            fullName = upBand.getName() + "." + fullName;
            upBand = upBand.parentBand;
        }
        return fullName;
    }

    public List<Band> getChildrenList() {
        List<Band> bandList = new ArrayList<Band>();
        for (List<Band> bands : childrenBands.values())
            bandList.addAll(bands);
        return bandList;
    }

    public void addChild(Band band) {
        if (!childrenBands.containsKey(band.getName())) {
            childrenBands.put(band.getName(), new ArrayList<Band>());
        }
        List<Band> bands = childrenBands.get(band.getName());
        bands.add(band);
    }

    public void addChildren(List<Band> bands) {
        for (Band band : bands)
            addChild(band);
    }

    public Band getChildByName(String bandName) {
        if (bandName == null) {
            throw new NullPointerException("Parameter bandName can not be null.");
        }
        if (getChildrenList() != null) {
            for (Band child : getChildrenList()) {
                if (bandName.equals(child.getName())) {
                    return child;
                }
            }
        }
        return null;
    }

    public Band findBandRecursively(String name) {
        if (getName().equals(name)) {
            return this;
        }
        for (Band child : getChildrenList()) {
            Band fromChild = child.findBandRecursively(name);
            if (fromChild != null) {
                return fromChild;
            }
        }
        return null;
    }

    public List<Band> findBandsRecursively(String name) {
        Band firstBand = findBandRecursively(name);
        List<Band> allBand = firstBand.getParentBand().getChildrenByName(name);
        return allBand;
    }

    public List<Band> getChildrenByName(String bandName) {
        if (bandName == null) {
            throw new NullPointerException("Parameter bandName can not be null.");
        }

        List<Band> children = new ArrayList<Band>();
        if (getChildrenList() != null) {
            for (Band child : getChildrenList()) {
                if (bandName.equals(child.getName())) {
                    children.add(child);
                }
            }
        }
        return children;
    }

    public Set<String> getFirstLevelBandDefinitionNames() {
        return firstLevelBandDefinitionNames;
    }

    public void setFirstLevelBandDefinitionNames(Set<String> firstLevelBandDefinitionNames) {
        this.firstLevelBandDefinitionNames = firstLevelBandDefinitionNames;
    }

    public Map<String, ReportValueFormat> getValuesFormats() {
        return Collections.emptyMap();
    }

    @Override
    public String toString() {
        StringBuilder sbf = new StringBuilder();
        sbf.append(name).append(":").append(data.toString()).append("\n");
        for (Band band : getChildrenList()) {
            for (int i = 0; i < level; i++)
                sbf.append("\t");
            sbf.append(band.toString());
        }
        return sbf.toString();
    }
}

