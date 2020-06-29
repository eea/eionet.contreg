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

import eionet.cr.harvest.scheduled.HarvestingJob;
import eionet.cr.spring.SpringApplicationContext;
import eionet.propertyplaceholderresolver.CircularReferenceException;
import eionet.propertyplaceholderresolver.ConfigurationPropertyResolver;
import eionet.propertyplaceholderresolver.UnresolvedPropertyException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author heinljab
 *
 */
public final class GeneralConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralConfig.class);

    /** */
    public static final String BUNDLE_NAME = "cr";
    public static final String PROPERTIES_FILE_NAME = "cr.properties";

    /** */
    public static final String HARVESTER_FILES_LOCATION = "harvester.tempFileDir";
    public static final String HARVESTER_BATCH_HARVESTING_HOURS = "harvester.batchHarvestingHours";

    /** Interval at which the {@link HarvestingJob} should run. */
    public static final String HARVESTING_JOB_INTERVAL = "harvester.jobInterval";

    /** Property for the default harvest interval. */
    public static final String DEFAULT_HARVEST_INTERVAL = "harvester.defaultHarvestInterval";

    /** Upper limit for the number of sources that are harvested in each interval. */
    public static final String HARVESTER_SOURCES_UPPER_LIMIT = "harvester.batchHarvestingUpperLimit";

    /** Upper limit for the number of urgent harvests performed at one interval. */
    public static final String HARVESTER_URGENT_HARVESTS_PER_INTERVAL = "urgentHarvestsPerInterval";

    /** Maximum content length of harvested sources. */
    public static final String HARVESTER_MAX_CONTENT_LENGTH = "harvester.maximumContentLength";

    /** The timeout that harvester will feed into java.net.URLConnection's setConnectTimeout() and setReadTimeout(). */
    public static final String HARVESTER_HTTP_TIMEOUT = "harvester.httpConnection.timeout";

    /** */
    public static final String XMLCONV_LIST_CONVERSIONS_URL = "xmlconv.listConversions.url";
    public static final String XMLCONV_CONVERT_URL = "xmlconv.convert.url";
    public static final String XMLCONV_CONVERT_PUSH_URL = "xmlconv.convertPush.url";
    public static final String XMLCONV_XSL_URL = "xmlconv.xsl.url";

    /** */
    public static final String MAIL_SYSADMINS = "mail.sysAdmins";

    /** */
    public static final String APPLICATION_VERSION = "application.version";
    public static final String APPLICATION_USERAGENT = "application.userAgent";
    public static final String APPLICATION_HOME_URL = "application.homeURL";

    /** */
    public static final String SUBJECT_SELECT_MODE = "subjectSelectMode";

    /** Cache-related constants */
    public static final String DELIVERY_SEARCH_PICKLIST_CACHE_UPDATE_INTERVAL = "deliverySearchPicklistCacheUpdateInterval";
    public static final String RECENT_DISCOVERED_FILES_CACHE_UPDATE_INTERVAL = "recentDiscoveredFilesCacheUpdateInterval";
    public static final String TYPE_CACHE_UPDATE_INTERVAL = "typeCacheUpdateInterval";
    public static final String TAG_CLOUD_CACHE_UPDATE_INTERVAL = "tagCloudCacheUpdateInterval";
    public static final String TYPE_CACHE_UPDATER_CRON_JOB = "typeCacheTablesUpdateCronJob";

    /*
     * TagCloud sizes.
     */
    public static final String TAGCLOUD_FRONTPAGE_SIZE = "tagcloud.frontpage.size";
    public static final String TAGCOLUD_TAGSEARCH_SIZE = "tagcloud.tagsearch.size";

    /** */
    public static final String VIRTUOSO_DB_DRV = "virtuoso.db.drv";
    public static final String VIRTUOSO_DB_URL = "virtuoso.db.url";
    public static final String VIRTUOSO_DB_USR = "virtuoso.db.usr";
    public static final String VIRTUOSO_DB_PWD = "virtuoso.db.pwd";

    /** */
    public static final String VIRTUOSO_DB_ROUSR = "virtuoso.db.rousr";
    public static final String VIRTUOSO_DB_ROPWD = "virtuoso.db.ropwd";

    /**
     * General ruleSet name for inferencing. Schema sources are added into that ruleset.
     * */
    @Deprecated
    public static final String VIRTUOSO_CR_RULESET_NAME = "virtuoso.cr.ruleset.name";

    /** */
    public static final String FILESTORE_PATH = "filestore.path";

    /** */
    public static final String FILE_DELETION_JOB_INTERVAL = "tempFileDeletionJob.interval";

    /**
     * Property name for property indicating how many rows SPARQL endpoint returns in HTML.
     */
    public static final String SPARQLENDPOINT_MAX_ROWS_COUNT = "sparql.max.rows";

    /** */
    public static final String APPLICATION_DISPLAY_NAME = "application.displayName";

    /** */
    public static final String USE_CENTRAL_AUTHENTICATION_SERVICE = "useCentralAuthenticationService";

    /** */
    public static final String ENABLE_EEA_FUNCTIONALITY = "enableEEAFunctionality";

    /** */
    public static final String PING_WHITELIST = "pingWhitelist";

    /** */
    public static final String PING_BLACKLIST = "pingBlacklist";

    /** */
    public static final String STAGING_FILES_DIR = "stagingFilesDir";

    /** */
    public static final String VIRTUOSO_REAL_TIME_FT_INDEXING = "virtuoso.unittest.realTimeFullTextIndexing";

    /** Interval for the job that deletes harvest sources in the background. */
    public static final String SOURCE_DELETION_JOB_INTERVAL = "sourceDeletionJob.interval";

    /** Comma-separated list of hours when source deletion background job should be active. */
    public static final String SOURCE_DELETION_JOB_ACTIVE_HOURS = "sourceDeletionJob.activeHours";

    /** Number of sources that the source deletion background job should delete during one run. */
    public static final String SOURCE_DELETION_JOB_BATCH_SIZE = "sourceDeletionJob.batchSize";

    /** Number of threads to use in multi-threaded RDF loading. Defaults to 1. */
    public static final String RDF_LOADER_THREADS = "harvester.rdfLoaderThreads";

    /** File size (in bytes) threshold (exclusive) upon which the harvester will use transactional loading. Defaults to 500000000. */
    public static final String TRANSACTIONAL_LOADING_FILE_SIZE_THRESHOLD_BYTES = "harvester.transactionalLoading.fileSizeThresholdBytes";

    /** Harvester loading duration threshold (in minutes) over which a checkpoint will be issued after loading. Defaults to 15. */
    public static final String CHECKPOINT_LOADING_DURATION_THRESHOLD_MINUTES = "harvester.checkpoint.loadingDurationThresholdMinutes";

    /** SQL Like patterns of harvest sources to skip/exclude. */
    public static final String HARVEST_SOURCE_EXCLUSION_SUBSTRINGS = "harvestSourcesExclusionSubstrings";

    /**
     * If number of triples in source is above this threshold, no XORing should be applied by harvester.
     * Defaults to 1000000000 (ie one billion) to ensure that by default XORing is always applied.
     */
    public static final String HARVESTER_SKIP_XORING_NOOFTRIPLES_THRESHOLD = "harvester.skipXoring.noOfTriplesThreshold";

    /**
     * If downloaded file size is above this bytes threshold, no XORing should be applied by harvester.
     * Used only when the harvester cannot guess the number of triples for some reason (see harvester.skipXoring.noOfTriplesThreshold).
     * Defaults to 1000000000000 (ie one terabyte) to ensure that by default XORing is always applied.
     */
    public static final String HARVESTER_SKIP_XORING_FILESIZE_BYTES_THRESHOLD = "harvester.skipXoring.fileSizeBytesThreshold";

    /** */
    public static final int SEVERITY_INFO = 1;
    public static final int SEVERITY_CAUTION = 2;
    public static final int SEVERITY_WARNING = 3;

    /** */
    public static final String HARVESTER_URI = getRequiredProperty(APPLICATION_HOME_URL) + "/harvester";

    /** Servlet response buffer size to be used for requests that go via Stripes. The unit is number of bytes. */
    public static final int SERVLET_RESPONSE_BUFFER_SIZE = NumberUtils.toInt(getProperty("servletResponseBufferSize"), 32768);

    /** */
    private static Properties properties;
    private static ConfigurationPropertyResolver propertyResolver;


    /**
     * Hide utility class constructor.
     */
    private GeneralConfig() {
        // Hide utility class constructor.
    }

    /**
     *
     * @param key
     * @return
     */
    public static synchronized String getProperty(String key) {

        if (properties == null) {
            properties = new Properties();
        }
        if (propertyResolver == null) {
            propertyResolver = SpringApplicationContext.getBean("configurationPropertyResolver");
        }

        String value = "";
        if (properties.containsKey(key)) {
            value = properties.getProperty(key);
        }
        else {
            try {
                value = propertyResolver.resolveValue(key);
            } catch (UnresolvedPropertyException e) {
                // Ignore intentionally.
            } catch (CircularReferenceException e) {
                // Ignore intentionally.
            }
            properties.put(key, value);
        }

        return value != null && !value.isEmpty() ? value : null;
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static synchronized String getProperty(String key, String defaultValue) {


        String value = getProperty(key);
        return value == null ? defaultValue : value;
    }

    /**
     *
     */
    public static synchronized boolean isPropertySet(String key) {
        return getProperty(key) == null ? false : true;
    }

    /**
     * Returns integer property.
     *
     * @param key
     *            property key in the properties file
     * @param defaultValue
     *            default value that is returned if not specified or in incorrect format
     * @return property value or default if not specified correctly
     */
    public static synchronized int getIntProperty(final String key, final int defaultValue) {

        String propValue = getProperty(key);
        int value = defaultValue;
        if (propValue != null) {
            try {
                value = Integer.parseInt(propValue.trim());
            } catch (NumberFormatException e) {
                // Ignore exceptions resulting from string-to-integer conversion here.
            }
        }

        return value;
    }

    /**
     * Returns long property.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static synchronized long getLongProperty(final String key, final long defaultValue) {

        String propValue = getProperty(key);
        long value = defaultValue;
        if (propValue != null) {
            try {
                value = Long.parseLong(propValue.trim());
            } catch (NumberFormatException e) {
                // Ignore exceptions resulting from string-to-integer conversion here.
            }
        }

        return value;
    }

    /**
     * Get property value of time in milliseconds presented by time value and unit suffix (1h, 30m, 15s etc).
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static synchronized Integer getTimePropertyMilliseconds(final String key, Integer defaultValue) {

        int coeficient = 1;

        String propValue = getProperty(key);
        Integer value = defaultValue;

        if (propValue != null) {

            propValue = propValue.replace(" ", "").toLowerCase();

            if (propValue.length() > 1 && propValue.endsWith("ms")
                    && propValue.replace("ms", "").length() == propValue.length() - 2) {
                coeficient = 1;
                propValue = propValue.replace("ms", "");
            }

            if (propValue.length() > 1 && propValue.endsWith("s") && propValue.replace("s", "").length() == propValue.length() - 1) {
                coeficient = 1000;
                propValue = propValue.replace("s", "");
            }

            if (propValue.length() > 1 && propValue.endsWith("m") && propValue.replace("m", "").length() == propValue.length() - 1) {
                coeficient = 1000 * 60;
                propValue = propValue.replace("m", "");
            }

            if (propValue.length() > 1 && propValue.endsWith("h") && propValue.replace("h", "").length() == propValue.length() - 1) {
                coeficient = 1000 * 60 * 60;
                propValue = propValue.replace("h", "");
            }

            try {
                value = Integer.valueOf(propValue) * coeficient;
            } catch (Exception e) {
                // Ignore exceptions resulting from string-to-integer conversion here.
            }
        }

        return value;
    }

    /**
     * Get property value of time in minutes presented by time value and unit suffix (1h, 30m, 15s etc).
     *
     * The results are rounded to nearest minute value if the value is not exact minutes.
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public static synchronized int getTimePropertyMinutes(String key, int defaultValue) {

        Integer ms = 0;
        ms = getTimePropertyMilliseconds(key, null);

        if (ms != null) {
            double exactMinutes = ms / ((double) 60 * 1000);
            int minutes = (int) Math.round(exactMinutes);
            return minutes;
        } else {
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @return
     * @throws CRConfigException
     */
    public static synchronized String getRequiredProperty(String key) {

        String value = getProperty(key);
        if (value == null || value.trim().length() == 0) {
            throw new CRConfigException("Missing required property: " + key);
        } else {
            return value;
        }
    }

    /**
     *
     * @return
     */
    public static synchronized Properties getProperties() {
        return properties;
    }

    /**
     *
     * @return
     */
    public static synchronized boolean isUseCentralAuthenticationService() {

        String useCentralAuthenticationService = getProperty(USE_CENTRAL_AUTHENTICATION_SERVICE);
        return StringUtils.isBlank(useCentralAuthenticationService) || !useCentralAuthenticationService.equals("false");
    }

    /**
     * If ruleset name property is available in cr.properties, then use inferencing in queries.
     *
     * @return
     *
     * @deprecated As inferencing is not used in CR
     */
    @Deprecated
    public static synchronized boolean isUseInferencing() {
        return false;
    }

    /**
     * Returns the interval at which the {@link HarvestingJob} should check for batch/urgent harvest queues.
     * The value is returned in seconds, as method name suggests!
     *
     * @return The interval as described above.
     */
    public static int getHarvestingJobIntervalSeconds() {

        int seconds = 0;

        // Get interval by the defualt property name.
        int millis = GeneralConfig.getTimePropertyMilliseconds(GeneralConfig.HARVESTING_JOB_INTERVAL, 0);
        if (millis <= 0) {
            // This is to backward-support the previous name we had for this property.
            seconds = NumberUtils.toInt(GeneralConfig.getProperty("harvester.batchHarvestingIntervalSeconds"));
        } else {
            seconds = Math.round(millis / 1000f);
        }

        return seconds <= 0 ? 20 : seconds;
    }

    /**
     * Returns default harvest interval to be used in situations where the system has auto-discovered new harvest sources,
     * or it is not possible to ask the interval from the user. The value is in minutes!
     *
     * @return The default harvest interval in minutes!
     */
    public static int getDefaultHarvestIntervalMinutes() {

        // Get interval minutes by the defualt property name.
        int minutes = GeneralConfig.getTimePropertyMinutes(GeneralConfig.DEFAULT_HARVEST_INTERVAL, 0);
        if (minutes <= 0) {
            // This is to backward-support the previous name we had for this property.
            minutes = GeneralConfig.getTimePropertyMinutes("harvester.referrals.interval", 0);
        }

        if (minutes <= 0) {
            // This is to backward-support the previous-previous name we had for this property. It reflected minutes as unit!
            // Apache Commons NumberUtils converts the string to int. If it fails or the property is missing, it returns 0.
            minutes = NumberUtils.toInt(GeneralConfig.getProperty("harvester.referrals.intervalMinutes"));
        }

        // If still no value found (i.e. it is <= 0), then fall back to 42 days, i.e. 60480 minutes.
        return minutes <= 0 ? 60480 : minutes;
    }

    /**
     * Ignore CR harvester redirects to login forms based on URL substrings returned by this function.
     * Never returns null, may return empty set.
     *
     * @return
     */
    public static Set<String> getHarvestingSourceExcludingSubstrings() {

        Set<String> result = new HashSet<>();
        String propValue = GeneralConfig.getProperty(GeneralConfig.HARVEST_SOURCE_EXCLUSION_SUBSTRINGS);
        if (StringUtils.isNotBlank(propValue)) {
            result.addAll(Arrays.asList(StringUtils.split(propValue)));
        }
        return result;
    }
}
