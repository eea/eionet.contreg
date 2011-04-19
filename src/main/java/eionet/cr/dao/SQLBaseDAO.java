package eionet.cr.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import eionet.cr.util.sql.DbConnectionProvider;
import eionet.cr.util.sql.SQLResultSetReader;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author jaanus
 *
 */
public abstract class SQLBaseDAO {

    /**
     *
     * @param conn
     */
    public void setSQLConnection(Connection conn) {
    }

    /**
     *
     * @return
     */
    protected Connection getSQLConnection() throws SQLException {

        return DbConnectionProvider.getConnection();
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
    protected <T> List<T> executeSQL(String sql, List<?> params, SQLResultSetReader<T> reader) throws DAOException {
        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeQuery(sql, params, reader, conn);
            List<T>  list = reader.getResultList();
            return list;
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     *
     * @param sql
     * @param reader
     * @throws DAOException
     */
    protected void executeSQL(String sql, SQLResultSetReader reader) throws DAOException {
        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeQuery(sql, reader, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * executes insert or update with given sql and parameters.
     *
     * @param sql - sql string to execute
     * @param params - sql params
     * @throws DAOException
     */
    protected void executeSQL(String sql, List<?> params) throws DAOException {
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getSQLConnection();
            if (params != null && !params.isEmpty()) {
                statement  = SQLUtil.prepareStatement(sql, params, conn);
                statement.execute();
            } else {
                SQLUtil.executeUpdate(sql, conn);
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(statement);
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
    protected <T> T executeUniqueResultSQL(String sql, List<?> params, SQLResultSetReader<T> reader) throws DAOException {
        List<T> result = executeSQL(sql, params, reader);
        return result == null || result.isEmpty()
                ? null
                : result.get(0);
    }

    /**
     *
     * @param sql
     * @return
     * @throws DAOException
     */
    protected Object executeSingleReturnValueSQL(String sql) throws DAOException {

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeSingleReturnValueQuery(sql, conn);
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

}
