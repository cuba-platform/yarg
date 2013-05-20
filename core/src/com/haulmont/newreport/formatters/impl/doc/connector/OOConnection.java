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

import com.haulmont.newreport.exception.FailedToConnectToOpenOfficeException;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

class OOConnection {
    protected XComponentContext xComponentContext;
    protected String openOfficePath;
    protected OOServer oooServer;
    protected Integer port;
    protected OOTaskRunner connector;
    protected BootstrapSocketConnector bsc;
    protected OOResourceProvider ooResourceProvider;

    public OOConnection(String openOfficePath, Integer port, ProcessManager processManager, OOTaskRunner connector) {
        try {
            this.port = port;
            this.connector = connector;
            this.oooServer = new OOServer(openOfficePath, OOServer.getDefaultOOoOptions(), "localhost", port, processManager);
            this.bsc = new BootstrapSocketConnector(oooServer);
            this.openOfficePath = openOfficePath;
            this.xComponentContext = bsc.connect("localhost", port);
            this.ooResourceProvider = new OOResourceProvider(xComponentContext);
        } catch (Exception e) {
            throw new FailedToConnectToOpenOfficeException("Unable to create Open office components.", e);
        } catch (BootstrapException e) {
            throw new FailedToConnectToOpenOfficeException("Unable to start Open office instance.", e);
        }
    }

    public OOResourceProvider getOOResourceProvider() {
        return ooResourceProvider;
    }

    void close() {
        bsc.disconnect();
    }

    void releaseResources() {
        oooServer.kill();
        connector.putPortBack(port);
    }
}