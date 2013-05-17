/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.newreport.util.groovy;

import java.util.Map;

public interface Scripting {
    <T> T evaluateGroovy(String script,Map<String, Object> params);
}
