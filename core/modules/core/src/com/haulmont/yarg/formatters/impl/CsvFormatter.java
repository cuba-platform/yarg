package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.exception.UnsupportedFormatException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportOutputType;
import com.opencsv.CSVWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static com.haulmont.yarg.formatters.impl.csv.SimpleSeparatorDetector.detectSeparator;


/**
 * @author birin
 * @version $Id$
 */
public class CsvFormatter extends AbstractFormatter {
    protected char separator;
    protected String[] header;
    protected List<String> parametersToInsert = new ArrayList<>();

    public CsvFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
        supportedOutputTypes.add(ReportOutputType.csv);
        readTemplateData();
    }

    @Override
    public void renderDocument() {
        if (ReportOutputType.csv.equals(outputType)) {
            writeCsvDocument(rootBand, outputStream);
        } else {
            throw new UnsupportedFormatException();
        }
    }

    protected void writeCsvDocument(BandData rootBand, OutputStream outputStream) {
        try {
            List<BandData> actualData = getActualData(rootBand);
            CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream), separator, CSVWriter.DEFAULT_QUOTE_CHARACTER);

            writer.writeNext(header);

            for (BandData row : actualData) {
                String[] entries = new String[parametersToInsert.size()];
                for (int i = 0; i < parametersToInsert.size(); i++) {
                    String parameterName = parametersToInsert.get(i);
                    String fullParameterName = row.getName() + "." + parameterName;
                    entries[i] = formatValue(row.getData().get(parameterName), parameterName, fullParameterName);
                }
                writer.writeNext(entries);
            }

            writer.close();
        } catch (IOException e) {
            throw new ReportFormattingException("Error while writing a csv document", e);
        }
    }

    protected List<BandData> getActualData(BandData rootBand) {
        List<BandData> resultData = new ArrayList<>();
        Map<String, List<BandData>> childrenBands = rootBand.getChildrenBands();

        if (childrenBands != null && !childrenBands.isEmpty()) {
            childrenBands.forEach((s, bandDataList) -> bandDataList.forEach(bandData -> {
                if (bandData.getData() != null && !bandData.getData().isEmpty()) {
                    resultData.add(bandData);
                }
            }));
        }

        return resultData;
    }

    protected void readTemplateData() {
        checkThreadInterrupted();
        InputStream documentContent = reportTemplate.getDocumentContent();
        BufferedReader in = new BufferedReader(new InputStreamReader(documentContent));

        StringBuilder headerData = new StringBuilder();
        try {
            String line;
            while((line = in.readLine()) != null) {
                checkThreadInterrupted();
                Matcher matcher = UNIVERSAL_ALIAS_PATTERN.matcher(line);
                if (!matcher.find())
                    headerData.append(line);
                else {
                    separator = detectSeparator(line);
                    matcher.reset();
                    while (matcher.find()) {
                        String parameterName = unwrapParameterName(matcher.group());
                        parametersToInsert.add(parameterName);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            throw new ReportFormattingException("Error while reading template data");
        }

        header = headerData.toString().replaceAll(String.valueOf(CSVWriter.DEFAULT_QUOTE_CHARACTER), "").split(String.valueOf(separator));
    }
}
