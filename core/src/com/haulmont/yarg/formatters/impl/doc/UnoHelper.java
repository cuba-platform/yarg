/*
 * Copyright (c) 2010 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Vasiliy Fontanenko
 * Created: 12.10.2010 19:21:36
 *
 * $Id$
 */
package com.haulmont.yarg.formatters.impl.doc;

import com.haulmont.yarg.exception.OpenOfficeException;
import com.haulmont.yarg.structure.ReportTemplate;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.frame.XStorable;
import com.sun.star.io.IOException;
import com.sun.star.io.XInputStream;
import com.sun.star.io.XOutputStream;
import com.sun.star.lang.XComponent;
import com.sun.star.util.XCloseable;
import org.apache.commons.io.IOUtils;

import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.asXCloseable;
import static com.haulmont.yarg.formatters.impl.doc.UnoConverter.asXStorable;

public final class UnoHelper {
    public static XInputStream getXInputStream(ReportTemplate reportTemplate) {
        try {
            return new OfficeInputStream(IOUtils.toByteArray(reportTemplate.getDocumentContent()));
        } catch (java.io.IOException e) {
            throw new OpenOfficeException("An error occurred while converting template to XInputStream", e);
        }
    }

    public static XComponent loadXComponent(XComponentLoader xComponentLoader, XInputStream inputStream) throws com.sun.star.lang.IllegalArgumentException, IOException {
        PropertyValue[] props = new PropertyValue[2];
        props[0] = new PropertyValue();
        props[1] = new PropertyValue();
        props[0].Name = "InputStream";
        props[0].Value = inputStream;
        props[1].Name = "Hidden";
        props[1].Value = true;
        return xComponentLoader.loadComponentFromURL("private:stream", "_blank", 0, props);
    }

    public static void closeXComponent(XComponent xComponent) {
        XCloseable xCloseable = asXCloseable(xComponent);
        try {
            xCloseable.close(false);
        } catch (com.sun.star.util.CloseVetoException e) {
            xComponent.dispose();
        }
    }

    public static void saveXComponent(XComponent xComponent, XOutputStream xOutputStream, String filterName) throws IOException {
        PropertyValue[] props = new PropertyValue[2];
        props[0] = new PropertyValue();
        props[1] = new PropertyValue();
        props[0].Name = "OutputStream";
        props[0].Value = xOutputStream;
        props[1].Name = "FilterName";
        props[1].Value = filterName;
        XStorable xStorable = asXStorable(xComponent);
        xStorable.storeToURL("private:stream", props);
    }

    public static void copy(XDispatchHelper xDispatchHelper, XDispatchProvider xDispatchProvider) {
        xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:Copy", "", 0, new PropertyValue[]{new PropertyValue()});
    }

    public static void paste(XDispatchHelper xDispatchHelper, XDispatchProvider xDispatchProvider) {
        xDispatchHelper.executeDispatch(xDispatchProvider, ".uno:Paste", "", 0, new PropertyValue[]{new PropertyValue()});
    }
}
