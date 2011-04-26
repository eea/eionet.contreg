package eionet.cr.dao.virtuoso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.helpers.FreeTextSearchHelper.FilterType;
import eionet.cr.dao.helpers.SearchHelper;
import eionet.cr.dao.readers.FreeTextSearchReader;
import eionet.cr.dao.readers.GraphUrisReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFilteredSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFreeTextSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoReferencesSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoSearchBySourceHelper;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.util.sql.VirtuosoFullTextQuery;

/**
 *
 * @author jaanus
 *
 */
public class VirtuosoSearchDAO extends VirtuosoBaseDAO implements SearchDAO {

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchByFreeText(eionet.cr.dao.util.SearchExpression, eionet.cr.dao.postgre.helpers.FreeTextSearchHelper.FilterType, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchByFreeText(
            SearchExpression expression, FilterType filterType, boolean exactMatch,
            PagingRequest pagingRequest,SortingRequest sortingRequest) throws DAOException {


        // if search expression is null or empty, return empty result
        if (expression == null || expression.isEmpty()) {
            return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        }

        // parse search expression for Virtuoso SPARQL
        VirtuosoFullTextQuery virtQuery = null;
        try {
            virtQuery = VirtuosoFullTextQuery.parse(expression);
            logger.trace("Free-text search string parsed for Virtuoso SPARQL: " + virtQuery);
        } catch (ParseException pe) {
            throw new DAOException("Error parsing the search text", pe);
        }

        // if search expression is empty after being parsed for Virtuoso SPARQL, return empty result
        if (virtQuery.getParsedQuery().length() == 0) {
            return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        }

        // create query helper
        VirtuosoFreeTextSearchHelper helper = new VirtuosoFreeTextSearchHelper(
                expression, virtQuery, exactMatch, pagingRequest, sortingRequest);

        // Set Filter
        helper.setFilter(filterType);

        // let the helper create the query
        // (no query parameters needed here, so supplying null)
        String query = helper.getQuery(null);

        long startTime = System.currentTimeMillis();
        logger.trace("Free-text search, executing subject finder query: " + query);

        // execute the query, using dedicated reader
        FreeTextSearchReader<String> matchReader = new FreeTextSearchReader<String>();
        matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
        List<String> subjectUris = executeSPARQL(query, matchReader);

        //get matching graph URIs
        List<String> graphUris = new ArrayList<String>();
        if (subjectUris != null && subjectUris.size() > 0) {
            graphUris = getGraphUris(subjectUris);
        }

        logger.debug("Free-text search, find subjects query time " + Util.durationSince(startTime));

        // initialize total match count and resultList
        Integer totalMatchCount = Integer.valueOf(0);
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not empty, do the necessary processing and get total match count
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // get the data of all found subjects, provide hit-sources to the reader
            SubjectDataReader dataReader = new SubjectDataReader(subjectUris);
            dataReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

            // only these predicates will be queried for
            String[] neededPredicates = {Predicates.RDF_TYPE, Predicates.RDFS_LABEL};

            logger.trace("Free-text search, getting the data of the found subjects");

            // get the subjects data
            resultList = getSubjectsData(subjectUris, neededPredicates, dataReader, graphUris);

            // if paging required, get distinct subjects total match count
            if (pagingRequest != null) {

                logger.trace("Free-text search, getting exact row count");
                totalMatchCount = new Integer(getExactRowCount(helper));
            }
        }
        logger.debug("Free-text search, total query time " + Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer, List<SubjectDTO>>(totalMatchCount, resultList);
    }

    //Graph URIs are used to get correct cr:contentLastModified for the subject
    //If subject from one graph gave the hit and the same subject in another graph didn't, then only the date of the first graph should be used
    private List<String> getGraphUris(List<String> subjectUris) throws DAOException {
        StringBuilder strBuilder = new StringBuilder().append("select distinct(?g) where {graph ?g {?s ?p ?o. ")
        .append("filter (?s IN (");

        int i = 0;
        for (String subjectUri : subjectUris) {
            if (i > 0) {
                strBuilder.append(", ");
            }
            strBuilder.append("<").append(subjectUri).append(">");
            i++;
        }
        strBuilder.append(")) ");
        strBuilder.append("}}");

        GraphUrisReader<String> reader = new GraphUrisReader<String>();

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            SesameUtil.executeQuery(strBuilder.toString(), reader, conn);
            return reader.getResultList();
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchByFilters(java.util.Map, java.util.Set, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, java.util.List)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchByFilters(
            Map<String, String> filters, Set<String> literalPredicates,
            PagingRequest pagingRequest, SortingRequest sortingRequest,
            List<String> selectedPredicates) throws DAOException {
        // create query helper
        VirtuosoFilteredSearchHelper helper = new VirtuosoFilteredSearchHelper(filters, literalPredicates,
                pagingRequest, sortingRequest);

        // create the list of IN parameters of the query
        ArrayList<Object> inParams = new ArrayList<Object>();

        // let the helper create the query and fill IN parameters
        String query = helper.getQuery(inParams);

        long startTime = System.currentTimeMillis();
        logger.trace("Search by filters, executing subject finder query: " + query);

        // execute the query, with the IN parameters
        List<String> subjectUris = executeSPARQL(query, new SingleObjectReader<String>());

        logger.debug("Search by filters, find subjects query time " + Util.durationSince(startTime));

        int totalRowCount = 0;
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and
        // total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates = null;
            if (literalPredicates != null && literalPredicates.size() > 0) {
                neededPredicates = literalPredicates.toArray(neededPredicates);
            }

            // get the data of all found subjects
            logger.trace("Search by filters, getting the data of the found subjects");
            resultList = getSubjectsData(subjectUris, neededPredicates, new SubjectDataReader(subjectUris), null);
        }
        // if paging required, get the total number of found subjects too
        if (pagingRequest != null) {
            logger.trace("Search by filters, getting exact row count");
            totalRowCount = new Integer(getExactRowCount(helper));
        }

        // return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        logger.debug("Search by filters, total query time " + Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer, List<SubjectDTO>>(totalRowCount, resultList);
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchReferences(java.lang.Long, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchBySpatialBox(eionet.cr.dao.util.BBOX, java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, boolean)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box,
            String sourceUri, PagingRequest pagingRequest,
            SortingRequest sortingRequest, boolean sortByObjectHash)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchByTypeAndFilters(java.util.Map, java.util.Set, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, java.util.List)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchByTypeAndFilters(
            Map<String, String> filters, Set<String> literalPredicates,
            PagingRequest pagingRequest, SortingRequest sortingRequest,
            List<String> selectedPredicates) throws DAOException {

        return searchByFilters(filters, literalPredicates, pagingRequest, sortingRequest, selectedPredicates);
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchBySource(java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchBySource(String sourceUrl,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {

        // if source URL to search by is blank, return empty result
        if (StringUtils.isBlank(sourceUrl)) {
            return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        }

        // create query helper
        VirtuosoSearchBySourceHelper helper = new VirtuosoSearchBySourceHelper(sourceUrl,
                pagingRequest, sortingRequest);

        // let the helper create the query
        String query = helper.getQuery(null);

        long startTime = System.currentTimeMillis();
        logger.trace("Search subjects in source, executing subject finder query: " + query);

        // execute the query
        SingleObjectReader<String> matchReader = new SingleObjectReader<String>();
        matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
        List<String> subjectUris = executeSPARQL(query, matchReader);

        Integer totalMatchCount = Integer.valueOf(0);
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            logger.trace("Search subjects in sources, getting the data of the found subjects");

            // get the data of all found subjects
            SubjectDataReader dataReader = new SubjectDataReader(subjectUris);
            dataReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
            resultList = getSubjectsData(subjectUris, null, dataReader, null);

            // if paging required, get the total number of found subjects too
            if (pagingRequest != null) {

                logger.trace("Search subjects in source, executing rowcount query: " + query);
                totalMatchCount = new Integer(getExactRowCount(helper));
            }
        }

        logger.debug("Search subjects in source, total query time " +
                Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer,List<SubjectDTO>>(totalMatchCount, resultList);
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchDeliveriesForROD(eionet.cr.util.pagination.PagingRequest)
     */
    @Override
    public Vector<Hashtable<String, Vector<String>>> searchDeliveriesForROD(
            PagingRequest pagingRequest) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchByTags(java.util.List, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, java.util.List)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchByTags(List<String> tags,
            PagingRequest pagingRequest, SortingRequest sortingRequest,
            List<String> selectedPredicates) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

    }

    /**
     *
     * @param helper
     * @return
     * @throws DAOException
     */
    private int getExactRowCount(SearchHelper helper) throws DAOException {

        String query = helper.getCountQuery(new ArrayList<Object>());
        Object resultObject = executeUniqueResultSPARQL(query, new SingleObjectReader<Long>());
        return Integer.valueOf(resultObject.toString());
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#getExactRowCountLimit()
     */
    @Override
    public int getExactRowCountLimit() {

        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SearchDAO#searchReferences(java.lang.String, eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchReferences(String subjectUri,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {

        // create query helper
        VirtuosoReferencesSearchHelper helper = new VirtuosoReferencesSearchHelper(
                subjectUri, pagingRequest, sortingRequest);

        // let the helper create the query
        // (no query parameters needed here, so supplying null)
        String query = helper.getQuery(null);

        long startTime = System.currentTimeMillis();
        logger.trace("Search references, executing subject finder query: " + query);

        // execute the query
        List<String> subjectUris = executeSPARQL(query, new SingleObjectReader<String>());

        Integer totalMatchCount = Integer.valueOf(0);
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            logger.trace("Search references, getting the data of the found subjects");

            // get the data of all found subjects
            String predicateQuery = helper.getSubjectsDataQuery(subjectUris, subjectUri);
            resultList = executeSPARQL(predicateQuery, new SubjectDataReader(subjectUris));

            // if paging required, get the total number of found subjects too
            if (pagingRequest != null) {

                logger.trace("Search references, executing rowcount query: " + query);
                totalMatchCount = new Integer(getExactRowCount(helper));
            }
        }

        logger.debug("Search references, total query time " +
                Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer,List<SubjectDTO>>(totalMatchCount, resultList);
    }
}
