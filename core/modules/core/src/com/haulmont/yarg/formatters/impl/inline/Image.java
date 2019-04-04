/*
 * Copyright 2019 Haulmont
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

package com.haulmont.yarg.formatters.impl.inline;

import com.haulmont.yarg.exception.ReportFormattingException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Image {

    public static String REGULAR_EXPRESSION_SIZE = "^([0-9]+?|AUTO)x([0-9]+?|AUTO)$";
    public static String REGULAR_EXPRESSION_RECT = "rect\\(([0-9]+?)x([0-9]+?)\\)";

    private byte[] imageContent;
    private int width;
    private int height;


    public Image(Object paramValue, Matcher paramsMatcher, AbstractInliner abstractInliner) {

        if (paramValue == null) {
            return;
        }

        imageContent = abstractInliner.getContent(paramValue, paramsMatcher);
        if (imageContent.length == 0) {
            imageContent = null;
            return;
        }
        initSize(paramsMatcher, abstractInliner);
    }

    private void initSize(Matcher paramsMatcher, AbstractInliner abstractInliner) {
        boolean rectType = false;
        Class classInliner = abstractInliner.getClass();
        if (classInliner.equals(ImageAllContentInliner.class)) {
            String size = paramsMatcher.group(2);
            Pattern pattern;
            if (size.contains("rect")) {
                pattern = Pattern.compile(REGULAR_EXPRESSION_RECT);
                rectType = true;
            } else {
                pattern = Pattern.compile(REGULAR_EXPRESSION_SIZE);
            }

            Matcher matcher = pattern.matcher(size);
            if (matcher.find()) {
                String width = matcher.group(1);
                String height = matcher.group(2);
                try {
                    setImageSize(height, width, abstractInliner, rectType);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                throw new ReportFormattingException("No correct formatter " + paramsMatcher.group());
            }
        } else {
            width = Integer.parseInt(paramsMatcher.group(1));
            height = Integer.parseInt(paramsMatcher.group(2));
        }
    }

    private void setImageSize(String height, String width, AbstractInliner abstractInliner, boolean rectType) throws IOException {

        ByteArrayInputStream imageContentInputStream = new ByteArrayInputStream(imageContent);
        BufferedImage bufferedImage = ImageIO.read(imageContentInputStream);

        double originalHeight = bufferedImage.getHeight();
        double originalWidth = bufferedImage.getWidth();

        double relation = originalHeight / originalWidth;

        if (abstractInliner.getClass().equals(ImageAllContentInliner.class)) {
            if (rectType) {
                int targetHeight = Integer.valueOf(height);
                int targetWidth = Integer.valueOf(width);

                double widthScale = (double) targetWidth / originalWidth;
                double heightScale = (double) targetHeight / originalHeight;
                double actualScale = Math.min(widthScale, heightScale);

                this.width = (int) Math.round(originalWidth * actualScale);
                this.height = (int) Math.round(originalHeight * actualScale);
            } else if (height.equals("AUTO") && width.equals("AUTO")) {
                this.height = (int) originalHeight;
                this.width = (int) originalWidth;
            } else if (height.equals("AUTO")) {
                this.width = Integer.valueOf(width);
                this.height = (int) (this.width * relation);
            } else if (width.equals("AUTO")) {
                this.height = Integer.valueOf(height);
                this.width = (int) (this.height / relation);
            } else {
                this.height = Integer.valueOf(height);
                this.width = Integer.valueOf(width);
            }
        } else {
            this.height = Integer.valueOf(height);
            this.width = Integer.valueOf(width);
        }
    }

    public byte[] getImageContent() {
        return imageContent;
    }

    public void setImageContent(byte[] imageContent) {
        this.imageContent = imageContent;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    boolean isValid() {
        return imageContent != null;
    }
}
