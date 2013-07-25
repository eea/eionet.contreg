package eionet.cr.harvest;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.SAXException;

import eionet.cr.dao.DAOException;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.Util;

/**
 * A mock of {@link UpToDateChecker} to be used by {@link UpToDateCheckerTest}.
 *
 * @author jaanus
 */
public class UpToDateCheckerMock extends UpToDateChecker {

    /** Various cases of URLs to check. */
    public static final String NOT_HARVEST_SOURCE = "http://not.harvest.source";
    public static final String NEVER_HARVESTED_YET = "http://never.harvested.yet";
    public static final String OUT_OF_DATE = "http://out.of.date";
    public static final String UP_TO_DATE = "http://up.to.date";
    public static final String CONVERSION_MODIFIED = "http://conversion.out.of.date";
    public static final String SCRIPTS_MODIFIED = "http://scripts.modified";

    /** Dummy conversion URL. */
    public static final String CONVERSION_URL = "http://conversion.com";

    /** Dummy date. */
    public static final Date JUST_A_DATE = Util.stringToDate("2013.03.01 10:00:00", "yyyy.MM.dd HH:mm:ss");

    /**
     * Default constructor.
     */
    public UpToDateCheckerMock() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    HarvestSourceDTO getHarvestSourceByUrl(String url) throws DAOException {


        if (NOT_HARVEST_SOURCE.equals(url)) {
            return null;
        } else {
            HarvestSourceDTO harvestSourceDTO = new HarvestSourceDTO();
            if (!NEVER_HARVESTED_YET.equals(url)) {
                harvestSourceDTO.setLastHarvest(JUST_A_DATE);
            }
            return harvestSourceDTO;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getConversionStylesheetUrl(String url) throws DAOException, IOException, SAXException, ParserConfigurationException {

        if (CONVERSION_MODIFIED.equals(url)) {
            return CONVERSION_URL;
        } else {
            return StringUtils.EMPTY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean hasScriptsModified(String url, Date lastHarvest) throws DAOException {
        return SCRIPTS_MODIFIED.equals(url);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    boolean isUrlModifiedSince(String urlString, long timestamp) throws IOException {

        if (NOT_HARVEST_SOURCE.equals(urlString)) {
            throw new IllegalArgumentException("If-Modified-Since should not be checked on a URL that is not a harvest source!");
        } else if (NEVER_HARVESTED_YET.equals(urlString)) {
            throw new IllegalArgumentException("If-Modified-Since should not be checked on a never-yet-harvested source!");
        } else if (OUT_OF_DATE.equals(urlString)) {
            return true;
        } else if (UP_TO_DATE.equals(urlString)) {
            return false;
        } else if (CONVERSION_MODIFIED.equals(urlString)) {
            return false;
        } else if (SCRIPTS_MODIFIED.equals(urlString)) {
            return false;
        } else if (CONVERSION_URL.equals(urlString)) {
            return true;
        } else {
            return false;
        }
    }
}
