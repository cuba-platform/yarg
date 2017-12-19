package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.reporting.DataExtractor;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportBand;

import java.util.Map;

/**
 * Default extraction context implementation
 */
public class DefaultExtractionContextFactory implements ExtractionContextFactory {

    protected DataExtractor dataExtractor;

    public DefaultExtractionContextFactory(DataExtractor dataExtractor) {
        this.dataExtractor = dataExtractor;
    }

    @Override
    public ExtractionContext context(ReportBand band, BandData parentBand, Map<String, Object> params) {
        return new ExtractionContextImpl(dataExtractor, band, parentBand, params);
    }
}
