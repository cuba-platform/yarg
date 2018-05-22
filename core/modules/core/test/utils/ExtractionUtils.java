/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package utils;

import com.haulmont.yarg.reporting.DataExtractor;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportBand;
import com.haulmont.yarg.structure.ReportQuery;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Assert;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtractionUtils {
    private ExtractionUtils() {}

    public static DataExtractor emptyExtractor() {
        return (report, data, params)-> {};
    }

    public static Map<String, Object> getParams(ReportBand band) {
        if (band == null) return Collections.emptyMap();
        return Stream.concat(
                Optional.ofNullable(band.getReportQueries()).orElse(Collections.emptyList()).stream()
                        .map(ReportQuery::getAdditionalParams),
                Optional.ofNullable(band.getChildren()).orElse(Collections.emptyList()).stream()
                        .map(ExtractionUtils::getParams))
                .filter(Objects::nonNull)
                .reduce((map1, map2)-> Stream.concat(map1.entrySet().stream(), map2.entrySet().stream())
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2)-> e1)))
                .orElse(Collections.emptyMap());
    }

    public static void checkHeader(Collection<BandData> bandDataCollection, int expected, String... headerFields) {
        Assert.assertNotNull(bandDataCollection);
        Assert.assertEquals(1, bandDataCollection.size());
        BandData bandData = bandDataCollection.iterator().next();
        Assert.assertTrue(CollectionUtils.isNotEmpty(bandData.getChildrenList()));
        Assert.assertEquals(expected, bandData.getChildrenList().size());

        bandData.getChildrenList().forEach(childData-> {
            Assert.assertNotNull(childData);
            Assert.assertNotNull(childData.getData());
            Stream.of(headerFields).forEach(key-> Assert.assertTrue(childData.getData().containsKey(key)));
        });
    }

    public static void checkMasterData(Collection<BandData> bandDataCollection, int expectedMasterDataCount, int expectedCrossDataCount, String... fields) {
        Assert.assertNotNull(bandDataCollection);
        Assert.assertEquals(expectedMasterDataCount, bandDataCollection.size());

        bandDataCollection.forEach(bandData-> {
            Assert.assertTrue(MapUtils.isNotEmpty(bandData.getData()));
            Assert.assertTrue(CollectionUtils.isNotEmpty(bandData.getChildrenList()));
            Assert.assertEquals(bandData.getChildrenList().size(), expectedCrossDataCount);

            if (fields.length > 0) {
                Assert.assertTrue(bandData.getData().containsKey(fields[0]));
            }
            if (fields.length > 1) {
                Assert.assertTrue(bandData.getData().containsKey(fields[1]));
            }

            bandData.getChildrenList().forEach(childData-> {
                Assert.assertNotNull(childData);
                Assert.assertNotNull(childData.getData());
            });
            if (fields.length > 2) {
                List<String> childFields = Arrays.asList(fields).subList(2, fields.length - 1);
                Assert.assertTrue(bandData.getChildrenList().stream().anyMatch(childData-> {
                    for (String field : childFields) {
                        if (!childData.getData().containsKey(field)) return false;
                    }
                    return true;
                }));
            }
        });
    }
}
