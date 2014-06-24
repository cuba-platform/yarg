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

/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.formatters.impl.doc.connector;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;

import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.asXComponentLoader;
import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.asXDesktop;
import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.asXDispatchHelper;

public class OfficeResourceProvider {
    protected XComponentContext xComponentContext;
    protected XDesktop xDesktop;
    protected XDispatchHelper xDispatchHelper;
    protected XComponentLoader xComponentLoader;

    public OfficeResourceProvider(XComponentContext xComponentContext) throws Exception {
        this.xComponentContext = xComponentContext;
        xDesktop = createDesktop();
        xDispatchHelper = createXDispatchHelper();
        xComponentLoader = asXComponentLoader(xDesktop);
    }

    public XComponentContext getXComponentContext() {
        return xComponentContext;
    }

    public XDesktop getXDesktop() {
        return xDesktop;
    }

    public XDispatchHelper getXDispatchHelper() {
        return xDispatchHelper;
    }

    public XComponentLoader getXComponentLoader() {
        return xComponentLoader;
    }

    protected XDispatchHelper createXDispatchHelper() throws Exception {
        Object o = xComponentContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.frame.DispatchHelper", xComponentContext);
        return asXDispatchHelper(o);
    }

    protected XDesktop createDesktop() throws com.sun.star.uno.Exception {
        Object o = xComponentContext.getServiceManager().createInstanceWithContext(
                "com.sun.star.frame.Desktop", xComponentContext);
        return asXDesktop(o);
    }


}
