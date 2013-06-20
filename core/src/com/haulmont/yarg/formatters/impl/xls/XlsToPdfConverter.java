package com.haulmont.yarg.formatters.impl.xls;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.doc.UnoHelper;
import com.haulmont.yarg.formatters.impl.doc.OfficeInputStream;
import com.haulmont.yarg.formatters.impl.doc.OfficeOutputStream;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeTask;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.XComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class XlsToPdfConverter implements XlsToPdfConverterAPI {
    protected static final Logger log = LoggerFactory.getLogger(XlsToPdfConverter.class);

    private static final String XLS_TO_PDF_OUTPUT_FILE = "calc_pdf_Export";

    protected OfficeIntegrationAPI officeIntegration;

    public XlsToPdfConverter(OfficeIntegrationAPI officeIntegration) {
        this.officeIntegration = officeIntegration;
    }

    @Override
    public void convertXlsToPdf(final byte[] documentBytes, final OutputStream outputStream) {
        try {
            doConvertXlsToPdf(documentBytes, outputStream);
        } catch (Exception e) {
            log.warn("An error occurred while converting xls to pdf. System will retry to generate report once again.", e);
            try {
                doConvertXlsToPdf(documentBytes, outputStream);
            } catch (NoFreePortsException e1) {
                throw new ReportingException("An error occurred while converting xls to pdf.", e);
            }
        }
    }

    private void doConvertXlsToPdf(final byte[] documentBytes, final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = new OfficeTask() {
            @Override
            public void processTaskInOpenOffice(OfficeResourceProvider ooResourceProvider) {
                try {
                    XInputStream xis = new OfficeInputStream(documentBytes);
                    XComponentLoader xComponentLoader = ooResourceProvider.getXComponentLoader();
                    XComponent xComponent = UnoHelper.loadXComponent(xComponentLoader, xis);
                    saveAndClose(xComponent, outputStream, XLS_TO_PDF_OUTPUT_FILE);
                } catch (Exception e) {
                    throw new ReportingException("An error occurred while running task in Open Office server", e);
                }
            }
        };
        officeIntegration.runTaskWithTimeout(officeTask, officeIntegration.getTimeoutInSeconds());
    }

    private void saveAndClose(XComponent xComponent, OutputStream outputStream, String filterName) throws com.sun.star.io.IOException {
        OfficeOutputStream officeOutputStream = new OfficeOutputStream(outputStream);
        UnoHelper.saveXComponent(xComponent, officeOutputStream, filterName);
        UnoHelper.closeXComponent(xComponent);
    }
}