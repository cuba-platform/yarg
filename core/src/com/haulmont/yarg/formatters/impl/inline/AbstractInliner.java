/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.inline;

import com.haulmont.yarg.exception.ReportingException;
import com.haulmont.yarg.formatters.impl.doc.OfficeComponent;
import com.haulmont.yarg.formatters.impl.doc.connector.OfficeResourceProvider;
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
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.*;
import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.asXPropertySet;

public abstract class AbstractInliner implements ContentInliner {
    private static final String TEXT_GRAPHIC_OBJECT = "com.sun.star.text.TextGraphicObject";
    private static final String GRAPHIC_PROVIDER_OBJECT = "com.sun.star.graphic.GraphicProvider";
    private static final int IMAGE_FACTOR = 27;

    protected Pattern tagPattern;
    protected int docxUniqueId1, docxUniqueId2;

    protected abstract byte[] getContent(Object paramValue);

    @Override
    public void inlineToDocx(WordprocessingMLPackage wordPackage, Text text, Object paramValue, Matcher paramsMatcher) {
        try {
            Image image = new Image(paramValue, paramsMatcher);
            if (image.isValid()) {
                BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(wordPackage, image.imageContent);
                Inline inline = imagePart.createImageInline("", "", docxUniqueId1++, docxUniqueId2++, false);
                ImageSize oldSize = imagePart.getImageInfo().getSize();
                double widhtExtent = (double) image.width / oldSize.getWidthPx();
                double heightExtent = (double) image.height / oldSize.getHeightPx();
                inline.getExtent().setCx((long) (inline.getExtent().getCx() * widhtExtent));
                inline.getExtent().setCy((long) (inline.getExtent().getCy() * heightExtent));
                org.docx4j.wml.Drawing drawing = new org.docx4j.wml.ObjectFactory().createDrawing();
                R run = (R) text.getParent();
                run.getContent().add(drawing);
                drawing.getAnchorOrInline().add(inline);
                text.setValue("");
            }
        } catch (Exception e) {
            throw new ReportingException("An error occurred while inserting bitmap to docx file", e);
        }
    }

    @Override
    public void inlineToXls(HSSFPatriarch patriarch, HSSFCell resultCell, Object paramValue, Matcher paramsMatcher) {
        try {
            Image image = new Image(paramValue, paramsMatcher);
            if (image.isValid()) {
                resultCell.getRow().setHeightInPoints(image.height);
                HSSFSheet sheet = resultCell.getSheet();
                HSSFWorkbook workbook = sheet.getWorkbook();

                int pictureIdx = workbook.addPicture(image.imageContent, Workbook.PICTURE_TYPE_JPEG);

                CreationHelper helper = workbook.getCreationHelper();
                ClientAnchor anchor = helper.createClientAnchor();
                anchor.setCol1(resultCell.getColumnIndex());
                anchor.setRow1(resultCell.getRowIndex());
                anchor.setCol2(resultCell.getColumnIndex());
                anchor.setRow2(resultCell.getRowIndex());
                if (patriarch == null) {
                    throw new IllegalArgumentException(String.format("No HSSFPatriarch object provided. Charts on this sheet could cause this effect. Please check sheet %s", resultCell.getSheet().getSheetName()));
                }
                HSSFPicture picture = patriarch.createPicture(anchor, pictureIdx);
                Dimension imageDimension = picture.getImageDimension();
                double actualHeight = imageDimension.getHeight();
                picture.resize((double) image.height / actualHeight);
            }
        } catch (IllegalArgumentException e) {
            throw new ReportingException("An error occurred while inserting bitmap to xls file", e);
        }
    }

    @Override
    public void inlineToDoc(OfficeComponent officeComponent, XTextRange textRange, XText destination, Object paramValue, Matcher paramsMatcher) throws Exception {
        try {
            if (paramValue != null) {
                Image image = new Image(paramValue, paramsMatcher);

                if (image.isValid()) {
                    XComponent xComponent = officeComponent.getOfficeComponent();
                    insertImage(xComponent, officeComponent.getOfficeResourceProvider(), destination, textRange, image);
                }
            }
        } catch (Exception e) {
            throw new ReportingException("An error occurred while inserting bitmap to doc file", e);
        }
    }

    protected void insertImage(XComponent document, OfficeResourceProvider officeResourceProvider, XText destination, XTextRange textRange,
                               Image image) throws Exception {
        XMultiServiceFactory xFactory = asXMultiServiceFactory(document);
        XComponentContext xComponentContext = officeResourceProvider.getXComponentContext();
        XMultiComponentFactory serviceManager = xComponentContext.getServiceManager();

        Object oImage = xFactory.createInstance(TEXT_GRAPHIC_OBJECT);
        Object oGraphicProvider = serviceManager.createInstanceWithContext(GRAPHIC_PROVIDER_OBJECT, xComponentContext);

        XGraphicProvider xGraphicProvider = asXGraphicProvider(oGraphicProvider);

        XPropertySet imageProperties = buildImageProperties(xGraphicProvider, oImage, image.imageContent);
        XTextContent xTextContent = asXTextContent(oImage);
        destination.insertTextContent(textRange, xTextContent, true);
        setImageSize(image.width, image.height, oImage, imageProperties);
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

    protected class Image {
        byte[] imageContent = null;
        int width = 0;
        int height = 0;

        public Image(Object paramValue, Matcher paramsMatcher) {
            if (paramValue == null) {
                return;
            }

            imageContent = getContent(paramValue);
            if (imageContent.length == 0) {
                imageContent = null;
                return;
            }

            width = Integer.parseInt(paramsMatcher.group(1));
            height = Integer.parseInt(paramsMatcher.group(2));
        }

        boolean isValid() {
            return imageContent != null;
        }
    }

}
