package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportBand;

import java.util.Map;

/**
 * This interface implementation should presents extraction context dependent state
 *
 * <p>The default implementation is <b>com.haulmont.yarg.reporting.extraction.ExtractionContextImpl</b></p>
 */
public interface ExtractionContext {
    /**
     * @return boolean flag that controller should create empty data row if no report query data presented
     */
    boolean putEmptyRowIfNoDataSelected();

    /**
     * @return current processing report band
     */
    ReportBand getBand();

    /**
     * @return parent report band loaded data
     */
    BandData getParentBandData();

    /**
     * @return params for data loader
     */
    Map<String, Object> getParams();

    /**
     * Method must extend existed params with presented params map
     */
    ExtractionContext extendParams(Map<String, Object> params);

    /**
     * Method must create new version of context with new params (not extended)
     */
    ExtractionContext withParams(Map<String, Object> params);

    /**
     * Method must create new version of context with new report band and parent band data
     */
    ExtractionContext withBand(ReportBand band, BandData parentBand);

    /**
     * Method must create new version of context with parent band data
     */
    ExtractionContext withParentData(BandData parentBand);
}
