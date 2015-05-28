package com.haulmont.yarg.formatters.impl.docx;

import com.haulmont.yarg.formatters.impl.DocxFormatterDelegate;
import com.haulmont.yarg.structure.BandData;
import org.docx4j.TraversalUtil;
import org.docx4j.XmlUtils;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

/**
* @author degtyarjov
* @version $Id$
*/
public class TableManager {
    private DocxFormatterDelegate docxFormatter;
    protected Tbl table;
    protected Tr firstRow = null;
    protected Tr rowWithAliases = null;
    protected String bandName = null;
    public final AliasVisitor INVARIANTS_SETTER;

    TableManager(DocxFormatterDelegate docxFormatter, Tbl tbl) {
        this.docxFormatter = docxFormatter;
        this.table = tbl;
        INVARIANTS_SETTER = new AliasVisitor(docxFormatter) {
            @Override
            protected void handle(Text text) {

            }
        };
    }

    public Tr copyRow(Tr row) {
        Tr copiedRow = XmlUtils.deepCopy(row);
        new TraversalUtil(copiedRow, INVARIANTS_SETTER);//set parent for each sub-element of copied row (otherwise parent would be JaxbElement)
        int index = table.getContent().indexOf(row);
        table.getContent().add(index, copiedRow);
        return copiedRow;
    }

    public void fillRowFromBand(Tr row, final BandData band) {
        new TraversalUtil(row, new AliasVisitor(docxFormatter) {
            @Override
            protected void handle(Text text) {
                String textValue = text.getValue();
                if (docxFormatter.containsJustOneAlias(textValue)) {//todo eude not only one value cells?
                    String parameterName = docxFormatter.unwrapParameterName(textValue);
                    String fullParameterName = bandName + "." + parameterName;
                    Object parameterValue = band.getParameterValue(parameterName);

                    if (docxFormatter.tryToApplyInliners(fullParameterName, parameterValue, text)) return;
                }

                String resultString = docxFormatter.insertBandDataToString(band, textValue);
                text.setValue(resultString);
                text.setSpace("preserve");
            }
        });
    }

    public Tbl getTable() {
        return table;
    }

    public Tr getFirstRow() {
        return firstRow;
    }

    public Tr getRowWithAliases() {
        return rowWithAliases;
    }

    public String getBandName() {
        return bandName;
    }
}
