/*
 * Copyright (c) 2011 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Artamonov Yuryi
 * Created: 11.03.11 18:46
 *
 * $Id: OfficeComponent.java 10343 2013-01-25 12:11:33Z degtyarjov $
 */
package com.haulmont.newreport.formatters.impl.doc;

import com.haulmont.newreport.formatters.impl.doc.connector.OOResourceProvider;
import com.sun.star.lang.XComponent;

public class OfficeComponent {
    private XComponent officeComponent;
    private OOResourceProvider ooResourceProvider;

    public OfficeComponent(OOResourceProvider ooResourceProvider, XComponent xComponent) {
        this.ooResourceProvider = ooResourceProvider;
        this.officeComponent = xComponent;
    }

    public OOResourceProvider getOoResourceProvider() {
        return ooResourceProvider;
    }

    public XComponent getOfficeComponent() {
        return officeComponent;
    }
}