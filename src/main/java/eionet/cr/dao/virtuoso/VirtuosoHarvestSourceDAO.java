package eionet.cr.dao.virtuoso;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
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
import eionet.cr.util.Bindings;
import eionet.cr.util.URLUtil;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 * Harvester methods in Virtuoso.
 *
 * @author kaido
 */

// TODO remove extending when all the methods have been copied to VirtuosoDAO
public class VirtuosoHarvestSourceDAO extends PostgreSQLHarvestSourceDAO {
    /**
     * SPARQL for deleting graph data.
     */
    private static final String CLEAR_GRAPH_SPARQL = "CLEAR GRAPH ?graph";
    @Override
    public void deleteSourceByUrl(String url) throws DAOException {

        boolean isSuccess = false;
        RepositoryConnection conn = null;

        Connection sqlConn = null;

        try {
            Bindings bindings = new Bindings();
            bindings.setURI("graph", URLUtil.replaceURLSpaces(url));

            // TODO remove PostgreSQL calls when PostgreSQL time is over
            // 2 updates brought here together to handle rollbacks correctly in
            // both DBs
            sqlConn = getSQLConnection();
            sqlConn.setAutoCommit(false);

            super.deleteSourceByUrl(url, sqlConn);

            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            executeUpdateSPARQL(CLEAR_GRAPH_SPARQL, conn, bindings);

            // remove source metadata from http://cr.eionet.europa.eu/harvetser graph
            ValueFactory valueFactory = conn.getValueFactory();
            URI subject = valueFactory.createURI(URLUtil.replaceURLSpaces(url));
            URI context = valueFactory.createURI(GeneralConfig.HARVESTER_URI);
            conn.remove(subject, null, null, (Resource)context);

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

            SQLUtil.close(sqlConn);
            SesameUtil.close(conn);

        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#deleteSourceTriples(String)
     */
    @Override
    public void deleteSourceTriples(String url) throws DAOException {

        boolean isSuccess = false;
        RepositoryConnection conn = null;

        try {

            Bindings bindings = new Bindings();
            bindings.setURI("graph", URLUtil.replaceURLSpaces(url));

            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            executeUpdateSPARQL(CLEAR_GRAPH_SPARQL, conn, bindings);

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
            SesameUtil.close(conn);
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
        ResultSet rs = null;
        String ret = "";
        PreparedStatement stmt = null;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("SELECT RS_URI FROM sys_rdf_schema where RS_NAME = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            rs = stmt.executeQuery();

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
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
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
        PreparedStatement stmt = null;
        ResultSet rs = null;

        boolean ret = false;
        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("SELECT RS_NAME FROM sys_rdf_schema where RS_NAME = ? AND RS_URI = ?");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            rs = stmt.executeQuery();
            while (rs.next()) {
                ret = true;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
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
        PreparedStatement stmt = null;
        boolean ret = false;

        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("rdfs_rule_set (?, ?)");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ret = stmt.execute();

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(stmt);
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
        PreparedStatement stmt = null;
        boolean ret = false;

        try {
            conn = SesameUtil.getSQLConnection();
            stmt = conn.prepareStatement("rdfs_rule_set (?, ?, 1)");
            stmt.setString(1, GeneralConfig.getRequiredProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME));
            stmt.setString(2, url);
            ret = stmt.execute();
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
            SQLUtil.close(stmt);
        }
        return ret;
    }

    /**
     * @see eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO#loadIntoRepository(java.io.File, RDFFormat, java.lang.String, boolean)
     */
    @Override
    public int loadIntoRepository(File file, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent) throws IOException, OpenRDFException {

        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            return loadIntoRepository(inputStream, rdfFormat, graphUrl, clearPreviousGraphContent);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * @see eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO#loadIntoRepository(java.io.InputStream, RDFFormat, java.lang.String, boolean)
     */
    @Override
    public int loadIntoRepository(InputStream inputStream, RDFFormat rdfFormat, String graphUrl, boolean clearPreviousGraphContent) throws IOException, OpenRDFException {

        int storedTriplesCount = 0;
        boolean isSuccess = false;
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            // convert graph URL into OpenRDF Resource
            org.openrdf.model.Resource graphResource = conn.getValueFactory().createURI(graphUrl);

            // start transaction
            conn.setAutoCommit(false);

            // if required, clear previous triples of this graph
            if (clearPreviousGraphContent){
                conn.clear(graphResource);
            }

            // add the stream content into repository under the given graph
            conn.add(inputStream, graphUrl, rdfFormat==null ? RDFFormat.RDFXML : rdfFormat, graphResource);

            long tripleCount = conn.size(graphResource);

            // commit transaction
            conn.commit();

            // set total stored triples count
            storedTriplesCount = Long.valueOf(tripleCount).intValue();

            // no transaction rollback needed, when reached this point
            isSuccess = true;
        } finally {
            if (!isSuccess) {
                SesameUtil.rollback(conn);
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

            try {
                conn = SesameUtil.getRepositoryConnection();
                conn.setAutoCommit(false);

                // The contextURI is always the harvester URI
                URI harvesterContext = conn.getValueFactory().createURI(GeneralConfig.HARVESTER_URI);

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
                        Literal literalObject = conn.getValueFactory().createLiteral(object.toString(), object.getDatatype());
                        conn.add(subject, predicate, literalObject, contextURI);
                    } else {
                        URI resourceObject = conn.getValueFactory().createURI(object.toString());
                        conn.add(subject, predicate, resourceObject, contextURI);
                    }
                }
            }
        }
    }

    /**
     * SPARQL for getting new sources based on the given source.
     */
    private static final String NEW_SOURCES_SPARQL = "DEFINE input:inference 'CRInferenceRule' PREFIX cr: "
        + "<http://cr.eionet.europa.eu/ontologies/contreg.rdf#> SELECT ?s FROM ?sourceUrl FROM ?deploymentHost WHERE "
        + "{ ?s a cr:File . OPTIONAL { ?s cr:lastRefreshed ?refreshed } FILTER( !BOUND(?refreshed)) }";

    /**
     * @see eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO#getNewSources(java.lang.String)
     */
    @Override
    public List<String> getNewSources(String sourceUrl) throws DAOException{

        if (!StringUtils.isBlank(sourceUrl)) {
            String deploymentHost = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);

            Bindings bindings = new Bindings();
            bindings.setURI("sourceUrl", sourceUrl);
            bindings.setURI("deploymentHost", deploymentHost);

            SubjectReader matchReader = new SubjectReader();
            matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
            return executeSPARQL(NEW_SOURCES_SPARQL, bindings, matchReader);

        }

        return null;
    }
    /**
     * SPARQL for getting source metadata.
     */
    private static final String SOURCE_METADATA_SPARQL = "select ?o where {graph ?g { ?subject ?predicate ?o . "
        + "filter (?g = ?deploymentHost)}}";
    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#getSourceMetadata(String, String)
     */
    @Override
    public String getHarvestSourceMetadata(String harvestSourceUri, String predicateUri) throws DAOException, RepositoryException, IOException {

        String ret = null;
        RepositoryConnection conn = null;
        try {

            Bindings bindings = new Bindings();
            bindings.setURI("deploymentHost", GeneralConfig.HARVESTER_URI);
            bindings.setURI("subject", harvestSourceUri);
            bindings.setURI("predicate", predicateUri);

            Object resultObject = executeUniqueResultSPARQL(SOURCE_METADATA_SPARQL, bindings, new SingleObjectReader<Long>());
            ret = (String) resultObject;
        } finally {
            SesameUtil.close(conn);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestSourceDAO#insertUpdateSourceMetadata(String, String, ObjectDTO)
     */
    @Override
    public void insertUpdateSourceMetadata(String subject, String predicate, ObjectDTO object) throws DAOException,
    RepositoryException, IOException {
        RepositoryConnection conn = null;
        try {

            conn = SesameUtil.getRepositoryConnection();

            URI harvesterContext = conn.getValueFactory().createURI(GeneralConfig.HARVESTER_URI);
            URI sub = conn.getValueFactory().createURI(subject);
            URI pred = conn.getValueFactory().createURI(predicate);

            conn.remove(sub, pred, null, harvesterContext);
            if (object.isLiteral()) {
                Literal literalObject = conn.getValueFactory().createLiteral(object.toString(), object.getDatatype());
                conn.add(sub, pred, literalObject, harvesterContext);
            } else {
                URI resourceObject = conn.getValueFactory().createURI(object.toString());
                conn.add(sub, pred, resourceObject, harvesterContext);
            }

        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     *
     */
    @Override
    public void deleteSubjectTriplesInSource(String subjectUri, String sourceUri) throws DAOException {

        if (!StringUtils.isBlank(subjectUri)) {
            RepositoryConnection conn = null;
            try {

                conn = SesameUtil.getRepositoryConnection();
                ValueFactory valueFactory = conn.getValueFactory();
                conn.remove(valueFactory.createURI(subjectUri), null, null, valueFactory.createURI(sourceUri));

            } catch (RepositoryException e) {
                throw new DAOException(e.getMessage(), e);
            } finally {
                SesameUtil.close(conn);
            }
        }
    }

}
