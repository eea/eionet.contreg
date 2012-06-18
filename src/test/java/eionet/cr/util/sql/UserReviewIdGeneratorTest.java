package eionet.cr.util.sql;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.DAOException;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 * 
 */

public class UserReviewIdGeneratorTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    @Test
    public void testReadLastReviewId() throws DAOException {

        /*
         * CRUser user = new CRUser("kaptejaa"); int lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user);
         * 
         * assertEquals(0, lastReviewId);
         * 
         * int newId = DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user); assertEquals(1, newId);
         * 
         * DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
         * DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
         * 
         * lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user); assertEquals(3, lastReviewId);
         * 
         * user = new CRUser("someoneelse");
         * 
         * lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user); assertEquals(0, lastReviewId);
         * 
         * DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
         * DAOFactory.get().getDao(HelperDAO.class).generateNewReviewId(user);
         * 
         * lastReviewId = DAOFactory.get().getDao(HelperDAO.class).getLastReviewId(user); assertEquals(2, lastReviewId);
         */
    }
}
