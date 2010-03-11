/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao;

import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;

public class ScheduledHarvestSourcesTest extends CRDatabaseTestCase{

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("emptydb.xml");
	}

	@Test
	public void testScheduledSources() throws Exception{
		
		HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
		
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource.setUrl("http://url.ee/id/1");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setIntervalMinutes(new Integer(5));
		dao.addSource(harvestSource, "bobsmith");
		
		harvestSource = new HarvestSourceDTO();
		harvestSource.setUrl("http://url.ee/id/2");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setIntervalMinutes(new Integer(10));
		dao.addSource(harvestSource, "bobsmith");
		
		harvestSource = new HarvestSourceDTO();
		harvestSource.setUrl("http://url.ee/id/3");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setIntervalMinutes(new Integer(15));
		dao.addSource(harvestSource, "bobsmith");

		harvestSource = new HarvestSourceDTO();
		harvestSource.setUrl("http://url.ee/id/4");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setIntervalMinutes(new Integer(20));
		dao.addSource(harvestSource, "bobsmith");

		List<HarvestSourceDTO> dtos = dao.getNextScheduledSources(1);
		assertNotNull(dtos);
		assertEquals(4, dtos.size());
	}
}
