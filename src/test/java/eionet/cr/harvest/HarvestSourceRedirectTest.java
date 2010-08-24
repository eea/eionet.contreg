package eionet.cr.harvest;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class HarvestSourceRedirectTest extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("emptydb.xml");
	}

	/**
	 * This test should test more than 4 redirections and throw harvest exception.
	 */
	
	@Test
	public void testHarvestRedirectedURLsMoreThan4(){
		
		try {

			String url = "http://localhost:8080/url-redirect-testcase/url1.jsp";
			try {
				DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(url, 200, false, "bob@europe.eu");
			}
			catch (DAOException ex){
			}
			
			PullHarvest harvest = new PullHarvest(url, null);
			harvest.execute();

			fail("Should have thrown too many redirections exception");
		}
		catch (HarvestException e) {
			// If test reaches exception, it is considered successful.
			assertTrue (true);
		}
	}
	
	/**
	 * This test should test up to 4 redirections and throw harvest exception as the original source is not found in DB.
	 */
	
	@Test
	public void testHarvestRedirectedOriginalSourceNotInDB(){
		
		try {

			String url = "http://localhost:8080/url-redirect-testcase/url2.jsp";
			
			PullHarvest harvest = new PullHarvest(url, null);
			harvest.execute();

			fail("Should have thrown exception for not having the source in DB");
		}
		catch (HarvestException e) {
			// If test reaches exception, it is considered successful.
			assertTrue (true);
		}
	}
	
	/**
	 * This test should test up to 4 redirections and harvest all the sources.
	 */
	@Test
	public void testHarvestRedirectedURLUpTo4(){
		
		try {

			String url = "http://localhost:8080/url-redirect-testcase/url2.jsp";
			
			HarvestSourceDTO harvestSource = new HarvestSourceDTO();
			harvestSource = new HarvestSourceDTO();
			harvestSource.setUrl(url);
			harvestSource.setEmails("bob@europe.eu");
			harvestSource.setIntervalMinutes(200);
			try {
				DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(url, 200, false, "bob@europe.eu");
			}
			catch (DAOException ex){
			}
			
			PullHarvest harvest = new PullHarvest(url, null);
			harvest.execute();
			
			assertNotNull(harvest.getSourceAvailable());
			assertTrue(harvest.getSourceAvailable().booleanValue()); // This source is available
			assertEquals((int)1, harvest.getDistinctSubjectsCount());
			assertEquals((int)5, harvest.getStoredTriplesCount());	
		}
		catch (HarvestException e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}
	}
	
	
}
