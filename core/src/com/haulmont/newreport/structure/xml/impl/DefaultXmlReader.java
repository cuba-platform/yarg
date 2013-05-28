/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.xml.impl;

import com.haulmont.newreport.structure.*;
import com.haulmont.newreport.structure.impl.BandOrientation;
import com.haulmont.newreport.exception.ReportingXmlException;
import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.impl.*;
import com.haulmont.newreport.structure.xml.XmlReader;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DefaultXmlReader implements XmlReader {

    @Override
    public Report parseXml(String xml) throws IOException {
        try {
            SAXReader reader = null;
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();

                SchemaFactory schemaFactory =
                        SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");

                factory.setSchema(schemaFactory.newSchema(
                        new Source[]{new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("reporting.xsd"))}));

                SAXParser parser = factory.newSAXParser();

                reader = new SAXReader(parser.getXMLReader());
            } catch (SAXException e) {
                throw new ReportingXmlException(String.format("An error occurred during loading reporting xsd. \\n[%s]", xml), e);
            } catch (ParserConfigurationException e) {
                throw new ReportingXmlException(String.format("An error occurred during loading reporting xsd. \\n[%s]", xml), e);
            }

            Document document = reader.read(new StringReader(xml));
            Element rootElement = document.getRootElement();
            Map<String, ReportTemplate> templateMap = parseTemplates(rootElement);
            List<ReportParameter> reportParameters = parseInputParameters(rootElement);
            BandDefinitionBuilder rootBandDefinitionBuilder = new BandDefinitionBuilder().name(Band.ROOT_BAND_NAME);
            parseChildBandDefinitions(rootElement.element("rootBand"), rootBandDefinitionBuilder);
            BandDefinition rootBandDefinition = rootBandDefinitionBuilder.build();

            ReportImpl report = new ReportImpl(rootElement.attribute("name").getText(), templateMap, rootBandDefinition, reportParameters);
            return report;
        } catch (DocumentException e) {
            throw new ReportingXmlException(String.format("An error occurred while parsing report xml. \\n[%s]", xml), e);
        } catch (FileNotFoundException e) {
            throw new ReportingXmlException(String.format("Could not find report template. \\n[%s]", xml), e);
        } catch (ClassNotFoundException e) {
            throw new ReportingXmlException(String.format("Report parameter class not found. \\n[%s]", xml), e);
        }
    }

    /**
     * Override this method to load files differently from basic file system way
     *
     * @param documentPath - path to document (file system path or other if overriden)
     * @throws FileNotFoundException
     */
    protected FileInputStream getDocumentContent(String documentPath) throws FileNotFoundException {
        return new FileInputStream(documentPath);
    }

    protected Map<String, ReportTemplate> parseTemplates(Element rootElement) throws IOException {
        Element templatesElement = rootElement.element("templates");
        List<Element> templates = templatesElement.elements("template");
        Map<String, ReportTemplate> templateMap = new HashMap<String, ReportTemplate>();
        for (Element template : templates) {
            String code = template.attribute("code").getText();
            String documentName = template.attribute("documentName").getText();
            String documentPath = template.attribute("documentPath").getText();
            String outputType = template.attribute("outputType").getText();

            ReportTemplateImpl reportTemplate = new ReportTemplateImpl(code, documentName, documentPath, getDocumentContent(documentPath), ReportOutputType.getOutputTypeById(outputType));
            templateMap.put(reportTemplate.getCode(), reportTemplate);
        }

        return templateMap;
    }

    protected List<ReportParameter> parseInputParameters(Element rootElement) throws FileNotFoundException, ClassNotFoundException {
        Element inputParametersElement = rootElement.element("parameters");
        List<Element> parameters = inputParametersElement.elements("parameter");
        List<ReportParameter> reportParameters = new ArrayList<ReportParameter>();
        for (Element parameter : parameters) {
            String name = parameter.attribute("name").getText();
            String alias = parameter.attribute("alias").getText();
            Boolean required = Boolean.valueOf(parameter.attribute("required").getText());
            Class type = Class.forName(parameter.attribute("type").getText());

            ReportParameterImpl reportParameter = new ReportParameterImpl(name, alias, required, type);
            reportParameters.add(reportParameter);
        }

        return reportParameters;
    }

    protected void parseChildBandDefinitions(Element bandDefinitionElement, BandDefinitionBuilder parentBandDefinitionBuilder) throws FileNotFoundException, ClassNotFoundException {
        Element childrenBandsElement = bandDefinitionElement.element("bands");
        List<Element> childrenBands = childrenBandsElement.elements("band");
        for (Element childBandElement : childrenBands) {
            String childBandName = childBandElement.attribute("name").getText();
            BandOrientation orientation = BandOrientation.fromId(childBandElement.attribute("orientation").getText());
            BandDefinitionBuilder childBandDefinitionBuilder =
                    new BandDefinitionBuilder()
                            .name(childBandName)
                            .orientation(orientation);

            Element dataSetsElement = childBandElement.element("dataSets");
            List<Element> dataSetElements = dataSetsElement.elements("dataSet");
            for (Element dataSetElement : dataSetElements) {
                String script = dataSetElement.element("script").getText();
                String type = dataSetElement.attribute("type").getText();
                String dataSetName = dataSetElement.attribute("name").getText();

                childBandDefinitionBuilder.dataSet(dataSetName, script, type);
            }

            parseChildBandDefinitions(childBandElement, childBandDefinitionBuilder);
            BandDefinition childBandDefinition = childBandDefinitionBuilder.build();
            parentBandDefinitionBuilder.band(childBandDefinition);
        }
    }
}
