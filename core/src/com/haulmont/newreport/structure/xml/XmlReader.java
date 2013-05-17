/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.xml;

import com.haulmont.newreport.structure.Report;

import java.io.IOException;

public interface XmlReader {
    Report parseXml(String xml) throws IOException;
}
