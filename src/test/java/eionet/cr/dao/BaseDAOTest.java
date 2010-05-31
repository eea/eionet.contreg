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
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dao.postgre.PostgreSQLBaseDAO;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.sql.SQLUtil;

/**
 * The class tests different methods in PostgreSQLBaseDAO class 
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>, Tieto Estonia
 */

public class BaseDAOTest  extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	@Override	
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("types-db.xml");
	}
	
	/**
	 * 
	 * @throws DAOException
	 */
	@Test
	public void testGetSubjectsDataWithTempTable() throws DAOException {
		MockPostgreSQLBaseDAO baseDAO = new MockPostgreSQLBaseDAO();
		
		Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
		SubjectDataReader reader = new SubjectDataReader(subjectsMap);
		
		//retreive data with temp tables
		List<SubjectDTO> list = baseDAO.getSubjectsDataWithTempTable(reader, "select SUBJECT from SPO");
		assertTrue(list.size()>0);
		assertEquals(3, list.get(0).getPredicateCount());
		assertEquals(5, list.get(1).getPredicateCount());

		//retreive data directly from spo
		List<SubjectDTO> list2 = baseDAO.getSubjectsData(reader, "select SUBJECT from SPO");
		
		//compare results
		assertTrue(list.equals(list2));

	}
	
	/**
	 * 
	 * @throws DAOException
	 */
	@Test
	public void testGetSubjectsDataWithPredicate() throws DAOException {
		MockPostgreSQLBaseDAO baseDAO = new MockPostgreSQLBaseDAO();
		
		Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
		subjectsMap.put(5550937339998314774L, null);
		SubjectDataReader reader = new SubjectDataReader(subjectsMap);
		reader.addPredicateHash(8744745537220110927L);
		
		//retreive data with subquery
		List<SubjectDTO> list1 = baseDAO.getSubjectsData(reader, "select SUBJECT from SPO where SUBJECT = 5550937339998314774");
		assertEquals(1, list1.size());
		assertEquals(1, list1.get(0).getPredicateCount());
		
		//retreive data for list of subjects
		List<SubjectDTO> list2 = baseDAO.getSubjectsData(reader);
		assertEquals(1, list2.size());
		assertEquals(1, list2.get(0).getPredicateCount());
		
		//compare results
		assertTrue(list1.equals(list2));

	}

	/**
	 * 
	 * @throws DAOException
	 */
	@Test
	public void testGetSubjectsData() throws DAOException {
		MockPostgreSQLBaseDAO baseDAO = new MockPostgreSQLBaseDAO();
		
		Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
		subjectsMap.put(-6601083188331798490L, null);
		
		//retreive data for list of subjects
		List<SubjectDTO> list = baseDAO.getSubjectsData(subjectsMap);
		assertEquals(1, list.size());
		assertEquals(5, list.get(0).getPredicateCount());
	}

	/**
	 * Test creating and droping temporary tables
	 * @throws DAOException
	 */
	@Test
	public void testCreateTempTables() throws Exception {
		MockPostgreSQLBaseDAO baseDAO = new MockPostgreSQLBaseDAO();

		String createTempSQL = baseDAO.getCreateTempSubjectsTablesQuery("select SUBJECT from SPO", null);
		String dropTempSQL = baseDAO.getDropTempTableQuery();
		String getSubjectsSQL = baseDAO.getSubjectsDataFromTmpTablesQuery();
		Connection conn1 = null;
		Connection conn2 = null;
		try{
			conn1 = baseDAO.getConnection();
			conn2 = baseDAO.getConnection();
			
			SQLUtil.executeUpdate(createTempSQL, conn1);
			SQLUtil.executeUpdate(dropTempSQL, conn2);

			SubjectDataReader reader = new SubjectDataReader(new LinkedHashMap<Long, SubjectDTO>());
			SQLUtil.executeQuery(getSubjectsSQL, null, reader, conn1);
			
			List<SubjectDTO> list = reader.getResultList();
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
	class MockPostgreSQLBaseDAO extends PostgreSQLBaseDAO {
		
		protected List<SubjectDTO> getSubjectsDataWithTempTable(SubjectDataReader reader, String subjectsSubQuery) throws DAOException{
			return super.getSubjectsDataWithTempTable(reader, subjectsSubQuery);
		}
		protected List<SubjectDTO> getSubjectsData(SubjectDataReader reader, String subjectsSubQuery) throws DAOException{
			return super.getSubjectsData(reader, subjectsSubQuery);
		}
		protected List<SubjectDTO> getSubjectsData(SubjectDataReader reader) throws DAOException{
			return super.getSubjectsData(reader);
		}
		protected List<SubjectDTO> getSubjectsData(Map<Long,SubjectDTO> subjectsMap) throws DAOException{
			return super.getSubjectsData(subjectsMap);
		}
		protected String getCreateTempSubjectsTablesQuery(String subjectsSubQuery, String predicateHashesCommaSeparated) {
			return super.getCreateTempSubjectsTablesQuery(subjectsSubQuery, predicateHashesCommaSeparated);
		}
		protected String getDropTempTableQuery(){
			return super.getDropTempTableQuery();
		}
		protected String getSubjectsDataFromTmpTablesQuery(){
			return super.getSubjectsDataFromTmpTablesQuery();
		}
		protected Connection getConnection() throws SQLException{
			return super.getConnection();
		}
	}
	
}
