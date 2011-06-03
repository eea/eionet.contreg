package eionet.cr.dao.virtuoso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SQLBaseDAO;
import eionet.cr.dao.helpers.SearchHelper;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Hashes;
import eionet.cr.util.sesame.SPARQLQueryUtil;
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
    
    private Bindings bindings;

    /**
     * 
     * @param <T>
     * @param sparql
     * @param bindings
     * @param reader
     * @return
     * @throws DAOException
     */
    protected <T> List<T> executeSPARQL(String sparql, Bindings bindings,
            SPARQLResultSetReader<T> reader) throws DAOException {
        
        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            SesameUtil.executeQuery(sparql, bindings, reader, conn);
            return reader.getResultList();
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

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
        
        return executeSPARQL(sparql, null, reader);
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
     * Executes SPARQL with no bindings that is expected to have only one result and returns the unique value.
     * @param <T>
     * @param sql
     * @param params
     * @param reader
     * @return
     * @throws DAOException if query fails
     */
    protected <T> T executeUniqueResultSPARQL(String sql,
            SPARQLResultSetReader<T> reader) throws DAOException {
        
        return executeUniqueResultSPARQL(sql, null, reader);
    }
    /**
     * Executes SPARQL that is expected to have only one result and returns the unique value.
     * @param <T>
     * @param sql
     * @param bindings Binding values for the prepared SPARQL
     * @param params
     * @param reader
     * @return
     * @throws DAOException if query fails
     */
    protected <T> T executeUniqueResultSPARQL(String sql, Bindings bindings,
            SPARQLResultSetReader<T> reader) throws DAOException {

        
        List<T> result = executeSPARQL(sql, bindings, reader);
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
        executeSPARQL(query, bindings, reader);

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
    private String getSubjectsDataQuery(final Collection<String> subjectUris, final String[] predicateUris,
            final Collection<String> graphUris, final boolean useInferencing) {
        String sparql = "";
        // initiate this class bindings to prevent initiating it when it is not
        // actually used
        this.bindings = new Bindings();
        if (subjectUris == null || subjectUris.isEmpty()) {
            throw new IllegalArgumentException("Subjects collection must not be null or empty!");
        }

        if (useInferencing) {
            sparql = SPARQLQueryUtil.getCrInferenceDefinition().toString();
        }
        // select * where {graph ?g {?s ?p ?o. filter (?s IN
        // (<http://rod.eionet.europa.eu/obligations/130>,
        // <http://rod.eionet.europa.eu/obligations/143>)
        sparql += "select * where {graph ?g {?s ?p ?o. filter (?s IN (" + urisToCSV(subjectUris, bindings) + ")) ";

        // if only certain predicates needed, add relevant filter
        if (predicateUris != null && predicateUris.length > 0) {
            sparql += "filter ("
                    + SPARQLQueryUtil.getSparqlOrConditions("p", "predicateValue", Arrays.asList(predicateUris),
                            bindings) + ") ";

        }

        // if only certain graphs needed, add relevant filter
        if (graphUris != null && graphUris.size() > 0) {
            sparql += "filter (" + SPARQLQueryUtil.getSparqlOrConditions("g", "graphValue", graphUris, bindings) + ") ";
        }
        sparql += "OPTIONAL { ?g ?crLastModified ?t } ";
        bindings.setURI("crLastModified", Predicates.CR_LAST_MODIFIED);

        sparql += "}} ORDER BY ?s ?p";
        return sparql;
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
       Bindings bindings = helper.getQueryBindings();
       Object resultObject = executeUniqueResultSPARQL(query, bindings, new SingleObjectReader<Long>());
       return Integer.valueOf(resultObject.toString());
   }
   
   /**
    * Returns comma-separated subject value aliases list that matches to the URIs count.
    * example: "subjectValue1,subjectValue2,subjectValue3" if there are 3 URIs
    * Puts the values to the given Bindings with same names
    * @param uriList
    * @param bindings
    * @return String to be used in SPARQL, for example in IN()
    */
   //TODO move this method to SPARQLQueryutils?, add optional aliasValue
   protected String urisToCSV(Collection<String> uriList, Bindings bindings) {
        StringBuilder strBuilder = new StringBuilder();
        if (uriList != null) {
            int i = 1;
            for (String uri : uriList) {
                String alias = "subjectValue" + i;
                if (strBuilder.length() > 0) {
                    strBuilder.append(",");
                }
                strBuilder.append("?" + alias);
                bindings.setURI(alias, uri);
                i++;
            }
        }
        return strBuilder.toString();
    }
}
