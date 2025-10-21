package eionet.cr.harvest;

import java.io.IOException;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import eionet.cr.ApplicationTestContext;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
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
    public static final String NOT_HARVEST_SOURCE = "http://test.ee/not_harvest_source";
    public static final String NEVER_HARVESTED_YET = "http://test.ee/never_harvested_yet";
    public static final String OUT_OF_DATE = "http://test.ee/out_of_date";
    public static final String UP_TO_DATE = "http://test.ee/up_to_date";
    public static final String CONVERSION_MODIFIED = "http://test.ee/conversion_out_of_date";
    public static final String SCRIPTS_MODIFIED = "http://test.ee/scripts_modified";

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
