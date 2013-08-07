/*
 * Copyright (c) 2012 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.xls.hints;

/**
 * @author artamonov
 * @version $Id: CopyColumnHint.java 9328 2012-10-18 15:28:32Z artamonov $
 */
public class CopyColumnHint extends AbstractHint {
    public CopyColumnHint() {
        super("##copyColumnWidth");
    }

    @Override
    public void apply() {
        for (DataObject dataObject : data) {
            dataObject.resultCell.getSheet().setColumnWidth(dataObject.resultCell.getColumnIndex(), dataObject.templateCell.getSheet().getColumnWidth(dataObject.templateCell.getColumnIndex()));
        }
    }
}