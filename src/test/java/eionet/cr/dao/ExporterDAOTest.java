package eionet.cr.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.postgre.PostgreSQLExporterDAO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

public class ExporterDAOTest  extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	@Override	
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("types-db.xml");
	}

	@Test
	public void testCreateConcurrentTempTables() throws Exception {
		MockPostgreSQLExporterDAO mockExporterDAO = new MockPostgreSQLExporterDAO();
		
		String createTempSQL = mockExporterDAO.getCreateTempSubjectsTablesQuery("select SUBJECT from SPO", "");
		String dropTempSQL = mockExporterDAO.getDropTempTableQuery();
		String getSubjectsSQL = mockExporterDAO.getSubjectsDataFromTmpTablesQuery();
		Connection conn1 = null;
		Connection conn2 = null;
		try{
			conn1 = mockExporterDAO.getSQLConnection();
			conn2 = mockExporterDAO.getSQLConnection();
			
			SQLUtil.executeUpdate(createTempSQL, conn1);
			SQLUtil.executeUpdate(dropTempSQL, conn2);

			SingleObjectReader<String> reader = new SingleObjectReader<String>();
			SQLUtil.executeQuery(getSubjectsSQL, null, reader, conn1);
			
			List<String> list = reader.getResultList();
			assertTrue(list.size()>0);
			
		}
		finally{
			SQLUtil.close(conn1);
			SQLUtil.close(conn2);
		}
		

	}	
	/**
	 * Mock class for accessing abstract class protected methods.
	 * 
	 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>, Tieto Estonia
	 */
	class MockPostgreSQLExporterDAO extends PostgreSQLExporterDAO {
		
		protected String getCreateTempSubjectsTablesQuery(String subjectsSubQuery, String predicateHashesCommaSeparated) {
			return super.getCreateTempSubjectsTablesQuery(subjectsSubQuery, predicateHashesCommaSeparated);
		}
		protected String getDropTempTableQuery(){
			return super.getDropTempTableQuery();
		}
		protected String getSubjectsDataFromTmpTablesQuery(){
			return super.getSubjectsDataFromTmpTablesQuery();
		}
		protected Connection getSQLConnection() throws SQLException{
			return super.getSQLConnection();
		}
	}
	
}
