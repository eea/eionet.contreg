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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.search.util.UriLabelPair;

/**
 * Util class to cache Dataflow picklist for Dataflow search.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public final class DataflowSearchPicklistCache {

	/** */
	protected static Log logger = LogFactory.getLog(RecentResourcesCache.class);

	/** */
	private static DataflowSearchPicklistCache cache;
	private Map<String, List<UriLabelPair>> dataflowPicklist;
	private Collection<String> localitiesCache;
	
	/**
	 * 
	 */
	private DataflowSearchPicklistCache() {
		//singleton pattern
		dataflowPicklist = Collections.synchronizedMap(new LinkedHashMap<String, List<UriLabelPair>>());
		localitiesCache = Collections.synchronizedCollection(new LinkedList<String>());
	}
	
	/**
	 * @return instance of the cache.
	 */
	public static DataflowSearchPicklistCache getInstance() {
		if (cache == null) {
			cache = new DataflowSearchPicklistCache();
		}
		return cache;
	}

	/**
	 * @return Dataflow picklist cache.
	 */
	public Map<String, List<UriLabelPair>> getDataflowPicklist(){
		return dataflowPicklist;
	}
	
	/**
	 * @return dataflow localities cache.
	 */
	public Collection<String> getDataflowLocalities(){
		return localitiesCache;
	}
	
	/**
	 * updates the cache.
	 * 
	 * @param update items to cache.
	 */
	public void updateCache(Map<String, ArrayList<UriLabelPair>> picklistCache, Collection<String> localitiesCache) {
		dataflowPicklist.clear();
		dataflowPicklist.putAll(picklistCache);
		this.localitiesCache.clear();
		this.localitiesCache.addAll(localitiesCache);
	}

}
