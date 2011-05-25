package eionet.cr.web.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import virtuoso.sesame2.driver.VirtuosoRepository;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.sparqlClient.helpers.CRJsonWriter;
import eionet.cr.web.sparqlClient.helpers.CRXmlWriter;
import eionet.cr.web.sparqlClient.helpers.QueryResult;

/**
 *
 * @author altnyris
 *
 */
@UrlBinding("/sparql")
public class SPARQLEndpointActionBean extends AbstractActionBean {

    /** */
    private static final int DEFAULT_NUMBER_OF_HITS = 20;

    /** */
    private static final String FORMAT_XML = "xml";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_HTML = "html";

    /** */
    private static final String FORM_PAGE = "/pages/sparqlClient.jsp";
    private static final String BOOKMARKED_QUERIES_PAGE = "/pages/bookmarkedQueries.jsp";

    /** */
    private static List<String> xmlFormats = new ArrayList<String>();

    /**
     *
     */
    static {
        xmlFormats.add("application/sparql-results+xml");
        xmlFormats.add("application/rdf+xml");
        xmlFormats.add("application/xml");
        xmlFormats.add("text/xml");
        xmlFormats.add("application/x-binary-rdf-results-table");
        xmlFormats.add("text/boolean"); // For ASK query
    }

    /** */
    private String query;
    private String newQuery;
    private String format;
    private int nrOfHits;
    private long executionTime;

    /** */
    private boolean useInferencing;
    boolean isAskQuery = false;
    boolean isConstructQuery = false;

    /** */
    private QueryResult result;
    private String resultAsk;

    /**
     * URI of the bookmarked query to fill the bean's properties from. Camel-case ignored, to keep the corresponding request
     * parameter lower-case.
     */
    private String fillfrom;

    /**
     * URI of the bookmarked query to fill the bean's properties from. Camel-case ignored, to keep the corresponding request
     * parameter lower-case. NB! The difference from "fillfrom" is that if "fillfrom" is specified, the properties are filled from
     * the bookmarked query, but the query is not executed! If "queryfrom" is specified, the properties are filled, and the query
     * executed!
     */
    private String queryfrom;

    /** Name of the query to bookmark. */
    private String bookmarkName;

    /**
     * List of bookmarked queries, each represented by a Map. Relevant only when user is known.
     */
    private List<Map<String, String>> bookmarkedQueries;
    
    /** */
    private List<String> deleteQueries;

    /**
     *
     * @return
     * @throws OpenRDFException
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution noEvent() throws OpenRDFException, DAOException {

        // If fillfrom is specified then fill the bean's properties from the
        // bookmarked query and forward to form page without executing the
        // query.
        // If queryfrom is specified then fill the bean's properties from the
        // bookmarked query and execute the query.
        // In other cases just execute the query.

        if (!StringUtils.isBlank(fillfrom)) {

            fillFromBookmark(fillfrom);
            addSystemMessage("Query filled from " + fillfrom);
            return new ForwardResolution(FORM_PAGE);

        } else {

            if (!StringUtils.isBlank(queryfrom)) {
                fillFromBookmark(queryfrom);
            }
            return execute();
        }
    }

    /**
     * Fills the bean's following properties from the bookmarked query: - the query itself - output format - hits per page - whether
     * to use inference.
     *
     * @throws DAOException
     */
    private void fillFromBookmark(String bookmarkedQueryUri) throws DAOException {

        SubjectDTO subjectDTO = DAOFactory.get().getDao(HelperDAO.class).getSubject(bookmarkedQueryUri);
        if (subjectDTO == null) {
            throw new DAOException("Could not find a subject with this URI: " + bookmarkedQueryUri);
        }

        this.query = subjectDTO.getObjectValue(Predicates.CR_SPARQL_QUERY);
        this.format = subjectDTO.getObjectValue(Predicates.DC_FORMAT);
        String flagString = subjectDTO.getObjectValue(Predicates.CR_USE_INFERENCE);
        this.useInferencing = Util.toBooolean(flagString);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution bookmark() throws DAOException {

        CRUser user = getUser();
        if (user == null) {
            addWarningMessage("Cannot bookmark for anonymous user!");
        } else if (StringUtils.isBlank(bookmarkName)) {
            addGlobalValidationError("Bookmark name is missing!");
        } else if (StringUtils.isBlank(query)) {
            addGlobalValidationError("Query is missing!");
        } else {

            // prepare bookmark subject

            String bookmarksUri = user.getBookmarksUri();
            String bookmarkUri = buildBookmarkUri(user, bookmarkName);

            SubjectDTO subjectDTO = new SubjectDTO(bookmarkUri, false);
            ObjectDTO objectDTO = new ObjectDTO(Subjects.CR_SPARQL_BOOKMARK, false);
            objectDTO.setSourceUri(bookmarksUri);
            subjectDTO.addObject(Predicates.RDF_TYPE, objectDTO);

            objectDTO = new ObjectDTO(query, true, XMLSchema.STRING);
            objectDTO.setSourceUri(bookmarksUri);
            subjectDTO.addObject(Predicates.CR_SPARQL_QUERY, objectDTO);

            String formatToUse = StringUtils.isBlank(format) ? "text/html" : format;
            objectDTO = new ObjectDTO(formatToUse, true, XMLSchema.STRING);
            objectDTO.setSourceUri(bookmarksUri);
            subjectDTO.addObject(Predicates.DC_FORMAT, objectDTO);

            objectDTO = new ObjectDTO(Boolean.toString(useInferencing), true, XMLSchema.BOOLEAN);
            objectDTO.setSourceUri(bookmarksUri);
            subjectDTO.addObject(Predicates.CR_USE_INFERENCE, objectDTO);

            objectDTO = new ObjectDTO(bookmarkName, true, XMLSchema.STRING);
            objectDTO.setSourceUri(bookmarksUri);
            subjectDTO.addObject(Predicates.RDFS_LABEL, objectDTO);

            // first remove previous triples of this bookmark (i.e. always overwrite bookmark with the same name)
            ArrayList<String> predicateUris = new ArrayList<String>();
            predicateUris.add(Predicates.RDF_TYPE);
            predicateUris.add(Predicates.CR_SPARQL_QUERY);
            predicateUris.add(Predicates.DC_FORMAT);
            predicateUris.add(Predicates.CR_USE_INFERENCE);
            predicateUris.add(Predicates.RDFS_LABEL);
            HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);
            dao.deleteTriples(bookmarkUri, predicateUris, bookmarksUri);
            
            
            // now save the bookmark subject
            dao.addTriples(subjectDTO);

            // store user folder in CR root home context
            if (!user.isHomeFolderRegistered()) {
                dao.registerUserFolderInCrHomeContext(user);
            }

            // log and display message about successful operation
            logger.debug("Query bookmarked with URI: " + bookmarksUri);
            addSystemMessage("Successfully bookmarked query: <a href=\"factsheet.action?uri="
                    + URLEncoder.encode(bookmarkUri) + "\">" + bookmarkUri + "</a>");
        }

        return new ForwardResolution(FORM_PAGE);
    }

    /**
     *
     * @return Resolution
     * @throws OpenRDFException
     */
    public Resolution execute() throws OpenRDFException {

        // if query is blank and there's also no such request parameter as query at all,
        // then assume user clicked the SPARQL client menu choice, and forward to the form page
        if (StringUtils.isBlank(query)) {

            Map paramsMap = getContext().getRequest().getParameterMap();
            boolean paramExists = paramsMap != null && paramsMap.containsKey("query");
            if (!paramExists) {
                return new ForwardResolution(FORM_PAGE);
            }
        }

        String acceptHeader = getContext().getRequest().getHeader("accept");
        String[] accept = { "" };
        if (acceptHeader != null && acceptHeader.length() > 0) {
            accept = acceptHeader.split(",");
            if (accept != null && accept.length > 0) {
                accept = accept[0].split(";");
            }
        }

        // Check if ASK query
        if (accept[0].equals("text/boolean") || isQuery("ASK")) {
            isAskQuery = true;
        }

        // Check if CONSTRUCT query
        if (accept[0].equals("application/x-trig") || isQuery("CONSTRUCT")) {
            isConstructQuery = true;
        }

        // If CONSTRUCT query, but output format is HTML then evaluate as simple
        // SELECT query
        if (isConstructQuery && format != null && format.equals("text/html")) {
            isConstructQuery = false;
        }

        if (!StringUtils.isBlank(format)) {
            accept[0] = format;
        }

        // If user has marked CR Inferencing checkbox,
        // then add inferencing command to the query
        newQuery = query;
        query = StringEscapeUtils.escapeHtml(query);
        if (useInferencing && !StringUtils.isBlank(query)) {
            String infCommand = "DEFINE input:inference '" + GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME)
                    + "'";

            // if inference command not yet present in the query, add it
            if (query.indexOf(infCommand) == -1) {
                newQuery = infCommand + "\n" + newQuery;
            }
        }

        if (isConstructQuery) {
            return new StreamingResolution("application/rdf+xml") {
                public void stream(HttpServletResponse response) throws Exception {
                    runQuery(newQuery, FORMAT_XML, response.getOutputStream());
                }
            };
        } else if (xmlFormats.contains(accept[0])) {
            return new StreamingResolution("application/sparql-results+xml") {
                public void stream(HttpServletResponse response) throws Exception {
                    runQuery(newQuery, FORMAT_XML, response.getOutputStream());
                }
            };
        } else if (accept[0].equals("application/sparql-results+json")) {
            return new StreamingResolution("application/sparql-results+json") {
                public void stream(HttpServletResponse response) throws Exception {
                    runQuery(newQuery, FORMAT_JSON, response.getOutputStream());
                }
            };
        } else {
            if (!StringUtils.isBlank(query)) {
                runQuery(newQuery, FORMAT_HTML, null);
            }
            return new ForwardResolution(FORM_PAGE);
        }
    }

    /**
     *
     * @param type
     * @return
     */
    private boolean isQuery(String type) {
        if (!StringUtils.isBlank(type) && !StringUtils.isBlank(query)) {
            StringTokenizer st = new StringTokenizer(query);
            while (st.hasMoreElements()) {
                String token = st.nextToken();
                if (token.toLowerCase().startsWith(type.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     *
     * @param query
     * @param format
     * @param out
     */
    private void runQuery(String query, String format, OutputStream out) {

        if (!StringUtils.isBlank(query)) {
            String url = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_URL);
            String username = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_ROUSR);
            String password = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_ROPWD);

            try {

                Repository myRepository = new VirtuosoRepository(url, username, password);
                myRepository.initialize();
                RepositoryConnection con = myRepository.getConnection();

                try {
                    // Evaluate ASK query
                    if (isAskQuery) {
                        BooleanQuery resultsTableBoolean = con.prepareBooleanQuery(QueryLanguage.SPARQL, query);
                        Boolean result = resultsTableBoolean.evaluate();

                        // ASK query in XML format
                        if (format != null && format.equals(FORMAT_XML)) {
                            OutputStreamWriter writer = new OutputStreamWriter(out);
                            writer.write("<?xml version=\"1.0\"?>");
                            writer.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
                            writer.write("<head></head>");
                            writer.write("<boolean>");
                            writer.write(result.toString());
                            writer.write("</boolean>");
                            writer.write("</sparql>");
                            writer.flush();
                            // ASK query in JSON format
                        } else if (format != null && format.equals(FORMAT_JSON)) {
                            OutputStreamWriter writer = new OutputStreamWriter(out);
                            writer.write("{  \"head\": { \"link\": [] }, \"boolean\": ");
                            writer.write(result.toString());
                            writer.write("}");
                            writer.flush();
                            // ASK query in HTML format
                        } else if (format != null && format.equals(FORMAT_HTML)) {
                            resultAsk = result.toString();
                        }
                        // Evaluate CONSTRUCT query. Returns XML format
                    } else if (isConstructQuery && !format.equals(FORMAT_HTML)) {
                        GraphQuery resultsTable = con.prepareGraphQuery(QueryLanguage.SPARQL, query);
                        RDFXMLWriter writer = new RDFXMLWriter(out);
                        resultsTable.evaluate(writer);
                        // Evaluate SELECT query
                    } else {
                        TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);

                        // Returns XML format
                        if (format != null && format.equals(FORMAT_XML)) {
                            CRXmlWriter sparqlWriter = new CRXmlWriter(out);
                            resultsTable.evaluate(sparqlWriter);
                            // Returns JSON format
                        } else if (format != null && format.equals(FORMAT_JSON)) {
                            CRJsonWriter sparqlWriter = new CRJsonWriter(out);
                            resultsTable.evaluate(sparqlWriter);
                            // Returns HTML format
                        } else if (format != null && format.equals(FORMAT_HTML)) {
                            long startTime = System.currentTimeMillis();
                            TupleQueryResult bindings = resultsTable.evaluate();
                            executionTime = System.currentTimeMillis() - startTime;
                            if (bindings != null) {
                                result = new QueryResult(bindings);
                            }
                        }
                    }
                } finally {
                    con.close();
                }
            } catch (RepositoryException rex) {
                rex.printStackTrace();
                addWarningMessage("Repository exception: '" + StringEscapeUtils.escapeHtml(rex.toString()) + "'");
                // throw new RuntimeException(rex.toString(), rex);
            } catch (Exception e) {
                e.printStackTrace();
                addWarningMessage("Error processing SPARQL: '" + StringEscapeUtils.escapeHtml(e.toString()) + "'");
                // throw new RuntimeException(e.toString(), e);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * 
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {
        
        Resolution resolution = new ForwardResolution(BOOKMARKED_QUERIES_PAGE);
        if (deleteQueries!=null && !deleteQueries.isEmpty()){
            
            System.out.println(deleteQueries);
        }
        return resolution;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query
     *            the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the result
     */
    public QueryResult getResult() {
        return result;
    }

    /**
     *
     * @return
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * @return
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param format
     */
    public void setFormat(String format) {
        this.format = format;
    }

    /**
     * @return
     */
    public int getNrOfHits() {

        if (nrOfHits == 0) {
            nrOfHits = DEFAULT_NUMBER_OF_HITS;
        }
        return nrOfHits;
    }

    /**
     * @param nrOfHits
     */
    public void setNrOfHits(int nrOfHits) {
        this.nrOfHits = nrOfHits;
    }

    /**
     * @return
     */
    public boolean isUseInferencing() {
        return useInferencing;
    }

    /**
     * @param useInferencing
     */
    public void setUseInferencing(boolean useInferencing) {
        this.useInferencing = useInferencing;
    }

    /**
     * @return
     */
    public String getResultAsk() {
        return resultAsk;
    }

    /**
     * @param resultAsk
     */
    public void setResultAsk(String resultAsk) {
        this.resultAsk = resultAsk;
    }

    /**
     * @return
     */
    public boolean isAskQuery() {
        return isAskQuery;
    }

    /**
     * @return the fillfrom
     */
    public String getFillfrom() {
        return fillfrom;
    }

    /**
     * @param fillfrom
     *            the fillfrom to set
     */
    public void setFillfrom(String fillfrom) {
        this.fillfrom = fillfrom;
    }

    /**
     * @param queryfrom
     *            the queryfrom to set
     */
    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    /**
     * @param bookmarkName
     *            the bookmarkName to set
     */
    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    /**
     * @return the bookmarkedQueries
     * @throws DAOException
     */
    public List<Map<String, String>> getBookmarkedQueries() throws DAOException {

        if (getUser() == null) {
            return null;
        } else if (bookmarkedQueries == null) {
            bookmarkedQueries = DAOFactory.get().getDao(HelperDAO.class).getSparqlBookmarks(getUser());
        }
        return bookmarkedQueries;
    }

    /**
     *
     * @param user
     * @param bookmarkName
     * @return
     */
    private static final String buildBookmarkUri(CRUser user, String bookmarkName) {

        return new StringBuilder(user.getBookmarksUri()).append("/").append(Hashes.spoHash(bookmarkName)).toString();
    }

    /**
     * @param deleteQueries the deleteQueries to set
     */
    public void setDeleteQueries(List<String> deleteQueries) {
        this.deleteQueries = deleteQueries;
    }
}
