package com.haulmont.yarg.formatters.impl.xlsx.hints;

import com.haulmont.yarg.structure.BandData;
import org.xlsx4j.sml.Cell;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractXlsxHint implements XlsxHint {

    protected static class DataObject {
        protected final Cell templateCell;
        protected final Cell resultCell;
        protected final BandData bandData;
        protected final List<String> params;

        public DataObject(Cell templateCell, Cell resultCell, BandData bandData, List<String> params) {
            this.templateCell = templateCell;
            this.resultCell = resultCell;
            this.bandData = bandData;
            this.params = params;
        }
    }

    protected List<AbstractXlsxHint.DataObject> data = new ArrayList<AbstractXlsxHint.DataObject>();

    @Override
    public void add(Cell templateCell, Cell resultCell, BandData bandData, List<String> params) {
        data.add(new AbstractXlsxHint.DataObject(templateCell, resultCell, bandData, params));
    }
}
