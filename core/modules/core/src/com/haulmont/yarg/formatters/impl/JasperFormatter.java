package com.haulmont.yarg.formatters.impl;

import com.haulmont.yarg.exception.ReportFormattingException;
import com.haulmont.yarg.formatters.factory.FormatterFactoryInput;
import com.haulmont.yarg.formatters.impl.jasper.CubaJRFunction;
import com.haulmont.yarg.formatters.impl.jasper.JRBandDataDataSource;
import com.haulmont.yarg.structure.ReportOutputType;
import com.haulmont.yarg.structure.ReportTemplate;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JRDesignParameter;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.*;

import java.util.HashMap;
import java.util.Map;

public class JasperFormatter extends AbstractFormatter {
    protected static final String JASPER_EXT = "jasper";
    protected static final String JRXML_EXT = "jrxml";

    protected static final String CSV_DELIMETER = ";";

    private static final String CUBA_PARAM = "REPORTING";

    public JasperFormatter(FormatterFactoryInput formatterFactoryInput) {
        super(formatterFactoryInput);
    }

    @Override
    public void renderDocument() {
        try {
            switch (getExtension(reportTemplate)) {
                case JASPER_EXT:
                    printReport((JasperReport) JRLoader.loadObject(reportTemplate.getDocumentContent()));
                    break;
                case JRXML_EXT:
                    JasperDesign design = JRXmlLoader.load(reportTemplate.getDocumentContent());
                    if (!design.getParametersMap().containsKey(CUBA_PARAM))
                        design.addParameter(createJRParameter());

                    printReport(JasperCompileManager.compileReport(design));
                    break;
                default:
                    throw new ReportFormattingException("Error handling template extension");
            }
        } catch (JRException e) {
            throw new ReportFormattingException("Error formatting jasper report: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    protected void printReport(JasperReport report) throws JRException {
        JRDataSource dataSource = new JRBandDataDataSource(rootBand);
        Map<String, Object> params = new HashMap<>();
        params.put(CUBA_PARAM, new CubaJRFunction(dataSource));

        JasperPrint jasperPrint = JasperFillManager.fillReport(report, params, dataSource);

        Exporter exporter = createExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.exportReport();
    }

    @SuppressWarnings("unchecked")
    protected Exporter createExporter() {
        Exporter exporter;
        if (ReportOutputType.pdf == reportTemplate.getOutputType()) {
            exporter = new JRPdfExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimplePdfExporterConfiguration());
        } else if (ReportOutputType.html == reportTemplate.getOutputType()) {
            exporter = new HtmlExporter();
            exporter.setExporterOutput(new SimpleHtmlExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleHtmlExporterConfiguration());
        } else if (ReportOutputType.csv == reportTemplate.getOutputType()){
            exporter = new JRCsvExporter();
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
            SimpleCsvExporterConfiguration config = new SimpleCsvExporterConfiguration();
            config.setFieldDelimiter(CSV_DELIMETER);
            exporter.setConfiguration(config);
        } else if (ReportOutputType.doc == reportTemplate.getOutputType() ){
            exporter = new JRRtfExporter();
            exporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleRtfExporterConfiguration());
        } else if (ReportOutputType.docx == reportTemplate.getOutputType()){
            exporter = new JRDocxExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleDocxExporterConfiguration());
        } else if (ReportOutputType.xls == reportTemplate.getOutputType()){
            exporter = new JRXlsExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleXlsExporterConfiguration());
        } else if (ReportOutputType.xlsx == reportTemplate.getOutputType()){
            exporter = new JRXlsxExporter();
            exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
            exporter.setConfiguration(new SimpleXlsxExporterConfiguration());
        } else
            throw new ReportFormattingException("Cannot create jasper exporter using defined output type: " + reportTemplate.getOutputType());

        return exporter;
    }

    protected JRParameter createJRParameter() {
        JRDesignParameter jrParameter = new JRDesignParameter();
        jrParameter.setName(CUBA_PARAM);
        jrParameter.setValueClass(com.haulmont.yarg.formatters.impl.jasper.CubaJRFunction.class);
        return jrParameter;
    }

    private String getExtension(ReportTemplate reportTemplate) {
        String[] split = reportTemplate.getDocumentName().split("\\.");
        return split[split.length - 1].toLowerCase();
    }
}
