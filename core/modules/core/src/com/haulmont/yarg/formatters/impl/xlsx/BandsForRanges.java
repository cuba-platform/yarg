package com.haulmont.yarg.formatters.impl.xlsx;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.haulmont.yarg.structure.BandData;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class BandsForRanges {
    protected BiMap<BandData, Range> bandsToTemplateRanges = HashBiMap.create();
    protected BiMap<BandData, Range> bandsToResultRanges = HashBiMap.create();

    public void add(BandData bandData, Range template, Range result) {
        bandsToTemplateRanges.forcePut(bandData, template);
        bandsToResultRanges.forcePut(bandData, result);
    }

    public BandData bandForResultRange(Range result) {
        return bandsToResultRanges.inverse().get(result);
    }

    public Range resultForBand(BandData bandData) {
        return bandsToResultRanges.get(bandData);
    }

    public Range templateForBand(BandData bandData) {
        return bandsToTemplateRanges.get(bandData);
    }
}
