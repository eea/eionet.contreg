package eionet.cr.util.xml;

import java.io.File;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ConversionsParserTest extends TestCase{

	/**
	 * 
	 */
	public void testConversionsParser(){
		
		ConversionsParser conversionsParser = new ConversionsParser();
		try {
			conversionsParser.parse(new File(this.getClass().getClassLoader().getResource("test-conversions.xml").getFile()));
		}
		catch (Throwable t) {
			t.printStackTrace();
			fail("Was not expecting this exception: " + t.toString());
		}
		
		assertNotNull(conversionsParser.getRdfConversionId());
		assertEquals(conversionsParser.getRdfConversionId(), "89");
	}
}
