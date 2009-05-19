package eionet.cr.test;
import java.net.URL;
import junit.framework.TestCase;

public class JavaURLTest extends TestCase {
	public void testURLWithSlash() throws Exception {
		URL basename = new URL("http://rs.tdwg.org/dwc/terms/");
		assertEquals(basename.toString(),"http://rs.tdwg.org/dwc/terms/");
		
		URL taxon = new URL(basename, "Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/Taxon");
	
		taxon  = new URL(basename, "sub/Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/sub/Taxon");
		
	}

	public void testURLWithHash() throws Exception {
		URL basename = new URL("http://rs.tdwg.org/dwc/terms/something.rdf#");
		assertEquals(basename.toString(),"http://rs.tdwg.org/dwc/terms/something.rdf#");
		
		URL taxon = new URL(basename, "Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/Taxon");
	
		taxon  = new URL(basename, "sub/Taxon");
		assertEquals(taxon.toString(),"http://rs.tdwg.org/dwc/terms/sub/Taxon");
		
	}

}
