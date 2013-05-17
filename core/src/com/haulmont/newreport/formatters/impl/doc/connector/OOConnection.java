/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 12.10.2010 19:21:36
 *
 * $Id$
 */
package com.haulmont.newreport.formatters.impl.doc.connector;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

import java.util.List;

import static com.haulmont.newreport.formatters.impl.doc.ODTUnoConverter.*;

public class OOConnection {
    protected XComponentContext xComponentContext;
    protected String openOfficePath;
    protected OOServer oooServer;
    protected Integer port;
    protected OOConnector connector;
    protected BootstrapSocketConnector bsc;

    public OOConnection(String openOfficePath, Integer port, ProcessManager processManager, OOConnector connector) {
        this.port = port;
        this.connector = connector;

        List oooOptions = OOServer.getDefaultOOoOptions();
        oooServer = new OOServer(openOfficePath, oooOptions, "localhost", port, processManager);
        bsc = new BootstrapSocketConnector(oooServer);
        this.openOfficePath = openOfficePath;
    }

    public XDesktop createDesktop() throws Exception {
        Object o = xComponentContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.frame.Desktop", xComponentContext);
        return asXDesktop(o);
    }

    public XComponentLoader createXComponentLoader() throws Exception {
        return asXComponentLoader(createDesktop());
    }

    public XDispatchHelper createXDispatchHelper() throws Exception {
        Object o = xComponentContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.frame.DispatchHelper", xComponentContext);
        return asXDispatchHelper(o);
    }

    public XComponentContext getxComponentContext() {
        return xComponentContext;
    }

    public Integer getPort() {
        return port;
    }

    public String getOpenOfficePath() {
        return openOfficePath;
    }

    void open() throws BootstrapException {
        xComponentContext = bsc.connect("localhost", port);
    }

    void close() {
        bsc.disconnect();
    }

    void finallyHandleClose() {
        oooServer.kill();
        connector.putPortBack(port);
    }
}