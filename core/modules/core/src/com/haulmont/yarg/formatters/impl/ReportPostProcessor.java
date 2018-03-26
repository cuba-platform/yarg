/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.structure.BandData;

/**
 * @param <T> the type of the object converted/extracted from the byte array
 *           by calling method {@link #fromByteArray(byte[] reportContent)}.
 *
 */

public interface ReportPostProcessor<T> {

    /**
     * @param source - object of any type suitable for processing converted/extracted from byte array report
     *           ( by calling method {@link #fromByteArray(byte[] reportContent)} ).
     * @return - updated report content as a byte array
     */
    byte[] postProcess(T source, BandData rootBand);

    /**
     * @param reportContent - original report content as byte array
     * @return - object of any type suitable for processing in context of a report format
     */
    T fromByteArray(byte[] reportContent);

    default byte[] postProcessReport(byte[] reportContent, BandData rootBand){
        T source = fromByteArray(reportContent);
        return postProcess(source, rootBand);
    }
}
