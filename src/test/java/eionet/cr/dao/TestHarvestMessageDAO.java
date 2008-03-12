package eionet.cr.dao;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * JUnit test tests HarvestScheduleDAO functionality.
 * 
 * @author altnyris
 *
 */
public class TestHarvestMessageDAO {
	private static HarvestMessageDTO harvestMessage;
	
	@Test
	public void testInsertHarvestMessage() throws Exception {
		harvestMessage = new HarvestMessageDTO();
		harvestMessage.setHarvestId(55);
		harvestMessage.setMessage("test");
		harvestMessage.setStackTrace("teststack");
		harvestMessage.setType("01");
		
		ConnectionUtil.setTestConnection(true);
		Integer messageID = DAOFactory.getDAOFactory().getHarvestMessageDAO().insertHarvestMessage(harvestMessage);
		assertNotNull(messageID);
		harvestMessage.setHarvestMessageId(messageID);
	}
	
	@Test
	public void testFindHarvestMessagesByHarvestID() throws Exception {
		
		ConnectionUtil.setTestConnection(true);
		List<HarvestMessageDTO> messages = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessagesByHarvestID(harvestMessage.getHarvestId());
		assertEquals(1, messages.size());
		
	}
	
	@Test
	public void testFindHarvestMessageByMessageID() throws Exception {
		
		ConnectionUtil.setTestConnection(true);
		HarvestMessageDTO message = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessageByMessageID(harvestMessage.getHarvestMessageId());
		assertEquals(55, message.getHarvestId());
		assertEquals("test", message.getMessage());
		assertEquals("teststack", message.getStackTrace());
		assertEquals("01", message.getType());
		
		harvestMessage = message;
	}
		
	@Test
	public void testDeleteMessage() throws Exception {

		ConnectionUtil.setTestConnection(true);
		DAOFactory.getDAOFactory().getHarvestMessageDAO().deleteMessage(harvestMessage.getHarvestMessageId());
		
		HarvestMessageDTO message = DAOFactory.getDAOFactory().getHarvestMessageDAO().findHarvestMessageByMessageID(harvestMessage.getHarvestMessageId());
		assertNull(message);
	}

}
