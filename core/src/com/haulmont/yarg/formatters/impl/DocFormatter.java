/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 12.10.2010 19:21:36
 *
 * $Id: DocFormatter.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.formatters.impl.doc.OfficeOutputStream;
import com.haulmont.yarg.formatters.impl.doc.TableManager;
import com.haulmont.yarg.formatters.impl.doc.connector.*;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.doc.OfficeComponent;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.structure.ReportTemplate;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.*;
import com.sun.star.table.XCell;
import com.sun.star.text.*;
import com.sun.star.util.XReplaceable;
import com.sun.star.util.XSearchDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.haulmont.yarg.formatters.impl.doc.UnoHelper.*;
import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.*;

/**
 * Document formatter for '.doc' file types
 */
public class DocFormatter extends AbstractFormatter {
    protected static final Logger log = LoggerFactory.getLogger(DocFormatter.class);

    protected static final String SEARCH_REGULAR_EXPRESSION = "SearchRegularExpression";

    protected static final String PDF_OUTPUT_FILE = "writer_pdf_Export";
    protected static final String MS_WORD_OUTPUT_FILE = "MS Word 97";

    protected XComponent xComponent;

    protected OfficeComponent officeComponent;

    protected OfficeIntegrationAPI officeIntegration;

    public DocFormatter(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream, OfficeIntegrationAPI officeIntegration) {
        super(rootBand, reportTemplate, outputStream);
        this.officeIntegration = officeIntegration;
        supportedOutputTypes.add(ReportOutputType.doc);
        supportedOutputTypes.add(ReportOutputType.pdf);
    }

    public void renderDocument() {
        try {
            doCreateDocument(reportTemplate.getOutputType(), outputStream);
        } catch (Exception e) {//just try again if any exceptions occurred
            log.warn(String.format("An error occurred while generating doc report [%s]. System will retry to generate report once again.", reportTemplate.getDocumentName()), e);
            try {
                doCreateDocument(reportTemplate.getOutputType(), outputStream);
            } catch (NoFreePortsException e1) {
                throw wrapWithReportingException("An error occurred while generating doc report.", e);
            }
        }
    }

    private void doCreateDocument(final ReportOutputType outputType, final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = new OfficeTask() {
            @Override
            public void processTaskInOpenOffice(OfficeResourceProvider ooResourceProvider) {
                try {
                    XInputStream xis = getXInputStream(reportTemplate);
                    xComponent = loadXComponent(ooResourceProvider.getXComponentLoader(), xis);
                    officeComponent = new OfficeComponent(ooResourceProvider, xComponent);

                    // Handling tables
                    fillTables(ooResourceProvider.getXDispatchHelper());
                    // Handling text
                    replaceAllAliasesInDocument();
                    // Saving document to output stream and closing
                    saveAndClose(xComponent, outputType, outputStream);
                } catch (Exception e) {
                    throw wrapWithReportingException("An error occurred while running task in Open Office server", e);
                }
            }
        };

        officeIntegration.runTaskWithTimeout(officeTask, officeIntegration.getTimeoutInSeconds());
    }

    private void saveAndClose(XComponent xComponent, ReportOutputType outputType, OutputStream outputStream)
            throws IOException {
        OfficeOutputStream ooos = new OfficeOutputStream(outputStream);
        String filterName;
        if (ReportOutputType.pdf.equals(outputType)) {
            filterName = PDF_OUTPUT_FILE;
        } else {
            filterName = MS_WORD_OUTPUT_FILE;
        }
        saveXComponent(xComponent, ooos, filterName);
        closeXComponent(xComponent);
    }

    //todo allow to define table name as docx formatter does (##band=Band1 in first cell)
    private void fillTables(XDispatchHelper xDispatchHelper) throws com.sun.star.uno.Exception {
        List<String> tablesNames = TableManager.getTablesNames(xComponent);
        tablesNames.retainAll(rootBand.getFirstLevelBandDefinitionNames());

        for (String tableName : tablesNames) {
            BandData band = rootBand.findBandRecursively(tableName);
            TableManager tableManager = new TableManager(xComponent, tableName);
            XTextTable xTextTable = tableManager.getXTextTable();

            if (band != null) {
                // todo remove this hack!
                // try to select one cell without it workaround
                int columnCount = xTextTable.getColumns().getCount();
                if (columnCount < 2) {
                    xTextTable.getColumns().insertByIndex(columnCount, 1);
                }
                fillTable(tableName, band.getParentBand(), tableManager, xDispatchHelper);
                // end of workaround ->
                if (columnCount < 2) {
                    xTextTable.getColumns().removeByIndex(columnCount, 1);
                }
            } else {
                if (tableManager.hasValueExpressions())
                    tableManager.deleteLastRow();
            }
        }
    }

    private void fillTable(String name, BandData parentBand, TableManager tableManager, XDispatchHelper xDispatchHelper)
            throws com.sun.star.uno.Exception {
        // Lock clipboard, cause uno uses it to grow tables
        synchronized (clipboardLock) {//todo try get rid of it
            XTextTable xTextTable = tableManager.getXTextTable();
            if (officeIntegration.isDisplayDeviceAvailable()) {
                clearClipboard();
            }
            int startRow = xTextTable.getRows().getCount() - 1;
            List<BandData> childrenBands = parentBand.getChildrenList();
            for (BandData child : childrenBands) {
                if (name.equals(child.getName())) {
                    tableManager.duplicateLastRow(xDispatchHelper, asXTextDocument(xComponent).getCurrentController());
                }
            }
            int i = startRow;
            for (BandData child : childrenBands) {
                if (name.equals(child.getName())) {
                    fillRow(child, tableManager, i);
                    i++;
                }
            }
            tableManager.deleteLastRow();
        }
    }

    private void fillRow(BandData band, TableManager tableManager, int row)
            throws com.sun.star.lang.IndexOutOfBoundsException, NoSuchElementException, WrappedTargetException {
        XTextTable xTextTable = tableManager.getXTextTable();
        int colCount = xTextTable.getColumns().getCount();
        for (int col = 0; col < colCount; col++) {
            fillCell(band, tableManager.getXCell(col, row));
        }
    }

    private void fillCell(BandData band, XCell xCell) throws NoSuchElementException, WrappedTargetException {
        String cellText = formatCellText(asXText(xCell).getString());
        List<String> parametersToInsert = new ArrayList<String>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(cellText);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
            XText xText = asXText(xCell);
            XTextCursor xTextCursor = xText.createTextCursor();

            String paramStr = "${" + parameterName + "}";
            int index = cellText.indexOf(paramStr);
            while (index >= 0) {
                xTextCursor.gotoStart(false);
                xTextCursor.goRight((short) (index + paramStr.length()), false);
                xTextCursor.goLeft((short) paramStr.length(), true);

                insertValue(xText, xTextCursor, band, parameterName);
                cellText = formatCellText(xText.getString());

                index = cellText.indexOf(paramStr);
            }
        }
    }

    /**
     * Replaces all aliases ${bandname.paramname} in document text.
     *
     * @throws com.haulmont.yarg.exception.ReportingException
     *          If there is not appropriate band or alias is bad
     */
    private void replaceAllAliasesInDocument() {
        XTextDocument xTextDocument = asXTextDocument(xComponent);
        XReplaceable xReplaceable = asXReplaceable(xTextDocument);
        XSearchDescriptor searchDescriptor = xReplaceable.createSearchDescriptor();
        searchDescriptor.setSearchString(ALIAS_WITH_BAND_NAME_REGEXP);
        try {
            searchDescriptor.setPropertyValue(SEARCH_REGULAR_EXPRESSION, true);
        } catch (Exception e) {
            throw new OpenOfficeException("An error occurred while setting search properties in Open office", e);
        }

        XIndexAccess indexAccess = xReplaceable.findAll(searchDescriptor);
        for (int i = 0; i < indexAccess.getCount(); i++) {
            try {
                XTextRange textRange = asXTextRange(indexAccess.getByIndex(i));
                String alias = unwrapParameterName(textRange.getString());

                BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);

                BandData band = findBandByPath(rootBand, bandAndParameter.bandPath);

                if (band == null) {
                    throw wrapWithReportingException(String.format("No band for alias : [%s] found", alias));
                }

                insertValue(textRange.getText(), textRange, band, bandAndParameter.parameterName);
            } catch (ReportingException e) {
                throw e;
            } catch (Exception e) {
                throw wrapWithReportingException(String.format("An error occurred while replacing aliases in document. Regexp [%s]. Replacement number [%d]", ALIAS_WITH_BAND_NAME_REGEXP, i), e);
            }
        }
    }

    private void insertValue(XText text, XTextRange textRange, BandData band, String paramName) {
        String fullParamName = band.getFullName() + "." + paramName;
        Object paramValue = band.getParameterValue(paramName);

        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldConverters();
        try {
            boolean handled = false;

            if (paramValue != null) {
                if ((formats != null) && (formats.containsKey(fullParamName))) {
                    String format = formats.get(fullParamName).getFormat();
                    // Handle doctags
                    for (ContentInliner contentInliner : contentInliners) {
                        Matcher matcher = contentInliner.getTagPattern().matcher(format);
                        if (matcher.find()) {
                            contentInliner.inlineToDoc(officeComponent, textRange, text, paramValue, matcher);
                            handled = true;
                        }
                    }
                }
                if (!handled) {
                    String valueString = formatValue(paramValue, fullParamName);
                    text.insertString(textRange, valueString, true);
                }
            } else {
                text.insertString(textRange, "", true);
            }
        } catch (Exception ex) {
            throw wrapWithReportingException(String.format("An error occurred while inserting parameter [%s] into text line [%s]", paramName, text.getString()), ex);
        }
    }

    //delete nonexistent symbols from cell text
    private String formatCellText(String cellText) {
        if (cellText != null) {
            return cellText.replace("\r", "");
        } else {
            return cellText;
        }
    }

    private static final Object clipboardLock = new Object();

    private static void clearClipboard() {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[0];
                }

                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return false;
                }

                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    throw new UnsupportedFlavorException(flavor);
                }
            }, null);
        } catch (IllegalStateException ignored) {
            //ignore exception
        }
    }
}