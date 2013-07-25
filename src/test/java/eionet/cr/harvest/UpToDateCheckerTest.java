package eionet.cr.harvest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.xml.sax.SAXException;

import eionet.cr.dao.DAOException;
import eionet.cr.harvest.UpToDateChecker.Resolution;

/**
 * Unit tests for the {@link UpToDateChecker} class.
 *
 * @author jaanus
 */
public class UpToDateCheckerTest {

    /**
     * Test various cases.
     *
     * @throws ParserConfigurationException Thrown by code under test.
     * @throws SAXException Thrown by code under test.
     * @throws IOException Thrown by code under test.
     * @throws DAOException Thrown by code under test.
     */
    @Test
    public void test() throws DAOException, IOException, SAXException, ParserConfigurationException {

        HashMap<String, Resolution> expectedResolutions = new HashMap<String, UpToDateChecker.Resolution>();
        expectedResolutions.put(UpToDateCheckerMock.NOT_HARVEST_SOURCE, Resolution.NOT_HARVEST_SOURCE);
        expectedResolutions.put(UpToDateCheckerMock.NEVER_HARVESTED_YET, Resolution.OUT_OF_DATE);
        expectedResolutions.put(UpToDateCheckerMock.OUT_OF_DATE, Resolution.OUT_OF_DATE);
        expectedResolutions.put(UpToDateCheckerMock.UP_TO_DATE, Resolution.UP_TO_DATE);
        expectedResolutions.put(UpToDateCheckerMock.CONVERSION_MODIFIED, Resolution.CONVERSION_MODIFIED);
        expectedResolutions.put(UpToDateCheckerMock.SCRIPTS_MODIFIED, Resolution.SCRIPTS_MODIFIED);

        UpToDateCheckerMock checkerMock = new UpToDateCheckerMock();
        String[] array = expectedResolutions.keySet().toArray(new String[expectedResolutions.size()]);
        Map<String, Resolution> actualResolutions = checkerMock.check(array);

        for (Entry<String, Resolution> entry : expectedResolutions.entrySet()) {

            String sourceUrl = entry.getKey();
            Resolution expectedResolution = entry.getValue();
            Resolution actualResolution = actualResolutions.get(sourceUrl);

            assertNotNull("Actual resolution should not be null!", actualResolution);
            assertEquals("Resolution for " + sourceUrl + ": ", expectedResolution.name(), actualResolution.name());
        }
    }
}
