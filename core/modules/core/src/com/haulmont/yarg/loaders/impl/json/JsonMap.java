package com.haulmont.yarg.loaders.impl.json;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Degtyarjov Eugene
 * Date: 08.04.14
 * $Id$
 */
public class JsonMap implements Map<String, Object> {
    private Map<String, Object> instance;

    public JsonMap(Map<String, Object> entity) {
        instance = entity;
    }

    @Override
    public int size() {
        return instance.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return instance.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return instance.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return getValue(instance, key != null ? key.toString() : null);
    }

    @Override
    public Object put(String key, Object value) {
        return instance.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return instance.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        instance.putAll(m);
    }

    @Override
    public void clear() {
        instance.clear();
    }

    @Override
    public Set<String> keySet() {
        return instance.keySet();
    }

    @Override
    public Collection<Object> values() {
        return instance.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return instance.entrySet();
    }

    protected Object getValue(Map instance, String key) {
        if (key == null) return null;
        if (key.contains(".")) {
            String thisLevelProperty = StringUtils.substringBefore(key, ".");
            String remainingPath = StringUtils.substringAfter(key, ".");

            Object value = instance.get(thisLevelProperty);
            if (value instanceof Map) {
                return getValue((Map) value, remainingPath);
            } else {
                return null;
            }
        } else {
            return instance.get(key);
        }
    }
}

