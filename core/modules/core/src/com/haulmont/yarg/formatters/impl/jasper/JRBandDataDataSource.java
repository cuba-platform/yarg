package com.haulmont.yarg.formatters.impl.jasper;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.structure.BandData;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

import java.util.*;

/**
 * Provides bypass of BandData tree.
 */
public class JRBandDataDataSource implements JRDataSource {
    protected BandData root;
    protected BandData currentBand;
    protected Iterator<BandData> currentIterator;
    protected Map<BandData, Iterator<BandData>> visitedBands = new HashMap<>();

    /**
     * Accepts root element.
     * Goes down one level because root must not have elements.
     *
     * @param root of the tree
     */
    public JRBandDataDataSource(BandData root) {
        this.root = root;
        List<BandData> children = root.getChildrenList();
        currentIterator = children.iterator();
        visitedBands.put(root, currentIterator);
        currentBand = root;
    }

    /**
     * Maintains visitedBands to continue bypass on the same
     * level after return from deeper level of hierarchy.
     * Creates iterator for each level.
     */
    @Override
    public boolean next() throws JRException {
        List<BandData> children = currentBand.getChildrenList();

        if (children != null && !children.isEmpty() && !visitedBands.containsKey(currentBand)) {
            currentIterator = children.iterator();
            visitedBands.put(currentBand, currentIterator);
        } else if (currentIterator == null) {
            currentIterator = Collections.singletonList(currentBand).iterator();
        }

        if (currentIterator.hasNext()) {
            currentBand = currentIterator.next();
            if (currentBand.getData().isEmpty())
                return next();

            return true;
        } else {
            BandData parentBand = currentBand.getParentBand();
            currentBand = parentBand;
            currentIterator = visitedBands.get(parentBand);

            if (currentIterator.hasNext())
                return next();

            if (parentBand == null || parentBand.equals(root))
                return false;

            return next();
        }
    }

    @Override
    public Object getFieldValue(JRField jrField) throws JRException {
        Object value = null;

        if (currentBand != null && currentBand.getData() != null) {
            value = currentBand.getData().get(jrField.getName());
        }

        return value;
    }

    /**
     * Search for first level children band with specified name
     * and return new datasource with this band as root element.
     */
    public JRBandDataDataSource subDataSource(String bandName) {
        List<BandData> childrenList = root.getChildrenList();
        Iterator<BandData> iterator = childrenList.iterator();

        while (iterator.hasNext()) {
            BandData bandData = iterator.next();

            if (bandData.getName().equals(bandName)) {
                visitedBands.put(bandData, iterator);
                return new JRBandDataDataSource(bandData);
            }
        }

        throw new ReportFormattingException("Cannot create sub data source with band: " + bandName);
    }
}
