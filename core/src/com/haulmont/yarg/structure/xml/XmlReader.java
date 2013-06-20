/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.xml;

import com.haulmont.yarg.structure.Report;

import java.io.IOException;

public interface XmlReader {
    Report parseXml(String xml) throws IOException;
}
