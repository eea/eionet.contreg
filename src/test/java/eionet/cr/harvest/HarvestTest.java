package eionet.cr.harvest;

import java.net.URL;

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
			URL o = getClass().getClassLoader().getResource("test-rdf.xml");
			Harvest harvest = new PullHarvest(o.toString(), null);
			harvest.execute();
			assertEquals((int)10, harvest.getDistinctSubjectsCount());
			assertEquals((int)44, harvest.getStoredTriplesCount());
			
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());
		}
	}
	
	@Test
	public void testHarvestNonExistingURL(){
		
		try {
			Harvest harvest = new PullHarvest("http://www.jaanusheinlaid.tw", null);
			harvest.execute();
			fail("Was expecting " + HarvestException.class.getName() + " with a cause of " + java.net.UnknownHostException.class.getName());
		}
		catch (Throwable e){
			if (e.getCause()==null || !(e.getCause() instanceof java.net.UnknownHostException))
				fail("Was expecting " + HarvestException.class.getName() + " with a cause of " + java.net.UnknownHostException.class.getName());
		}
	}
}
