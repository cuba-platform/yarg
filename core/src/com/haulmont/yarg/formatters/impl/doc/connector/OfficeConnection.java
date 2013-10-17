/*
 * Copyright 2013 Haulmont
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.haulmont.yarg.formatters.impl.doc.connector;

import com.haulmont.yarg.exception.OpenOfficeException;
import com.sun.star.comp.helper.BootstrapException;
import com.sun.star.uno.Exception;
import com.sun.star.uno.XComponentContext;

public class OfficeConnection {
    protected XComponentContext xComponentContext;
    protected String openOfficePath;
    protected OOServer oooServer;
    protected Integer port;
    protected OfficeIntegration connector;
    protected BootstrapSocketConnector bsc;
    protected OfficeResourceProvider officeResourceProvider;

    public OfficeConnection(String openOfficePath, Integer port, ProcessManager processManager, OfficeIntegration connector) {
        try {
            this.port = port;
            this.connector = connector;
            this.oooServer = new OOServer(openOfficePath, OOServer.getDefaultOOoOptions(), "localhost", port, processManager);
            this.bsc = new BootstrapSocketConnector(oooServer);
            this.openOfficePath = openOfficePath;
            this.xComponentContext = bsc.connect("localhost", port);
            this.officeResourceProvider = new OfficeResourceProvider(xComponentContext);
        } catch (Exception e) {
            throw new OpenOfficeException("Unable to create Open office components.", e);
        } catch (BootstrapException e) {
            throw new OpenOfficeException("Unable to start Open office instance.", e);
        }
    }

    public OfficeResourceProvider getOOResourceProvider() {
        return officeResourceProvider;
    }

    public void close() {
        bsc.disconnect();
    }

    public void releaseResources() {
        oooServer.kill();
        connector.putPortBack(port);
    }
}