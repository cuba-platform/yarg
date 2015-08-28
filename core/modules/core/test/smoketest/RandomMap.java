package smoketest;

import java.util.*;

/**
 * @author degtyarjov
 * @version $Id$
 */
public class RandomMap implements Map<String, Object> {
    protected Random random = new Random();
    protected Integer maximum = 1000000;

    public RandomMap() {
    }

    public RandomMap(Integer maximum) {
        this.maximum = maximum;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return Math.abs(maximum != null ? random.nextInt() % maximum : random.nextInt());
    }

    @Override
    public Object put(String key, Object value) {
        return null;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map m) {

    }

    @Override
    public void clear() {

    }

    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection<Object> values() {
        return Collections.emptyList();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.emptySet();
    }
}
