package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.SearchDAOIT;
import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.dto.FactsheetDTO;
import eionet.cr.dto.SearchResultDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;
import eionet.cr.util.SortOrder;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Unit tests for SPARQL and SQL queries that are potentially sensitive to Virtuoso upgrade.
 *
 * @author Jaanus
 */
public class VirtuosoUpgradeIT extends CRDatabaseTestCase {

    /** Obligations seed file. */
    private static final String OBLIGATIONS_RDF = "obligations.rdf";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getRDFXMLSeedFiles()
     */
    @Override
    protected List<String> getRDFXMLSeedFiles() {
        return Arrays.asList("rdf_national_chars_utf8.rdf.xml", OBLIGATIONS_RDF, "persons.rdf", "tags.rdf");
    }

    /**
     * Factsheet query is VERY complex. Test that it doesn't throw any exceptions.
     * The query uses the following Virtuoso functions and/or Virtuoso impementations of standard SPARQL functions:
     * from standard SPARQL:
     * - min()
     * - isBlank()
     * - lang()
     * - str()
     * - datatype()
     * - iri()
     * from Virtuoso:
     * - bif:either()
     * - bif:substring()
     * - bif:concat()
     * - bif:length()
     * - bif:md5()
     * - bif:left()
     * - bif:coalesce()
     * - type casting like xsd:int(some_string)
     *
     *
     * @throws DAOException If query fails.
     */
    public void testFactsheetQuery() throws DAOException {

        // "select ?pred min(xsd:int(isBlank(?s))) as ?anonSubj "
        // + "min(bif:either(isLiteral(?o),"
        // +
        // "bif:concat(bif:substring(str(?o),1,LEN),'<|>',lang(?o),'<|>',str(datatype(?o)),'<|><|>0<|>',str(?g),'<|>',str(bif:length(str(?o))),'<|>',bif:md5(str(?o))),"
        // +
        // "bif:concat(bif:coalesce(str(?oLabel),bif:left(str(?o),LEN)),'<|>',lang(?oLabel),'<|>',str(datatype(?oLabel)),'<|>',bif:left(str(?o),LEN),'<|>',str(isBlank(?o)),'<|>',str(?g),'<|><|>')"
        // + ")) as ?objData " + "count(distinct ?o) as ?objCount " + "where {" + "graph ?g {"
        // + "?s ?pred ?o. filter(?s=iri(?subjectUri))}" + ". optional {?o <" + Predicates.RDFS_LABEL + "> ?oLabel}"
        // + "} group by ?pred";

        String subjectUri = "http://rod.eionet.europa.eu/obligations/171";
        FactsheetDTO factsheet = DAOFactory.get().getDao(HelperDAO.class).getFactsheet(subjectUri, null, null);

        assertNotNull("Was expecting a factsheet for " + subjectUri, factsheet);
        assertEquals("Was expecting factsheet URI to be same as " + subjectUri, subjectUri, factsheet.getUri());

        int predicateCount = factsheet.getPredicateCount();
        assertTrue("Was expecting a factsheet with at least 1 triple", predicateCount > 0);
    }

    /**
     * Test that free-text search syntax has survived the Virtuoso upgrade.
     * NB! Note that this is a syntax check only. The actual free-text search matches are checked in
     * {@link SearchDAOTest#testFreeTextSearchCountResults()}.
     *
     * @throws DAOException If query fails.
     */
    public void testFreeTextSearchSyntax() throws DAOException {

        // Should not test full-text search if there is no real-time full-text indexing activated in the underlying repository.
        // By "real-time" we mean that the index is updated instantly after loading a triple.
        String value = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_REAL_TIME_FT_INDEXING);
        if (BooleanUtils.toBoolean(value) == false) {
            System.out.println("Skipping full-text search test, as no real-time full-text indexing has been activated!");
            return;
        }

        PagingRequest pagingRequest = PagingRequest.create(1);
        SearchResultDTO<SubjectDTO> result =
                DAOFactory
                        .get()
                        .getDao(SearchDAO.class)
                        .searchByFreeText(new SearchExpression("Questionnaire"), FreeTextSearchHelper.FilterType.ANY_OBJECT,
                                false, pagingRequest, null);
    }

    /**
     * Test that search by filters has survived the Virtuoso upgrade.
     *
     * @throws DAOException If query fails.
     */
    public void testSearchByFilters() throws DAOException {

        Map<String, String> filters = new HashMap<String, String>();
        filters.put("http://rod.eionet.europa.eu/schema.rdf#formalReporter", "http://rod.eionet.europa.eu/spatial/10");
        filters.put("http://rod.eionet.europa.eu/schema.rdf#otherClient", "http://rod.eionet.europa.eu/clients/90");

        PagingRequest pagingRequest = PagingRequest.create(1, 200);
        SortingRequest sortRequest = new SortingRequest("http://purl.org/dc/terms/title", SortOrder.DESCENDING);
        List<String> predicates = Arrays.asList("http://purl.org/dc/terms/title", "http://purl.org/dc/terms/valid");

        SearchResultDTO<SubjectDTO> result =
                DAOFactory.get().getDao(SearchDAO.class).searchByFilters(filters, false, pagingRequest, sortRequest, predicates);
        assertNotNull("Expected search result not to be null", result);
        List<SubjectDTO> items = result.getItems();
        assertNotNull("Expected search result items not to be null", items);
        assertEquals("Expected 2 matching items", 2, items.size());
        SubjectDTO firstSubject = items.iterator().next();
        assertEquals("Expected first subject to be http://rod.eionet.europa.eu/obligations/569",
                "http://rod.eionet.europa.eu/obligations/569", firstSubject.getUri());
   
    }

    /**
     * Test that search by filters has survived the Virtuoso upgrade.
     *
     * @throws DAOException If query fails.
     */
    public void testSearchBySource() throws DAOException {

        PagingRequest pagingRequest = PagingRequest.create(1, 200);
        SortingRequest sortRequest = new SortingRequest("http://purl.org/dc/terms/title", SortOrder.DESCENDING);
        Pair<Integer, List<SubjectDTO>> result =
                DAOFactory.get().getDao(SearchDAO.class)
                        .searchBySource(getSeedFileGraphUri(OBLIGATIONS_RDF), pagingRequest, sortRequest);

        assertNotNull("Expected search result not to be null", result);

        List<SubjectDTO> items = result.getRight();
        assertNotNull("Expected search result items not to be null", items);
        assertEquals("Expected 3 matching items", 3, items.size());

        SubjectDTO firstSubject = items.iterator().next();
        assertEquals("Expected first subject to be http://rod.eionet.europa.eu/obligations/370",
                "http://rod.eionet.europa.eu/obligations/370", firstSubject.getUri());
    }

    /**
     * Test that search by tags has survived the Virtuoso upgrade.
     *
     * @throws DAOException If query fails.
     */
    public void testSearchByTags() throws DAOException {

        PagingRequest pagingRequest = PagingRequest.create(1, 200);
        SortingRequest sortRequest = new SortingRequest(Predicates.RDFS_LABEL, SortOrder.DESCENDING);
        List<String> tags = Arrays.asList("tag3", "tag4");

        SearchResultDTO<SubjectDTO> result =
                DAOFactory.get().getDao(SearchDAO.class).searchByTags(tags, pagingRequest, sortRequest);

        assertNotNull("Expected search result not to be null", result);

        List<SubjectDTO> items = result.getItems();
        assertNotNull("Expected search result items not to be null", items);
        assertEquals("Expected 3 matching items", 3, items.size());

        SubjectDTO firstSubject = items.iterator().next();
        assertEquals("Expected first subject to be http://www.yahoo.com", "http://www.yahoo.com", firstSubject.getUri());
    }

    /**
     * Test that search of references has survived the Virtuoso upgrade.
     *
     * @throws DAOException If query fails.
     */
    public void testSearchReferences() throws DAOException {

        String subjectUri = "http://rod.eionet.europa.eu/spatial/10";
        PagingRequest pagingRequest = PagingRequest.create(1, 200);
        SortingRequest sortRequest = new SortingRequest(Predicates.RDFS_LABEL, SortOrder.DESCENDING);

        SearchDAO dao = DAOFactory.get().getDao(SearchDAO.class);
        Pair<Integer, List<SubjectDTO>> result = dao.searchReferences(subjectUri, pagingRequest, sortRequest);

        assertNotNull("Expected search result not to be null", result);

        List<SubjectDTO> items = result.getRight();
        assertNotNull("Expected search result items not to be null", items);
        assertFalse("Expected search result items not to be empty", items.isEmpty());

        String firstSubject = items.iterator().next().getUri();
        String expectedUri = "http://rod.eionet.europa.eu/obligations/370";
        assertEquals("Expected first subject to be " + expectedUri, expectedUri, firstSubject);
    }

    /**
     * Test that various quite specific Virtuoso and/or SPARQL function have survived the Virtuoso upgrade.
     *
     * @throws Exception If any error occurs.
     */
    public void testVariousFunctions() throws Exception {

        // The following queries are tested syntactically only, to enusre Virtuoso supports the specific functions used in them.
        // No actual data comparisons are made.

        RepositoryConnection repoConn = null;
        try {
            repoConn = SesameUtil.getRepositoryConnection();

            String query = "select fn:substring-after(str(?o),\"whatever\") where {?s ?p ?o} limit 1";
            runTupleQuery(repoConn, query);

            query = "select fn:substring-before(str(?o),\"whatever\") where {?s ?p ?o} limit 1";
            runTupleQuery(repoConn, query);

            query = "select strStarts(str(?o),\"whatever\") where {?s ?p ?o} limit 1";
            runTupleQuery(repoConn, query);

            query = "select strEnds(str(?o),\"whatever\") where {?s ?p ?o} limit 1";
            runTupleQuery(repoConn, query);
        } finally {
            SesameUtil.close(repoConn);
        }
    }

    /**
     * Test that the graph renaming syntax has survived the Virtuoso upgrade.
     *
     * @throws Exception
     */
    public void testGraphRename() throws Exception {

        String oldGraph = getSeedFileGraphUri(OBLIGATIONS_RDF);
        String newGraph = "http://new.graph.uri";

        String sqlTemplate = VirtuosoHarvestSourceDAO.RENAME_GRAPH_SQL;
        String renameSql = sqlTemplate.replace("%old_graph%", oldGraph).replaceFirst("%new_graph%", newGraph);

        String countSql = "select count(*) from DB.DBA.RDF_QUAD where G=iri_to_id('%graph_uri%')";

        Connection sqlConn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            sqlConn = SesameUtil.getSQLConnection();
            stmt = sqlConn.createStatement();

            rs = stmt.executeQuery(countSql.replace("%graph_uri%", oldGraph));
            int countOld = rs.next() ? rs.getInt(1) : 0;
            assertTrue("Expected at least one triple in " + oldGraph, countOld > 0);
            SQLUtil.close(rs);

            stmt.execute(renameSql);

            rs = stmt.executeQuery(countSql.replace("%graph_uri%", newGraph));
            int countNew = rs.next() ? rs.getInt(1) : 0;

            assertEquals("Expected row-count to stay same after rename", countOld, countNew);

        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(sqlConn);
        }
    }

    /**
     * Runs the given SPAQRL tuple query on the given repository connection.
     *
     * @param repoConn The repository connection.
     * @param query The SPAQRL query.
     * @return True if the query succeeded and returned at least one row. Otherwise false.
     * @throws OpenRDFException If the query fails.
     */
    private boolean runTupleQuery(RepositoryConnection repoConn, String query) throws OpenRDFException {

        TupleQueryResult queryResult = null;
        try {
            TupleQuery tupleQuery = repoConn.prepareTupleQuery(QueryLanguage.SPARQL, query);
            queryResult = tupleQuery.evaluate();
            return queryResult != null && queryResult.hasNext();
        } finally {
            SesameUtil.close(queryResult);
        }
    }
}
