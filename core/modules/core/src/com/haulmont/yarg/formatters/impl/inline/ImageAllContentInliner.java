/*
 * Copyright 2019 Haulmont
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

package com.haulmont.yarg.formatters.impl.inline;

import com.haulmont.yarg.exception.ReportFormattingException;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageAllContentInliner extends AbstractInliner {

    private static final String URL = "url";
    private static final String BYTEARRAY = "bytearray";

    private static final String REGULAR_EXPRESSION = "\\$\\{img:(url|bytearray):(.*)\\}";

    public ImageAllContentInliner() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    @Override
    protected byte[] getContent(Object paramValue, Matcher matcher) {
        String type = matcher.group(1);
        switch (type) {
            case URL: {
                try {
                    return IOUtils.toByteArray(new URL(paramValue.toString()).openStream());
                } catch (IOException e) {
                    throw new ReportFormattingException("Unable to get image from " + paramValue, e);
                }
            }
            case BYTEARRAY: {
                return (byte[]) paramValue;
            }
            default: {
                throw new ReportFormattingException("Invalid picture type " + type);
            }
        }
    }

    @Override
    public Pattern getTagPattern() {
        return tagPattern;
    }
}
