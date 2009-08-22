/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
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
			assertEquals((int)11, harvest.getDistinctSubjectsCount());
			assertEquals((int)48, harvest.getStoredTriplesCount());
			
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
