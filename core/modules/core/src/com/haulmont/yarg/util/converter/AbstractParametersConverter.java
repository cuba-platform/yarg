package com.haulmont.yarg.util.converter;

import com.haulmont.yarg.exception.ReportingException;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.apache.commons.lang.reflect.MethodUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author degtyarjov
 * @version $Id$
 */
public abstract class AbstractParametersConverter  implements ParametersConverter {
    protected Object convertFromStringUnresolved(Class parameterClass, String paramValueStr) {
        try {
            Constructor constructor = ConstructorUtils.getAccessibleConstructor(parameterClass, String.class);
            if (constructor != null) {
                return constructor.newInstance(paramValueStr);
            } else {
                Method valueOf = MethodUtils.getAccessibleMethod(parameterClass, "valueOf", String.class);
                if (valueOf != null) {
                    return valueOf.invoke(null, paramValueStr);
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new ReportingException(
                    String.format("Could not instantiate object with class [%s] from [%s] string.",
                            parameterClass.getCanonicalName(),
                            paramValueStr));
        }
        return paramValueStr;
    }
}
