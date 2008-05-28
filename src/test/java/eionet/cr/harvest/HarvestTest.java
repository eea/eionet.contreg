package eionet.cr.harvest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * 
 * @author heinljab
 *
 */
public class HarvestTest extends TestCase{

	@Test
	public void testHarvestFile(){
				
		try {
			Harvest harvest = new PullHarvest(getClass().getClassLoader().getResource("test-rdf.xml").toString(), null, null);
			harvest.execute();
			assertEquals((int)10, harvest.getCountTotalResources());
			assertEquals((int)10, harvest.getCountEncodingSchemes());
			assertEquals((int)44, harvest.getCountTotalStatements());
			assertEquals((int)20, harvest.getCountLiteralStatements());
			
		} catch (HarvestException e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}
	}
	
	@Test
	public void testHarvestNonExistingURL(){
		
		try {
			Harvest harvest = new PullHarvest("http://www.jaanusheinlaid.tw", null, null);
			harvest.execute();
			fail("Was expecting " + HarvestException.class.getName() + " with a cause of " + java.net.UnknownHostException.class.getName());
		} catch (HarvestException e){
			if (e.getCause()==null || !(e.getCause() instanceof java.net.UnknownHostException))
				fail("Was expecting " + HarvestException.class.getName() + " with a cause of " + java.net.UnknownHostException.class.getName());
		}
	}

}
