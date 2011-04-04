package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.SQLException;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO;
import eionet.cr.util.sesame.SesameUtil;

/**
 * Harvester methods in Virtuoso
 * 
 * @author kaido
 */

// TODO remove extending when all the methods have been copied to VirtuosoDAO
public class VirtuosoHarvestSourceDAO extends PostgreSQLHarvestSourceDAO {

    @Override
    public void deleteSourceByUrl(String url) throws DAOException {
        String sparql = "CLEAR GRAPH <" + url + ">";
        boolean isSuccess = false;
        RepositoryConnection conn = null;

        Connection sqlConn = null;

        try {

            // TODO remove PostgreSQL calls when PostgreSQL time is over
            // 2 updates brought here together to handle rollbacks correctly in
            // both DBs
            sqlConn = getSQLConnection();
            sqlConn.setAutoCommit(false);

            super.deleteSourceByUrl(url, sqlConn);

            conn = SesameUtil.getConnection();
            conn.setAutoCommit(false);
            executeUpdateSPARQL(sparql, conn);

            sqlConn.commit();
            conn.commit();
            isSuccess = true;
        } catch (Exception e) {
            throw new DAOException("Error deleting source " + url, e);
        } finally {
            if (!isSuccess && conn != null) {
                try {
                    conn.rollback();
                } catch (RepositoryException re) {
                }
            }
            if (!isSuccess && sqlConn != null) {
                try {
                    sqlConn.rollback();
                } catch (SQLException e) {
                }
            }

        }

    }

}
