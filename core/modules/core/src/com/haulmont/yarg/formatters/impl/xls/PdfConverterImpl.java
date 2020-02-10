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

package com.haulmont.yarg.formatters.impl.xls;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.doc.OfficeOutputStream;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeTask;
import com.sun.star.lang.XComponent;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class PdfConverterImpl implements PdfConverter {
    protected static final Logger log = LoggerFactory.getLogger(PdfConverterImpl.class);

    private static final String XLS_TO_PDF_OUTPUT_FILE = "calc_pdf_Export";
    private static final String ODT_TO_PDF_OUTPUT_FILE = "writer_pdf_Export";

    protected OfficeIntegrationAPI officeIntegration;

    public PdfConverterImpl(OfficeIntegrationAPI officeIntegration) {
        this.officeIntegration = officeIntegration;
    }

    @Override
    public void convertToPdf(FileType fileType, final byte[] documentBytes, final OutputStream outputStream) {
        String convertPattern = FileType.SPREADSHEET == fileType ? XLS_TO_PDF_OUTPUT_FILE : ODT_TO_PDF_OUTPUT_FILE;
        try {
            doConvertToPdf(convertPattern, documentBytes, outputStream);
        } catch (Exception e) {
            retryPdfConversion(convertPattern, documentBytes, outputStream, e, 0);
        }
    }

    protected void retryPdfConversion(final String pattern,
                                      final byte[] documentBytes,
                                      final OutputStream outputStream,
                                      Exception lastTryException,
                                      int retriesCount) {
        if (officeIntegration.getCountOfRetry() != 0 && retriesCount < officeIntegration.getCountOfRetry()) {
            log.warn(String.format("An error occurred while converting to pdf. " +
                    "System will retry to convert again (Current attempt: %s).", retriesCount + 1));
            log.debug(ExceptionUtils.getStackTrace(lastTryException));
            try {
                Thread.sleep(officeIntegration.getRetryIntervalMs());

                doConvertToPdf(pattern, documentBytes, outputStream);
            } catch (InterruptedException e) {
                throw new ReportingException("Unable to convert to pdf. Retry interrupted", e);
            } catch (Exception e) {
                retryPdfConversion(pattern, documentBytes, outputStream, e, ++retriesCount);
            }
        } else {
            if (lastTryException instanceof NoFreePortsException)
                throw (NoFreePortsException) lastTryException;

            throw new ReportingException("Unable to convert to pdf. All attempts failed", lastTryException);
        }
    }

    private void doConvertToPdf(final String convertPattern, final byte[] documentBytes, final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = new OfficeTask() {
            @Override
            public void processTaskInOpenOffice(OfficeResourceProvider ooResourceProvider) {
                try {
                    XComponent xComponent = ooResourceProvider.loadXComponent(documentBytes);
                    saveAndClose(ooResourceProvider, xComponent, outputStream, convertPattern);
                } catch (Exception e) {
                    throw new ReportingException("An error occurred while running task in Open Office server", e);
                }
            }
        };
        officeIntegration.runTaskWithTimeout(officeTask, officeIntegration.getTimeoutInSeconds());
    }

    private void saveAndClose(OfficeResourceProvider ooResourceProvider, XComponent xComponent, OutputStream outputStream, String filterName) throws com.sun.star.io.IOException {
        OfficeOutputStream officeOutputStream = new OfficeOutputStream(outputStream);
        ooResourceProvider.saveXComponent(xComponent, officeOutputStream, filterName);
        ooResourceProvider.closeXComponent(xComponent);
    }
}