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
package eionet.cr.dao;

import static eionet.cr.dao.mysql.MySQLDAOFactory.get;

import java.util.Collections;
import java.util.List;

import org.dbunit.DBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.util.DbHelper;
import eionet.cr.util.pagination.PaginationRequest;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * JUnit test tests HarvestSourceDAO functionality.
 * 
 * @author altnyris
 *
 */
public class HarvestSourceDAOTest extends DBTestCase {
	
	/**
	 * Provide a connection to the database.
	 */
	public HarvestSourceDAOTest(String name)
	{
		super( name );
		DbHelper.createConnectionPropertiesInSystem();
	}
	
	/*
	 * Load the data which will be inserted for the test.
	 * 
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		
		IDataSet loadedDataSet = new FlatXmlDataSet(
				getClass().getClassLoader().getResourceAsStream(GeneralConfig.SEED_FILE_NAME));
		return loadedDataSet;
	}
	
	@Test
	public void testAddSource() throws Exception {
		
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource = new HarvestSourceDTO();
		harvestSource.setUrl("http://rod.eionet.europa.eu/testObligations");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setIntervalMinutes(new Integer(0));
		
		ConnectionUtil.setReturnSimpleConnection(true);
		Integer harvestSourceID = get().getDao(HarvestSourceDAO.class).addSource(harvestSource, "bobsmith");
		assertNotNull(harvestSourceID);
	}
	
	@Test
	public void testGetHarvestSources() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		List<HarvestSourceDTO> sources = get().getDao(HarvestSourceDAO.class).getHarvestSources("", new PaginationRequest(1,100), null);
		assertEquals(42, sources.size());
	}
	
	@Test
	public void testGetHarvestSourceById() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		HarvestSourceDTO source = get().getDao(HarvestSourceDAO.class).getHarvestSourceById(45);
		assertEquals("http://localhost:8080/cr/pages/test.xml", source.getUrl());
		assertEquals("bob@europe.eu", source.getEmails());
		assertEquals(false, source.isTrackedFile());
	}
	
	@Test
	public void testEditSource() throws Exception {
		
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource.setSourceId(45);
		harvestSource.setUrl("http://localhost:8080/cr/pages/test.xml");
		harvestSource.setEmails("bob2@europe.eu");
		harvestSource.setIntervalMinutes(new Integer(0));
		
		ConnectionUtil.setReturnSimpleConnection(true);
		get().getDao(HarvestSourceDAO.class).editSource(harvestSource);
		
		HarvestSourceDTO source = get().getDao(HarvestSourceDAO.class).getHarvestSourceById(45);
		assertEquals("http://localhost:8080/cr/pages/test.xml", source.getUrl());
		assertEquals("bob2@europe.eu", source.getEmails());
		assertEquals(false, source.isTrackedFile());
		
	}
	
	@Test
	public void testDeleteSource() throws Exception {
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource.setSourceId(45);

		ConnectionUtil.setReturnSimpleConnection(true);
		get().getDao(HarvestSourceDAO.class).deleteSourceByUrl("http://localhost:8080/cr/pages/test.xml");
		
		HarvestSourceDTO source = get().getDao(HarvestSourceDAO.class).getHarvestSourceById(45);
		assertNull(source);
	}

}
