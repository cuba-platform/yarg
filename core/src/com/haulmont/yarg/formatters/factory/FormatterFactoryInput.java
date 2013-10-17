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

/**
 *
 * @author degtyarjov
 * @version $Id: FormatterFactoryInput.java 11666 2013-05-16 17:33:03Z degtyarjov $
 */
package com.haulmont.yarg.formatters.factory;

import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportTemplate;

import java.io.OutputStream;

public class FormatterFactoryInput {

    protected String templateExtension;
    protected BandData rootBand;
    protected ReportTemplate reportTemplate;
    protected OutputStream outputStream;

    public FormatterFactoryInput(String templateExtension, BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        if (templateExtension == null) {
            throw new NullPointerException("templateExtension can not be null");
        }

        if (rootBand == null) {
            throw new NullPointerException("rootBand can not be null");
        }

        this.templateExtension = templateExtension;
        this.rootBand = rootBand;
        this.reportTemplate = reportTemplate;
        this.outputStream = outputStream;
    }
}
