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


import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.inline.ContentInliner;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportFieldFormat;
import com.haulmont.yarg.structure.ReportOutputType;
import org.apache.commons.io.IOUtils;
import org.docx4j.Docx4J;
import org.docx4j.TextUtils;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.convert.out.HTMLSettings;
import org.docx4j.convert.out.pdf.viaXSLFO.PdfSettings;
import org.docx4j.model.structure.HeaderFooterPolicy;
import org.docx4j.model.structure.SectionWrapper;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.*;
import org.jvnet.jaxb2_commons.ppp.Child;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.*;

/**
 * * Document formatter for '.docx' file types
 */
public class DocxFormatter extends AbstractFormatter {
    protected WordprocessingMLPackage wordprocessingMLPackage;
    protected DocumentWrapper documentWrapper;

    public DocxFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.docx);
        supportedOutputTypes.add(ReportOutputType.pdf);
    }

    @Override
    public void renderDocument() {
        loadDocument();

        fillTables();

        replaceAllAliasesInDocument();

        saveAndClose();
    }

    protected void loadDocument() {
        if (reportTemplate == null)
            throw new NullPointerException("Template file can't be null.");
        try {
            wordprocessingMLPackage = WordprocessingMLPackage.load(reportTemplate.getDocumentContent());
            documentWrapper = new DocumentWrapper(wordprocessingMLPackage.getMainDocumentPart());
        } catch (Docx4JException e) {
            throw wrapWithReportingException(String.format("An error occurred while reading docx template. File name [%s]", reportTemplate.getDocumentName()), e);
        }
    }

    protected void saveAndClose() {
        try {
            if (ReportOutputType.docx.equals(reportTemplate.getOutputType())) {
                writeToOutputStream(wordprocessingMLPackage, outputStream);
                outputStream.flush();
            } else if (ReportOutputType.pdf.equals(reportTemplate.getOutputType())) {
                Docx4J.toPDF(wordprocessingMLPackage,outputStream);
                outputStream.flush();
            } else if (ReportOutputType.html.equals(reportTemplate.getOutputType())) {
                HTMLSettings htmlSettings = Docx4J.createHTMLSettings();
                htmlSettings.setWmlPackage(wordprocessingMLPackage);
                Docx4J.toHTML(htmlSettings, outputStream, Docx4J.FLAG_NONE);
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

    protected void replaceAllAliasesInDocument() {
        for (TextWrapper text : documentWrapper.texts) {
            text.fillTextWithBandData();
        }
    }

    protected void fillTables() {
        for (TableManager resultingTable : documentWrapper.tables) {
            if (resultingTable.rowWithAliases != null) {
                List<BandData> bands = rootBand.findBandsRecursively(resultingTable.bandName);
                for (final BandData band : bands) {
                    Tr newRow = resultingTable.copyRow(resultingTable.rowWithAliases);
                    resultingTable.fillRowFromBand(newRow, band);
                }
                resultingTable.table.getContent().remove(resultingTable.rowWithAliases);
            }
        }
    }

    protected class DocumentWrapper {
        protected MainDocumentPart mainDocumentPart;
        protected Set<TableManager> tables;
        protected Set<TextWrapper> texts;

        protected DocumentWrapper(MainDocumentPart mainDocumentPart) {
            this.mainDocumentPart = mainDocumentPart;
            collectData();
        }

        protected void collectDataFromObjects(Object... objects) {
            for (Object object : objects) {
                if (object != null) {
                    AliasCollector collectAliasesCallback = new AliasCollector();
                    new TraversalUtil(object, collectAliasesCallback);
                    texts.addAll(collectAliasesCallback.textWrappers);
                }
            }
        }

        void collectData() {
            TableCollector collectTablesCallback = new TableCollector();
            new TraversalUtil(mainDocumentPart, collectTablesCallback);
            AliasCollector collectAliasesCallback = new AliasCollector();
            new TraversalUtil(mainDocumentPart, collectAliasesCallback);
            tables = collectTablesCallback.tableManagers;
            texts = collectAliasesCallback.textWrappers;

            //collect data from headers
            List<SectionWrapper> sectionWrappers = wordprocessingMLPackage.getDocumentModel().getSections();
            for (SectionWrapper sw : sectionWrappers) {
                HeaderFooterPolicy hfp = sw.getHeaderFooterPolicy();
                collectDataFromObjects(hfp.getFirstHeader(), hfp.getDefaultHeader(), hfp.getEvenHeader(), hfp.getFirstFooter(), hfp.getDefaultFooter(), hfp.getEvenFooter());
            }
        }
    }

    protected class TextWrapper {
        protected Text text;

        protected TextWrapper(Text text) {
            this.text = text;
        }

        void fillTextWithBandData() {

            Matcher matcher = ALIAS_WITH_BAND_NAME_PATTERN.matcher(text.getValue());
            while (matcher.find()) {
                String alias = matcher.group(1);
                String stringFunction = matcher.group(2);

                BandPathAndParameterName bandAndParameter = separateBandNameAndParameterName(alias);

                if (isBlank(bandAndParameter.bandPath) || isBlank(bandAndParameter.parameterName)) {
                    if (alias.matches("[A-z0-9_]+?")) {//skip aliases in tables
                        continue;
                    }

                    throw wrapWithReportingException("Bad alias : " + text.getValue());
                }

                BandData band = findBandByPath(rootBand, bandAndParameter.bandPath);

                if (band == null) {
                    throw wrapWithReportingException(String.format("No band for alias [%s] found", alias));
                }

                String fullParameterName = band.getName() + "." + bandAndParameter.parameterName;
                Object paramValue = band.getParameterValue(bandAndParameter.parameterName);

                Map<String, ReportFieldFormat> valueFormats = rootBand.getReportFieldFormats();
                if (paramValue != null && valueFormats != null && valueFormats.containsKey(fullParameterName)) {
                    String format = valueFormats.get(fullParameterName).getFormat();
                    for (ContentInliner contentInliner : DocxFormatter.this.contentInliners) {
                        Matcher contentMatcher = contentInliner.getTagPattern().matcher(format);
                        if (contentMatcher.find()) {
                            contentInliner.inlineToDocx(wordprocessingMLPackage, text, paramValue, contentMatcher);
                            return;
                        }
                    }
                }

                text.setValue(inlineParameterValue(text.getValue(), alias, formatValue(paramValue, bandAndParameter.parameterName, fullParameterName, stringFunction)));
            }
        }
    }

    protected class TableManager {
        protected Tbl table;
        protected Tr firstRow = null;
        protected Tr rowWithAliases = null;
        protected String bandName = null;

        TableManager(Tbl tbl) {
            this.table = tbl;
        }

        public Tr copyRow(Tr row) {
            Tr copiedRow = XmlUtils.deepCopy(row);
            int index = table.getContent().indexOf(row);
            table.getContent().add(index, copiedRow);
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

    protected class AliasCollector extends TraversalUtil.CallbackImpl {
        protected Set<TextWrapper> textWrappers = new HashSet<TextWrapper>();
        protected R mergeRun = null;
        protected boolean doMerge = false;
        protected List<Text> textsToRemove = new ArrayList<Text>();


        @Override
        public List<Object> apply(Object o) {
            if (o instanceof Text && doMerge) {
                Text text = (Text) o;
                String textValue = text.getValue();
                R currentRun = (R) text.getParent();
                if (mergeRun != null) {//merge started - need to merge current fragment
                    Text mergeRunText = getFirstText(mergeRun);
                    mergeRunText.setValue(mergeRunText.getValue() + textValue);
                    textsToRemove.add(text);

                    if (textValue.contains("}") && !textValue.contains("${")) {
                        textWrappers.add(new TextWrapper(mergeRunText));
                        mergeRun = null;
                    }
                } else if (UNIVERSAL_ALIAS_PATTERN.matcher(textValue).find()
                        && countMatches(textValue, "${") == countMatches(textValue, "}")) {//no need to merge - fragment is appropriate text to inline band data
                    textWrappers.add(new TextWrapper(text));
                } else if (textValue.contains("${") && mergeRun == null) {//need to start merge
                    mergeRun = currentRun;
                }
            }

            return null;
        }

        protected Text getFirstText(R run) {
            for (Object object : run.getContent()) {
                Object currentRunElement = XmlUtils.unwrap(object);
                if (currentRunElement instanceof Text) {
                    return (Text) currentRunElement;
                }
            }

            throw new IllegalStateException("Merge run doesn't contain text element");//should never be thrown
        }

        public void walkJAXBElements(Object parent) {
            List children = getChildren(parent);
            if (children != null) {

                for (Object o : children) {
                    o = XmlUtils.unwrap(o);

                    if (o instanceof Child) {
                        ((Child) o).setParent(parent);
                    }

                    if (o instanceof P) {
                        initParagraph((P) o);
                    }

                    this.apply(o);

                    if (this.shouldTraverse(o)) {
                        walkJAXBElements(o);
                    }
                }
            }
        }

        protected void initParagraph(P paragraph) {
            mergeRun = null;
            String paragraphText = getParagraphText(paragraph);
            Matcher matcher = ALIAS_WITH_BAND_NAME_PATTERN.matcher(paragraphText);
            doMerge = matcher.find();

            for (Text textToRemove : textsToRemove) {
                R run = (R) textToRemove.getParent();
                for (Iterator iterator = run.getContent().iterator(); iterator.hasNext(); ) {
                    Object element = XmlUtils.unwrap(iterator.next());
                    if (element instanceof Text && element == textToRemove) {
                        iterator.remove();
                    }
                }
            }
            textsToRemove.clear();
        }

        protected String getParagraphText(P paragraph) {
            StringWriter w = new StringWriter();
            try {
                TextUtils.extractText(paragraph, w);
            } catch (Exception e) {
                throw wrapWithReportingException(String.format("An error occurred while rendering docx template. File name [%s]", reportTemplate.getDocumentName()), e);
            }

            return w.toString();
        }
    }

    protected class TableCollector extends TraversalUtil.CallbackImpl {
        protected Stack<TableManager> currentTables = new Stack<TableManager>();
        protected Set<TableManager> tableManagers = new HashSet<TableManager>();
        protected boolean skipCurrentTable = false;

        public List<Object> apply(Object o) {
            if (skipCurrentTable) return null;

            if (o instanceof Tr) {
                final TableManager currentTable = currentTables.peek();

                Tr currentRow = (Tr) o;
                if (currentTable.firstRow == null) {
                    currentTable.firstRow = currentRow;

                    findNameForCurrentTable(currentTable);

                    if (currentTable.bandName == null) {
                        skipCurrentTable = true;
                    } else {
                        tableManagers.add(currentTable);
                    }
                }

                if (currentTable.rowWithAliases == null) {
                    RegexpFinder callback = new RegexpFinder(UNIVERSAL_ALIAS_PATTERN);
                    new TraversalUtil(currentRow, callback);

                    if (callback.getValue() != null) {
                        currentTable.rowWithAliases = currentRow;
                    }
                }
            }

            return null;
        }

        protected void findNameForCurrentTable(final TableManager currentTable) {
            new TraversalUtil(currentTable.firstRow,
                    new RegexpFinder(BAND_NAME_DECLARATION_PATTERN) {
                        @Override
                        protected void onFind(Text o, Matcher matcher) {
                            super.onFind(o, matcher);
                            currentTable.bandName = matcher.group(1);
                            o.setValue(matcher.replaceFirst(""));
                        }
                    });
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
                        currentTables.push(new TableManager((Tbl) o));
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

    protected class RegexpFinder extends TraversalUtil.CallbackImpl {
        protected Pattern regularExpression;
        protected String value;

        public RegexpFinder(Pattern regularExpression) {
            this.regularExpression = regularExpression;
        }

        @Override
        public List<Object> apply(Object o) {
            if (o instanceof Text) {
                String text = ((Text) o).getValue();
                if (isNotBlank(text)) {
                    Matcher matcher = regularExpression.matcher(text);
                    if (matcher.find()) {
                        onFind((Text) o, matcher);
                    }
                }
            }

            return null;
        }

        protected void onFind(Text o, Matcher matcher) {
            value = matcher.group(0);
        }

        public String getValue() {
            return value;
        }
    }

    protected void writeToOutputStream(WordprocessingMLPackage mlPackage, OutputStream outputStream) throws Docx4JException {
        SaveToZipFile saver = new SaveToZipFile(mlPackage);
        saver.save(outputStream);
    }
}
