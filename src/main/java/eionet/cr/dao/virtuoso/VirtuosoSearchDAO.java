package eionet.cr.dao.virtuoso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.displaytag.properties.SortOrderEnum;
import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.helpers.FreeTextSearchHelper.FilterType;
import eionet.cr.dao.helpers.QueryHelper;
import eionet.cr.dao.readers.DeliverySearchReader;
import eionet.cr.dao.readers.FreeTextSearchReader;
import eionet.cr.dao.readers.GenericSubjectDataReader;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.util.BBOX;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dao.virtuoso.helpers.VirtuosoDeliveriesSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFilteredSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoFreeTextSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoReferencesSearchHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoSearchBySourceHelper;
import eionet.cr.dao.virtuoso.helpers.VirtuosoTagSearchHelper;
import eionet.cr.dto.DeliveryDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SearchResultDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Bindings;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sesame.SPARQLResultSetReader;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.util.sql.VirtuosoFullTextQuery;
import eionet.cr.web.util.CustomPaginatedList;

/**
 * DAO methods for search in Virtuoso.
 *
 * @author jaanus
 */
public class VirtuosoSearchDAO extends VirtuosoBaseDAO implements SearchDAO {

    /** Sort object constant. */
    private static final String SORT_OBJECT_VALUE_VARIABLE = "sortObjVal";

    /**
     * Free text search implementation in Virtuoso.
     *
     * @see eionet.cr.dao.SearchDAO#searchByFreeText(eionet.cr.dao.util.SearchExpression,
     *      eionet.cr.dao.helpers.FreeTextSearchHelper.FilterType, eionet.cr.util.pagination.PagingRequest,
     *      eionet.cr.util.SortingRequest)
     * @param exactMatch indicates if only exact amtch of String is searched
     * @return
     * @throws DAOException if query fails.
     */
    @Override
    public SearchResultDTO<SubjectDTO> searchByFreeText(final SearchExpression expression, final FilterType filterType,
            final boolean exactMatch, final PagingRequest pagingRequest, final SortingRequest sortingRequest) throws DAOException {
        SearchResultDTO<SubjectDTO> result = new SearchResultDTO<SubjectDTO>();

        // if search expression is null or empty, return empty result
        if (expression == null || expression.isEmpty()) {
            return result;
        }

        // parse search expression for Virtuoso SPARQL
        VirtuosoFullTextQuery virtQuery = null;
        try {
            virtQuery = VirtuosoFullTextQuery.parse(expression);
            logger.debug("Free-text search string parsed for Virtuoso SPARQL: " + virtQuery);
            logger.debug("Parsed q: " + virtQuery.getParsedQuery());
        } catch (ParseException pe) {
            throw new DAOException("Error parsing the search text", pe);
        }

        // if search expression is empty after being parsed for Virtuoso SPARQL, return empty result
        if (virtQuery.getParsedQuery().length() == 0) {
            return result;
        }

        // create query helper
        VirtuosoFreeTextSearchHelper helper =
                new VirtuosoFreeTextSearchHelper(expression, virtQuery, exactMatch, pagingRequest, sortingRequest);

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
        List<String> subjectUris = executeSPARQL(query, helper.getQueryBindings(), matchReader);

        result.setQuery(QueryHelper.getFormatedQuery(query, helper.getQueryBindings()));

        logger.debug("Free-text search, find subjects query time " + Util.durationSince(startTime));

        // initialize total match count and resultList
        Integer totalMatchCount = Integer.valueOf(0);
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not empty, do the necessary processing and get total match count
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates = {Predicates.RDF_TYPE, Predicates.RDFS_LABEL};

            // get the subjects data
            logger.trace("Free-text search, getting the data of the found subjects");
            resultList = getFoundSubjectsData(subjectUris, neededPredicates);

            // if paging required, get distinct subjects total match count
            if (pagingRequest != null) {

                logger.trace("Free-text search, getting exact row count");
                totalMatchCount = new Integer(getExactRowCount(helper));
            }
        }
        logger.debug("Free-text search, total query time " + Util.durationSince(startTime));

        result.setItems(resultList);
        result.setMatchCount(totalMatchCount);
        return result;
    }

    /**
     * Alternative version of "searchByFilters" that does it all in one query. i.e. as opposed to the currently used
     * {@link #searchByFilters(Map, boolean, PagingRequest, SortingRequest, List, boolean)}, which is a so-called "two-step"
     * approach where the first query gets the URIs of the matching subjects in the requested order, and the second query gets the
     * requested predicates of these subjects.
     *
     * This version was tried as faster alternative to the "two-step" approach. However, after a performance bug was fixed in the
     * latter, this version was again dropped, as it is much more complicated and harder to follow. However, we keep it here in case
     * we need to bring it back for whichever reason after all.
     *
     * @param filters
     * @param checkFiltersRange
     * @param pagingRequest
     * @param sortingRequest
     * @param selectPredicates
     * @param useInference
     * @return
     * @throws DAOException
     */
    private SearchResultDTO<SubjectDTO> searchByFilters_alternative(Map<String, String> filters, boolean checkFiltersRange,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectPredicates, boolean useInference)
                    throws DAOException {

        SearchResultDTO<SubjectDTO> result = new SearchResultDTO<SubjectDTO>();
        Bindings bindings = new Bindings();

        final Map<String, String> predicates = new LinkedHashMap<String, String>();
        for (String predicateUri : selectPredicates) {
            predicates.put(URIUtil.extractURILabel(predicateUri), predicateUri);
        }

        StringBuilder query = new StringBuilder();
        if (useInference) {
            query.append(SPARQLQueryUtil.getCrInferenceDefinition());
        }
        query.append("SELECT ?s ?" + SORT_OBJECT_VALUE_VARIABLE);
        for (String predicate : predicates.keySet()) {
            query.append(" ?" + predicate);
        }
        query.append(" WHERE { ");
        query.append(getWhereContents(filters, bindings) + " . ");
        for (String predicate : predicates.keySet()) {
            query.append(" OPTIONAL {{");
            query.append("SELECT ?s (sql:group_concat(?" + predicate + ",', ') AS ?" + predicate + ") WHERE { ");
            query.append(" {");
            query.append("  SELECT DISTINCT ?s (STR(?" + predicate + ") AS ?" + predicate + ") {");
            query.append("   ?s <" + predicates.get(predicate) + "> ?" + predicate + ".");
            query.append("  } order by asc(?" + predicate + ")");
            query.append(" }");
            query.append("} GROUP BY ?s ");
            query.append("}}");
        }
        if (sortingRequest != null && StringUtils.isNotEmpty(sortingRequest.getSortingColumnName())) {
            String sortPredicate = URIUtil.extractURILabel(sortingRequest.getSortingColumnName()) + "_sorting";
            String sortPredicateUri = predicates.get(URIUtil.extractURILabel(sortingRequest.getSortingColumnName()));
            if (sortPredicateUri != null) {
                query.append(" OPTIONAL {");
                query.append(" {");
                query.append("  SELECT ?s");
                query.append(" sql:group_concat(bif:either(bif:starts_with(?" + sortPredicate + ", 'http://') +");
                query.append(" bif:starts_with(?" + sortPredicate + ", 'ftp://') +");
                query.append(" bif:starts_with(?" + sortPredicate + ", 'news://'),");
                query.append(" bif:subseq(?" + sortPredicate + ",");
                query.append(" bif:strrchr(bif:replace(?" + sortPredicate + ", '/', '#'), '#')), ?" + sortPredicate + "),");
                query.append(" ', ') AS ?" + SORT_OBJECT_VALUE_VARIABLE);
                query.append("  WHERE { {SELECT DISTINCT ?s (bif:lcase(STR(?" + sortPredicate + ")) AS ?" + sortPredicate
                        + ") { ?s <" + sortPredicateUri + "> ?" + sortPredicate + ".} ORDER BY ASC(?" + sortPredicate + ") }");
                query.append("  } GROUP BY ?s");
                query.append(" }");
                query.append("}");
            }
        }
        query.append("} GROUP BY ?s ");
        if (sortingRequest != null) {
            if (StringUtils.isNotEmpty(sortingRequest.getSortingColumnName())) {
                String sortColumn = SORT_OBJECT_VALUE_VARIABLE;
                query.append(" ORDER BY " + sortingRequest.getSortOrder() + "(?" + sortColumn + ")");
            }
        }
        if (pagingRequest != null) {
            query.append(" LIMIT ").append(pagingRequest.getItemsPerPage()).append(" OFFSET ").append(pagingRequest.getOffset());
        }

        List<SubjectDTO> resultList = executeSPARQL(query.toString(), bindings, new SPARQLResultSetReader<SubjectDTO>() {
            private List<SubjectDTO> subjects = new ArrayList<SubjectDTO>();

            @Override
            public List<SubjectDTO> getResultList() {
                return subjects;
            }

            @Override
            public void endResultSet() {
            }

            @Override
            public void startResultSet(List<String> bindingNames) {
            }

            @Override
            public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

                Value subjectValue = bindingSet.getValue("s");
                String subjectUri = subjectValue.stringValue();

                boolean anonymousSubject = subjectValue instanceof BNode;
                if (anonymousSubject) {
                    subjectUri = BNODE_URI_PREFIX + subjectUri;
                }
                SubjectDTO subject = new SubjectDTO(subjectUri, anonymousSubject);

                for (String predicate : predicates.keySet()) {
                    Value value = bindingSet.getValue(predicate);
                    if (value == null) {
                        continue;
                    }

                    String valueString = value.stringValue();

                    String[] valuesArr = valueString.split(",");
                    for (String val : valuesArr) {
                        ObjectDTO obj = new ObjectDTO(val.trim(), !URIUtil.isSchemedURI(val.trim()));
                        subject.addObject(predicates.get(predicate), obj);
                    }
                }

                subjects.add(subject);
            }

        });

        if (pagingRequest != null) {
            StringBuilder countQuery = new StringBuilder();
            countQuery.append("SELECT COUNT(DISTINCT ?s) WHERE {");
            countQuery.append(getWhereContents(filters, bindings));
            countQuery.append("}");

            String total = executeUniqueResultSPARQL(countQuery.toString(), bindings, new SingleObjectReader<String>());
            result.setMatchCount(Integer.parseInt(total));
        }

        result.setItems(resultList);
        result.setQuery(QueryHelper.getFormatedQuery(query.toString(), bindings));

        return result;
    }

    /**
     * Builds the query 's "where contents", i.e. the part that goes in between the curly brackets in "where {}".
     *
     * @return Query parameter string for SPARQL
     */
    private String getWhereContents(Map<String, String> filters, Bindings bindings) {

        String result = "";
        int filterIndex = 0;

        for (Entry<String, String> entry : filters.entrySet()) {

            String predicateUri = entry.getKey();
            String objectValue = entry.getValue();

            if (!StringUtils.isBlank(predicateUri) && !StringUtils.isBlank(objectValue)) {

                filterIndex++;

                String predicateVariable = "p" + filterIndex;
                String objectVariable = "o" + filterIndex;
                String predicateValueVariable = predicateVariable + "Val";
                String objectValueVariable = objectVariable + "Val";

                if (filterIndex > 1) {
                    result += " . ";
                }

                result += "?s ?" + predicateVariable + " ?" + objectVariable;
                result += " . filter(?" + predicateVariable + " = ?" + predicateValueVariable + ")";
                bindings.setURI(predicateValueVariable, predicateUri);

                if (Util.isSurroundedWithQuotes(objectValue)) {
                    result += " . filter(?" + objectVariable + " = ?" + objectValueVariable + ")";
                    bindings.setString(objectValueVariable, Util.removeSurroundingQuotes(objectValue));
                } else if (URIUtil.isSchemedURI(objectValue)) {
                    result += " . filter(?" + objectVariable + " = ?" + objectValueVariable + ")";
                    bindings.setURI(objectValueVariable, objectValue);
                } else {
                    result += " . filter bif:contains(?" + objectVariable + ", ?" + objectValueVariable + ")";
                    // Quotes are added for bif:contains expression
                    objectValue = "\"" + objectValue + "\"";
                    bindings.setString(objectValueVariable, objectValue);
                }
            }
        }

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchByFilters(java.util.Map, java.util.Set, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest, java.util.List)
     */
    @Override
    public SearchResultDTO<SubjectDTO> searchByFilters(Map<String, String> filters, boolean checkFiltersRange,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectPredicates, boolean useInference)
                    throws DAOException {

        SearchResultDTO<SubjectDTO> result = new SearchResultDTO<SubjectDTO>();

        // create query helper
        Set<String> literalRangeFilters = null;
        if (checkFiltersRange) {
            literalRangeFilters = DAOFactory.get().getDao(HelperDAO.class).getLiteralRangeSubjects(filters.keySet());
        }
        VirtuosoFilteredSearchHelper helper =
                new VirtuosoFilteredSearchHelper(filters, literalRangeFilters, pagingRequest, sortingRequest, useInference);

        // create the list of IN parameters of the query

        // let the helper create the query and fill IN parameters
        ArrayList<Object> inParams = new ArrayList<Object>();
        String query = helper.getQuery(inParams);
        Bindings bindings = helper.getQueryBindings();

        long startTime = System.currentTimeMillis();
        logger.trace("Search by filters, executing subject finder query: " + query);

        // execute the query, with the IN parameters
        List<String> subjectUris = executeSPARQL(query, bindings, new SingleObjectReader<String>());

        result.setQuery(QueryHelper.getFormatedQuery(query, bindings));

        logger.debug("Search by filters, find subjects query time " + Util.durationSince(startTime));

        int totalRowCount = 0;
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and
        // total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates = new String[] {};
            if (selectPredicates != null && selectPredicates.size() > 0) {
                neededPredicates = selectPredicates.toArray(neededPredicates);
            }

            // get the data of all found subjects
            logger.trace("Search by filters, getting the data of the found subjects");
            resultList = getFoundSubjectsData(subjectUris, neededPredicates);
        }
        // if paging required, get the total number of found subjects too
        if (pagingRequest != null) {
            logger.trace("Search by filters, getting exact row count");
            totalRowCount = new Integer(getExactRowCount(helper));
        }

        logger.debug("Search by filters, total query time " + Util.durationSince(startTime));

        result.setMatchCount(totalRowCount);
        result.setItems(resultList);

        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchDeliveries(java.lang.String, java.lang.String, java.lang.String,
     * eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest)
     */
    @Override
    public CustomPaginatedList<DeliveryDTO> searchDeliveries(String obligation, String locality, String year, String sortCol,
            PagingRequest pagingRequest, SortingRequest sortingRequest) throws DAOException {

        CustomPaginatedList<DeliveryDTO> ret = new CustomPaginatedList<DeliveryDTO>();

        VirtuosoDeliveriesSearchHelper helper =
                new VirtuosoDeliveriesSearchHelper(obligation, locality, year, pagingRequest, sortingRequest);

        // let the helper create the query and fill IN parameters
        ArrayList<Object> inParams = new ArrayList<Object>();
        String query = helper.getQuery(inParams);

        List<String> subjectUris = executeSPARQL(query, new SingleObjectReader<String>());

        String valuesQuery = helper.getValuesQuery(subjectUris);

        DeliverySearchReader reader = new DeliverySearchReader();
        if (!StringUtils.isBlank(valuesQuery)) {
            executeSPARQL(valuesQuery, reader);
        }
        Map<String, DeliveryDTO> resultMap = reader.getMap();

        ret.setList(getOrderedList(subjectUris, resultMap));
        ret.setFullListSize(getExactRowCount(helper));
        ret.setPageNumber(pagingRequest.getPageNumber());
        ret.setSortCriterion(sortCol);
        ret.setObjectsPerPage(pagingRequest.getItemsPerPage());
        if (sortingRequest.getSortOrder().equals(SortOrder.ASCENDING)) {
            ret.setSortDirection(SortOrderEnum.ASCENDING);
        } else if (sortingRequest.getSortOrder().equals(SortOrder.DESCENDING)) {
            ret.setSortDirection(SortOrderEnum.DESCENDING);
        }

        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchReferences(java.lang.Long, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchReferences(Long subjectHash, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchBySpatialBox(eionet.cr.dao.util.BBOX, java.lang.String,
     * eionet.cr.util.pagination.PagingRequest, eionet.cr.util.SortingRequest, boolean)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchBySpatialBox(BBOX box, String sourceUri, PagingRequest pagingRequest,
            SortingRequest sortingRequest, boolean sortByObjectHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");

        // // create query helper
        // PostgreSpatialSearchHelper helper = new PostgreSpatialSearchHelper(box, sourceUri,
        // pagingRequest, sortingRequest, sortByObjectHash);
        //
        // // create the list of IN parameters of the query
        // ArrayList<Object> inParams = new ArrayList<Object>();
        //
        // // let the helper create the query and fill IN parameters
        // String query = helper.getQuery(inParams);
        //
        // long startTime = System.currentTimeMillis();
        // logger.trace("Spatial search, executing subject finder query: " + query);
        //
        // // execute the query, with the IN parameters
        // List<Long> list = executeSQL(query, inParams, new SingleObjectReader<Long>());
        //
        // int totalRowCount = 0;
        // List<SubjectDTO> subjects = new ArrayList<SubjectDTO>();
        //
        // // if result list not null and not empty, then get the subjects data and total rowcount
        // if (list != null && !list.isEmpty()) {
        //
        // // create the subjects map that needs to be fed into the subjects data reader
        // Map<Long,SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
        // for (Long hash : list) {
        // subjectsMap.put(hash, null);
        // }
        //
        // logger.trace("Spatial search, getting the data of the found subjects");
        //
        // // get the data of all found subjects
        // subjects = getSubjectsData(subjectsMap);
        //
        // // if paging required, get the total number of found subjects too
        // if (pagingRequest != null) {
        //
        // inParams = new ArrayList<Object>();
        // query = helper.getCountQuery(inParams);
        //
        // logger.trace("Spatial search, executing rowcount query: " + query);
        //
        // totalRowCount = Integer.valueOf(executeUniqueResultSQL(
        // helper.getCountQuery(inParams),
        // inParams, new SingleObjectReader<Long>()).toString());
        // }
        // }
        //
        // logger.debug("Search references, total query time " + Util.durationSince(startTime));
        //
        // // the result Pair contains total number of subjects and the requested sub-list
        // return new Pair<Integer,List<SubjectDTO>>(Integer.valueOf(totalRowCount), subjects);

    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchByTypeAndFilters(java.util.Map, java.util.Set, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest, java.util.List)
     */
    @Override
    public SearchResultDTO<SubjectDTO> searchByTypeAndFilters(Map<String, String> filters, boolean checkFiltersRange,
            PagingRequest pagingRequest, SortingRequest sortingRequest, List<String> selectPredicates) throws DAOException {

        return searchByFilters(filters, checkFiltersRange, pagingRequest, sortingRequest, selectPredicates, true);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchBySource(java.lang.String, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchBySource(String sourceUrl, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        // if source URL to search by is blank, return empty result
        if (StringUtils.isBlank(sourceUrl)) {
            return new Pair<Integer, List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        }

        // create query helper
        VirtuosoSearchBySourceHelper helper = new VirtuosoSearchBySourceHelper(sourceUrl, pagingRequest, sortingRequest);

        // let the helper create the query
        String query = helper.getQuery(null);

        long startTime = System.currentTimeMillis();
        logger.trace("Searching subjects in source, executing finder query: " + query);

        // execute the query
        SingleObjectReader<String> matchReader = new SingleObjectReader<String>();
        matchReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);
        List<String> subjectUris = executeSPARQL(query, helper.getQueryBindings(), matchReader);

        Integer totalMatchCount = Integer.valueOf(0);
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // get the data of all found subjects
            logger.trace("Searching subjects in sources, getting the data of the found subjects");
            resultList = getFoundSubjectsData(subjectUris, null);

            // if paging required, get the total number of found subjects too
            if (pagingRequest != null) {

                logger.trace("Search subjects in source, executing rowcount query: " + query);
                totalMatchCount = new Integer(getExactRowCount(helper));
            }
        }

        logger.debug("Search subjects in source, total query time " + Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer, List<SubjectDTO>>(totalMatchCount, resultList);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchDeliveriesForROD(eionet.cr.util.pagination.PagingRequest)
     */
    @Override
    public Vector<Hashtable<String, Vector<String>>> searchDeliveriesForROD(PagingRequest pagingRequest) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
        // StringBuilder sBuilder = new StringBuilder("select distinct").
        // append(" SUBJECT as SUBJECT_HASH, PREDICATE as PREDICATE_HASH, OBJECT, LIT_OBJ").
        // append(" from SPO").
        // append(" where").
        // append(" PREDICATE in (").append(Util.toCSV(RODDeliveryReader.getPredicateHashes())).
        // append(") and ANON_OBJ='N'").
        // append(" and SUBJECT in (select distinct SUBJECT from SPO").
        // append(" where PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
        // append(" and OBJECT_HASH=").append(Hashes.spoHash(Subjects.ROD_DELIVERY_CLASS)).
        // append(" order by SUBJECT offset ").append(pagingRequest.getOffset()).
        // append(" limit ").append(pagingRequest.getItemsPerPage()).
        // append(" ) order by SUBJECT");
        //
        // logger.debug("Executing delivery search for ROD");
        //
        // RODDeliveryReader reader = new RODDeliveryReader();
        // executeSQL(sBuilder.toString(), reader);
        // return reader.getResultVector();

    }

    /**
     * SPARQL for receiving Subproperties of cr:tag.
     */
    private static final String CRTAG_SUBPROPS_SPARQL = "SELECT ?s WHERE { ?s ?subPropertyOf  ?crTagPredicate  }";

    /**
     * Search by tags implementation in Virtuoso.
     *
     * @see eionet.cr.dao.SearchDAO#searchByTags(java.util.List, eionet.cr.util.pagination.PagingRequest,
     *      eionet.cr.util.SortingRequest, java.util.List)
     * @param tags List<String> - tag names
     * @param selectedPredicates List<String> - predicates to be shown
     * @param pagingRequest sortingRequest PagingRequest
     * @param sortingRequest pagingRequest SortingRequest
     * @return Pair <Integer, List<SubjectDTO>>
     * @throws DAOException if query fails
     */
    @Override
    public SearchResultDTO<SubjectDTO> searchByTags(final List<String> tags, final PagingRequest pagingRequest,
            final SortingRequest sortingRequest, final List<String> selectedPredicates) throws DAOException {

        SearchResultDTO<SubjectDTO> result = new SearchResultDTO<SubjectDTO>();

        Map<String, String> filters = buildTagsInputParameter(tags);

        // create query helper
        VirtuosoTagSearchHelper helper = new VirtuosoTagSearchHelper(filters, null, pagingRequest, sortingRequest, false);
        helper.setTags(tags);

        // let the helper create the query and fill IN parameters
        String query = helper.getQuery(new ArrayList<Object>());

        long startTime = System.currentTimeMillis();
        logger.debug("Search by filters, executing subject finder query: " + query);

        // execute the query, with the IN parameters
        List<String> subjectUris = executeSPARQL(query, helper.getQueryBindings(), new SingleObjectReader<String>());

        result.setQuery(QueryHelper.getFormatedQuery(query, helper.getQueryBindings()));

        logger.debug("Search by tags, find subjects query time " + Util.durationSince(startTime));

        int totalRowCount = 0;
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and
        // total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            // only these predicates will be queried for
            String[] neededPredicates = new String[] {Predicates.CR_TAG, Predicates.RDF_TYPE, Predicates.RDFS_LABEL};

            // get the subjects data
            resultList = getFoundSubjectsData(subjectUris, neededPredicates);
        }
        // if paging required, get the total number of found subjects too
        if (pagingRequest != null) {
            logger.trace("Search by filters, getting exact row count");
            totalRowCount = new Integer(getExactRowCount(helper));
        }

        // return new Pair<Integer,List<SubjectDTO>>(0, new LinkedList<SubjectDTO>());
        logger.debug("Search by filters, total query time " + Util.durationSince(startTime));

        result.setItems(resultList);
        result.setMatchCount(totalRowCount);
        return result;

    }

    /**
     * Alternative version of "searchByTags" that does it all in one query. i.e. as opposed to the currently used
     * {@link #searchByTags(List, PagingRequest, SortingRequest, List)}, which is a so-called "two-step" approach where the first
     * query gets the URIs of the matching subjects in the requested order, and the second query gets the requested predicates of
     * these subjects.
     *
     * This version was tried as faster alternative to the "two-step" approach. However, after a performance bug was fixed in the
     * latter, this version was again dropped, as it is much more complicated and harder to follow. However, we keep it here in case
     * we need to bring it back for whichever reason after all.
     *
     * @param tags
     * @param pagingRequest
     * @param sortingRequest
     * @param selectedPredicates
     * @return
     * @throws DAOException
     */
    private SearchResultDTO<SubjectDTO> searchByTags_alternative(final List<String> tags, final PagingRequest pagingRequest,
            final SortingRequest sortingRequest, final List<String> selectedPredicates) throws DAOException {
        SearchResultDTO<SubjectDTO> result = new SearchResultDTO<SubjectDTO>();

        Map<String, String> sortingMap = new HashMap<String, String>();
        sortingMap.put(Predicates.CR_TAG, "tags");
        sortingMap.put(Predicates.RDFS_LABEL, "oneLabel");
        sortingMap.put(Predicates.RDF_TYPE, "sortTypes");

        Bindings bindings = new Bindings();
        String tagPredicate = "tagPredicate";
        bindings.setURI(tagPredicate, Predicates.CR_TAG);

        StringBuilder query = new StringBuilder();
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>");
        query.append("PREFIX cr: <http://cr.eionet.europa.eu/ontologies/contreg.rdf#>");
        query.append("SELECT ?bookmark ?oneLabel ?tags ?types ?sortTypes WHERE { ");
        for (int i = 0; i < tags.size(); i++) {
            String tagLiteral = "tagLiteral" + i;
            bindings.setString(tagLiteral, tags.get(i));

            query.append("?bookmark ");
            query.append("?" + tagPredicate);
            query.append(" ?" + tagLiteral + " .");
        }
        query.append("{");
        query.append(" SELECT ?bookmark (sql:group_concat(?tag,', ') AS ?tags)");
        query.append(" WHERE { {SELECT DISTINCT ?bookmark ?tag { ?bookmark cr:tag ?tag. } }} GROUP BY ?bookmark");
        query.append("} ");
        query.append("OPTIONAL {");
        query.append(" {");
        query.append("  SELECT ?bookmark (sql:group_concat(?type,', ') AS ?types)");
        query.append("  sql:group_concat(bif:substring(?type, bif:strrchr(bif:replace(?type, '/', '#'), '#')+2, 5),', ') AS ?sortTypes");
        query.append("  WHERE { {SELECT DISTINCT ?bookmark (STR(?type) AS ?type) { ?bookmark a ?type.} }");
        query.append("  } GROUP BY ?bookmark");
        query.append(" }");
        query.append("}");
        query.append("OPTIONAL {");
        query.append(" {");
        query.append("  SELECT ?bookmark (bif:min(?label) AS ?oneLabel)");
        query.append("  WHERE {?bookmark rdfs:label ?label.}");
        query.append(" }");
        query.append("}");
        query.append("} GROUP BY ?bookmark ");

        if (sortingRequest != null) {
            if (StringUtils.isNotEmpty(sortingRequest.getSortingColumnName())) {
                if (StringUtils.isNotEmpty(sortingMap.get(sortingRequest.getSortingColumnName()))) {
                    query.append("ORDER BY " + sortingRequest.getSortOrder() + "(?"
                            + sortingMap.get(sortingRequest.getSortingColumnName()) + ")");
                }
            }
        }

        if (pagingRequest != null) {
            query.append(" LIMIT ").append(pagingRequest.getItemsPerPage()).append(" OFFSET ").append(pagingRequest.getOffset());
        }

        List<SubjectDTO> resultList = executeSPARQL(query.toString(), bindings, new SPARQLResultSetReader<SubjectDTO>() {
            private List<SubjectDTO> subjects = new ArrayList<SubjectDTO>();

            @Override
            public List<SubjectDTO> getResultList() {
                return subjects;
            }

            @Override
            public void endResultSet() {
            }

            @Override
            public void startResultSet(List<String> bindingNames) {
            }

            @Override
            public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

                Value subjectValue = bindingSet.getValue("bookmark");
                String subjectUri = subjectValue.stringValue();

                Value labelValue = bindingSet.getValue("oneLabel");
                String label = null;
                if (labelValue != null) {
                    label = labelValue.stringValue();
                }

                Value tagsValue = bindingSet.getValue("tags");
                String tags = tagsValue.stringValue();

                Value typesValue = bindingSet.getValue("types");
                String types = null;
                if (typesValue != null) {
                    types = typesValue.stringValue();
                }

                boolean anonymousSubject = subjectValue instanceof BNode;
                if (anonymousSubject) {
                    subjectUri = BNODE_URI_PREFIX + subjectUri;
                }

                SubjectDTO subject = new SubjectDTO(subjectUri, anonymousSubject);

                if (StringUtils.isNotEmpty(label)) {
                    ObjectDTO labelObject = new ObjectDTO(label, true);
                    subject.addObject(Predicates.RDFS_LABEL, labelObject);
                }

                String[] tagsArr = tags.split(",");
                for (String tag : tagsArr) {
                    ObjectDTO tagsObject = new ObjectDTO(tag.trim(), true);
                    subject.addObject(Predicates.CR_TAG, tagsObject);
                }

                if (StringUtils.isNotEmpty(types)) {
                    String[] typesArr = types.split(",");
                    for (String type : typesArr) {
                        ObjectDTO typeUri = new ObjectDTO(type.trim(), false);
                        subject.addObject(Predicates.RDF_TYPE, typeUri);
                    }
                }

                subjects.add(subject);
            }

        });

        if (pagingRequest != null) {
            StringBuilder countQuery = new StringBuilder();
            countQuery.append("SELECT COUNT(DISTINCT ?s) WHERE {");
            for (int i = 0; i < tags.size(); i++) {
                String tagLiteral = "tagLiteral" + i;
                bindings.setString(tagLiteral, tags.get(i));

                countQuery.append("?s ");
                countQuery.append("?" + tagPredicate);
                countQuery.append(" ?" + tagLiteral + " .");
            }
            countQuery.append("}");

            String total = executeUniqueResultSPARQL(countQuery.toString(), bindings, new SingleObjectReader<String>());
            result.setMatchCount(Integer.parseInt(total));
        }

        result.setItems(resultList);
        result.setQuery(QueryHelper.getFormatedQuery(query.toString(), bindings));

        return result;
    }

    /**
     * Helper method to convert array of tag to a map required by search method.
     *
     * @param tags List<String> tag names
     * @return Map<String, String> in format [tag predicate: tag name]
     */
    private Map<String, String> buildTagsInputParameter(final List<String> tags) {
        Map<String, String> tagFilters = new HashMap<String, String>();
        for (String tag : tags) {
            tagFilters.put(Predicates.CR_TAG, tag);
        }
        return tagFilters;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#getExactRowCountLimit()
     */
    @Override
    public int getExactRowCountLimit() {

        return Integer.MAX_VALUE;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#searchReferences(java.lang.String, eionet.cr.util.pagination.PagingRequest,
     * eionet.cr.util.SortingRequest)
     */
    @Override
    public Pair<Integer, List<SubjectDTO>> searchReferences(String subjectUri, PagingRequest pagingRequest,
            SortingRequest sortingRequest) throws DAOException {

        // create query helper
        VirtuosoReferencesSearchHelper helper = new VirtuosoReferencesSearchHelper(subjectUri, pagingRequest, sortingRequest);

        // let the helper create the query
        // (no query parameters needed here, so supplying null)
        String query = helper.getQuery(null);

        long startTime = System.currentTimeMillis();
        logger.debug("Search references, executing subject finder query: " + query);

        // execute the query
        SingleObjectReader<String> reader = new SingleObjectReader<String>();
        reader.setBlankNodeUriPrefix(BNODE_URI_PREFIX);

        List<String> subjectUris = executeSPARQL(query, helper.getQueryBindings(), reader);

        Integer totalMatchCount = Integer.valueOf(0);
        List<SubjectDTO> resultList = new ArrayList<SubjectDTO>();

        // if result list not null and not empty, then get the subjects data and total rowcount
        if (subjectUris != null && !subjectUris.isEmpty()) {

            logger.debug("Search references, getting the data of the found subjects");

            // get the data of all found subjects
            String predicateQuery = helper.getSubjectsDataQuery(subjectUris, subjectUri);
            SubjectDataReader sdReader = new GenericSubjectDataReader(subjectUris);
            sdReader.setBlankNodeUriPrefix(BNODE_URI_PREFIX);
            logger.debug("Predicate query: " + predicateQuery);
            // separate bindings for subject data
            resultList = executeSPARQL(predicateQuery, helper.getSubjectDataBindings(), sdReader);

            // if paging required, get the total number of found subjects too
            if (pagingRequest != null) {

                logger.debug("Search references, executing rowcount query: " + query);
                totalMatchCount = new Integer(getExactRowCount(helper));
                // totalMatchCount = resultList.size();
            }
        }

        logger.debug("Search references, total query time " + Util.durationSince(startTime));

        // the result Pair contains total number of subjects and the requested sub-list
        return new Pair<Integer, List<SubjectDTO>>(totalMatchCount, resultList);
    }

    /**
     * SPARQL for Type cache entries.
     */
    private static final String TYPES_CACHE_SPARQL = "SELECT DISTINCT ?o WHERE {?s a ?o . filter isURI(?o)} ORDER BY ?o ";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SearchDAO#getTypes()
     */
    @Override
    public List<SubjectDTO> getTypes() throws DAOException {

        List<String> typeUris = executeSPARQL(TYPES_CACHE_SPARQL, new SingleObjectReader<String>());
        String[] neededPredicates = {Predicates.RDFS_LABEL};

        List<SubjectDTO> resultList = getFoundSubjectsData(typeUris, neededPredicates);
        return resultList;
    }
}
