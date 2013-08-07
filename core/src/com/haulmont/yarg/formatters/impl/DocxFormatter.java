/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: degtyarjov
 * Created: 12.02.13 11:46
 *
 * $Id$
 */
package com.haulmont.yarg.formatters.impl;


import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.structure.ReportTemplate;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;

import javax.xml.bind.JAXBElement;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;

/**
 * * Document formatter for '.docx' file types
 */
public class DocxFormatter extends AbstractFormatter {
    private WordprocessingMLPackage wordprocessingMLPackage;

    public DocxFormatter(BandData rootBand, ReportTemplate reportTemplate, OutputStream outputStream) {
        super(rootBand, reportTemplate, outputStream);
        supportedOutputTypes.add(ReportOutputType.docx);
        supportedOutputTypes.add(ReportOutputType.pdf);
    }

    @Override
    public void renderDocument() {
        if (reportTemplate == null)
            throw new NullPointerException("Template file can't be null.");
        try {
            wordprocessingMLPackage = WordprocessingMLPackage.load(reportTemplate.getDocumentContent());
        } catch (Docx4JException e) {
            throw wrapWithReportingException(String.format("An error occurred while reading docx template. File name [%s]", reportTemplate.getDocumentName()), e);
        }

        MainDocumentPart mainDocumentPart = wordprocessingMLPackage.getMainDocumentPart();
        DocumentWrapper documentWrapper = new DocumentWrapper(mainDocumentPart);

        //process tables
        for (TableWrapper resultingTable : documentWrapper.tables) {
            if (resultingTable.secondRow == null) {
                resultingTable.secondRow = resultingTable.firstRow;
            }
            List<BandData> bands = rootBand.findBandsRecursively(resultingTable.bandName);
            for (final BandData band : bands) {
                Tr newRow = resultingTable.copyRow(resultingTable.secondRow);
                resultingTable.fillRowFromBand(newRow, band);
            }
            resultingTable.table.getContent().remove(resultingTable.secondRow);
        }

        //replace all other aliases with band data
        for (TextWrapper text : documentWrapper.texts) {
            text.fillTextWithBandData();
        }

        try {
            if (ReportOutputType.docx.equals(reportTemplate.getOutputType())) {
                writeToOutputStream(wordprocessingMLPackage, outputStream);
                outputStream.flush();
            } else if (ReportOutputType.pdf.equals(reportTemplate.getOutputType())) {
                org.docx4j.convert.out.pdf.PdfConversion c
                        = new org.docx4j.convert.out.pdf.viaXSLFO.Conversion(wordprocessingMLPackage);
                c.output(outputStream, new PdfSettings());
                outputStream.flush();
            } else {
                throw new UnsupportedOperationException(String.format("DocxFormatter could not output file with type [%s]", reportTemplate.getOutputType()));
            }
        } catch (Docx4JException e) {
            throw wrapWithReportingException("An error occurred while saving result report", e);
        } catch (IOException e) {
            throw wrapWithReportingException("An error occurred while saving result report to PDF", e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    private class DocumentWrapper {
        MainDocumentPart mainDocumentPart;
        Set<TableWrapper> tables;
        Set<TextWrapper> texts;

        DocumentWrapper(MainDocumentPart mainDocumentPart) {
            this.mainDocumentPart = mainDocumentPart;
            collectData();
        }

        private void collectDataFromObjects(Object... objects) {
            for (Object object : objects) {
                if (object != null) {
                    CollectAliasesCallbackImpl collectAliasesCallback = new CollectAliasesCallbackImpl();
                    new TraversalUtil(object, collectAliasesCallback);
                    texts.addAll(collectAliasesCallback.textWrappers);
                }
            }
        }

        void collectData() {
            CollectTablesCallbackImpl collectTablesCallback = new CollectTablesCallbackImpl();
            new TraversalUtil(mainDocumentPart, collectTablesCallback);
            CollectAliasesCallbackImpl collectAliasesCallback = new CollectAliasesCallbackImpl();
            new TraversalUtil(mainDocumentPart, collectAliasesCallback);
            tables = collectTablesCallback.tableWrappers;
            texts = collectAliasesCallback.textWrappers;

            //collect data from headers
            List<SectionWrapper> sectionWrappers = wordprocessingMLPackage.getDocumentModel().getSections();
            for (SectionWrapper sw : sectionWrappers) {
                HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
                collectDataFromObjects(hfp.getFirstHeader(), hfp.getDefaultHeader(), hfp.getEvenHeader(), hfp.getFirstFooter(), hfp.getDefaultFooter(), hfp.getEvenFooter());
            }
        }
    }

    private class TextWrapper {
        Text text;
        String alias;

        private TextWrapper(Text text, String alias) {
            this.text = text;
            this.alias = alias;
        }

        void fillTextWithBandData() {
            BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);

            if (StringUtils.isBlank(bandAndParameter.bandPath) || StringUtils.isBlank(bandAndParameter.parameterName)) {
                if (alias.matches("[A-z0-9_]+?")) {//skip aliases in tables
                    return;
                }

                throw wrapWithReportingException("Bad alias : " + text.getValue());
            }

            BandData band = findBandByPath(rootBand, bandAndParameter.bandPath);

            if (band == null) {
                throw wrapWithReportingException("No band found for alias : " + alias);
            }

            Object paramValue = band.getParameterValue(bandAndParameter.parameterName);

            Map<String, ReportFieldFormat> valueFormats = rootBand.getReportFieldConverters();
            boolean handled = false;
            if (paramValue != null && valueFormats != null && valueFormats.containsKey(alias)) {
                String format = valueFormats.get(alias).getFormat();
                // Handle doctags
                for (ContentInliner contentInliner : DocxFormatter.this.contentInliners) {
                    Matcher matcher = contentInliner.getTagPattern().matcher(format);
                    if (matcher.find()) {
                        contentInliner.inlineToDocx(wordprocessingMLPackage, text, paramValue, matcher);
                        handled = true;
                    }
                }
            }

            if (!handled) {
                text.setValue(inlineParameterValue(text.getValue(), alias, formatValue(paramValue, bandAndParameter.parameterName)));
            }
        }
    }

    private class TableWrapper {
        Tbl table;
        Tr firstRow = null;
        Tr secondRow = null;
        String bandName = null;

        TableWrapper(Tbl tbl) {
            this.table = tbl;
        }

        public Tr copyRow(Tr row) {
            Tr copiedRow = XmlUtils.deepCopy(row);
            table.getContent().add(copiedRow);
            return copiedRow;
        }

        public void fillRowFromBand(Tr row, final BandData band) {
            new TraversalUtil(row, new TraversalUtil.CallbackImpl() {
                @Override
                public List<Object> apply(Object o) {
                    if (o instanceof Text) {
                        Text text = (Text) o;
                        String sourceString = text.getValue();
                        String resultString = insertBandDataToString(band, sourceString);
                        text.setValue(resultString);
                    }

                    return null;
                }
            });
        }
    }

    private class CollectAliasesCallbackImpl extends TraversalUtil.CallbackImpl {
        Set<TextWrapper> textWrappers = new HashSet<TextWrapper>();

        @Override
        public List<Object> apply(Object o) {
            if (o instanceof Text) {
                Text handlingText = (Text) o;
                String textValue = handlingText.getValue().trim();
                Matcher matcher = ALIAS_WITH_BAND_NAME_PATTERN.matcher(textValue);
                if (matcher.find()) {
                    String alias = matcher.group(1);
                    textWrappers.add(new TextWrapper(handlingText, alias));
                } else if (textValue.contains("}")) {
                    String previousJoinedTexts = StringUtils.join(currentParagraphTextsValues, "");
                    matcher = ALIAS_WITH_BAND_NAME_PATTERN.matcher(previousJoinedTexts);
                    //if join contains alias - remove all exisitng texts and put join to single text (last one)
                    if (matcher.find()) {
                        removeAllTextsInParagraphExceptCurrent(handlingText);
                        handlingText.setValue(previousJoinedTexts);
                        textWrappers.add(new TextWrapper(handlingText, matcher.group(1)));
                    }
                }
            }

            return null;
        }

        private void removeAllTextsInParagraphExceptCurrent(Text handlingText) {
            for (Iterator<Text> currentParagraphTextsIterator = currentParagraphTexts.iterator(); currentParagraphTextsIterator.hasNext(); ) {
                Text __text = currentParagraphTextsIterator.next();//not optimal removing
                if (__text != handlingText) {
                    R run = (R) __text.getParent();
                    for (Iterator<Object> currentRunTextsIterator = run.getContent().iterator(); currentRunTextsIterator.hasNext(); ) {
                        JAXBElement element = (JAXBElement) currentRunTextsIterator.next();
                        if (element.getValue() == __text) {
                            currentRunTextsIterator.remove();
                        }
                    }
                }

                currentParagraphTextsIterator.remove();
            }
        }

        private Set<Text> currentParagraphTexts;
        private List<String> currentParagraphTextsValues;

        public void walkJAXBElements(Object parent) {
            List children = getChildren(parent);
            if (children != null) {

                for (Object o : children) {
                    o = XmlUtils.unwrap(o);

                    if (o instanceof Child) {
                        ((Child) o).setParent(parent);
                    }

                    if (o instanceof P) {
                        currentParagraphTexts = new HashSet<Text>();
                        currentParagraphTextsValues = new ArrayList<String>();
                    }

                    if (o instanceof Text && currentParagraphTexts != null) {
                        Text text = (Text) o;
                        currentParagraphTexts.add(text);
                        currentParagraphTextsValues.add(text.getValue());
                    }

                    this.apply(o);

                    if (this.shouldTraverse(o)) {
                        walkJAXBElements(o);
                    }

                    if (o instanceof P) {
                        currentParagraphTexts = null;
                        currentParagraphTextsValues = null;
                    }
                }
            }
        }
    }

    private class CollectTablesCallbackImpl extends TraversalUtil.CallbackImpl {
        private Stack<TableWrapper> currentTables = new Stack<TableWrapper>();
        private Set<TableWrapper> tableWrappers = new HashSet<TableWrapper>();
        private boolean skipCurrentTable = false;

        public List<Object> apply(Object o) {
            if (skipCurrentTable) return null;

            if (o instanceof Tr) {
                final TableWrapper currentTable = currentTables.peek();

                Tr currentRow = (Tr) o;
                if (currentTable.firstRow == null) {
                    currentTable.firstRow = currentRow;

                    new TraversalUtil(currentTable.firstRow, new TraversalUtil.CallbackImpl() {
                        @Override
                        public List<Object> apply(Object o) {
                            if (o instanceof Text && currentTable.bandName == null) {
                                String text = ((Text) o).getValue();
                                if (StringUtils.isNotBlank(text)) {
                                    Matcher matcher = BAND_NAME_DECLARATION_PATTERN.matcher(text);
                                    if (matcher.find()) {
                                        currentTable.bandName = matcher.group(1);
                                        ((Text) o).setValue("");
                                    }
                                }
                            }

                            return null;
                        }
                    });

                    if (currentTable.bandName == null) {
                        skipCurrentTable = true;
                    } else {
                        tableWrappers.add(currentTable);
                    }
                } else if (currentTable.secondRow == null) {
                    currentTable.secondRow = currentRow;
                }
            }

            return null;
        }

        // Depth first
        public void walkJAXBElements(Object parent) {
            List children = getChildren(parent);
            if (children != null) {

                for (Object o : children) {
                    o = XmlUtils.unwrap(o);

                    if (o instanceof Child) {
                        ((Child) o).setParent(parent);
                    }

                    if (o instanceof Tbl) {
                        currentTables.push(new TableWrapper((Tbl) o));
                    }

                    this.apply(o);

                    if (this.shouldTraverse(o)) {
                        walkJAXBElements(o);
                    }

                    if (o instanceof Tbl) {
                        currentTables.pop();
                        skipCurrentTable = false;
                    }

                }
            }
        }
    }

    private void writeToOutputStream(WordprocessingMLPackage mlPackage, OutputStream outputStream) throws Docx4JException {
        SaveToZipFile saver = new SaveToZipFile(mlPackage);
        saver.save(outputStream);
    }
}
