package eionet.cr.dao;

import java.util.List;

import org.dbunit.DBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.test.util.DbHelper;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * JUnit test tests HarvestScheduleDAO functionality.
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
		Integer messageID = DAOFactory.getDAOFactory().getHarvestMessageDAO().insertHarvestMessage(harvestMessage);
		assertNotNull(messageID);
	}
	
	@Test
	public void testFindHarvestMessagesByHarvestID() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		List<HarvestMessageDTO> messages = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessagesByHarvestID(121);
		assertEquals(2, messages.size());
		
	}
	
	@Test
	public void testFindHarvestMessageByMessageID() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		HarvestMessageDTO message = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessageByMessageID(4);
		assertEquals(new Integer(121), message.getHarvestId());
		assertEquals("ERROR", message.getMessage());
		assertEquals("Stack Trace: whatever", message.getStackTrace());
		assertEquals("err", message.getType());
	}
		
	@Test
	public void testDeleteMessage() throws Exception {

		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestMessageDAO().deleteMessage(5);
		
		HarvestMessageDTO message = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessageByMessageID(5);
		assertNull(message);
	}

}
