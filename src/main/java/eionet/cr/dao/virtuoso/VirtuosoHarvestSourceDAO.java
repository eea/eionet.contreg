package eionet.cr.dao.virtuoso;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO;
import eionet.cr.dao.readers.SubjectReader;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

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

            // remove source metadata from http://cr.eionet.europa.eu/harvetser
            // graph
            ValueFactory fac = conn.getValueFactory();
            URI subject = fac.createURI(url);
            String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.DEPLOYMENT_HOST);
            URI context = fac.createURI(deploymentHost + "/harvester");
            conn.remove(subject, null, null, (Resource) context);

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
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourceTriples(String)
     */
    @Override
    public void deleteSourceTriples(String url) throws DAOException {
        String sparql = "CLEAR GRAPH <" + url + ">";
        boolean isSuccess = false;
        RepositoryConnection conn = null;

        try {

            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            executeUpdateSPARQL(sparql, conn);

            conn.commit();
            isSuccess = true;
        } catch (Exception e) {
            throw new DAOException("Error deleting source triples " + url, e);
        } finally {
            if (!isSuccess && conn != null) {
                try {
                    conn.rollback();
                } catch (RepositoryException re) {
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
        try {
            conn = SesameUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT RS_URI FROM sys_rdf_schema where RS_NAME = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            ResultSet rs = stmt.executeQuery();
            StringBuffer sb = new StringBuffer();
            while (rs.next()) {
                String graphUri = rs.getString("RS_URI");
                if (!StringUtils.isBlank(graphUri)) {
                    sb.append("'").append(graphUri).append("'");
                    sb.append(",");
                }
            }

            ret = sb.toString();
            // remove last comma
            if (!StringUtils.isBlank(ret)) {
                ret = ret.substring(0, ret.lastIndexOf(","));
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
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
        try {
            conn = SesameUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT RS_NAME FROM sys_rdf_schema where RS_NAME = ? AND RS_URI = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ret = true;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
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
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
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
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceToRepository(File, String)
     */
    @Override
    public int addSourceToRepository(File file, String sourceUrlString) throws DAOException, RepositoryException,
            RDFParseException, IOException {

        int storedTriplesCount = 0;
        boolean isSuccess = false;
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            // see http://www.openrdf.org/doc/sesame2/users/ch08.html#d0e1218
            // for what's a context
            org.openrdf.model.URI context = conn.getValueFactory().createURI(sourceUrlString);

            // start transaction
            conn.setAutoCommit(false);

            // clear previous triples of this context
            conn.clear(context);

            // add the file's contents into repository and under this context
            conn.add(file, sourceUrlString, RDFFormat.RDFXML, context);

            long tripleCount = conn.size(context);

            // commit transaction
            conn.commit();

            // set total stored triples count
            storedTriplesCount = Long.valueOf(tripleCount).intValue();

            // no transaction rollback needed, when reached this point
            isSuccess = true;
        } finally {
            if (!isSuccess && conn != null) {
                try {
                    conn.rollback();
                } catch (RepositoryException e) {
                }
            }
            SesameUtil.close(conn);
        }
        return storedTriplesCount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#addSourceMetadata(SubjectDTO)
     */
    @Override
    public void addSourceMetadata(SubjectDTO sourceMetadata) throws DAOException, RDFParseException, RepositoryException,
            IOException {

        if (sourceMetadata.getPredicateCount() > 0) {

            boolean isSuccess = false;
            RepositoryConnection conn = null;

            logger.debug("Storing auto-generated triples for the source");
            try {
                conn = SesameUtil.getRepositoryConnection();
                conn.setAutoCommit(false);

                // The contextURI is always the harvester URI
                // (which is generated from the deployment hostname).
                String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.DEPLOYMENT_HOST);
                URI harvesterContext = conn.getValueFactory().createURI(deploymentHost + "/harvester");
                
                if (sourceMetadata != null) {
                    URI subject = conn.getValueFactory().createURI(sourceMetadata.getUri());
                    
                    // Remove old predicates
                    conn.remove(subject, null, null, harvesterContext);
                    
                    if (sourceMetadata.getPredicateCount() > 0) {
                        insertMetadata(sourceMetadata, conn, harvesterContext, subject);
                    }
                }
                // commit transaction
                conn.commit();
                
                // no transaction rollback needed, when reached this point
                isSuccess = true;
            } finally {
                if (!isSuccess && conn != null) {
                    try {
                        conn.rollback();
                    } catch (RepositoryException e) {
                    }
                }
                SesameUtil.close(conn);
            }
        }
    }

    /**
     * @param subjectDTO
     * @param conn
     * @param contextURI
     * @param subject
     * @throws RepositoryException
     */
    private void insertMetadata(SubjectDTO subjectDTO, RepositoryConnection conn, URI contextURI, URI subject)
            throws RepositoryException {

        for (String predicateUri : subjectDTO.getPredicates().keySet()) {

            Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
            if (objects != null && !objects.isEmpty()) {

                URI predicate = conn.getValueFactory().createURI(predicateUri);
                for (ObjectDTO object : objects) {
                    if (object.isLiteral()) {
                        Literal literalObject = conn.getValueFactory().createLiteral(object.toString());
                        conn.add(subject, predicate, literalObject, contextURI);
                    } else {
                        URI resourceObject = conn.getValueFactory().createURI(object.toString());
                        conn.add(subject, predicate, resourceObject, contextURI);
                    }
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getNewSources(String)
     */
    @Override
    public List<String> getNewSources(String sourceUrl) throws DAOException, RDFParseException, RepositoryException, IOException {

        List<String> ret = null;

        if (!StringUtils.isBlank(sourceUrl)) {

            RepositoryConnection conn = null;
            try {
                conn = SesameUtil.getRepositoryConnection();

                String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.DEPLOYMENT_HOST);

                String crInfRuleName = GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME);

                StringBuffer sqlBuf = new StringBuffer().append("DEFINE input:inference '").append(crInfRuleName).append("' ")
                        .append("PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#> ").append("SELECT ?s ")
                        .append("FROM <").append(sourceUrl).append("> ").append("FROM <").append(deploymentHost)
                        .append("/harvester> ").append("WHERE { ").append("?s a cr:File . ")
                        .append("OPTIONAL { ?s cr:lastRefreshed ?refreshed } ").append("FILTER( !BOUND(?refreshed)) }");

                SubjectReader matchReader = new SubjectReader();
                matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
                ret = executeSPARQL(sqlBuf.toString(), matchReader);

            } finally {
                SesameUtil.close(conn);
            }
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#getSourceMetadata(String, String)
     */
    @Override
    public String getSourceMetadata(String subject, String predicate) throws DAOException, RepositoryException {

        String ret = null;
        RepositoryConnection conn = null;
        try {
            String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.DEPLOYMENT_HOST);

            StringBuffer sqlBuf = new StringBuffer().append("select ?o where {graph ?g { ").append("<").append(subject)
                    .append("> <").append(predicate).append("> ?o .").append("filter (?g = <").append(deploymentHost)
                    .append("/harvester>) ").append("}}");
            Object resultObject = executeUniqueResultSPARQL(sqlBuf.toString(), new SingleObjectReader<Long>());
            ret = (String) resultObject;
        } finally {
            SesameUtil.close(conn);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(String,
     * String, String)
     */
    @Override
    public void insertUpdateSourceMetadata(String subject, String predicate, String value) throws DAOException, RepositoryException {
        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();

            String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.DEPLOYMENT_HOST);
            URI harvesterContext = conn.getValueFactory().createURI(deploymentHost + "/harvester");
            URI sub = conn.getValueFactory().createURI(subject);
            URI pred = conn.getValueFactory().createURI(predicate);
            Literal literalObject = conn.getValueFactory().createLiteral(value);

            conn.remove(sub, pred, null, harvesterContext);
            conn.add(sub, pred, literalObject, harvesterContext);

        } finally {
            SesameUtil.close(conn);
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.HarvestSourceDAO#removeAllPredicatesFromHarvesterContext(String, String)
     */
    @Override
    public void removeAllPredicatesFromHarvesterContext(String subject) throws DAOException, RepositoryException {
        if (!StringUtils.isBlank(subject)) {
            RepositoryConnection conn = null;
            try {
                conn = SesameUtil.getRepositoryConnection();
    
                String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.DEPLOYMENT_HOST);
                URI harvesterContext = conn.getValueFactory().createURI(deploymentHost + "/harvester");
                URI sub = conn.getValueFactory().createURI(subject);
                
                conn.remove(sub, null, null, harvesterContext);
    
            } finally {
                SesameUtil.close(conn);
            }
        }
    }

}
