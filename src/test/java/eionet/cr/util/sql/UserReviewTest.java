package eionet.cr.util.sql;

import junit.framework.TestCase;

import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.ReviewsDAO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */
public class UserReviewTest extends TestCase {

    private static String[] TEST_USER_NAMES = {"kaptejaa", "test2"};

    @Override
    protected void setUp() throws Exception {
        cleanUpReviews();
        super.setUp();
    }

    @Test
    public void testReadLastReviewId() throws DAOException {

        CRUser user = new CRUser(TEST_USER_NAMES[0]);
        int lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).getLastReviewId(user);

        assertEquals(0, lastReviewId);

        int newId = DAOFactory.get().getDao(ReviewsDAO.class).generateNewReviewId(user);
        assertEquals(1, newId);

        DAOFactory.get().getDao(ReviewsDAO.class).generateNewReviewId(user);
        DAOFactory.get().getDao(ReviewsDAO.class).generateNewReviewId(user);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).getLastReviewId(user);
        assertEquals(3, lastReviewId);

        user = new CRUser(TEST_USER_NAMES[1]);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).getLastReviewId(user);
        assertEquals(0, lastReviewId);

        DAOFactory.get().getDao(ReviewsDAO.class).generateNewReviewId(user);
        DAOFactory.get().getDao(ReviewsDAO.class).generateNewReviewId(user);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).getLastReviewId(user);
        assertEquals(2, lastReviewId);
    }

    @Test
    public void testAddNewReview() throws DAOException {

        ReviewDTO testReview = new ReviewDTO();
        testReview.setObjectUrl("http://www.tieto.com");
        testReview.setReviewContent("Some content");
        testReview.setTitle("Title");

        CRUser user = new CRUser(TEST_USER_NAMES[0]);
        int lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).getLastReviewId(user);
        assertEquals(0, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview, user);
        assertEquals(1, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview, user);
        assertEquals(2, lastReviewId);
    }

    @Test
    public void testAddNewReviewWithoutPrecondition() throws DAOException {

        ReviewDTO testReview;
        testReview = new ReviewDTO();
        testReview.setObjectUrl("http://www.tieto.com");
        testReview.setReviewContent("Some content");
        testReview.setTitle("Title");

        CRUser user = new CRUser(TEST_USER_NAMES[0]);
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview, user);
        assertEquals(1, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview, user);
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

        CRUser user = new CRUser(TEST_USER_NAMES[0]);
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview1, user);
        assertEquals(1, lastReviewId);

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview2, user);
        assertEquals(2, lastReviewId);

        // just issue this call without using the return value
        DAOFactory.get().getDao(ReviewsDAO.class).getReviewList(user);
    }

    @Test
    public void testReadSingleReview() throws DAOException {

        ReviewDTO testReview1;
        testReview1 = new ReviewDTO();
        testReview1.setObjectUrl("http://www.tieto.com");
        testReview1.setReviewContent("Some content");
        testReview1.setTitle("Title 1");

        CRUser user = new CRUser(TEST_USER_NAMES[0]);
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview1, user);
        assertEquals(1, lastReviewId);

        ReviewDTO result = DAOFactory.get().getDao(ReviewsDAO.class).getReview(user, 1);
        assertEquals(1, result.getReviewID());
    }

    @Test
    public void testDeleteReview() throws DAOException {

        ReviewDTO testReview1;
        testReview1 = new ReviewDTO();
        testReview1.setObjectUrl("http://www.tieto.com");
        testReview1.setReviewContent("Some content");
        testReview1.setTitle("Title 1");

        CRUser user = new CRUser(TEST_USER_NAMES[0]);
        int lastReviewId;

        lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).addReview(testReview1, user);
        assertEquals(1, lastReviewId);

        DAOFactory.get().getDao(ReviewsDAO.class).deleteReview(user, 1, true);

        ReviewDTO result = DAOFactory.get().getDao(ReviewsDAO.class).getReview(user, 1);
        assertNull(result);
    }

    private void cleanUpReviews() throws RepositoryException, DAOException {
        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            for (String userName : TEST_USER_NAMES) {
                CRUser user = new CRUser(userName);
                int lastReviewId = DAOFactory.get().getDao(ReviewsDAO.class).getLastReviewId(user);
                for (int i = 1; i < lastReviewId + 1; i++) {
                    SesameUtil.executeSPARUL("CLEAR graph <" + user.getReviewUri(lastReviewId) + ">", null, conn);
                }
                URI context = conn.getValueFactory().createURI(user.getHomeUri());
                URI sub = conn.getValueFactory().createURI(user.getHomeUri());
                URI pred = conn.getValueFactory().createURI(Predicates.CR_USER_REVIEW_LAST_NUMBER);

                conn.remove(sub, pred, null, context);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
            throw e;
        } catch (OpenRDFException e) {
            e.printStackTrace();
        } finally {
            SesameUtil.close(conn);
        }

    }
}
