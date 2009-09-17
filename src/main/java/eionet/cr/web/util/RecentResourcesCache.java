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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import eionet.cr.util.Pair;

/**
 * Util class to cache recently discovered files.
 * 
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public final class RecentResourcesCache{

	/** limit of records inside cache */
	private static final int CACHE_LIMIT = 10;
	
	/** */
	protected static Log logger = LogFactory.getLog(RecentResourcesCache.class);

	/** */
	private static RecentResourcesCache cache;
	private List<Pair<String,String>> recentFiles;
	
	/**
	 * 
	 */
	private RecentResourcesCache() {
		//singleton pattern
		recentFiles = Collections.synchronizedList(
				new ArrayList<Pair<String,String>>()
				);
	}
	
	/**
	 * @return instance of the cache.
	 */
	public static RecentResourcesCache getInstance() {
		if (cache == null) {
			cache = new RecentResourcesCache();
		}
		return cache;
	}

	/**
	 * recently discovered files.
	 * 
	 * @param limit size of the return list.
	 * @return
	 */
	public List<Pair<String,String>> getRecentDiscoveredFiles(int limit) {
		return new LinkedList<Pair<String,String>>(
				recentFiles.subList(
						Math.max(
								0, 
								recentFiles.size() - limit),
						recentFiles.size()));
	}
	
	/**
	 * updates the cache.
	 * 
	 * @param update items to cache.
	 */
	public void updateCache(List<Pair<String,String>> update) {
		
		if (update!=null){
			recentFiles.addAll(update);
			if(recentFiles.size() > CACHE_LIMIT) {
				recentFiles.subList(0, recentFiles.size() - 10).clear();
			}
		}
	}
}
