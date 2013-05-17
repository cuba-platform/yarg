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

import com.haulmont.newreport.formatters.impl.doc.connector.OOConnection;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.lang.XComponent;

public class OfficeComponent {
    private OOConnection officeConnection;
    private XComponentLoader officeLoader;
    private XComponent officeComponent;

    public OfficeComponent(OOConnection officeConnection, XComponentLoader officeLoader, XComponent officeComponent) {
        this.officeConnection = officeConnection;
        this.officeLoader = officeLoader;
        this.officeComponent = officeComponent;
    }

    public OOConnection getOfficeConnection() {
        return officeConnection;
    }

    public XComponentLoader getOfficeLoader() {
        return officeLoader;
    }

    public XComponent getOfficeComponent() {
        return officeComponent;
    }
}