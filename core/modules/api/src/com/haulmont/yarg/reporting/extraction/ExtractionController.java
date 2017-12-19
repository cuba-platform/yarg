package com.haulmont.yarg.reporting.extraction;

import com.haulmont.yarg.structure.BandData;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This interface implementation should contains data extraction logic
 *
 * <p>The default implementation is <b>com.haulmont.yarg.reporting.extraction.controller.DefaultExtractionController</b></p>
 */
@FunctionalInterface
public interface ExtractionController {

    /**
     * Method should presents controller logic for data extraction and band tree traversal logic
     * @param context contains band, parent band data and params
     * @return list of loaded and wrapped for formatting data
     */
    List<BandData> extract(ExtractionContext context);

    /**
     * <p>Method may presents specific logic for for data extraction without traverse</p>
     * ex: data extraction for root band
     * com.haulmont.yarg.reporting.DataExtractorImpl#extractData
     *
     * @param context - should contains band, parent band data and params
     * @return list of loaded data
     */
    default List<Map<String, Object>> extractData(ExtractionContext context) {
        return Collections.emptyList();
    }

}
