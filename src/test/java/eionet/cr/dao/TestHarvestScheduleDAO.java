package eionet.cr.dao;

import static org.junit.Assert.*;

import org.junit.Test;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestScheduleDTO;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * JUnit test tests HarvestScheduleDAO functionality.
 * 
 * @author altnyris
 *
 */
public class TestHarvestScheduleDAO {
	private static HarvestScheduleDTO harvestSchedule;
	
	@Test
	public void testAddSchedule() throws Exception {
		harvestSchedule = new HarvestScheduleDTO();
		harvestSchedule.setHour(12);
		harvestSchedule.setPeriod(2);
		harvestSchedule.setWeekday("Monday");
		harvestSchedule.setHarvestSourceId(55);
		
		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestScheduleDAO().addSchedule(harvestSchedule);
	}
	
	@Test
	public void testGetHarvestScheduleBySourceId() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(harvestSchedule.getHarvestSourceId());
		assertEquals(12, schedule.getHour());
		assertEquals(2, schedule.getPeriod());
		assertEquals("Monday", schedule.getWeekday());
		
		harvestSchedule = schedule;
	}
	
	@Test
	public void testEditSchedule() throws Exception {

		harvestSchedule.setHour(10);
		harvestSchedule.setPeriod(4);
		harvestSchedule.setWeekday("Sunday");
				
		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestScheduleDAO().editSchedule(harvestSchedule);
		
		HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(harvestSchedule.getHarvestSourceId());
		assertEquals(10, schedule.getHour());
		assertEquals(4, schedule.getPeriod());
		assertEquals("Sunday", schedule.getWeekday());
		
	}
	
	@Test
	public void testDeleteSchedule() throws Exception {

		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestScheduleDAO().deleteSchedule(harvestSchedule.getHarvestSourceId());
		
		HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(harvestSchedule.getHarvestSourceId());
		assertNull(schedule);
	}

}
