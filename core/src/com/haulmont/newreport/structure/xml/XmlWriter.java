/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.structure.xml;

import com.haulmont.newreport.structure.Report;

public interface XmlWriter {
    String buildXml(Report report);
}
