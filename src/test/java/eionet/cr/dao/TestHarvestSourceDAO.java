package eionet.cr.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestScheduleDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.sql.ConnectionUtil;

public class TestHarvestSourceDAO {
	private static HarvestSourceDTO harvestSource;
	
	@Test
	public void testAddSource() throws Exception {
		harvestSource = new HarvestSourceDTO();
		harvestSource.setName("obligations");
		harvestSource.setUrl("http://rod.eionet.europa.eu/testObligations");
		harvestSource.setCreator("bobsmith");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setType("data");
		
		HarvestScheduleDTO schedule = new HarvestScheduleDTO();
		schedule.setHour(12);
		schedule.setPeriod(2);
		schedule.setWeekday("Monday");
		harvestSource.setHarvestSchedule(schedule);
		
		ConnectionUtil.setTestConnection(true);
		Integer harvestSourceID = DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(harvestSource, "bobsmith");
		assertTrue(harvestSourceID != null);
		harvestSource.setSourceId(harvestSourceID);
	}
	
	@Test
	public void testGetHarvestSourceById() throws Exception {
		
		ConnectionUtil.setTestConnection(true);
		HarvestSourceDTO source = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
		assertEquals("obligations", source.getName());
		assertEquals("http://rod.eionet.europa.eu/testObligations", source.getUrl());
		assertEquals("bobsmith", source.getCreator());
		assertEquals("bob@europe.eu", source.getEmails());
		assertEquals("data", source.getType());
		
		assertEquals(12, source.getHarvestSchedule().getHour());
		assertEquals(2, source.getHarvestSchedule().getPeriod());
		assertEquals("Monday", source.getHarvestSchedule().getWeekday());
		
		harvestSource = source;
	}
	
	@Test
	public void testEditSource() throws Exception {

		harvestSource.setName("obligations2");
		harvestSource.setUrl("http://rod.eionet.europa.eu/testObligations2");
		harvestSource.setEmails("bob2@europe.eu");
		harvestSource.setType("schema");
		
		HarvestScheduleDTO schedule = new HarvestScheduleDTO();
		schedule.setHour(14);
		schedule.setPeriod(4);
		schedule.setWeekday("Sunday");
		harvestSource.setHarvestSchedule(schedule);
		
		ConnectionUtil.setTestConnection(true);
		DAOFactory.getDAOFactory().getHarvestSourceDAO().editSource(harvestSource);
		
		HarvestSourceDTO source = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
		assertEquals("obligations2", source.getName());
		assertEquals("http://rod.eionet.europa.eu/testObligations2", source.getUrl());
		assertEquals("bob2@europe.eu", source.getEmails());
		assertEquals("schema", source.getType());
		
	}
	
	@Test
	public void testDeleteSource() throws Exception {

		ConnectionUtil.setTestConnection(true);
		DAOFactory.getDAOFactory().getHarvestSourceDAO().deleteSource(harvestSource);
		
		HarvestSourceDTO source = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(harvestSource.getSourceId());
		assertNull(source);
	}

}
