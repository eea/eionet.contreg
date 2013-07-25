package eionet.cr.harvest;

import java.io.IOException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.URLUtil;

/**
 * Provides functionality for checking that a given set of harvest sources or a particular harvest source is up-to-date in terms of
 * the "Last-Modified" HTTP header returned by the harvest source response.
 *
 * @author jaanus
 */
public class UpToDateChecker {

    /** Static Log4j logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(UpToDateChecker.class);

    /** Initialize DAO already here. */
    private HarvestSourceDAO harvestSourceDao = DAOFactory.get().getDao(HarvestSourceDAO.class);

    /** Initialize DAO already here. */
    private PostHarvestScriptDAO postHarvestScriptDao = DAOFactory.get().getDao(PostHarvestScriptDAO.class);

    /** Initialize DAO already here. */
    private HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);

    /**
     * Default constructor.
     */
    public UpToDateChecker() {
        // Just an empty default constructor.
    }

    /**
     * Checks harvest sources by the given URLs. These MUST NOT BE ESCAPED, as the method sanitizes each URl with
     * {@link URLUtil#escapeIRI(String)} before checking it. Also, each URL will be stripped of fragment part
     * before executing the check.
     *
     * @param urls The URLs.
     * @return A map of resolutions where keys are the checked URLs and values are the corresponding resolutions.
     * @throws DAOException When an error happens while checking harvest sources in the database.
     * @throws ParserConfigurationException Could be thrown when checking available conversions for the checked URL.
     * @throws SAXException Could be thrown when checking available conversions for the checked URL.
     * @throws IOException Could be thrown when connecting the checked URLs or their conversion sheets.
     */
    public Map<String, Resolution> check(String... urls) throws DAOException, IOException, SAXException,
            ParserConfigurationException {

        LinkedHashMap<String, Resolution> resultMap = new LinkedHashMap<String, Resolution>();
        if (urls != null && urls.length > 0) {

            for (int i = 0; i < urls.length; i++) {

                String url = StringUtils.substringBefore(URLUtil.escapeIRI(urls[i]), "#");
                resultMap.put(url, checkUrl(url));
            }
        }
        return resultMap;
    }

    /**
     * This is the worker method that is called by the {@link #check(String...)} method for each URL it wants to check.
     *
     * @param url The URL to check.
     * @return The resolution.
     * @throws DAOException When an error happens while checking harvest sources in the database.
     * @throws ParserConfigurationException Could be thrown when checking available conversions for the checked URL.
     * @throws SAXException Could be thrown when checking available conversions for the checked URL.
     * @throws IOException Could be thrown when connecting the checked URLs or their conversion sheets.
     */
    private Resolution checkUrl(String url) throws DAOException, IOException, SAXException, ParserConfigurationException {

        LOGGER.trace("Checking " + url);

        HarvestSourceDTO harvestSource = getHarvestSourceByUrl(url);
        if (harvestSource == null) {
            return Resolution.NOT_HARVEST_SOURCE;
        } else {
            Date lastHarvest = harvestSource.getLastHarvest();
            if (lastHarvest == null || lastHarvest.getTime() == 0L) {
                LOGGER.trace(url + " has never been harvested!");
                return Resolution.OUT_OF_DATE;
            } else {
                boolean isOutOfDate = isUrlModifiedSince(url, lastHarvest.getTime());
                if (!isOutOfDate) {

                    String conversionStylesheetUrl = getConversionStylesheetUrl(url);
                    if (StringUtils.isNotBlank(conversionStylesheetUrl)) {
                        isOutOfDate = isUrlModifiedSince(conversionStylesheetUrl, lastHarvest.getTime());
                        if (isOutOfDate) {
                            LOGGER.trace(url + " has a conversion that is out of date!");
                        }
                    }

                    if (!isOutOfDate) {
                        isOutOfDate = hasScriptsModified(url, lastHarvest);
                        if (isOutOfDate) {
                            LOGGER.trace(url + " has a post-harvest scripts that have been modified since the date checked!");
                        }
                    }
                } else {
                    LOGGER.trace(url + " is out of date!");
                }

                return isOutOfDate ? Resolution.OUT_OF_DATE : Resolution.UP_TO_DATE;
            }
        }
    }

    /**
     * Worker method for getting a harvest source by the given URL.
     * Can be overridden by mocks for unit testing.
     *
     * @param url The URl by which to get the harvest source.
     * @return The harvest source.
     * @throws DAOException If database access error happens.
     */
    HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException {
        return harvestSourceDao.getHarvestSourceByUrl(url);
    }

    /**
     * Worker method for getting the URL of the potential conversion stylesheet of the given URL.
     *
     * @param url The given URL whose conversion stylesheet URL is to be returned.
     * @return The conversion stylesheet URL.
     *
     * @throws DAOException When an error happens while checking harvest sources in the database.
     * @throws ParserConfigurationException Could be thrown when checking available conversions for the checked URL.
     * @throws SAXException Could be thrown when checking available conversions for the checked URL.
     * @throws IOException Could be thrown when connecting the checked URLs or their conversion sheets.
     */
    String getConversionStylesheetUrl(String url) throws DAOException, IOException, SAXException, ParserConfigurationException {
        return PullHarvest.getConversionStylesheetUrl(helperDAO, url);
    }

    /**
     * Worker method for checking if the given harvest source URL has any associated post-harvest scripts modified since given
     * timestamp. Can be overridden by mocks for unit testing.
     *
     * @param url The URL of the harvest source.
     * @param lastHarvest The last harvest timestamp.
     * @return True, if source has nya modified scripts.
     * @throws DAOException If database access error happens.
     */
    boolean hasScriptsModified(String url, Date lastHarvest) throws DAOException {
        return postHarvestScriptDao.isScriptsModified(lastHarvest, url);
    }

    /**
     * Worker method for checking if the content behind the given URL has been modified since given timestamp.
     * Can be overridden by mocks for unit tests.
     *
     * @param urlString The URL.
     * @param timestamp The timestamp.
     * @return The boolean indicating the result.
     * @throws IOException In casy any IO exepction happens.
     */
    boolean isUrlModifiedSince(String urlString, long timestamp) throws IOException {
        return URLUtil.isModifiedSince(urlString, timestamp);
    }

    /**
     * An enumeration that represents possible resolutions to the harvest source up-to-date checking.
     *
     * @author jaanus
     */
    public static enum Resolution {

        NOT_HARVEST_SOURCE("There is not harvest source with this URL yet!"),
        OUT_OF_DATE("The source was found to be out of date!"),
        UP_TO_DATE("The source was found to be up to date!");

        /** Explanatory user-friendly message that describes this resolution. */
        private String message;

        /**
         * Default constructor.
         *
         * @param message User-friendly message describing this resolution.
         */
        private Resolution(String message) {
            this.message = message;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return message;
        }
    }
}
