/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Eugeniy Degtyarjov
 * Created: 18.01.2011 11:10:15
 *
 * $Id: HSSFCellHelper.java 10587 2013-02-19 08:40:16Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl.xls;

import com.haulmont.newreport.structure.impl.Band;
import com.haulmont.newreport.structure.impl.ReportValueFormat;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.awt.*;
import java.util.Date;
import java.util.Map;

import static com.haulmont.newreport.formatters.impl.AbstractFormatter.unwrapParameterName;

public final class HSSFCellHelper {
    private HSSFCellHelper() {
    }

    /**
     * Copies template cell to result cell and fills it with band data
     *
     * @param band              - band
     * @param templateCellValue - template cell value
     * @param resultCell        - result cell
     */
    public static void updateValueCell(Band rootBand, Band band, String templateCellValue, HSSFCell resultCell, HSSFPatriarch patriarch) {
        String parameterName = templateCellValue;
        parameterName = unwrapParameterName(parameterName);

        if (StringUtils.isEmpty(parameterName)) return;

        if (!band.getData().containsKey(parameterName)) {
            resultCell.setCellValue((String) null);
            return;
        }

        Object parameterValue = band.getData().get(parameterName);
        Map<String, ReportValueFormat> valuesFormats = rootBand.getValuesFormats();

        if (parameterValue == null) {
            resultCell.setCellType(HSSFCell.CELL_TYPE_BLANK);
        } else if (parameterValue instanceof Number) {
            resultCell.setCellValue(((Number) parameterValue).doubleValue());
        } else if (parameterValue instanceof Boolean) {
            resultCell.setCellValue((Boolean) parameterValue);
        } else if (parameterValue instanceof Date) {
            resultCell.setCellValue((Date) parameterValue);
        } else {
            if (valuesFormats.containsKey(parameterName)) {
                String formatString = valuesFormats.get(parameterName).getFormatString();
                ImageExtractor imageExtractor = new ImageExtractor(formatString, parameterValue);

                if (ImageExtractor.isImage(formatString)) {
                    ImageExtractor.Image image = imageExtractor.extract();
                    if (image != null) {
                        int targetHeight = image.getHeight();
                        resultCell.getRow().setHeightInPoints(targetHeight);
                        HSSFSheet sheet = resultCell.getSheet();
                        HSSFWorkbook workbook = sheet.getWorkbook();

                        int pictureIdx = workbook.addPicture(image.getContent(), Workbook.PICTURE_TYPE_JPEG);

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
                        picture.resize((double) targetHeight / actualHeight);
                        return;
                    }
                }
            }
            resultCell.setCellValue(new HSSFRichTextString(parameterValue.toString()));
        }
    }

    /**
     * Detects if cell contains only one template to inleine value
     *
     * @param cell - cell
     * @return -
     */
    public static boolean isOneValueCell(HSSFCell cell, String value) {
        boolean result = true;
        if (cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            if (value.lastIndexOf("${") != 0) {
                result = false;
            } else {
                result = value.indexOf("}") == value.length() - 1;
            }
        }
        return result;
    }

    public static HSSFCell getCellFromReference(CellReference cref, HSSFSheet templateSheet) {
        return getCellFromReference(templateSheet, cref.getCol(), cref.getRow());
    }

    public static HSSFCell getCellFromReference(HSSFSheet templateSheet, int colIndex, int rowIndex) {
        HSSFRow row = templateSheet.getRow(rowIndex);
        row = row == null ? templateSheet.createRow(rowIndex) : row;
        HSSFCell cell = row.getCell(colIndex);
        cell = cell == null ? row.createCell(colIndex) : cell;
        return cell;
    }
}
