package com.haulmont.yarg.formatters.impl.docx;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;

public class HtmlImportProcessorImpl implements HtmlImportProcessor {
    @Override
    public String processHtml(String source) {
        org.jsoup.nodes.Document document = Jsoup.parse(source);
        processHtmlDocument(document);
        document.outputSettings()
                .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml)
                .prettyPrint(false)
                .escapeMode(Entities.EscapeMode.xhtml);


        return document.html();
    }

    protected void processHtmlDocument(org.jsoup.nodes.Document document) {
        processFontColor(document);
    }

    protected void processFontColor(org.jsoup.nodes.Document document) {
        Elements elements = document.getElementsByTag("font");
        for (Element element : elements) {
            String color = element.attr("color");
            if (color != null) {
                String style = StringUtils.trim(element.attr("style"));
                if (style != null) {
                    if (StringUtils.endsWith(style, ";")) {
                        style = style + ";";
                    }
                    style = style + "color:" + color;
                } else {
                    style = "color:" + color;
                }
                element.attr("style", style);
                element.removeAttr("color");
            }
        }
    }
}
