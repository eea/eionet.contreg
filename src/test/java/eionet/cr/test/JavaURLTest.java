package eionet.cr.test;
import java.net.URL;
import junit.framework.TestCase;

public class JavaURLTest extends TestCase {
	/**
	 * This method tests the rdf:about="..." when the base URL ends with a "/"
	 * @throws Exception
	 */
	public void testURLWithSlash() throws Exception {
		URL basename = new URL("http://rs.tdwg.org/dwc/terms/");
		assertEquals(basename.toString(),"http://rs.tdwg.org/dwc/terms/");
		
		URL taxon = new URL(basename, "Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/Taxon");
	
		taxon  = new URL(basename, "sub/Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/sub/Taxon");

		URL samesame = new URL(basename, "");
		assertEquals(samesame.toString(),"http://rs.tdwg.org/dwc/terms/");
		
		// Test rdf:ID="kingdom" == rdf:about="#kingdom"
		URL rdfid = new URL(basename, "#kingdom");
		assertEquals(rdfid.toString(),"http://rs.tdwg.org/dwc/terms/#kingdom");
	}

	/**
	 * This method tests the rdf:about="..." when the base url ends with a real file.
	 * @throws Exception
	 */
	public void testURLWithHash() throws Exception {
		URL basename = new URL("http://rs.tdwg.org/dwc/terms/something.rdf#");
		assertEquals(basename.toString(),"http://rs.tdwg.org/dwc/terms/something.rdf#");
		
		URL taxon = new URL(basename, "Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/Taxon");
	
		// Test rdf:about="sub/Taxon"
		taxon  = new URL(basename, "sub/Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/sub/Taxon");
		
		// Test rdf:about=""
		URL samesame = new URL(basename, "");
		assertEquals(samesame.toString(),"http://rs.tdwg.org/dwc/terms/something.rdf#");

		// Test rdf:ID="kingdom" == rdf:about="#kingdom"
		URL rdfid = new URL(basename, "#kingdom");
		assertEquals(rdfid.toString(),"http://rs.tdwg.org/dwc/terms/something.rdf#kingdom");

	}

	/**
	 * This method explains the problem with local-file URLs obtained with getClass().getClassLoader().getResource(). 
	 */
	public void testURLWithResource(){
		
		URL url = getClass().getClassLoader().getResource("taxon-under-rdf.xml");
		assertEquals("file:///C:/projects/eeasvn/cr2/target/test-classes/taxon-under-rdf.xml", url.toString());
	}
}
