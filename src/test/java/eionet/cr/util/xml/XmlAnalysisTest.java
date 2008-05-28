package eionet.cr.util.xml;

import java.io.File;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class XmlAnalysisTest extends TestCase{

	/**
	 * 
	 */
	public void testXmlAnalysis(){
		
		XmlAnalysis xmlAnalysis = new XmlAnalysis();
		try{
			xmlAnalysis.parse(new File(this.getClass().getClassLoader().getResource("test-xml.xml").getFile()));
		}
		catch (Throwable t){
			t.printStackTrace();
			fail("Was not expecting this exception: " + t.toString());
		}
		
		assertNotNull(xmlAnalysis.getSchemaLocation()!=null);
		assertEquals(xmlAnalysis.getSchemaLocation(), "http://biodiversity.eionet.europa.eu/schemas/dir9243eec/habitats.xsd");
		
		assertTrue(xmlAnalysis.getSchemaNamespace()==null || xmlAnalysis.getSchemaNamespace().length()==0);
		
		assertNotNull(xmlAnalysis.getStartTag());
		assertEquals(xmlAnalysis.getStartTag(), "habitat");
		
		assertTrue(xmlAnalysis.getStartTagNamespace()==null || xmlAnalysis.getStartTagNamespace().length()==0);
		
		assertNull(xmlAnalysis.getSystemDtd());
		assertNull(xmlAnalysis.getPublicDtd());
	}
}
