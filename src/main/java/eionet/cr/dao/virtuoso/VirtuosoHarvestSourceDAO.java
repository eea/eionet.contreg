package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

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

            conn = SesameUtil.getRepositoryConnection();
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
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getSourcesInInferenceRules()
     */
    @Override
    public String getSourcesInInferenceRules() throws DAOException {

        Connection conn = null;
        String ret = "";
        try{
            conn = SesameUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT RS_URI FROM sys_rdf_schema where RS_NAME = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            ResultSet rs = stmt.executeQuery();
            StringBuffer sb = new StringBuffer();
            while (rs.next()){
                String graphUri = rs.getString("RS_URI");
                if (!StringUtils.isBlank(graphUri)) {
                    sb.append("'").append(graphUri).append("'");
                    sb.append(",");
                }
            }
            
            ret = sb.toString();
            //remove last comma
            if (!StringUtils.isBlank(ret)) {
                ret = ret.substring(0, ret.lastIndexOf(","));
            }
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
        
        return ret;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#isSourceInInferenceRule()
     */
    @Override
    public boolean isSourceInInferenceRule(String url) throws DAOException {

        Connection conn = null;
        boolean ret = false;
        try{
            conn = SesameUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT RS_NAME FROM sys_rdf_schema where RS_NAME = ? AND RS_URI = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                ret = true;
            }
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
        return ret;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceIntoInferenceRule()
     */
    @Override
    public boolean addSourceIntoInferenceRule(String url) throws DAOException {
        
        Connection conn = null;
        boolean ret = false;
        
        try {
            conn = SesameUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement("rdfs_rule_set (?, ?)");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ret = stmt.execute();
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
        return ret;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#removeSourceFromInferenceRule()
     */
    @Override
    public boolean removeSourceFromInferenceRule(String url) throws DAOException {
        
        Connection conn = null;
        boolean ret = false;
        
        try {
            conn = SesameUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement("rdfs_rule_set (?, ?, 1)");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ret = stmt.execute();
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
        return ret;
    }

}
