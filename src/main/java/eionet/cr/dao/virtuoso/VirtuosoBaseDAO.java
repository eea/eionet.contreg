package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SQLBaseDAO;
import eionet.cr.dao.helpers.SearchHelper;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.sesame.SPARQLResultSetReader;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;

/**
 *
 * @author jaanus
 *
 */
public abstract class VirtuosoBaseDAO extends SQLBaseDAO {

    /** */
    // public static final String BNODE_URI_PREFIX = "nodeID://";
    public static final String BNODE_URI_PREFIX = "_:";

    /** */
    protected Logger logger = Logger.getLogger(VirtuosoBaseDAO.class);

    /**
     *
     * @param <T>
     * @param sparql
     * @param reader
     * @return
     * @throws DAOException
     */
    protected <T> List<T> executeSPARQL(String sparql,
            SPARQLResultSetReader<T> reader) throws DAOException {

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            SesameUtil.executeQuery(sparql, reader, conn);
            return reader.getResultList();
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * Executes SPARQL that updates data.
     * @param sparql SPARQL
     * @param conn Virtuoso repository connection
     * @throws DAOException if update fails
     */
    protected void executeUpdateSPARQL(final String sparql, final RepositoryConnection conn) throws DAOException {
        try {
            SesameUtil.executeUpdateQuery(sparql, conn);
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        }
    }

    /**
     * Executes SPARQL that is expected to have only one result and returns the unique value.
     * @param <T>
     * @param sql
     * @param params
     * @param reader
     * @return
     * @throws DAOException if query fails
     */
    protected <T> T executeUniqueResultSPARQL(String sql,
            SPARQLResultSetReader<T> reader) throws DAOException {

        List<T> result = executeSPARQL(sql, reader);
        return (result == null || result.isEmpty()) ? null : result.get(0);
    }

    /**
     * @param subjectUris subject URIs
     * @param predicateUris array of needed predicate URIs
     * @param reader subject reader
     * @param graphUris set of graphs
     * @return List<SubjectDTO> list of Subject data objects
     * @throws DAOException
     *             Default call of getSubjectsData() - SubjectDTO are created if
     *             not existing
     */
    protected List<SubjectDTO> getSubjectsData(final Collection<String> subjectUris,
            final String[] predicateUris, final SubjectDataReader reader,
            final Collection<String> graphUris) throws DAOException {
        return getSubjectsData(subjectUris, predicateUris, reader, graphUris,
                true, false);
    }

    /**
     * Returns list of Data objects of given subjects for given predicates.
     * @param subjectUris list of subject URIs
     * @param predicateUris array of predicates which data is requested
     * @param reader bindingset reader
     * @param graphUris Graphs which data is requested
     * @param createMissingDTOs  indicates if to create a SubjectDTO object if it does not exist
     * @param useInferencing true if to use inferencing - (temporary until Virtuoso is fixed)
     * @return List<SubjectDTO> list of Subject data objects
     * @throws DAOException if query fails
     */
    protected List<SubjectDTO> getSubjectsData(final Collection<String> subjectUris,
            final String[] predicateUris, final SubjectDataReader reader,
            final Collection<String> graphUris, final boolean createMissingDTOs, final boolean useInferencing)
            throws DAOException {

        if (subjectUris == null || subjectUris.isEmpty()) {
            throw new IllegalArgumentException(
                    "Subjects collection must not be null or empty!");
        }

        String query = getSubjectsDataQuery(subjectUris, predicateUris,
                graphUris, useInferencing);
        executeSPARQL(query, reader);

        Map<Long, SubjectDTO> subjectsMap = reader.getSubjectsMap();
        if (subjectsMap != null && !subjectsMap.isEmpty()) {

            for (String subjectUri : subjectUris) {

                Long subjectHash = Long.valueOf(Hashes.spoHash(subjectUri));
                if (subjectsMap.get(subjectHash) == null && createMissingDTOs) {

                    // TODO: don't hardcode isAnonymous to false
                    SubjectDTO subjectDTO = new SubjectDTO(subjectUri, false);
                    subjectsMap.put(subjectHash, subjectDTO);
                }
            }
        }

        return reader.getResultList();
    }

    /**
     * @param subjectUris - List of subjects the data is be queried
     * @param predicateUris - String [] list of predicates
     * @param graphUris - list of graphs where the data is queried (optional)
     * @param useInferencing - if to use inferencing in the query
     * @return String SPARQL query
     */
    private String getSubjectsDataQuery(final Collection<String> subjectUris,
            final String[] predicateUris, final Collection<String> graphUris, final boolean useInferencing) {

        if (subjectUris == null || subjectUris.isEmpty()) {
            throw new IllegalArgumentException(
                    "Subjects collection must not be null or empty!");
        }

        StringBuilder strBuilder = new StringBuilder();
                if (useInferencing) {
                    strBuilder.append("define input:inference '")
                    .append(GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME)).append("' ");
                }
                strBuilder.append("select * where {graph ?g {?s ?p ?o. ").append(
                "filter (?s IN (");

        int i = 0;
        for (String subjectUri : subjectUris) {
            if (i > 0) {
                strBuilder.append(", ");
            }
            strBuilder.append("<").append(subjectUri).append(">");
            i++;
        }
        strBuilder.append(")) ");

        // if only certain predicates needed, add relevant filter
        if (predicateUris != null && predicateUris.length > 0) {

            i = 0;
            strBuilder.append("filter (");
            for (String predicateUri : predicateUris) {
                if (i > 0) {
                    strBuilder.append(" || ");
                }
                strBuilder.append("?p = <").append(predicateUri).append(">");
                i++;
            }

            strBuilder.append(") ");
        }

        // if only certain graphs needed, add relevant filter
        int z = 0;
        if (graphUris != null && graphUris.size() > 0) {
            strBuilder.append("filter (");
            for (String graphUri : graphUris) {
                if (z > 0) {
                    strBuilder.append(" || ");
                }
                strBuilder.append("?g = <").append(graphUri).append(">");
                z++;
            }
            strBuilder.append(") ");
        }

        strBuilder.append("OPTIONAL { ?g <").append(Predicates.CR_LAST_MODIFIED).append("> ?t } ");

        strBuilder.append("}} ORDER BY ?s ?p ?o");
        return strBuilder.toString();
    }
    /**
    * Count the total number of rows retrieved by the query constructed in SearchHelper.
    *
    * @param helper SearchHelper object.
    * @return number of rows
    * @throws DAOException
    */
   protected int getExactRowCount(SearchHelper helper) throws DAOException {

       String query = helper.getCountQuery(new ArrayList<Object>());
       Object resultObject = executeUniqueResultSPARQL(query, new SingleObjectReader<Long>());
       return Integer.valueOf(resultObject.toString());
   }
}
