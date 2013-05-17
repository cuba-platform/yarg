/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.xml.impl;

import com.haulmont.newreport.exception.ReportingException;
import com.haulmont.newreport.structure.*;
import com.haulmont.newreport.structure.xml.XmlWriter;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class DefaultXmlWriter implements XmlWriter {

    @Override
    public String buildXml(Report report) {
        try {
            Document document = DocumentFactory.getInstance().createDocument();
            Element root = document.addElement("report");

            root.addAttribute("name", report.getName());
            writeTemplates(report, root);
            writeInputParameters(report, root);
            writeRootBand(report, root);

            StringWriter stringWriter = new StringWriter();
            new XMLWriter(stringWriter, OutputFormat.createPrettyPrint()).write(document);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new ReportingException(e);
        }
    }

    protected void writeRootBand(Report report, Element root) {
        BandDefinition rootBandDefinition = report.getRootBandDefinition();
        Element rootBandDefinitionElement = root.addElement("rootBand");
        writeBandDefinition(rootBandDefinitionElement, rootBandDefinition);
    }

    protected void writeInputParameters(Report report, Element root) {
        Element reportTemplatesElement = root.addElement("parameters");
        for (ReportParameter reportParameter : report.getReportParameters()) {
            Element reportTemplateElement = reportTemplatesElement.addElement("parameter");
            reportTemplateElement.addAttribute("name", reportParameter.getName());
            reportTemplateElement.addAttribute("alias", reportParameter.getAlias());
            reportTemplateElement.addAttribute("required", String.valueOf(reportParameter.getRequired()));
            reportTemplateElement.addAttribute("type", reportParameter.getType().getCanonicalName());
        }
    }

    protected void writeTemplates(Report report, Element root) {
        Map<String, ReportTemplate> reportTemplates = report.getReportTemplates();
        Element reportTemplatesElement = root.addElement("templates");
        for (ReportTemplate reportTemplate : reportTemplates.values()) {
            Element reportTemplateElement = reportTemplatesElement.addElement("template");
            reportTemplateElement.addAttribute("code", reportTemplate.getCode());
            reportTemplateElement.addAttribute("documentName", reportTemplate.getDocumentName());
            reportTemplateElement.addAttribute("documentPath", reportTemplate.getDocumentPath());
            reportTemplateElement.addAttribute("outputType", reportTemplate.getOutputType().getId());
        }
    }

    protected void writeBandDefinition(Element element, BandDefinition bandDefinition) {
        element.addAttribute("name", bandDefinition.getName());
        element.addAttribute("orientation", bandDefinition.getOrientation().id);
        Element childrenBandsElement = element.addElement("bands");

        Element dataSetsElement = element.addElement("dataSets");
        for (DataSet dataSet : bandDefinition.getDataSets()) {
            Element dataSetElement = dataSetsElement.addElement("dataSet");
            dataSetElement.addAttribute("name", dataSet.getName());
            dataSetElement.addAttribute("type", dataSet.getLoaderType());
            dataSetElement.addElement("script").setText(dataSet.getScript());
        }

        for (BandDefinition childBandDefinition : bandDefinition.getChildrenBandDefinitions()) {
            Element childBandElement = childrenBandsElement.addElement("band");
            writeBandDefinition(childBandElement, childBandDefinition);
        }
    }
}
