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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author heinljab
 *
 */
public class GeneralConfig {
	
	/** */
	public static final String BUNDLE_NAME = "cr";
	public static final String PROPERTIES_FILE_NAME = "cr.properties";
	public static final String SEED_FILE_NAME = "seed-cr.xml";

	/** */
	public static final String DB_URL = "db.url";
	public static final String DB_DRV = "db.drv";
	public static final String DB_USER_ID = "db.usr";
	public static final String DB_USER_PWD = "db.pwd";
	
	/** */
	public static final String HARVESTER_FILES_LOCATION = "harvester.tempFileDir";
	public static final String HARVESTER_BATCH_HARVESTING_HOURS = "harvester.batchHarvestingHours";
	public static final String HARVESTER_JOB_INTERVAL_SECONDS = "harvester.batchHarvestingIntervalSeconds";
	public static final String HARVESTER_REFERRALS_INTERVAL = "harvester.referrals.intervalMinutes";
	
	/** */
	public static final String DATASOURCE_NAME = "datasource.name";

	/** */
	public static final String XMLCONV_LIST_CONVERSIONS_URL = "xmlconv.listConversions.url";
	public static final String XMLCONV_CONVERT_URL = "xmlconv.convert.url";
	
	/** */
	public static final String MAIL_SYSADMINS = "mail.sysAdmins";
	
	/** */
	public static final String HARVESTER_USE_DOWNLOADED_FILES = "harvest.debug.useDownloadedFiles";
	public static final String HARVESTER_DELETE_DOWNLOADED_FILES = "harvest.debug.deleteDownloadedFiles";
	
	/** */
	public static final String APPLICATION_VERSION = "application.version";
	public static final String APPLICATION_USERAGENT = "application.userAgent";
	
	/** */
	public static final String SUBJECT_SELECT_MODE = "subjectSelectMode";
	
	/**
	 * Constant to get dataflow picklist cache update interval (in milliseconds). 
	 */
	public static final String DATAFLOW_PICKLIST_CACHE_UPDATE_INTERVAL = "dataflowPicklistCacheUpdateInterval";
	public static final String RECENT_DISCOVERED_FILES_CACHE_UPDATE_INTERVAL = "recentDiscoveredFilesCacheUpdateInterval";
	public static final String TYPE_CACHE_UPDATE_INTERVAL = "typeCacheUpdateInterval";
	public static final String GARBAGE_COLLECTOR_CRON_JOB = "garbageCollectorCronJob";
	
	/** */
	public static final int SEVERITY_INFO = 1;
	public static final int SEVERITY_CAUTION = 2;
	public static final int SEVERITY_WARNING = 3;

	/** */
	private static Log logger = LogFactory.getLog(GeneralConfig.class);

	/** */
	private static Properties properties = null;
	
	/** */
	private static void init(){
		properties = new Properties();
		try{
			String s = GeneralConfig.class.getClassLoader().getResource(PROPERTIES_FILE_NAME).toString();
			properties.load(GeneralConfig.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME));
		}
		catch (IOException e){
			logger.fatal("Failed to load properties from " + PROPERTIES_FILE_NAME, e);
		}
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	public static synchronized String getProperty(String key){
		
		if (properties==null)
			init();
		
		return properties.getProperty(key);
	}

	/**
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static synchronized String getProperty(String key, String defaultValue){
		
		if (properties==null)
			init();
		
		return properties.getProperty(key, defaultValue);
	}
	
	/**
	 * 
	 * @param key
	 * @return
	 * @throws CRConfigException 
	 */
	public static synchronized String getRequiredProperty(String key){
		
		String value = getProperty(key);
		if (value==null || value.trim().length()==0)
			throw new CRConfigException("Missing required property: " + key);
		else
			return value;
	}

	/**
	 * 
	 * @return
	 */
	public static synchronized Properties getProperties(){

		if (properties==null)
			init();
		
		return properties;
	}
}
