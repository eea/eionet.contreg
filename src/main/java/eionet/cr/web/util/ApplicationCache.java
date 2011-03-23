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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.TagDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.ObjectLabelPair;

/**
 * A place to hold all application caches.
 * 
 * @author Aleksandr Ivanov <a
 *         href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class ApplicationCache implements ServletContextListener {

    public static final String APPLICATION_CACHE = "ApplicationCache";

    private static final String DATAFLOW_SEARCH_PICKLIST_CACHE = "dataflowSearchPicklist";
    private static final String LOCALITIES_CACHE = "localitiesCache";
    private static final String RECENT_RESOURCES_CACHE = "recentResources";
    private static final String TAG_CLOUD_CACHE = "tagCloud";

    private static final String TYPE_CACHE = "typeCache";

    private static final String TYPE_COLUMNS_CACHE = "typeColumnsCache";

    /**
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     *      {@inheritDoc}
     */
    public void contextDestroyed(ServletContextEvent arg0) {
        CacheManager.getInstance().shutdown();
    }

    private static Cache getCache() {
        return CacheManager.getInstance().getCache(APPLICATION_CACHE);
    }

    /**
     * update recent resource cache.
     * 
     * @param update
     */
    public static void updateRecentResourceCache(List<Pair<String, String>> update) {
        getCache().put(new Element(RECENT_RESOURCES_CACHE, update));
    }

    /**
     * get recently discovered files.
     * 
     * @param limit
     *            - how many files to fetch
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Pair<String, String>> getRecentDiscoveredFiles(int limit) {
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
     * @param update
     */
    public static void updateTagCloudCache(List<TagDTO> update) {
        getCache().put(new Element(TAG_CLOUD_CACHE, update));
    }

    /**
     * get tag cloud.
     * 
     * @param limit
     *            - how many tags to fetch
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<TagDTO> getTagCloud(int limit) {
        Element element = getCache().get(TAG_CLOUD_CACHE);

        if (element == null || element.getValue() == null) {
            return new LinkedList<TagDTO>();
        }

        List<TagDTO> cache = (List<TagDTO>) element.getValue();
        limit = limit == 0 ? cache.size() : limit;

        List<TagDTO> result = new LinkedList<TagDTO>(cache.subList(0, Math.min(cache.size(), limit)));

        return result;

    }

    public static List<TagDTO> getTagCloudSortedByName(int limit) {
        List<TagDTO> result = getTagCloud(limit);
        Collections.sort(result, new TagDTO.NameComparatorAsc());

        return result;
    }

    public static List<TagDTO> getTagCloudSortedByCount(int limit) {
        List<TagDTO> result = getTagCloud(limit);
        Collections.sort(result, new TagDTO.CountComparatorDesc());

        return result;
    }

    /**
     * Update dataflow picklist cache.
     * 
     * @param picklistCache
     *            - picklist cache
     * @param localitiesCache
     *            - localities cache
     */
    public static void updateDataflowPicklistCache(Map<String, ArrayList<UriLabelPair>> picklistCache,
            Collection<ObjectLabelPair> localitiesCache) {
        getCache().put(new Element(LOCALITIES_CACHE, localitiesCache));
        getCache().put(new Element(DATAFLOW_SEARCH_PICKLIST_CACHE, picklistCache));
    }

    /**
     * fetch cached localities.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Collection<ObjectLabelPair> getLocalities() {
        Element element = getCache().get(LOCALITIES_CACHE);

        return element == null || element.getValue() == null ? Collections.EMPTY_SET
                : (Collection<ObjectLabelPair>) element.getValue();
    }

    /**
     * fetch dataflow picklist cache.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, List<UriLabelPair>> getDataflowPicklist() {
        Element element = getCache().get(DATAFLOW_SEARCH_PICKLIST_CACHE);

        return element == null || element.getValue() == null ? Collections.EMPTY_MAP
                : (Map<String, List<UriLabelPair>>) getCache().get(DATAFLOW_SEARCH_PICKLIST_CACHE).getValue();
    }

    /**
     * fetch cached dataflows.
     * 
     * @return
     */
    public static Collection<ObjectLabelPair> getDataflows() {
        SortedSet<ObjectLabelPair> result = new TreeSet<ObjectLabelPair>();
        Map<String, List<UriLabelPair>> cache = getDataflowPicklist();
        for (Entry<String, List<UriLabelPair>> entry : cache.entrySet()) {
            for (UriLabelPair pair : entry.getValue()) {
                result.add(new ObjectLabelPair(pair.getUri(), pair.getLabel()));
            }
        }
        return result;
    }

    /**
     * fetch cached instruments.
     * 
     * @return
     */
    public static Collection<ObjectLabelPair> getInstruments() {
        Map<String, List<UriLabelPair>> cache = getDataflowPicklist();
        List<ObjectLabelPair> result = new LinkedList<ObjectLabelPair>();
        for (String instrumentName : cache.keySet()) {
            result.add(new ObjectLabelPair(instrumentName, instrumentName));
        }

        return result;
    }

    /**
     * fetch cached type URIs.
     * 
     * @return
     */
    public static Collection<ObjectLabelPair> getTypeURIs() {
        List<Pair<String, String>> types = getTypes();
        List<ObjectLabelPair> typeUris = new LinkedList<ObjectLabelPair>();
        for (Pair<String, String> type : types) {
            typeUris.add(new ObjectLabelPair(type.getLeft(), type.getLeft()));
        }
        return typeUris;
    }

    /**
     * fetch cached types.
     * 
     * @return
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
     * @param update
     */
    public static void updateTypes(List<Pair<String, String>> update) {
        getCache().put(new Element(TYPE_CACHE, update));
    }

    /**
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     *      {@inheritDoc}
     */
    public void contextInitialized(ServletContextEvent arg0) {
        CacheManager cacheManager = CacheManager.getInstance();
        cacheManager.addCache(APPLICATION_CACHE);
    }

    public static void updateTypeColumns(Map<String, Map<String, String>> update) {
        getCache().put(new Element(TYPE_COLUMNS_CACHE, update));
    }

    /**
     * fetch cached type columns.
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> getTypeColumns(String type) {
        Element element = getCache().get(TYPE_COLUMNS_CACHE);
        if (element == null || element.getValue() == null) {
            return Collections.EMPTY_MAP;
        }
        return (Map<String, String>) ((Map<String, Map<String, String>>) element.getValue()).get(type);
    }
}
