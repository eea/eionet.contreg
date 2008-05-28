package eionet.cr.dao;

import org.dbunit.DBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestScheduleDTO;
import eionet.cr.test.util.DbHelper;
import eionet.cr.util.sql.ConnectionUtil;

/**
 * JUnit test tests HarvestScheduleDAO functionality.
 * 
 * @author altnyris
 *
 */
public class HarvestScheduleDAOTest extends DBTestCase {
	
	/**
	 * Provide a connection to the database.
	 */
	public HarvestScheduleDAOTest(String name)
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
	public void testAddSchedule() throws Exception {
		HarvestScheduleDTO harvestSchedule = new HarvestScheduleDTO();
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
		HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(45);
		assertEquals(new Integer(0), schedule.getHour());
		assertEquals(new Integer(1), schedule.getPeriod());
		assertEquals("monday", schedule.getWeekday());
	}
	
	@Test
	public void testEditSchedule() throws Exception {
		
		HarvestScheduleDTO harvestSchedule = new HarvestScheduleDTO();
		harvestSchedule.setHarvestSourceId(45);
		harvestSchedule.setHour(10);
		harvestSchedule.setPeriod(4);
		harvestSchedule.setWeekday("Sunday");
				
		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestScheduleDAO().editSchedule(harvestSchedule);
		
		HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(45);
		assertEquals(new Integer(10), schedule.getHour());
		assertEquals(new Integer(4), schedule.getPeriod());
		assertEquals("Sunday", schedule.getWeekday());
		
	}
	
	@Test
	public void testDeleteSchedule() throws Exception {

		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestScheduleDAO().deleteSchedule(45);
		
		HarvestScheduleDTO schedule = DAOFactory.getDAOFactory().getHarvestScheduleDAO().getHarvestScheduleBySourceId(45);
		assertNull(schedule);
	}

}
