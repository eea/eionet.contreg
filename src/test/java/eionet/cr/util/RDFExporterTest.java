package eionet.cr.util;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dbunit.dataset.IDataSet;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.Harvest;
import eionet.cr.harvest.PullHarvest;
import eionet.cr.test.helpers.CRDatabaseTestCase;

public class RDFExporterTest extends CRDatabaseTestCase{

	

	protected IDataSet getDataSet() throws Exception {
			return getXmlDataSet("emptydb.xml");
	}
/*		
	public void testDistinctPredicatesReader(){	
		try {
			String uri = "http://www.eionet.europa.eu/gemet/concept/7697";
			DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(uri, 100, false, null);
			
			URL url = new URL(uri);
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			Long subjectHash = Hashes.spoHash("http://www.eionet.europa.eu/gemet/concept/7697");
			
			SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(subjectHash);
			assertNotNull(subject);
			
			System.out.println("Reading distinct predicates for " + subject.getUri());
			System.out.println("FOUND: ");
			
			List<PredicateDTO> predicates = DAOFactory.get().getDao(HelperDAO.class).readDistinctPredicates(Hashes.spoHash(subject.getUri()));
			predicates.add(new PredicateDTO("http://www.estweb.ee/jaak/thisisnotrealaddress.rdf"));
			
			for (int a=0; a<predicates.size(); a++){
				System.out.println(predicates.get(a));
			}
			
			System.out.println("Namespace / URL mappings:");
			HashMap<String, String> namespacePrefixes = NamespaceUtil.getNamespacePrefix(predicates);
			
			Iterator iterator = namespacePrefixes.keySet().iterator();  
   
			while (iterator.hasNext()) {  
				String key = iterator.next().toString();  
				String value = namespacePrefixes.get(key).toString();  
				System.out.println(key + "  -  " + value);  
			}  
		
			
			
			System.out.println();
			System.out.println();
			System.out.println();
			
		} catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}

	public void testDistinctSubjectUriReader(){	
		try {
			String uri = "http://www.eionet.europa.eu/gemet/concept/7697";
			DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(uri, 100, false, null);
			
			URL url = new URL(uri);
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			Long subjectHash = Hashes.spoHash("http://www.eionet.europa.eu/gemet/concept/7697");
			
			SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(subjectHash);
			assertNotNull(subject);
			
			System.out.println("Reading distinct subjects for " + subject.getUri());
			System.out.println("FOUND: ");
			
			List<String> subjectUris = DAOFactory.get().getDao(HelperDAO.class).readDistinctSubjectUrls(Hashes.spoHash(subject.getUri()));
			
			for (int a=0; a<subjectUris.size(); a++){
				System.out.println(subjectUris.get(a));
			}
			
			System.out.println();
			System.out.println();
			System.out.println();
			
		} catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	*/
	public void testTriplesReader(){
		try {
			String uri = "http://www.eionet.europa.eu/gemet/concept/7697";
			DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(uri, 100, false, null);
			
			URL url = new URL(uri);
			Harvest harvest = new PullHarvest(url.toString(), null);
			harvest.execute();
			
			Long sourceHash = Hashes.spoHash("http://www.eionet.europa.eu/gemet/concept/7697");
			
			SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(sourceHash);
			assertNotNull(subject);
			
			System.out.println(subject.getUri());
			
			System.out.println(RDFExporter.export(sourceHash));
			
		} catch(Exception ex){
			System.out.println(ex.getMessage());
		}
	}
	
	public void testGetKnownNamespace(){
		assertEquals("rdf", NamespaceUtil.getKnownNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
		assertEquals("rdfs", NamespaceUtil.getKnownNamespace("http://www.w3.org/2000/01/rdf-schema#"));
		assertEquals("owl", NamespaceUtil.getKnownNamespace("http://www.w3.org/2002/07/owl#"));
		assertEquals("dc", NamespaceUtil.getKnownNamespace("http://purl.org/dc/elements/1.1/"));
		assertEquals("eper", NamespaceUtil.getKnownNamespace("http://rdfdata.eionet.europa.eu/eper/dataflow"));
	}
	
	public void testGetPredicate(){
		assertEquals("predicate1", NamespaceUtil.extractPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate1"));
		assertEquals("predicate1", NamespaceUtil.extractPredicate("http://www.w3.org/1999/02/22-rdf-syntax-ns/predicate1"));
	}

}
