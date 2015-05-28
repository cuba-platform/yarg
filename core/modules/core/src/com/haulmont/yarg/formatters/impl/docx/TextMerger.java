package com.haulmont.yarg.formatters.impl.docx;

import org.docx4j.XmlUtils;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
* @author degtyarjov
* @version $Id$
*/
public class TextMerger {
    protected Set<Text> resultingTexts = new HashSet<Text>();
    protected Set<Text> textsToRemove = new HashSet<Text>();

    protected Text startText = null;
    protected Set<Text> mergedTexts = null;
    protected StringBuilder mergedTextsString = null;
    protected Pattern regexpPattern;
    protected P paragraph;
    protected String regexp;
    protected String first2SymbolsOfRegexp;

    public TextMerger(P paragraph, String regexp) {
        this.paragraph = paragraph;
        this.regexp = regexp;
        this.regexpPattern = Pattern.compile(regexp);
        this.first2SymbolsOfRegexp = regexp.replaceAll("\\\\", "").substring(0, 2);
    }

    public Set<Text> mergeMatchedTexts() {
        for (Object paragraphContentObject : paragraph.getContent()) {
            if (paragraphContentObject instanceof R) {
                R currentRun = (R) paragraphContentObject;
                for (Object runContentObject : currentRun.getContent()) {
                    Object unwrappedRunContenObject = XmlUtils.unwrap(runContentObject);
                    if (unwrappedRunContenObject instanceof Text) {
                        handleText((Text) unwrappedRunContenObject);
                    }
                }
            }
        }

        removeUnnecessaryTexts();

        return resultingTexts;
    }

    protected void removeUnnecessaryTexts() {
        for (Text text : textsToRemove) {
            Object parent = XmlUtils.unwrap(text.getParent());
            if (parent instanceof R) {
                ((R) parent).getContent().remove(text);
            }
        }
    }

    protected void handleText(Text currentText) {
        if (startText == null && containsStartOfRegexp(currentText.getValue())) {
            initMergeQueue(currentText);
        }

        if (startText != null) {
            addToMergeQueue(currentText);

            if (mergeQueueMatchesRegexp()) {
                handleMatchedText();
            }
        }
    }

    private void initMergeQueue(Text currentText) {
        startText = currentText;
        mergedTexts = new HashSet<Text>();
        mergedTextsString = new StringBuilder();
    }

    private boolean containsStartOfRegexp(String text) {
        return text.contains(first2SymbolsOfRegexp);
    }

    protected void addToMergeQueue(Text currentText) {
        mergedTexts.add(currentText);
        mergedTextsString.append(currentText.getValue());
    }

    protected boolean mergeQueueMatchesRegexp() {
        return regexpPattern.matcher(mergedTextsString).find();
    }

    protected void handleMatchedText() {
        resultingTexts.add(startText);
        startText.setValue(mergedTextsString.toString());
        for (Text mergedText : mergedTexts) {
            if (mergedText != startText) {
                mergedText.setValue("");
                textsToRemove.add(mergedText);
            }
        }

        if (!containsStartOfRegexp(startText.getValue().replace(regexp, ""))) {
            startText = null;
            mergedTexts = null;
            mergedTextsString = null;
        } else {
            mergedTexts = new HashSet<Text>();
            mergedTexts.add(startText);
            mergedTextsString = new StringBuilder(startText.getValue());
        }
    }
}
