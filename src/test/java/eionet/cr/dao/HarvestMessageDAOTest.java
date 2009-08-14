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

import java.util.List;

import org.dbunit.DBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.mysql.MySQLHarvestMessageDAO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.test.util.DbHelper;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * JUnit test tests HarvestMessageDAO functionality.
 * 
 * @author altnyris
 *
 */
public class HarvestMessageDAOTest extends DBTestCase {
	
	/**
	 * Provide a connection to the database.
	 */
	public HarvestMessageDAOTest(String name)
	{
		super( name );
		DbHelper.setUpConnectionProperties();
	}
	/**
	 * Load the data which will be inserted for the test
	 */
	protected IDataSet getDataSet() throws Exception {
		IDataSet loadedDataSet = new FlatXmlDataSet(
				getClass().getClassLoader().getResourceAsStream(GeneralConfig.SEED_FILE_NAME));
		return loadedDataSet;
	}
	
	@Test
	public void testInsertHarvestMessage() throws Exception {
		HarvestMessageDTO harvestMessage = new HarvestMessageDTO();
		harvestMessage.setHarvestId(55);
		harvestMessage.setMessage("test");
		harvestMessage.setStackTrace("teststack");
		harvestMessage.setType("01");
		
		ConnectionUtil.setReturnSimpleConnection(true);
		Integer messageID = get().getDao(HarvestMessageDAO.class).insertHarvestMessage(harvestMessage);
		assertNotNull(messageID);
	}
	
	@Test
	public void testFindHarvestMessagesByHarvestID() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		List<HarvestMessageDTO> messages = get().getDao(HarvestMessageDAO.class).findHarvestMessagesByHarvestID(121);
		assertEquals(2, messages.size());
		
	}
	
	@Test
	public void testFindHarvestMessageByMessageID() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		HarvestMessageDTO message = get().getDao(HarvestMessageDAO.class).findHarvestMessageByMessageID(4);
		assertEquals(new Integer(121), message.getHarvestId());
		assertEquals("ERROR", message.getMessage());
		assertEquals("Stack Trace: whatever", message.getStackTrace());
		assertEquals("err", message.getType());
	}
		
	@Test
	public void testDeleteMessage() throws Exception {

		ConnectionUtil.setReturnSimpleConnection(true);
		get().getDao(HarvestMessageDAO.class).deleteMessage(5);
		
		HarvestMessageDTO message = get().getDao(HarvestMessageDAO.class).findHarvestMessageByMessageID(5);
		assertNull(message);
	}

}
