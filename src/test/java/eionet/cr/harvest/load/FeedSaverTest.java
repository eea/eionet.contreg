package eionet.cr.harvest.load;

import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Tests for FeedSaver class.
 *
 * @author Kaido Laine
 */
public class FeedSaverTest {

    /** test atom RSS feed. */
    private static final String TEST_FEED = "test-atomfeed.xml";

    /** Fake context URL .*/
    private static final String CONTEXT_URI = "http://localhost:8080/" + TEST_FEED;


    /**
     * Loads test feed to test repository.
     * @throws Exception if loading fails
     */
    @BeforeClass
    public static void initTestFeed() throws Exception {

        RepositoryConnection repoConn = null;
        Connection sqlConn = null;
        InputStream is = null;

        String contextUri = CONTEXT_URI;
        URLConnection urlConn = null;
        try {
            is = (new FeedSaverTest().getClass().getClassLoader().getResourceAsStream(TEST_FEED));
            repoConn = SesameConnectionProvider.getRepositoryConnection();
            sqlConn = SesameUtil.getSQLConnection();
            FeedSaver feedSaver = new FeedSaver(repoConn, sqlConn, contextUri);

            feedSaver.save(is);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            SesameUtil.close(repoConn);
            SQLUtil.close(sqlConn);
            URLUtil.disconnect(urlConn);
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Clears test data.
     * @throws Exception if clearing fails.
     */
    @AfterClass
    public static void clearTestData() throws Exception {
        RepositoryConnection conn = null;

        try {
            conn = SesameConnectionProvider.getRepositoryConnection();
            ValueFactory valueFactory = conn.getValueFactory();

            Resource graphResource = valueFactory.createURI(CONTEXT_URI);

            Resource harvesterContext = valueFactory.createURI(CONTEXT_URI);

            conn.clear(graphResource);
            conn.remove(graphResource, null, null, harvesterContext);

        } finally {
            SesameUtil.close(conn);
        }

    }

    /**
     * tests if dates are stored in dcterms:date.
     * @throws Exception if test fails
     */
    @Test
    public void testDates() throws Exception {
        HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);

        SubjectDTO subj = dao.getSubject("http://www.eionet.europa.eu/news/gmesatmosphericworkshop");

        Collection<ObjectDTO> dates = subj.getObjects(Predicates.DCTERMS_DATE);
        assertTrue(dates != null && dates.size() == 2);

        // loop all the date objects, cannot compare strings because of TZ'z syndFeed stores in Virtuoso
        boolean foundDate = false;
        DateFormat dfFeed = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfFeed.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date expectedDate = dfFeed.parse("2008-11-21T17:19:57Z");

        DateFormat dfVirtuoso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        for (ObjectDTO dateDTO : dates) {
            Date date = dfVirtuoso.parse(dateDTO.getValue());
            foundDate = (expectedDate.getTime() == date.getTime());
        }

        assertTrue(foundDate);

    }

    /**
    * Test if published element is saved to dcterms:issued.
    * @throws Exception if error
    */
    @Test
    public void testIssued() throws Exception {
        HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);

        DateFormat dfFeed = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfFeed.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat dfVirtuoso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        SubjectDTO subj = dao.getSubject("http://www.eionet.europa.eu/news/gmesatmosphericworkshop");

        Collection<ObjectDTO> issued = subj.getObjects(Predicates.DCTERMS_ISSUED);
        assertTrue(issued != null && issued.size() == 1);

        ObjectDTO issuedDTO = issued.iterator().next();
        Date issuedDate = dfVirtuoso.parse(issuedDTO.getValue());
        Date expectedIssued = dfFeed.parse("2008-01-03T00:00:00Z");

        boolean foundIssued = (issuedDate.getTime() == expectedIssued.getTime());
        assertTrue(foundIssued);

    }

    /**
     * Test if updated element is saved to dcterms:modified.
     * @throws Exception if error
     */
    @Test
    public void testModified() throws Exception {
        HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);

        DateFormat dfFeed = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfFeed.setTimeZone(TimeZone.getTimeZone("UTC"));
        DateFormat dfVirtuoso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        SubjectDTO subj = dao.getSubject("http://www.eionet.europa.eu/news/cft-inspire-cafe");

        Collection<ObjectDTO> issued = subj.getObjects(Predicates.DCTERMS_MODIFIED);
        assertTrue(issued != null && issued.size() == 1);

        ObjectDTO issuedDTO = issued.iterator().next();
        Date issuedDate = dfVirtuoso.parse(issuedDTO.getValue());
        Date expectedIssued = dfFeed.parse("2008-11-21T17:19:57Z");

        boolean foundIssued = (issuedDate.getTime() == expectedIssued.getTime());
        assertTrue(foundIssued);

    }

}
