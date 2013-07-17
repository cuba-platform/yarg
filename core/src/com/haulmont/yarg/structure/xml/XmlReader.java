/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.structure.xml;

import com.haulmont.yarg.structure.Report;

import java.io.IOException;

/**
 * This class describes logic which read report from xml string
 */
public interface XmlReader {
    Report parseXml(String xml) throws IOException;
}
