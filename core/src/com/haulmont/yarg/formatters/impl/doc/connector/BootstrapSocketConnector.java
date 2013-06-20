/*
 * Copyright (c) 2013 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.
 */

package com.haulmont.yarg.formatters.impl.doc.connector;

import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.XComponentContext;

/**
 * A Bootstrap Connector which uses a socket to connect to an OOo server.
 */
class BootstrapSocketConnector extends BootstrapConnector {

    /**
     * Constructs a bootstrap socket connector which connects to the specified
     * OOo server.
     *
     * @param oooServer The OOo server
     */
    public BootstrapSocketConnector(OOServer oooServer) {
        super(oooServer);
    }

    /**
     * Connects to an OOo server using the specified host and port for the
     * socket and returns a component context for using the connection to the
     * OOo server.
     *
     * @param host The host
     * @param port The port
     * @return The component context
     */
    public XComponentContext connect(String host, int port) throws BootstrapException {
        String unoConnectString = "uno:socket,host=" + host + ",port=" + port + ";urp;StarOffice.ComponentContext";
        return connect(unoConnectString);
    }
}