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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.web.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.TagDTO;
import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.Pair;

/**
 * A place to hold all application caches.
 * 
 * @author Aleksandr Ivanov <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class ApplicationCache implements ServletContextListener {
    /**
     * Application (main) cache name.
     */
    public static final String APPLICATION_CACHE = "ApplicationCache";

    /** Delivery search picklist cache name. */
    private static final String DELIVERY_SEARCH_PICKLIST_CACHE = "deliverySearchPicklist";

    /**
     * Localities cache name.
     */
    private static final String LOCALITIES_CACHE = "localitiesCache";
    /**
     * Recent resources cache name.
     */
    private static final String RECENT_RESOURCES_CACHE = "recentResources";
    /**
     * Tag cloud cache name.
     */
    private static final String TAG_CLOUD_CACHE = "tagCloud";
    /**
     * type cache name.
     */
    private static final String TYPE_CACHE = "typeCache";
    private static final String TYPE_URIS_CACHE = "typeUrisCache";

    /**
     * type columns cache name.
     */
    private static final String TYPE_COLUMNS_CACHE = "typeColumnsCache";

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent) {@inheritDoc}
     */
    public void contextDestroyed(final ServletContextEvent arg0) {
        CacheManager.getInstance().shutdown();
    }

    /**
     * Returnes application cache.
     * 
     * @return Cache - main application cache
     */
    private static Cache getCache() {
        return CacheManager.getInstance().getCache(APPLICATION_CACHE);
    }

    /**
     * update recent resource cache.
     * 
     * @param update List<Pair<String, String>> resources to be updated
     */
    public static void updateRecentResourceCache(final List<Pair<String, String>> update) {
        getCache().put(new Element(RECENT_RESOURCES_CACHE, update));
    }

    /**
     * get recently discovered files.
     * 
     * @param limit - how many files to fetch
     * @return List<Pair<String, String>>
     */
    @SuppressWarnings("unchecked")
    public static List<Pair<String, String>> getRecentDiscoveredFiles(final int limit) {
        Element element = getCache().get(RECENT_RESOURCES_CACHE);

        if (element == null || element.getValue() == null) {
            return new LinkedList<Pair<String, String>>();
        }

        List<Pair<String, String>> cache = (List<Pair<String, String>>) element.getValue();

        return new LinkedList<Pair<String, String>>(cache.subList(Math.max(0, cache.size() - limit), cache.size()));
    }

    /**
     * update tag cloud.
     * 
     * @param update List<TagDTO> tags to be updated
     */
    public static void updateTagCloudCache(final List<TagDTO> update) {
        getCache().put(new Element(TAG_CLOUD_CACHE, update));
    }

    /**
     * get tag cloud.
     * 
     * @param limit - how many tags to fetch
     * @return List<TagDTO>
     */
    @SuppressWarnings("unchecked")
    public static List<TagDTO> getTagCloud(final int limit) {
        Element element = getCache().get(TAG_CLOUD_CACHE);

        if (element == null || element.getValue() == null) {
            return new LinkedList<TagDTO>();
        }

        List<TagDTO> cache = (List<TagDTO>) element.getValue();
        // retrunable list size
        int tagCloudListSize = limit == 0 ? cache.size() : limit;

        List<TagDTO> result = new LinkedList<TagDTO>(cache.subList(0, Math.min(cache.size(), tagCloudListSize)));

        return result;

    }

    /**
     * Returns tag cloud list sorted by tag name.
     * 
     * @param limit returned list size
     * @return List<TagDTO>
     */

    public static List<TagDTO> getTagCloudSortedByName(final int limit) {
        List<TagDTO> result = getTagCloud(limit);
        Collections.sort(result, new TagDTO.NameComparatorAsc());

        return result;
    }

    /**
     * Returns tag cloud list sorted by tag count.
     * 
     * @param limit returned list size
     * @return List<TagDTO>
     */
    public static List<TagDTO> getTagCloudSortedByCount(final int limit) {
        List<TagDTO> result = getTagCloud(limit);
        Collections.sort(result, new TagDTO.CountComparatorDesc());

        return result;
    }

    /**
     * Update delivery search picklist cache.
     * 
     * @param picklistCache - picklist cache
     * @param localitiesCache - localities cache
     */
    public static void updateDeliverySearchPicklistCache(final Map<UriLabelPair, ArrayList<UriLabelPair>> picklistCache,
            final Collection<ObjectLabelPair> localitiesCache) {
        getCache().put(new Element(LOCALITIES_CACHE, localitiesCache));
        getCache().put(new Element(DELIVERY_SEARCH_PICKLIST_CACHE, picklistCache));
    }

    /**
     * fetch cached localities.
     * 
     * @return Collection<ObjectLabelPair>
     */
    @SuppressWarnings("unchecked")
    public static Collection<ObjectLabelPair> getLocalities() {
        Element element = getCache().get(LOCALITIES_CACHE);

        return element == null || element.getValue() == null ? Collections.EMPTY_SET : (Collection<ObjectLabelPair>) element
                .getValue();
    }

    /**
     * Fetch delivery search picklist cache.
     * 
     * @return Map<String, List<UriLabelPair>>
     */
    @SuppressWarnings("unchecked")
    public static Map<UriLabelPair, List<UriLabelPair>> getDeliverySearchPicklist() {
        Element element = getCache().get(DELIVERY_SEARCH_PICKLIST_CACHE);

        return element == null || element.getValue() == null ? Collections.EMPTY_MAP
                : (Map<UriLabelPair, List<UriLabelPair>>) getCache().get(DELIVERY_SEARCH_PICKLIST_CACHE).getValue();
    }

    /**
     * Fetch cached obligations.
     * 
     * @return Collection<ObjectLabelPair>
     */
    public static Collection<ObjectLabelPair> getObligations() {
        SortedSet<ObjectLabelPair> result = new TreeSet<ObjectLabelPair>();
        Map<UriLabelPair, List<UriLabelPair>> cache = getDeliverySearchPicklist();
        for (Entry<UriLabelPair, List<UriLabelPair>> entry : cache.entrySet()) {
            for (UriLabelPair pair : entry.getValue()) {
                result.add(new ObjectLabelPair(pair.getUri(), pair.getLabel()));
            }
        }
        return result;
    }

    /**
     * fetch cached instruments.
     * 
     * @return Collection
     */
    public static Collection<ObjectLabelPair> getInstruments() {
        Map<UriLabelPair, List<UriLabelPair>> cache = getDeliverySearchPicklist();
        List<ObjectLabelPair> result = new LinkedList<ObjectLabelPair>();
        for (UriLabelPair instrument : cache.keySet()) {
            result.add(new ObjectLabelPair(instrument.getUri(), instrument.getLabel()));
        }

        return result;
    }

    /**
     * Fetch the URIs of cached types.
     * 
     * @return Collection
     */
    public static List<String> getTypeUris() {

        Element element = getCache().get(TYPE_URIS_CACHE);
        if (element == null || element.getValue() == null) {
            return Collections.EMPTY_LIST;
        }
        return (List<String>) element.getValue();
    }

    /**
     * fetch cached types.
     * 
     * @return List<Pair<String, String>> types
     */
    @SuppressWarnings("unchecked")
    public static List<Pair<String, String>> getTypes() {
        Element element = getCache().get(TYPE_CACHE);
        if (element == null || element.getValue() == null) {
            return Collections.EMPTY_LIST;
        }
        return (List<Pair<String, String>>) element.getValue();
    }

    /**
     * update type cache.
     * 
     * @param types List<Pair<String, String>> Types to be updated
     */
    public static void updateTypes(final List<Pair<String, String>> types) {

        getCache().put(new Element(TYPE_CACHE, types));

        ArrayList<String> uris = new ArrayList<String>();
        for (Pair<String, String> pair : types) {
            uris.add(pair.getLeft());
        }
        Collections.sort(uris);
        getCache().put(new Element(TYPE_URIS_CACHE, uris));
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent) {@inheritDoc}
     */
    public void contextInitialized(final ServletContextEvent arg0) {
        CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.addCache(APPLICATION_CACHE);
    }

    /**
     * updates type columns cache.
     * 
     * @param update Map<String, Map<String, String>> - cache to be updated
     */
    public static void updateTypeColumns(final Map<String, Map<String, String>> update) {
        getCache().put(new Element(TYPE_COLUMNS_CACHE, update));
    }

    /**
     * fetch cached type columns.
     * 
     * @param type String
     * @return Map<String, String>
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getTypeColumns(final String type) {
        Element element = getCache().get(TYPE_COLUMNS_CACHE);
        if (element == null || element.getValue() == null) {
            return Collections.EMPTY_MAP;
        }
        return ((Map<String, Map<String, String>>) element.getValue()).get(type);
    }
}
