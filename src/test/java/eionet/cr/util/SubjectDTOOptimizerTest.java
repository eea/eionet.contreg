package eionet.cr.util;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.IDataSet;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.test.helpers.CRDatabaseTestCase;

public class SubjectDTOOptimizerTest extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("emptydb.xml");
	}
	
	public void testOptimizer(){

		try {
			// Alias for> 
			String uri = "http://www.eionet.europa.eu/gemet/concept/7697";
			//String uri = "http://www.eionet.europa.eu/gemet/concept?ns=1&cp=7697";
			
			HarvestSourceDTO newSource = new HarvestSourceDTO();
			newSource.setUrl(uri);
			newSource.setIntervalMinutes(100);
			
			DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(newSource, "test");

			
			URL url = new URL(uri);
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			Long subjectHash = Hashes.spoHash("http://www.eionet.europa.eu/gemet/concept/7697");
			
			
			SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(subjectHash);
			
			assertNotNull(subject);
			
			System.out.println(subject.getUri());
			System.out.println(Hashes.spoHash(subject.getUri()));
			System.out.println(Hashes.spoHash("http://www.eionet.europa.eu/gemet/concept/7697"));
			
			SubjectDTOOptimizer.optimizeSubjectDTOFactsheetView(subject, Util.getAcceptedLanguagesByImportance("et,pl;q=0.5,dk,ru;q=0.7"));
			
		} catch(Exception ex){
			System.out.println(ex.getMessage());
		}
		
	}
	
	public void testAcceptedLanguagePriority(){
		assertEquals(1.000, Util.getHTTPAcceptedLanguageImportance("en"));
		assertEquals(0.5000, Util.getHTTPAcceptedLanguageImportance("en;q=0.5"));
		assertEquals(0.55, Util.getHTTPAcceptedLanguageImportance("en;q=0.55"));
	}
	
	public void testOrdering(){
		List<String> languages = Util.getAcceptedLanguagesByImportance("et,pl;q=0.5,dk,ru;q=0.7");
		
		assertEquals("et", languages.get(0));
		assertEquals("dk", languages.get(1));
		assertEquals("ru", languages.get(2));
		assertEquals("pl", languages.get(3));
		assertEquals("en", languages.get(4));
		assertEquals("", languages.get(5));
		
	}

}
