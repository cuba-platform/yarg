package com.haulmont.yarg.reporting.extraction.preprocessor;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.haulmont.yarg.loaders.QueryLoaderPreprocessor;
import com.haulmont.yarg.structure.ProxyWrapper;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * SQL (and JPQL also) implementation of params preprocessor
 *
 * Preprocessor searches params in sql fragment and preparing them for
 * data linking between query bands, that contains data for vertical and horizontal band distribution.
 *
 * Another function is wrapping query object to inject processed query without breaking reportQuery immutability
 */
public class SqlCrosstabPreprocessor implements QueryLoaderPreprocessor {

    private static final Pattern REF_PATTERN = Pattern.compile("(\\w+)@(.+?)\\b");
    private static final String REF_NAME = "%s_%s";

    @Override
    public List<Map<String, Object>> preprocess(ReportQuery reportQuery, Map<String, Object> params,
                           BiFunction<ReportQuery, Map<String, Object>, List<Map<String, Object>>> after) {
        Multimap<String, String> references = HashMultimap.create();
        Matcher matcher = REF_PATTERN.matcher(checkNotNull(reportQuery.getScript()));
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            references.put(matcher.group(1), matcher.group(2));
            matcher.appendReplacement(sb, String.format(REF_NAME, matcher.group(1), matcher.group(2)));
        }
        matcher.appendTail(sb);
        String processedQuery = sb.toString();
        Map<String, Object> decoratedParams = new HashMap<>(params);
        references.entries().forEach(e-> {
            List<Map<String, Object>> qValues = ObjectUtils
                    .defaultIfNull((List<Map<String, Object>>)params.get(e.getKey()),
                            Collections.singletonList(Collections.emptyMap()));
            decoratedParams.put(String.format(REF_NAME, e.getKey(), e.getValue()),
                    qValues.stream().map(data-> data.get(e.getValue()))
                            .filter(Objects::nonNull).collect(Collectors.toList()));

        });
        ReportQuery reportQueryProxy = (ReportQuery) Proxy.newProxyInstance(reportQuery.getClass().getClassLoader(),
                new Class[] { ReportQuery.class, ProxyWrapper.class },
                (proxy, method, args)-> {
                    if ("getScript".equals(method.getName())) {
                        return processedQuery;
                    } else if ("unwrap".equals(method.getName())) {
                        return reportQuery;
                    } else {
                        return method.invoke(reportQuery, args);
                    }
        });

        return after.apply(reportQueryProxy, decoratedParams);
    }
}
