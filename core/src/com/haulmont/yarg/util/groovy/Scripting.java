/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.util.groovy;

import java.util.Map;

/**
 * This interface describes logic which evaluates groovy scripts
 */
public interface Scripting {
    <T> T evaluateGroovy(String script,Map<String, Object> params);
}
