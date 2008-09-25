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
	public static final String LUCENE_INDEX_LOCATION = "lucene.index.location";
	public static final String HARVESTER_FILES_LOCATION = "harvester.files.location";
	public static final String HARVESTER_JOB_CRON_EXPRESSION = "harvester.job.quartzCronExpression";
	
	/** */
	public static final String DATASOURCE_NAME = "datasource.name";

	/** */
	public static final String XMLCONV_LIST_CONVERSIONS_URL = "xmlconv.listConversions.url";
	public static final String XMLCONV_CONVERT_URL = "xmlconv.convert.url";
	
	/** */
	public static final String ENVIRONMENT_PRODUCTION = "environment.production";

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
}
