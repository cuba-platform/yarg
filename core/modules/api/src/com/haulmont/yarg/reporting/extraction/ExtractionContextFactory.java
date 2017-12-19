package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportBand;

import java.util.Map;

/**
 * This interface implementation should create immutable extraction context object
 *
 * <p>The default implementation is <b>com.haulmont.yarg.reporting.extraction.DefaultExtractionContextFactory</b></p>
 */
public interface ExtractionContextFactory {
    /**
     * Method should always return new <b>immutable</b> context object
     */
    ExtractionContext context(ReportBand band, BandData parentBand, Map<String, Object> params);
}
