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

import com.sun.star.lib.util.NativeLibraryLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Starts and stops an OOo server.
 * Most of the source code in this class has been taken from the Java class
 * "Bootstrap.java" (Revision: 1.15) from the UDK projekt (Uno Software Develop-
 * ment Kit) from OpenOffice.org (http://udk.openoffice.org/). The source code
 * is available for example through a browser based online version control
 * access at http://udk.openoffice.org/source/browse/udk/. The Java class
 * "Bootstrap.java" is there available at
 * http://udk.openoffice.org/source/browse/udk/javaunohelper/com/sun/star/comp/helper/Bootstrap.java?view=markup
 */
public class OOServer {
    protected static final Logger log = LoggerFactory.getLogger(OOServer.class);
    /**
     * The OOo server process.
     */
    private Process oooProcess;

    /**
     * The folder of the OOo installation containing the soffice executable.
     */
    private String oooExecFolder;


    private String host;

    private int port;

    /**
     * The options for starting the OOo server.
     */
    private List oooOptions;

    private ProcessManager processManager;

    /**
     * Constructs an OOo server which uses the folder of the OOo installation
     * containing the soffice executable and a given list of options to start
     * OOo.
     *
     * @param oooExecFolder The folder of the OOo installation containing the soffice executable
     * @param oooOptions    The list of options
     */
    public OOServer(String oooExecFolder, List oooOptions, String host, int port, ProcessManager processManager) {
        this.oooProcess = null;
        this.oooExecFolder = oooExecFolder;
        this.host = host;
        this.port = port;
        this.oooOptions = oooOptions;
        this.processManager = processManager;
    }

    /**
     * Starts an OOo server which uses the specified accept option.
     * The accept option can be used for two different types of connections:
     * 1) The socket connection
     * 2) The named pipe connection
     * To create a socket connection a host and port must be provided.
     * For example using the host "localhost" and the port "8100" the
     * accept option looks like this:
     * - accept option    : -accept=socket,host=localhost,port=8100;urp;
     * To create a named pipe a pipe name must be provided. For example using
     * the pipe name "oooPipe" the accept option looks like this:
     * - accept option    : -accept=pipe,name=oooPipe;urp;
     */
    public synchronized void start() throws BootstrapException, IOException {
        // find office executable relative to this class's class loader
        String sOffice = System.getProperty("os.name").startsWith("Windows") ? "soffice.exe" : "soffice";
        //accept option !Note! we are using old version notation (- instead of --) to support old version of office
        String oooAcceptOption = "-accept=socket,host=" + host + ",port=" + port + ",tcpNoDelay=1;urp;";

        URL[] oooExecFolderURL = new URL[]{new File(oooExecFolder).toURI().toURL()};
        URLClassLoader loader = new URLClassLoader(oooExecFolderURL);
        File fOffice = NativeLibraryLoader.getResource(loader, sOffice);
        if (fOffice == null)
            throw new BootstrapException("no office executable found!");

        // create call with arguments
        int arguments = (oooOptions != null) ? oooOptions.size() + 1 : 1;
        arguments++;
        String[] oooCommand = new String[arguments];
        oooCommand[0] = fOffice.getPath();

        for (int i = 0; i < oooOptions.size(); i++) {
            oooCommand[i + 1] = (String) oooOptions.get(i);
        }

        oooCommand[arguments - 1] = oooAcceptOption;

        // start office process
        oooProcess = Runtime.getRuntime().exec(oooCommand);

        pipe(oooProcess.getInputStream(), "OUT");
        pipe(oooProcess.getErrorStream(), "ERR");
    }

    /**
     * Kills the OOo server process from the previous start.
     * If there has been no previous start of the OOo server, the kill does
     * nothing.
     * If there has been a previous start, kill destroys the process.
     */
    public synchronized void kill() {
        if (oooProcess != null) {
            log.info("OOServer is killing office instance with port {}", port);
            List<Long> pids = processManager.findPid(host, port);
            processManager.kill(oooProcess, pids);
            oooProcess = null;
        }
    }

    protected void pipe(final InputStream in, final String prefix) {
        new Thread(String.format("OOServer: %s", prefix)) {
            @Override
            public void run() {
                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                try {
                    for (; ; ) {
                        String s = r.readLine();
                        if (s == null) {
                            break;
                        }
                        log.debug("{}: {}", prefix, s);
                    }
                } catch (IOException e) {
                    log.debug("OOServer error:", e);
                }
            }
        }.start();
    }

    /**
     * Returns the list of default options.
     * !Note! we are using old version notation (- instead of --) to support old version of office
     *
     * @return The list of default options
     */
    public static List<String> getDefaultOOoOptions() {

        ArrayList<String> options = new ArrayList<String>();

        options.add("-nologo");
        options.add("-nodefault");
        options.add("-norestore");
        options.add("-nocrashreport");
        options.add("-nolockcheck");
        options.add("-nofirststartwizard");
        options.add("-headless");

        return options;
    }
}