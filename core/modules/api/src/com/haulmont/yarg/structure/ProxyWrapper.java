package com.haulmont.yarg.structure;

/**
 * This interface used for proxy wrapping
 *
 * <p>ex: com.haulmont.yarg.reporting.extraction.preprocessor.SqlCrosstabPreprocessor#preprocess</p>
 */
public interface ProxyWrapper {
    /**
     * Internal.
     * Use {@link ProxyWrapper#unwrap(java.lang.Object)} to unwrap proxied instance
     */
    Object unwrap();

    /**
     * Method checks instance inheritance of ProxyWrapper and unwrapping their real instance
     *
     * @param obj - instance of any object
     * @return unwrapped instance (if proxied) or same object
     */
    static<T> T unwrap(T obj) {
        return obj != null && obj instanceof ProxyWrapper ? (T)((ProxyWrapper)obj).unwrap() : obj;
    }
}
