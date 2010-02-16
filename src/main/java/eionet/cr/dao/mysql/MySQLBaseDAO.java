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
package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.util.Pair;
import eionet.cr.util.Util;
import eionet.cr.util.sql.ConnectionUtil;
import eionet.cr.util.sql.MySQLUtil;
import eionet.cr.util.sql.ResultSetBaseReader;
import eionet.cr.util.sql.ResultSetListReader;
import eionet.cr.util.sql.SQLUtil;

/**
 * 
 * @author heinljab, altnyris
 *
 */
public abstract class MySQLBaseDAO {
	
	protected Logger logger = Logger.getLogger(MySQLBaseDAO.class);
	
	/**
	 * 
	 * @return
	 */
	protected Connection getConnection() throws SQLException{
		return ConnectionUtil.getConnection();
	}
	
	/**
	 * 
	 * @param conn
	 */
	protected void closeConnection(Connection conn){
		ConnectionUtil.closeConnection(conn);
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 * @throws DAOException
	 */
	protected Integer getLastInsertID(Connection conn) throws SQLException{
		return MySQLUtil.getLastInsertID(conn);
	}

	/**
	 * helper method to execute sql queries.
	 * Handles connection init, close. Wraps Exceptions into {@link DAOException}
	 * @param <T> - type of the returned object
	 * @param sql - sql string
	 * @param params - parameters to insert into sql string
	 * @param reader - reader, to convert resultset
	 * @return result of the sql query
	 * @throws DAOException
	 */
	protected <T> List<T> executeQuery(String sql, List<?> params, ResultSetListReader<T> reader) throws DAOException {
		Connection conn = null;
		try {
			conn = getConnection();
			SQLUtil.executeQuery(sql, params, reader, conn);
			List<T>  list = reader.getResultList();
			return list;
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/**
	 * 
	 * @param sql
	 * @param reader
	 * @throws DAOException
	 */
	protected void executeQuery(String sql, ResultSetBaseReader reader) throws DAOException {
		Connection conn = null;
		try {
			conn = getConnection();
			SQLUtil.executeQuery(sql, reader, conn);
		}
		catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		}
		finally{
			ConnectionUtil.closeConnection(conn);
		}
	}

	/**
	 * executes insert or update with given sql and parameters.
	 * 
	 * @param sql - sql string to execute
	 * @param params - sql params
	 * @throws DAOException
	 */
	protected void execute(String sql, List<?> params) throws DAOException {
		Connection conn = null;
		PreparedStatement statement = null;
		try {
			conn = getConnection();
			if (params != null && !params.isEmpty()) {
				statement  = SQLUtil.prepareStatement(sql, params, conn);
				statement.execute();
			} else {
				SQLUtil.executeUpdate(sql, conn);
			}
		} catch (Exception e){
			throw new DAOException(e.getMessage(), e);
		} finally{
			ConnectionUtil.closeConnection(conn);
			ConnectionUtil.clostStatement(statement);
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param sql
	 * @param params
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	protected <T> Pair<Integer, List<T>> executeQueryWithRowCount(String sql, List<?> params, ResultSetListReader<T> reader) throws DAOException {
		
		Connection conn = null;
		try {
			conn = getConnection();
			SQLUtil.executeQuery(sql, params, reader, conn);
			List<T> list = reader.getResultList();
			int rowCount = MySQLUtil.getTotalRowCount(conn);
			return new Pair<Integer,List<T>> (rowCount,list);
		}
		catch (Exception fatal) {
			throw new DAOException(fatal.getMessage(), fatal);
		}
		finally {
			ConnectionUtil.closeConnection(conn);
		}
	}
	
	/**
	 * 
	 * @param <T>
	 * @param sql
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	protected <T> Pair<Integer, List<T>> executeQueryWithRowCount(String sql, ResultSetListReader<T> reader) throws DAOException {
		
		Connection conn = null;
		try {
			conn = getConnection();
			SQLUtil.executeQuery(sql, reader, conn);
			List<T> list = reader.getResultList();
			int rowCount = MySQLUtil.getTotalRowCount(conn);
			return new Pair<Integer,List<T>> (rowCount,list);
		} catch (Exception fatal) {
			throw new DAOException(fatal.getMessage(), fatal);
		} finally {
			ConnectionUtil.closeConnection(conn);
		}
	}
	
	/**
	 * 
	 * @param <T>
	 * @param sql
	 * @param params
	 * @param reader
	 * @return
	 * @throws DAOException
	 */
	protected <T> T executeQueryUniqueResult(String sql, List<?> params, ResultSetListReader<T> reader) throws DAOException {
		List<T> result = executeQuery(sql, params, reader);
		return result == null || result.isEmpty()
				? null
				: result.get(0);
	}

	/**
	 * 
	 * @param subjectHashes
	 * @return
	 */
	protected String getSubjectsDataQuery(Collection<Long> subjectHashes) {
		
		if (subjectHashes==null || subjectHashes.isEmpty())
			throw new IllegalArgumentException("The subject hashes collection must be null or empty!");

		StringBuffer buf = new StringBuffer().
		append("select distinct ").
		append("SUBJECT as SUBJECT_HASH, SUBJ_RESOURCE.URI as SUBJECT_URI, SUBJ_RESOURCE.LASTMODIFIED_TIME as SUBJECT_MODIFIED, ").
		append("PREDICATE as PREDICATE_HASH, PRED_RESOURCE.URI as PREDICATE_URI, ").
		append("OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_SOURCE_OBJECT, OBJ_DERIV_SOURCE, SOURCE, ").
		append("SRC_RESOURCE.URI as SOURCE_URI, DSRC_RESOURCE.URI as DERIV_SOURCE_URI ").
		append("from SPO ").
		append("left join RESOURCE as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
		append("left join RESOURCE as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
		append("left join RESOURCE as SRC_RESOURCE on (SOURCE=SRC_RESOURCE.URI_HASH) ").
		append("left join RESOURCE as DSRC_RESOURCE on (OBJ_DERIV_SOURCE=DSRC_RESOURCE.URI_HASH) ").
		append("where ").
		append("SUBJECT in (").append(Util.toCSV(subjectHashes)).append(") ").  
		append("order by ").
		append("SUBJECT, PREDICATE, OBJECT");
		return buf.toString();
	}
}
