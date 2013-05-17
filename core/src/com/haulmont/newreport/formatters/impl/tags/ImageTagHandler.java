/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 26.02.11 12:11
 *
 * $Id: ImageTagHandler.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl.tags;

import com.haulmont.newreport.formatters.impl.doc.OfficeComponent;
import com.sun.star.awt.Size;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XNameContainer;
import com.sun.star.drawing.XShape;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.text.HoriOrientation;
import com.sun.star.text.XText;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextRange;
import org.apache.commons.lang.StringUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.wml.Text;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.newreport.formatters.impl.doc.ODTUnoConverter.*;

/**
 * Handle images with format string: ${image:[Width]x[Height]}
 */
public class ImageTagHandler implements TagHandler {

    private final static String REGULAR_EXPRESSION = "\\$\\{image:([0-9]+?)x([0-9]+?)\\}";

    private static final String DRAWING_BITMAP_TABLE = "com.sun.star.drawing.BitmapTable";
    private static final String TEXT_GRAPHIC_OBJECT = "com.sun.star.text.TextGraphicObject";
    private static final int IMAGE_FACTOR = 27;

    private Pattern tagPattern;

    public ImageTagHandler() {
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
            String imageUrl = paramValue.toString();
            if (!StringUtils.isEmpty(imageUrl)) {
                int width = Integer.parseInt(paramsMatcher.group(1));
                int height = Integer.parseInt(paramsMatcher.group(2));
                try {
                    XComponent xComponent = officeComponent.getOfficeComponent();
                    insertImage(xComponent, destination, textRange, imageUrl, width, height);
                    inserted = true;
                } catch (Exception ignored) {
                }
            }
        }
        if (!inserted)
            destination.getText().insertString(textRange, "", true);
    }

    public void handleTag(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher matcher) {
        throw new UnsupportedOperationException();
    }

    private void insertImage(XComponent document, XText destination, XTextRange textRange,
                             String imageUrl, int width, int height)
            throws Exception {
        XMultiServiceFactory xFactory = asXMultiServiceFactory(document);
        Object oBitmapTable = xFactory.createInstance(DRAWING_BITMAP_TABLE);
        Object oImage = xFactory.createInstance(TEXT_GRAPHIC_OBJECT);
        XNameContainer xContainer = asXNameContainer(oBitmapTable);

        String fieldName = UUID.randomUUID().toString();
        xContainer.insertByName(fieldName, imageUrl);
        String internalImageURL = (String) xContainer.getByName(fieldName);

        XPropertySet imageProperties = buildImageProperties(oImage, internalImageURL);
        XTextContent xTextContent = asXTextContent(oImage);
        destination.insertTextContent(textRange, xTextContent, true);
        setImageSize(width, height, oImage, imageProperties);
    }

    private void setImageSize(int width, int height, Object oImage, XPropertySet imageProperties)
            throws Exception {
        Size aActualSize = (Size) imageProperties.getPropertyValue("ActualSize");
        aActualSize.Height = height * IMAGE_FACTOR;
        aActualSize.Width = width * IMAGE_FACTOR;
        XShape xShape = asXShape(oImage);
        xShape.setSize(aActualSize);
    }

    private XPropertySet buildImageProperties(Object oImage, String internalImageURL)
            throws Exception {
        XPropertySet imageProperties = asXPropertySet(oImage);
        imageProperties.setPropertyValue("GraphicURL", internalImageURL);
        imageProperties.setPropertyValue("HoriOrient", HoriOrientation.NONE);
        imageProperties.setPropertyValue("VertOrient", HoriOrientation.NONE);
        imageProperties.setPropertyValue("HoriOrientPosition", 0);
        imageProperties.setPropertyValue("VertOrientPosition", 0);
        return imageProperties;
    }
}