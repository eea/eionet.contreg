package eionet.cr.dao;

import java.util.List;

import org.dbunit.DBTestCase;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Test;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.test.util.DbHelper;
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
	public void testAddSource() throws Exception {
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource = new HarvestSourceDTO();
		harvestSource.setName("obligations");
		harvestSource.setUrl("http://rod.eionet.europa.eu/testObligations");
		harvestSource.setCreator("bobsmith");
		harvestSource.setEmails("bob@europe.eu");
		harvestSource.setType("data");
		
		ConnectionUtil.setReturnSimpleConnection(true);
		Integer harvestSourceID = DAOFactory.getDAOFactory().getHarvestSourceDAO().addSource(harvestSource, "bobsmith");
		assertNotNull(harvestSourceID);
	}
	
	@Test
	public void testGetHarvestSources() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		List<HarvestSourceDTO> sources = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSources();
		assertEquals(42, sources.size());
	}
	
	@Test
	public void testGetHarvestSourceById() throws Exception {
		
		ConnectionUtil.setReturnSimpleConnection(true);
		HarvestSourceDTO source = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(45);
		assertEquals("test1", source.getName());
		assertEquals("http://localhost:8080/cr/pages/test.xml", source.getUrl());
		assertEquals("bobsmith", source.getCreator());
		assertEquals("bob@europe.eu", source.getEmails());
		assertEquals("data", source.getType());
	}
	
	@Test
	public void testEditSource() throws Exception {
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource.setSourceId(45);
		harvestSource.setName("test2");
		harvestSource.setUrl("http://localhost:8080/cr/pages/test2.xml");
		harvestSource.setEmails("bob2@europe.eu");
		harvestSource.setType("schema");
		
		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestSourceDAO().editSource(harvestSource);
		
		HarvestSourceDTO source = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(45);
		assertEquals("test2", source.getName());
		assertEquals("http://localhost:8080/cr/pages/test2.xml", source.getUrl());
		assertEquals("bob2@europe.eu", source.getEmails());
		assertEquals("schema", source.getType());
		
	}
	
	@Test
	public void testDeleteSource() throws Exception {
		HarvestSourceDTO harvestSource = new HarvestSourceDTO();
		harvestSource.setSourceId(45);

		ConnectionUtil.setReturnSimpleConnection(true);
		DAOFactory.getDAOFactory().getHarvestSourceDAO().deleteSource(harvestSource);
		
		HarvestSourceDTO source = DAOFactory.getDAOFactory().getHarvestSourceDAO().getHarvestSourceById(45);
		assertNull(source);
	}

}
