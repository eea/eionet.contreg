package eionet.cr.util.sql;

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class UserReviewTest extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("emptydb.xml");
	}
	
	@Test
	public void testReadLastReviewId(){
		try {
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
		
		} catch(Exception ex){
			
		}
	}
	
	@Test
	public void testAddNewReview(){
		try {
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
			
		} catch (Exception ex){
			
		}
		
	}

	@Test
	public void testAddNewReviewWithoutPrecondition(){
		try {
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
			
		} catch (Exception ex){
			
		}
		
	}
	
	@Test
	public void testReadReviews(){
		try {
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
			
			List<ReviewDTO> reviews = DAOFactory.get().getDao(HelperDAO.class).getReviewList(user);
			
			System.out.println("Review count: "+reviews.size());
			
			for (int a=0; a<reviews.size(); a++){
				System.out.println("Review @" + a +": "+reviews.get(a).getReviewSubjectUri()+ " / "+reviews.get(a).getTitle());
			}
			
		} catch (Exception ex){
			System.out.println("Error: "+ex.getMessage());
		}
		
	}
	
	@Test
	public void testReadSingleReview(){
		try {
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
			
			System.out.println(result.getTitle());
			
			
		} catch (Exception ex){
			System.out.println("Error: "+ex.getMessage());
		}
		
	}
	
}
