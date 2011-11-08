package eionet.cr.util.sql;

import org.dbunit.dataset.IDataSet;
import org.junit.Ignore;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */
@Ignore
public class UserReviewTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }
    /*
    @Test
    public void testReadLastReviewId() throws DAOException {

        CRUser user = new CRUser("kaptejaa");
        int lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user);

        assertEquals(0, lastReviewId);

        int newId = DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
        assertEquals(1, newId);

        DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
        DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user);
        assertEquals(3, lastReviewId);

        user = new CRUser("someoneelse");

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user);
        assertEquals(0, lastReviewId);

        DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
        DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user);
        assertEquals(2, lastReviewId);
    }

    @Test
    public void testAddNewReview() throws DAOException {

        ReviewDTO testReview = new ReviewDTO();
        testReview.setObjectUrl("http://www.tieto.com");
        testReview.setReviewContent("Some content");
        testReview.setTitle("Title");

        CRUser user = new CRUser("kaptejaa");
        int lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user);
        assertEquals(0, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview, user);
        assertEquals(1, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview, user);
        assertEquals(2, lastReviewId);
    }

    @Test
    public void testAddNewReviewWithoutPrecondition() throws DAOException {

        ReviewDTO testReview;
        testReview = new ReviewDTO();
        testReview.setObjectUrl("http://www.tieto.com");
        testReview.setReviewContent("Some content");
        testReview.setTitle("Title");

        CRUser user = new CRUser("kaptejaa");
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview, user);
        assertEquals(1, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview, user);
        assertEquals(2, lastReviewId);
    }

    @Test
    public void testReadReviews() throws DAOException {

        ReviewDTO testReview1;
        testReview1 = new ReviewDTO();
        testReview1.setObjectUrl("http://www.tieto.com");
        testReview1.setReviewContent("Some content");
        testReview1.setTitle("Title 1");

        ReviewDTO testReview2;
        testReview2 = new ReviewDTO();
        testReview2.setObjectUrl("http://www.tieto.com");
        testReview2.setReviewContent("Some content");
        testReview2.setTitle("Title 2");

        CRUser user = new CRUser("kaptejaa");
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview1, user);
        assertEquals(1, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview2, user);
        assertEquals(2, lastReviewId);

        // just issue this call without using the return value
        DAOFactory.get().getDao(HelperDAO.class).getReviewList(user);
    }

    @Test
    public void testReadSingleReview() throws DAOException {

        ReviewDTO testReview1;
        testReview1 = new ReviewDTO();
        testReview1.setObjectUrl("http://www.tieto.com");
        testReview1.setReviewContent("Some content");
        testReview1.setTitle("Title 1");

        CRUser user = new CRUser("kaptejaa");
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(HelperDAO.class).addReview(testReview1, user);
        assertEquals(1, lastReviewId);

        ReviewDTO result = DAOFactory.get().getDao(HelperDAO.class).getReview(user, 1);
        assertEquals(1, result.getReviewID());
    }*/
}
