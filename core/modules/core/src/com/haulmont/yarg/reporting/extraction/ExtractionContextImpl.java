package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.reporting.DataExtractor;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportBand;

import java.util.Collections;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default data extraction context implementation
 */
public class ExtractionContextImpl implements ExtractionContext {
    protected DataExtractor extractor;
    protected ReportBand band;
    protected BandData parentBand;
    protected Map<String, Object> params;

    public ExtractionContextImpl(DataExtractor extractor, ReportBand band, BandData parentBand, Map<String, Object> params) {
        checkNotNull(extractor);
        checkNotNull(band);
        checkNotNull(params);

        this.extractor = extractor;
        this.band = band;
        this.parentBand = parentBand;
        this.params = params;
    }

    public boolean putEmptyRowIfNoDataSelected() {
        return extractor.getPutEmptyRowIfNoDataSelected();
    }

    public ReportBand getBand() {
        return band;
    }

    public BandData getParentBandData() {
        return parentBand;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }

    public ExtractionContextImpl extendParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

    public ExtractionContextImpl withParams(Map<String, Object> params) {
        return new ExtractionContextImpl(extractor, band, parentBand, params);
    }

    @Override
    public ExtractionContext withBand(ReportBand band, BandData parentBand) {
        return new ExtractionContextImpl(extractor, band, parentBand, params);
    }

    public ExtractionContextImpl withParentData(BandData parentBand) {
        return new ExtractionContextImpl(extractor, band, parentBand, params);
    }
}
