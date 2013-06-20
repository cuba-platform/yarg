/**
 *
 * @author degtyarjov
 * @version $Id$
 */
package com.haulmont.yarg.util.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.util.Map;

public class DefaultScriptingImpl implements Scripting {
    @Override
    public <T> T evaluateGroovy(String script, Map<String, Object> params) {
        Binding binding = new Binding(params);
        GroovyShell shell = new GroovyShell(Thread.currentThread().getContextClassLoader(), binding);
        return (T) shell.evaluate(script);
    }
}
