package eionet.cr.util;

import java.io.File;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UtilTest extends TestCase{

	/**
	 * 
	 */
	public void test_isValidXmlFile(){
		
		try {
			boolean b = Util.isValidXmlFile(new File(this.getClass().getClassLoader().getResource("test-rdf.xml").getFile()));
			assertTrue(b);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}
	}
}
