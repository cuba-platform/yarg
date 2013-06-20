/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.xml;

import com.haulmont.yarg.structure.Report;

public interface XmlWriter {
    String buildXml(Report report);
}
