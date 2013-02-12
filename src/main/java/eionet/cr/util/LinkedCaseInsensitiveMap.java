/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.util;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * A robust case-insensitive implementation that implements java.util.Map<String, V>, and uses LinkedHashMap beneath.
 * Case-insensitive means that methods like containsKey(Object key), get(Object key), remove(Object key), put(key, value) and
 * putAll(Map<? extends String, ? extends V> map) operate case-insensitively. Methods like keySet() and entrySet() return keys in
 * their original case.
 *
 * remove(Object key) removes all mappings where the keys match to the given key case-insensitively!
 *
 * @author jaanus
 */
public class LinkedCaseInsensitiveMap<V> implements java.util.Map<String, V> {

    /** */
    private LinkedHashMap<String, V> originalKeysMap;

    /** */
    private LinkedHashMap<String, V> lowerCaseKeysMap;

    /**
     * Simple constructor.
     */
    public LinkedCaseInsensitiveMap() {
        originalKeysMap = new LinkedHashMap<String, V>();
        lowerCaseKeysMap = new LinkedHashMap<String, V>();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#clear()
     */
    @Override
    public void clear() {
        originalKeysMap.clear();
        lowerCaseKeysMap.clear();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object key) {
        return lowerCaseKeysMap.containsKey(key == null ? key : key.toString().toLowerCase());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(Object value) {
        return originalKeysMap.containsValue(value);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, V>> entrySet() {
        return originalKeysMap.entrySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#get(java.lang.Object)
     */
    @Override
    public V get(Object key) {
        return lowerCaseKeysMap.get(key == null ? key : key.toString().toLowerCase());
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return originalKeysMap.isEmpty();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#keySet()
     */
    @Override
    public Set<String> keySet() {
        return originalKeysMap.keySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public V put(String key, V value) {

        V previousValue = get(key);

        if (key == null) {
            originalKeysMap.put(key, value);
            lowerCaseKeysMap.put(key, value);
        } else {

            if (containsKey(key)) {
                removeFromOriginalKeysMap(key);
            }
            originalKeysMap.put(key, value);
            lowerCaseKeysMap.put(key == null ? key : key.toLowerCase(), value);
        }
        return previousValue;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends V> map) {

        if (map == null || map.isEmpty()) {
            return;
        }

        for (Map.Entry<? extends String, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#remove(java.lang.Object)
     */
    @Override
    public V remove(Object key) {

        V previousValue = get(key);
        lowerCaseKeysMap.remove(key == null ? key : key.toString().toLowerCase());
        removeFromOriginalKeysMap(key);
        return previousValue;
    }

    /**
     * Removes from original map all mappings where key matches the given input key regardless of case.
     * @param key
     */
    private void removeFromOriginalKeysMap(Object key) {

        if (key == null) {
            originalKeysMap.remove(key);
        } else {
            String keyLowerCase = key.toString().toLowerCase();
            Set<String> keySet = originalKeysMap.keySet();
            for (String originalKey : keySet) {
                if (originalKey != null && originalKey.toString().toLowerCase().equals(keyLowerCase)) {
                    originalKeysMap.remove(originalKey);
                    break;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#size()
     */
    @Override
    public int size() {
        return originalKeysMap.size();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.util.Map#values()
     */
    @Override
    public Collection<V> values() {
        return originalKeysMap.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return originalKeysMap.toString();
    }

    /**
     *
     * @return
     */
    private LinkedHashMap<String, V> getLowerKeysMap() {
        return lowerCaseKeysMap;
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        LinkedCaseInsensitiveMap<Long> map = new LinkedCaseInsensitiveMap<Long>();
        map.put("jAAnus", 1L);

        System.out.println("map = " + map);
        System.out.println("getLowerKeysMap() = " + map.getLowerKeysMap().toString());

        map.put("jaAnuS", 2L);

        System.out.println("map = " + map);
        System.out.println("getLowerKeysMap() = " + map.getLowerKeysMap().toString());

        map.put(null, 1L);

        System.out.println("map = " + map);
        System.out.println("getLowerKeysMap() = " + map.getLowerKeysMap().toString());

        map.put("enriko", 3L);

        System.out.println("map = " + map);
        System.out.println("getLowerKeysMap() = " + map.getLowerKeysMap().toString());

        map.put("risto", 4L);

        System.out.println("map = " + map);
        System.out.println("getLowerKeysMap() = " + map.getLowerKeysMap().toString());

        System.out.println("map.get(\"JAANUS\") = " + map.get("JAANUS"));
        map.remove("Jaanus");

        System.out.println("map = " + map);
        System.out.println("getLowerKeysMap() = " + map.getLowerKeysMap().toString());

        System.out.println();
    }
}
