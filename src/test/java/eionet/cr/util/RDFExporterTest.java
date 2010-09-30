package eionet.cr.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.dbunit.dataset.IDataSet;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RDFExporterTest extends CRDatabaseTestCase{

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
			return getXmlDataSet("emptydb.xml");
	}

	/**
	 * @throws DAOException 
	 * @throws HarvestException 
	 * @throws MalformedURLException 
	 * 
	 */
	public void testTriplesReader() throws DAOException, HarvestException, MalformedURLException{
		
		String uri = "http://www.eionet.europa.eu/gemet/concept/7697";

		DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(uri, 100, false, null);

		URL url = new URL(uri);
		Harvest harvest = new PullHarvest(url.toString(), null);
		harvest.execute();

		Long sourceHash = Hashes.spoHash(uri);

		SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(sourceHash);
		assertNotNull(subject);
	}
	
	/**
	 * 
	 */
	public void testGetKnownNamespace(){
		
		assertEquals("rdf", NamespaceUtil.getKnownNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		assertEquals("rdfs", NamespaceUtil.getKnownNamespace("http://www.w3.org/2000/01/rdf-schema#"));
		assertEquals("owl", NamespaceUtil.getKnownNamespace("http://www.w3.org/2002/07/owl#"));
		assertEquals("dc", NamespaceUtil.getKnownNamespace("http://purl.org/dc/elements/1.1/"));
		assertEquals("eper", NamespaceUtil.getKnownNamespace("http://rdfdata.eionet.europa.eu/eper/dataflow"));
	}
	
	/**
	 * 
	 */
	public void testGetPredicate(){
		
		assertEquals("predicate1", NamespaceUtil.extractPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate1"));
		assertEquals("predicate1", NamespaceUtil.extractPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns/predicate1"));
	}

}
