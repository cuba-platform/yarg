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
package com.haulmont.yarg.formatters.impl;

import com.google.common.base.Preconditions;
import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.doc.OfficeComponent;
import com.haulmont.yarg.formatters.impl.doc.OfficeOutputStream;
import com.haulmont.yarg.formatters.impl.doc.TableManager;
import com.haulmont.yarg.formatters.impl.doc.connector.NoFreePortsException;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeIntegrationAPI;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeTask;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.XIndexAccess;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.XComponent;
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

import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.*;
import static com.haulmont.yarg.formatters.impl.doc.UnoHelper.*;

/**
 * Document formatter for '.doc' and '.odt' file types
 */
public class DocFormatter extends AbstractFormatter {
    protected static final Logger log = LoggerFactory.getLogger(DocFormatter.class);

    protected static final String SEARCH_REGULAR_EXPRESSION = "SearchRegularExpression";

    protected static final String PDF_OUTPUT_FILE = "writer_pdf_Export";
    protected static final String MS_WORD_OUTPUT_FILE = "MS Word 97";

    protected XComponent xComponent;

    protected OfficeComponent officeComponent;

    protected OfficeIntegrationAPI officeIntegration;

    public DocFormatter(FormatterFactoryInput formatterFactoryInput, OfficeIntegrationAPI officeIntegration) {
        super(formatterFactoryInput);
        Preconditions.checkNotNull("\"officeIntegration\" parameter can not be null", officeIntegration);

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

    protected void doCreateDocument(final ReportOutputType outputType, final OutputStream outputStream) throws NoFreePortsException {
        OfficeTask officeTask = new OfficeTask() {
            @Override
            public void processTaskInOpenOffice(OfficeResourceProvider ooResourceProvider) {
                try {
                    loadDocument(ooResourceProvider);

                    // Handling tables
                    fillTables(ooResourceProvider.getXDispatchHelper());
                    // Handling text
                    replaceAllAliasesInDocument();
                    replaceAllAliasesInDocument();//we do it second time to handle several open office bugs (page breaks in html, etc). Do not remove.
                    // Saving document to output stream and closing
                    saveAndClose(xComponent, outputType, outputStream);
                } catch (Exception e) {
                    throw wrapWithReportingException("An error occurred while running task in Open Office server", e);
                }
            }
        };

        officeIntegration.runTaskWithTimeout(officeTask, officeIntegration.getTimeoutInSeconds());
    }

    protected void loadDocument(OfficeResourceProvider ooResourceProvider) throws com.sun.star.lang.IllegalArgumentException, IOException {
        XInputStream xis = getXInputStream(reportTemplate);
        xComponent = loadXComponent(ooResourceProvider.getXComponentLoader(), xis);
        officeComponent = new OfficeComponent(ooResourceProvider, xComponent);
    }

    protected void saveAndClose(XComponent xComponent, ReportOutputType outputType, OutputStream outputStream)
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

    protected void fillTables(XDispatchHelper xDispatchHelper) throws com.sun.star.uno.Exception {
        List<String> tablesNames = TableManager.getTablesNames(xComponent);

        for (String tableName : tablesNames) {
            TableManager tableManager = new TableManager(xComponent, tableName);
            BandFinder bandFinder = new BandFinder(tableManager).find();

            BandData band = bandFinder.getBand();
            String bandName = bandFinder.getBandName();
            int numberOfRowWithAliases = tableManager.findRowWithAliases();

            if (band != null && numberOfRowWithAliases > -1) {
                XTextTable xTextTable = tableManager.getXTextTable();

                // todo remove this hack!
                // try to select one cell without it workaround
                int columnCount = xTextTable.getColumns().getCount();
                if (columnCount < 2) {
                    xTextTable.getColumns().insertByIndex(columnCount, 1);
                }

                fillTable(band.getName(), band.getParentBand(), tableManager, xDispatchHelper, numberOfRowWithAliases);

                // end of workaround ->
                if (columnCount < 2) {
                    xTextTable.getColumns().removeByIndex(columnCount, 1);
                }
            } else if (numberOfRowWithAliases > -1
                    && rootBand.getFirstLevelBandDefinitionNames() != null
                    && rootBand.getFirstLevelBandDefinitionNames().contains(bandName)) {
                //if table is linked with band and has aliases on it, but no band data found -
                //we are removing the row
                tableManager.deleteRow(numberOfRowWithAliases);
            }
        }
    }

    protected void fillTable(String name, BandData parentBand, TableManager tableManager, XDispatchHelper xDispatchHelper, int numberOfRowWithAliases)
            throws com.sun.star.uno.Exception {
        // Lock clipboard, cause uno uses it to grow tables
        synchronized (clipboardLock) {//todo try get rid of it
            XTextTable xTextTable = tableManager.getXTextTable();
            if (officeIntegration.isDisplayDeviceAvailable()) {
                clearClipboard();
            }
            List<BandData> childrenBands = parentBand.getChildrenList();
            for (BandData child : childrenBands) {
                if (name.equals(child.getName())) {
                    tableManager.copyRow(xDispatchHelper, asXTextDocument(xComponent).getCurrentController(), numberOfRowWithAliases);
                }
            }

            int i = numberOfRowWithAliases;
            for (BandData child : childrenBands) {
                if (name.equals(child.getName())) {
                    fillRow(child, tableManager, i);
                    i++;
                }
            }
            tableManager.deleteRow(i);
        }
    }

    protected void fillRow(BandData band, TableManager tableManager, int row)
            throws com.sun.star.lang.IndexOutOfBoundsException, NoSuchElementException, WrappedTargetException {
        XTextTable xTextTable = tableManager.getXTextTable();
        int colCount = xTextTable.getColumns().getCount();
        for (int col = 0; col < colCount; col++) {
            fillCell(band, tableManager.getXCell(col, row));
        }
    }

    protected void fillCell(BandData band, XCell xCell) throws NoSuchElementException, WrappedTargetException {
        XText xText = asXText(xCell);
        String cellText = xText.getString();
        List<String> parametersToInsert = new ArrayList<String>();
        Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(cellText);
        while (matcher.find()) {
            parametersToInsert.add(unwrapParameterName(matcher.group()));
        }
        for (String parameterName : parametersToInsert) {
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
     * @throws com.haulmont.yarg.exception.ReportingException If there is not appropriate band or alias is bad
     */
    protected void replaceAllAliasesInDocument() {
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

                if (band != null) {
                    insertValue(textRange.getText(), textRange, band, bandAndParameter.parameterName);
                } else {
                    throw wrapWithReportingException(String.format("No band for alias : [%s] found", alias));
                }
            } catch (ReportingException e) {
                throw e;
            } catch (Exception e) {
                throw wrapWithReportingException(String.format("An error occurred while replacing aliases in document. Regexp [%s]. Replacement number [%d]", ALIAS_WITH_BAND_NAME_REGEXP, i), e);
            }
        }
    }

    protected void insertValue(XText text, XTextRange textRange, BandData band, String paramName) {
        String paramFullName = band.getName() + "." + paramName;
        Object paramValue = band.getParameterValue(paramName);

        Map<String, ReportFieldFormat> formats = rootBand.getReportFieldFormats();
        try {
            boolean handled = false;

            if (paramValue != null) {
                if ((formats != null) && (formats.containsKey(paramFullName))) {
                    String format = formats.get(paramFullName).getFormat();
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
                    String valueString = formatValue(paramValue, paramFullName);
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
    protected String formatCellText(String cellText) {
        if (cellText != null) {
            return cellText.replace("\r", "");
        } else {
            return cellText;
        }
    }

    protected static final Object clipboardLock = new Object();

    protected static void clearClipboard() {
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

    protected class BandFinder {
        protected String tableName;
        protected TableManager tableManager;
        protected String bandName;
        protected BandData band;

        public BandFinder(TableManager tableManager) {
            this.tableName = tableManager.getTableName();
            this.tableManager = tableManager;
        }

        public String getBandName() {
            return bandName;
        }

        public BandData getBand() {
            return band;
        }

        public BandFinder find() {
            bandName = tableName;
            band = rootBand.findBandRecursively(bandName);
            if (band == null) {
                XText xText = tableManager.findFirstEntryInRow(BAND_NAME_DECLARATION_PATTERN, 0);
                if (xText != null) {
                    Matcher matcher = BAND_NAME_DECLARATION_PATTERN.matcher(xText.getString());
                    if (matcher.find()) {
                        bandName = matcher.group(1);
                        band = rootBand.findBandRecursively(bandName);
                        XTextCursor xTextCursor = xText.createTextCursor();

                        xTextCursor.gotoStart(false);
                        xTextCursor.goRight((short) matcher.end(), false);
                        xTextCursor.goLeft((short) matcher.group().length(), true);

                        xText.insertString(xTextCursor, "", true);
                    }
                }
            }
            return this;
        }
    }
}