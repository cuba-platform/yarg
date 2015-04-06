package com.haulmont.yarg.formatters.impl.xlsx;

import com.google.common.collect.ArrayListMultimap;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RangeDependencies {
    protected ArrayListMultimap<Range, Range> rangeDependencies = ArrayListMultimap.create();

    public Set<Range> templates(){
        return rangeDependencies.keySet();
    }

    public Collection<Range> results(){
        return rangeDependencies.values();
    }

    public List<Range> resultsForTemplate(Range template){
        return rangeDependencies.get(template);
    }

    public void addDependency(Range templateRange, Range resultRange) {
        rangeDependencies.put(templateRange, resultRange);
    }
}
