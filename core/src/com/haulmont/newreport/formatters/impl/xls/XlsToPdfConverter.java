package com.haulmont.newreport.formatters.impl.xls;

import com.haulmont.newreport.formatters.impl.doc.ODTHelper;
import com.haulmont.newreport.formatters.impl.doc.OOInputStream;
import com.haulmont.newreport.formatters.impl.doc.OOOutputStream;
import com.haulmont.newreport.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.newreport.formatters.impl.doc.connector.OOResourceProvider;
import com.haulmont.newreport.formatters.impl.doc.connector.OOTaskRunnerAPI;
import com.haulmont.newreport.formatters.impl.doc.connector.OfficeTask;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.XComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;

public class XlsToPdfConverter implements XlsToPdfConverterAPI {
    protected static final Logger log = LoggerFactory.getLogger(XlsToPdfConverter.class);

    private static final String XLS_TO_PDF_OUTPUT_FILE = "calc_pdf_Export";

    protected OOTaskRunnerAPI taskRunner;

    public XlsToPdfConverter(OOTaskRunnerAPI taskRunner) {
        this.taskRunner = taskRunner;
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
                //todo handle
            }
        }
    }

    private void doConvertXlsToPdf(final byte[] documentBytes, final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = new OfficeTask() {
            @Override
            public void processTaskInOpenOffice(OOResourceProvider ooResourceProvider) {
                try {
                    XInputStream xis = new OOInputStream(documentBytes);
                    XComponentLoader xComponentLoader = ooResourceProvider.getXComponentLoader();
                    XComponent xComponent = ODTHelper.loadXComponent(xComponentLoader, xis);
                    saveAndClose(xComponent, outputStream, XLS_TO_PDF_OUTPUT_FILE);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        taskRunner.runTaskWithTimeout(officeTask, taskRunner.getTimeoutInSeconds());
    }

    private void saveAndClose(XComponent xComponent, OutputStream outputStream, String filterName) throws com.sun.star.io.IOException {
        OOOutputStream ooOutputStream = new OOOutputStream(outputStream);
        ODTHelper.saveXComponent(xComponent, ooOutputStream, filterName);
        ODTHelper.closeXComponent(xComponent);
    }
}