package com.haulmont.yarg.formatters.impl.xlsx.hints;

import com.haulmont.yarg.formatters.impl.xlsx.Document;
import com.haulmont.yarg.formatters.impl.xlsx.StyleSheet;
import com.haulmont.yarg.structure.BandData;
import org.xlsx4j.sml.Cell;

public class CustomCellStyleXlsxHint extends AbstractXlsxHint {
    protected Document document;

    public CustomCellStyleXlsxHint(Document document) {
        this.document = document;
    }

    @Override
    public String getName() {
        return "style";
    }

    @Override
    public void apply() {
        StyleSheet styleSheet = document.getStyleSheet();
        for (AbstractXlsxHint.DataObject dataObject : data) {
            Cell resultCell = dataObject.resultCell;
            BandData bandData = dataObject.bandData;

            if (dataObject.params.size() == 0) continue;

            String styleParamName = dataObject.params.get(0);

            String styleName = (String) bandData.getParameterValue(styleParamName);
            if (styleName == null) continue;

            StyleSheet.CellStyle namedStyle = styleSheet.getNamedStyle(styleName);
            if (namedStyle == null) continue;

            if (resultCell.getS() == 0) {
                //cell with default style. Find cellXfs. If exists set it on cell,
                //else create new cell xfs and set it on cell
                StyleSheet.CellXfs cellXfs = new StyleSheet.CellXfs(namedStyle.getXfId(),
                        namedStyle.getNumFmtId(),
                        namedStyle.getFontId(),
                        namedStyle.getFillId(),
                        namedStyle.getBorderId());
                long idx = styleSheet.getCellXfsIndex(cellXfs);
                if (idx > 0) {
                    resultCell.setS(idx);
                } else {
                    styleSheet.addCellXfs(cellXfs);
                    resultCell.setS(styleSheet.getCellXfsIndex(cellXfs));
                }
            } else {
                //find cellXfs
                StyleSheet.CellXfs cellXfs = styleSheet.getCellStyle(resultCell.getS());
                if (cellXfs == null) continue;

                StyleSheet.CellXfs newCellXfs = new StyleSheet.CellXfs(
                        namedStyle.getXfId(),
                        namedStyle.getNumFmtId(),
                        namedStyle.getFontId(),
                        namedStyle.getFillId(),
                        cellXfs.getBorderId());
                long idx = styleSheet.getCellXfsIndex(newCellXfs);
                if (idx > 0) {
                    resultCell.setS(idx);
                } else {
                    styleSheet.addCellXfs(newCellXfs);
                    resultCell.setS(styleSheet.getCellXfsIndex(newCellXfs));
                }
            }
        }
        styleSheet.saveStyle();
    }
}

