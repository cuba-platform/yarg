/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.newreport.formatters.impl.tags;

import com.haulmont.newreport.exception.ReportingException;
import com.haulmont.newreport.formatters.impl.doc.OfficeComponent;
import com.haulmont.newreport.formatters.impl.doc.connector.OOResourceProvider;
import com.sun.star.awt.Size;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.drawing.XShape;
import com.sun.star.graphic.XGraphic;
import com.sun.star.graphic.XGraphicProvider;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextRange;
import com.sun.star.uno.XComponentContext;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.newreport.formatters.impl.doc.ODTUnoConverter.*;

/**
 * <p>$Id: BitmapTagHandler.java 10587 2013-02-19 08:40:16Z degtyarjov $</p>
 *
 * @author artamonov
 */
public class BitmapTagHandler implements TagHandler {

    private final static String REGULAR_EXPRESSION = "\\$\\{bitmap:([0-9]+?)x([0-9]+?)\\}";

    private static final String TEXT_GRAPHIC_OBJECT = "com.sun.star.text.TextGraphicObject";
    private static final String GRAPHIC_PROVIDER_OBJECT = "com.sun.star.graphic.GraphicProvider";

    private static final int IMAGE_FACTOR = 27;

    protected Pattern tagPattern;

    private int docxUniqueId1, docxUniqueId2;

    public BitmapTagHandler() {
        tagPattern = Pattern.compile(REGULAR_EXPRESSION, Pattern.CASE_INSENSITIVE);
    }

    public Pattern getTagPattern() {
        return tagPattern;
    }

    /**
     * Insert image in Doc document
     *
     * @param officeComponent OpenOffice Objects
     * @param destination     Text
     * @param textRange       Place for insert
     * @param paramValue      Parameter
     * @param paramsMatcher
     */
    public void handleTag(OfficeComponent officeComponent,
                          XText destination, XTextRange textRange,
                          Object paramValue, Matcher paramsMatcher) throws Exception {

        boolean inserted = false;
        if (paramValue != null) {
            byte[] imageContent = getContent(paramValue);
            if (imageContent.length != 0) {
                int width = Integer.parseInt(paramsMatcher.group(1));
                int height = Integer.parseInt(paramsMatcher.group(2));
                try {
                    XComponent xComponent = officeComponent.getOfficeComponent();
                    insertImage(xComponent, officeComponent.getOoResourceProvider(), destination, textRange, imageContent, width, height);
                    inserted = true;
                } catch (Exception ignored) {
                }
            }
        }
        if (!inserted)
            destination.getText().insertString(textRange, "", true);
    }

    public void handleTag(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher paramsMatcher) {
        try {
            byte[] imageContent = getContent(paramValue);
            BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, imageContent);
            int width = Integer.parseInt(paramsMatcher.group(1));
            int height = Integer.parseInt(paramsMatcher.group(2));
            Inline inline = imagePart.createImageInline("", "", docxUniqueId1++, docxUniqueId2++, false);
            ImageSize oldSize = imagePart.getImageInfo().getSize();
            double widhtExtent = (double) width / oldSize.getWidthPx();
            double heightExtent = (double) height / oldSize.getHeightPx();
            inline.getExtent().setCx((long) (inline.getExtent().getCx() * widhtExtent));
            inline.getExtent().setCy((long) (inline.getExtent().getCy() * heightExtent));
            org.docx4j.wml.Drawing drawing = new org.docx4j.wml.ObjectFactory().createDrawing();
            R run = (R) text.getParent();
            run.getContent().add(drawing);
            drawing.getAnchorOrInline().add(inline);
            text.setValue("");
        } catch (Exception e) {
            throw new ReportingException("An error occured while isnerting bitmap to docx file", e);
        }
    }

    protected byte[] getContent(Object paramValue) {
        return (byte[]) paramValue;
    }

    protected void insertImage(XComponent document, OOResourceProvider ooResourceProvider, XText destination, XTextRange textRange,
                               byte[] imageContent, int width, int height) throws Exception {
        XMultiServiceFactory xFactory = asXMultiServiceFactory(document);
        XComponentContext xComponentContext = ooResourceProvider.getXComponentContext();
        XMultiComponentFactory serviceManager = xComponentContext.getServiceManager();

        Object oImage = xFactory.createInstance(TEXT_GRAPHIC_OBJECT);
        Object oGraphicProvider = serviceManager.createInstanceWithContext(GRAPHIC_PROVIDER_OBJECT, xComponentContext);

        XGraphicProvider xGraphicProvider = asXGraphicProvider(oGraphicProvider);

        XPropertySet imageProperties = buildImageProperties(xGraphicProvider, oImage, imageContent);
        XTextContent xTextContent = asXTextContent(oImage);
        destination.insertTextContent(textRange, xTextContent, true);
        setImageSize(width, height, oImage, imageProperties);
    }

    protected void setImageSize(int width, int height, Object oImage, XPropertySet imageProperties)
            throws Exception {
        Size aActualSize = (Size) imageProperties.getPropertyValue("ActualSize");
        aActualSize.Height = height * IMAGE_FACTOR;
        aActualSize.Width = width * IMAGE_FACTOR;
        XShape xShape = asXShape(oImage);
        xShape.setSize(aActualSize);
    }

    protected XPropertySet buildImageProperties(XGraphicProvider xGraphicProvider, Object oImage, byte[] imageContent)
            throws Exception {
        XPropertySet imageProperties = asXPropertySet(oImage);

        PropertyValue[] propValues = new PropertyValue[]{new PropertyValue()};
        propValues[0].Name = "InputStream";
        propValues[0].Value = new ByteArrayToXInputStreamAdapter(imageContent);

        XGraphic graphic = xGraphicProvider.queryGraphic(propValues);
        if (graphic != null) {
            imageProperties.setPropertyValue("Graphic", graphic);

            imageProperties.setPropertyValue("HoriOrient", HoriOrientation.NONE);
            imageProperties.setPropertyValue("VertOrient", HoriOrientation.NONE);

            imageProperties.setPropertyValue("HoriOrientPosition", 0);
            imageProperties.setPropertyValue("VertOrientPosition", 0);
        }

        return imageProperties;
    }
}