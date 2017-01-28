package com.haulmont.yarg.formatters.impl.xlsx.hints;

import com.haulmont.yarg.structure.BandData;
import org.xlsx4j.sml.Cell;

import java.util.List;

public interface XlsxHint {

    String getName();

    void add(Cell templateCell, Cell resultCell, BandData bandData, List<String> params);

    void apply();
}
