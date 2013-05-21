/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.formatters.impl.doc.connector;

import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.uno.*;
import com.sun.star.uno.Exception;

import static com.haulmont.newreport.formatters.impl.doc.UnoConverter.asXComponentLoader;
import static com.haulmont.newreport.formatters.impl.doc.UnoConverter.asXDesktop;
import static com.haulmont.newreport.formatters.impl.doc.UnoConverter.asXDispatchHelper;

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
