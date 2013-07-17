/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.xml;

import com.haulmont.yarg.structure.Report;

/**
 * This class describes logic which convert report to xml string
 */
public interface XmlWriter {
    String buildXml(Report report);
}
