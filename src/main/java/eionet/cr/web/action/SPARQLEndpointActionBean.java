package eionet.cr.web.action;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.export.ExportFormat;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.sparqlClient.helpers.CRJsonWriter;
import eionet.cr.web.sparqlClient.helpers.CRXmlSchemaWriter;
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
    private static final String FORMAT_XML_SCHEMA = "xml_schema";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_HTML = "html";
    private static final String FORMAT_HTML_PLUS = "html+";

    /** */
    private static final String FORM_PAGE = "/pages/sparqlClient.jsp";
    private static final String BOOKMARK_PAGE = "/pages/bookmarkQuery.jsp";

    /** */
    private static List<String> xmlFormats = new ArrayList<String>();

    /** Internal variable for HTTP error code. */
    private int errorCode;

    /** Error message to be returned to external client. */
    private String errorMessage;

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

    private static final String DEFAULT_GRAPH_URI = "default-graph-uri";

    private static final String NAMED_GRAPH_URI = "named-graph-uri";

    /** */
    private String query;
    private String format;
    private int nrOfHits;
    private long executionTime;
    private String[] defaultGraphUris;
    private String[] namedGraphUris;

    // CONSTRUCT query to HOMESPACE variables
    private String exportType;
    private String datasetName;
    private String folder;
    private boolean overwriteDataset;

    /** */
    private boolean isAskQuery;
    private boolean useInferencing;

    /** */
    private QueryResult result;
    private String resultAsk;

    private SPARQLParser parser = new SPARQLParser();

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

    /** Selected query bookmark name. */
    private String selectedBookmarkName;

    /** True, if the query to bookmark will be shared. */
    private boolean sharedBookmark;

    /**
     * List of bookmarked queries, each represented by a Map. Relevant only when user is known.
     */
    private List<Map<String, String>> bookmarkedQueries;

    /**
     * List of shared bookmarked queries, each represented by a Map. Relevant only when user is known.
     */
    private List<Map<String, String>> sharedBookmarkedQueries;

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
            setGraphUri();

            fillFromBookmark(fillfrom);
            // addSystemMessage("Query filled from " + fillfrom);
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
        this.bookmarkName = subjectDTO.getObjectValue(Predicates.RDFS_LABEL);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution bookmark() throws DAOException {

        setGraphUri();

        CRUser user = getUser();
        String requestMethod = getContext().getRequest().getMethod();

        if (user == null) {
            addWarningMessage("Cannot bookmark for anonymous user!");
        } else if (StringUtils.isBlank(query)) {
            addGlobalValidationError("Query is missing!");
        } else if (requestMethod.equalsIgnoreCase("get")) {
            return new ForwardResolution(BOOKMARK_PAGE);
        } else if (StringUtils.isBlank(bookmarkName)) {
            addGlobalValidationError("Bookmark name is missing!");
        } else {
            if (sharedBookmark) {
                if (!isSharedBookmarkPrivilege()) {
                    addGlobalValidationError("No privilege to update shared SPARQL bookmark.");
                    return new ForwardResolution(FORM_PAGE);
                }
                storeSharedBookmark();
            } else {
                storePersonalBookmark();
            }
            // log and display message about successful operation
            addSystemMessage("Successfully bookmarked query: " + bookmarkName);
            selectedBookmarkName = bookmarkName;
        }

        return new ForwardResolution(FORM_PAGE);
    }

    /**
     * Stores user's SPARQL bookmark.
     *
     * @throws DAOException
     */
    private void storePersonalBookmark() throws DAOException {
        CRUser user = getUser();
        // prepare bookmark subject
        String bookmarksUri = user.getBookmarksUri();
        String bookmarkUri = buildBookmarkUri(bookmarksUri, bookmarkName);

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

        List<String> subjectUris = Collections.singletonList(bookmarkUri);
        List<String> sourceUris = Collections.singletonList(bookmarksUri);

        HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);
        dao.deleteSubjectPredicates(subjectUris, predicateUris, sourceUris);

        // now save the bookmark subject
        dao.addTriples(subjectDTO);
        logger.debug("Query bookmarked with URI: " + bookmarksUri);
    }

    /**
     * Stores shared SPARQL bookmark.
     *
     * @throws DAOException
     */
    private void storeSharedBookmark() throws DAOException {

        // prepare bookmark subject
        String bookmarksUri = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL) + "/sparqlbookmarks";
        String bookmarkUri = buildBookmarkUri(bookmarksUri, bookmarkName);

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

        List<String> subjectUris = Collections.singletonList(bookmarkUri);
        List<String> sourceUris = Collections.singletonList(bookmarksUri);

        HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);
        dao.deleteSubjectPredicates(subjectUris, predicateUris, sourceUris);

        // now save the bookmark subject
        dao.addTriples(subjectDTO);
        logger.debug("Query bookmarked with URI: " + bookmarksUri);
    }

    /**
     *
     * @return Resolution
     * @throws OpenRDFException
     */
    public Resolution execute() throws OpenRDFException {
        setGraphUri();
        // if query is blank and there's also no such request parameter as query at all,
        // then assume user clicked the SPARQL client menu choice, and forward to the form page
        if (StringUtils.isBlank(query)) {

            Map paramsMap = getContext().getRequest().getParameterMap();
            boolean paramExists = paramsMap != null && paramsMap.containsKey("query");
            if (!paramExists) {
                if (isWebBrowser()) {
                    return new ForwardResolution(FORM_PAGE);
                } else {

                    return new ErrorStreamingResolution(HttpServletResponse.SC_BAD_REQUEST, "Parameter 'query'"
                            + "is missing in the request");

                }
            }
        }

        String acceptHeader = getContext().getRequest().getHeader("accept");
        String[] accept = {""};
        if (acceptHeader != null && acceptHeader.length() > 0) {
            accept = acceptHeader.split(",");
            if (accept != null && accept.length > 0) {
                accept = accept[0].split(";");
            }
        }

        if (!StringUtils.isBlank(format)) {
            accept[0] = format;
        }

        // If user has marked CR Inferencing checkbox,
        // then add inferencing command to the query
        Resolution resolution = null;
        if (useInferencing && !StringUtils.isBlank(query)) {
            String infCommand =
                "DEFINE input:inference '" + GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME) + "'";

            // if inference command not yet present in the query, add it
            if (query.indexOf(infCommand) == -1) {
                query = infCommand + "\n" + query;
            }
        }

        if (xmlFormats.contains(accept[0])) {
            if (exportType != null && exportType.equals("HOMESPACE")) {
                // Export result to user homespace
                String dataset = folder + "/" + StringUtils.replace(datasetName, " ", "%20");
                int nrOfTriples = exportToHomespace(dataset);
                if (nrOfTriples > 0) {
                    resolution = new RedirectResolution(FactsheetActionBean.class).addParameter("uri", dataset);
                } else {
                    resolution = new ForwardResolution(FORM_PAGE);
                }
            } else {
                resolution = new StreamingResolution("application/sparql-results+xml") {
                    @Override
                    public void stream(HttpServletResponse response) throws Exception {
                        response.setHeader("filename", "sparql-result.xml");
                        runQuery(query, FORMAT_XML, response.getOutputStream());
                    }
                };
                ((StreamingResolution) resolution).setFilename("sparql-result.xml");
            }
        } else if (accept[0].equals("application/x-ms-access-export+xml")) {
            resolution = new StreamingResolution("application/x-ms-access-export+xml") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    runQuery(query, FORMAT_XML_SCHEMA, response.getOutputStream());
                }
            };
            ((StreamingResolution) resolution).setFilename("sparql-result.xml");
        } else if (accept[0].equals("application/sparql-results+json")) {
            resolution = new StreamingResolution("application/sparql-results+json") {
                @Override
                public void stream(HttpServletResponse response) throws Exception {
                    runQuery(query, FORMAT_JSON, response.getOutputStream());
                }
            };
            ((StreamingResolution) resolution).setFilename("sparql-result.json");
        } else {
            if (!StringUtils.isBlank(query)) {
                if (accept[0].equals("text/html+")) {
                    runQuery(query, FORMAT_HTML_PLUS, null);
                } else {
                    runQuery(query, FORMAT_HTML, null);
                }
            }
            resolution = new ForwardResolution(FORM_PAGE);
        }

        if (!isWebBrowser() && errorCode != 0) {
            return new ErrorStreamingResolution(errorCode, errorMessage);
        }
        return resolution;
    }

    /**
     * Gets the default-graph-uri and named-graph-uri parameters from request and stores them into ActionBean properties.
     */
    private void setGraphUri() {
        defaultGraphUris = getContext().getRequest().getParameterValues(DEFAULT_GRAPH_URI);
        namedGraphUris = getContext().getRequest().getParameterValues(NAMED_GRAPH_URI);
    }

    /**
     * Checks if user has update rights to "sparqlbookmarks" ACL.
     *
     * @return true, if user can add/delete shared SPARQL bookmars.
     */
    public boolean isSharedBookmarkPrivilege() {
        if (getUser() != null && CRUser.hasPermission(getContext().getRequest().getSession(), "/sparqlbookmarks", "u")) {
            return true;
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
            RepositoryConnection con = null;
            try {
                con = SesameConnectionProvider.getReadOnlyRepositoryConnection();

                Query queryObject = con.prepareQuery(QueryLanguage.SPARQL, query);
                SesameUtil.setDatasetParameters(queryObject, con, defaultGraphUris, namedGraphUris);

                TupleQueryResult queryResult = null;
                try {
                    // Evaluate ASK query
                    // if (isAskQuery) {
                    if (queryObject instanceof BooleanQuery) {

                        isAskQuery = true;
                        Boolean rslt = ((BooleanQuery) queryObject).evaluate();

                        // ASK query in XML format
                        if (format != null && format.equals(FORMAT_XML)) {
                            OutputStreamWriter writer = new OutputStreamWriter(out);
                            writer.write("<?xml version=\"1.0\"?>");
                            writer.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
                            writer.write("<head></head>");
                            writer.write("<boolean>");
                            writer.write(rslt.toString());
                            writer.write("</boolean>");
                            writer.write("</sparql>");
                            writer.flush();
                        } else if (format != null && format.equals(FORMAT_XML_SCHEMA)) {
                            OutputStreamWriter writer = new OutputStreamWriter(out);
                            writer.write("<?xml version=\"1.0\"?>");
                            writer.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
                            writer.write("<head></head>");
                            writer.write("<boolean>");
                            writer.write(rslt.toString());
                            writer.write("</boolean>");
                            writer.write("</sparql>");
                            writer.flush();
                            // ASK query in JSON format
                        } else if (format != null && format.equals(FORMAT_JSON)) {
                            OutputStreamWriter writer = new OutputStreamWriter(out);
                            writer.write("{  \"head\": { \"link\": [] }, \"boolean\": ");
                            writer.write(rslt.toString());
                            writer.write("}");
                            writer.flush();
                            // ASK query in HTML format
                        } else if (format != null && format.equals(FORMAT_HTML)) {
                            resultAsk = rslt.toString();
                        }
                        // Evaluate CONSTRUCT query. Returns XML format
                        // } else if (isConstructQuery && !format.equals(FORMAT_HTML)) {
                    } else if (queryObject instanceof GraphQuery) {
                        if (!format.equals(FORMAT_HTML)) {
                            RDFXMLWriter writer = new RDFXMLWriter(out);
                            ((GraphQuery) queryObject).evaluate(writer);
                        } else {
                            long startTime = System.currentTimeMillis();
                            TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
                            TupleQueryResult bindings = resultsTable.evaluate();
                            executionTime = System.currentTimeMillis() - startTime;
                            if (bindings != null) {
                                result = new QueryResult(bindings, false);
                            }
                        }
                        // Evaluate SELECT query
                    } else {
                        // Returns XML format
                        if (format != null && format.equals(FORMAT_XML)) {
                            CRXmlWriter sparqlWriter = new CRXmlWriter(out);
                            ((TupleQuery) queryObject).evaluate(sparqlWriter);
                            // Returns XML format
                        } else if (format != null && format.equals(FORMAT_XML_SCHEMA)) {
                            CRXmlSchemaWriter sparqlWriter = new CRXmlSchemaWriter(out);
                            ((TupleQuery) queryObject).evaluate(sparqlWriter);
                            // Returns JSON format
                        } else if (format != null && format.equals(FORMAT_JSON)) {
                            CRJsonWriter sparqlWriter = new CRJsonWriter(out);
                            ((TupleQuery) queryObject).evaluate(sparqlWriter);
                            // Returns HTML format
                        } else if (format != null && format.equals(FORMAT_HTML)) {
                            long startTime = System.currentTimeMillis();
                            queryResult = ((TupleQuery) queryObject).evaluate();

                            executionTime = System.currentTimeMillis() - startTime;
                            if (queryResult != null) {
                                result = new QueryResult(queryResult, false);
                            }
                            // Returns HTML+ format
                        } else if (format != null && format.equals(FORMAT_HTML_PLUS)) {
                            long startTime = System.currentTimeMillis();

                            queryResult = ((TupleQuery) queryObject).evaluate();

                            executionTime = System.currentTimeMillis() - startTime;
                            if (queryResult != null) {
                                result = new QueryResult(queryResult, true);
                            }
                        }
                    }
                } finally {
                    SesameUtil.close(queryResult);
                }
            } catch (Exception e) {
                e.printStackTrace();
                addWarningMessage("Encountered exception: " + e.toString());

                //Syntax error in query: http code - 400 - check query syntax with sesame parser
                handleVirtuosoError(e, query);

            } finally {
                try {
                    if (out != null) {
                        out.close();
                    }
                    SesameUtil.close(con);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Export CONSTRUCT query result to user homespace
    public int exportToHomespace(String dataset) {
        int nrOfTriples = 0;
        try {
            // Maximum Rows count
            int maxRowsCount = GeneralConfig.getIntProperty(GeneralConfig.SPARQLENDPOINT_MAX_ROWS_COUNT, 2000);

            nrOfTriples =
                DAOFactory.get().getDao(HelperDAO.class)
                .addTriples(query, dataset, defaultGraphUris, namedGraphUris, maxRowsCount);

            if (nrOfTriples > 0) {
                // prepare and insert cr:hasFile predicate
                ObjectDTO objectDTO = new ObjectDTO(dataset, false);
                objectDTO.setSourceUri(getUser().getHomeUri());
                SubjectDTO homeSubjectDTO = new SubjectDTO(folder, false);
                homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(homeSubjectDTO);

                // Create source
                DAOFactory.get().getDao(HarvestSourceDAO.class)
                .addSourceIgnoreDuplicate(HarvestSourceDTO.create(dataset, false, 0, getUserName()));

                // Insert last modified predicate
                DAOFactory
                .get()
                .getDao(HarvestSourceDAO.class)
                .insertUpdateSourceMetadata(dataset, Predicates.CR_LAST_MODIFIED,
                        ObjectDTO.createLiteral(Util.virtuosoDateToString(new Date()), XMLSchema.DATETIME));

                // Insert harvested statements predicate
                DAOFactory
                .get()
                .getDao(HarvestSourceDAO.class)
                .insertUpdateSourceMetadata(dataset, Predicates.CR_HARVESTED_STATEMENTS,
                        ObjectDTO.createLiteral(String.valueOf(nrOfTriples), XMLSchema.INTEGER));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nrOfTriples;
    }

    /**
     * @throws DAOException
     */
    @ValidationMethod(on = {"execute"})
    public void validateSaveEvent() throws DAOException {

        // the below validation is relevant only when exported to HOMESPACE
        if (exportType == null || !exportType.equals("HOMESPACE")) {
            return;
        }

        // user must be authorized
        if (exportType != null && exportType.equals("HOMESPACE") && getUser() == null) {
            addGlobalValidationError("User not logged in!");
            return;
        }

        // no folder selected
        if (StringUtils.isBlank(folder)) {
            addGlobalValidationError("Folder not selected!");
        }

        // no folder selected
        if (StringUtils.isBlank(datasetName)) {
            addGlobalValidationError("Dataset name is mandatory!");
        }

        // Check if file already exists
        if (!overwriteDataset && !StringUtils.isBlank(datasetName) && !StringUtils.isBlank(folder)) {
            String datasetUri = folder + "/" + StringUtils.replace(datasetName, " ", "%20");
            boolean exists = DAOFactory.get().getDao(FolderDAO.class).fileOrFolderExists(datasetUri);
            if (exists) {
                addGlobalValidationError("File named \"" + datasetName + "\" already exists in folder \"" + folder + "\"!");
            }
        }

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {
            Resolution resolution = new ForwardResolution(FORM_PAGE);
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     * Deletes shared bookmark.
     *
     * @return
     * @throws DAOException
     */
    public Resolution deleteSharedBookmark() throws DAOException {
        return deleteBookmarked(true);
    }

    /**
     * Deletes personal bookmark.
     *
     * @return
     * @throws DAOException
     */
    public Resolution deletePersonalBookmark() throws DAOException {
        return deleteBookmarked(false);
    }

    /**
     * Deletes SPARQL bookmarked query from triplestore.
     *
     * @param deleteSharedBookmark true, if shared bookmark will be deleted, otherwise personal bookmark
     * @return
     * @throws DAOException
     */
    private Resolution deleteBookmarked(boolean deleteSharedBookmark) throws DAOException {

        setGraphUri();

        CRUser user = getUser();
        if (user == null) {
            addWarningMessage("Operation now allowed for anonymous users!");
            return new ForwardResolution(FORM_PAGE);
        }

        // Resolution resolution = new ForwardResolution(BOOKMARKED_QUERIES_PAGE);
        Resolution resolution = new ForwardResolution(FORM_PAGE);
        if (deleteQueries != null && !deleteQueries.isEmpty()) {

            logger.debug("Delete shared: " + deleteSharedBookmark);
            logger.debug("Deleting these bookmarked queries: " + deleteQueries);

            List<String> sourceUris = new ArrayList<String>();
            if (deleteSharedBookmark) {
                if (!isSharedBookmarkPrivilege()) {
                    addGlobalValidationError("No privilege to update shared SPARQL bookmark.");
                    return new ForwardResolution(FORM_PAGE);
                }
                sourceUris.add(GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL) + "/sparqlbookmarks");
            } else {
                sourceUris.add(user.getBookmarksUri());
            }
            DAOFactory.get().getDao(HelperDAO.class).deleteSubjectPredicates(deleteQueries, null, sourceUris);

            if (deleteQueries.size() == 1) {
                addSystemMessage("Selected query was successfully deleted!");
            } else {
                addSystemMessage("Selected queries were successfully deleted!");
            }
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
     * @param query the query to set
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
     * @param fillfrom the fillfrom to set
     */
    public void setFillfrom(String fillfrom) {
        this.fillfrom = fillfrom;
    }

    /**
     * @param queryfrom the queryfrom to set
     */
    public void setQueryfrom(String queryfrom) {
        this.queryfrom = queryfrom;
    }

    /**
     * @param bookmarkName the bookmarkName to set
     */
    public void setBookmarkName(String bookmarkName) {
        this.bookmarkName = bookmarkName;
    }

    /**
     * Returns bookmarked queries.
     *
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
     * Returns shared bookmarked queries.
     *
     * @return
     * @throws DAOException
     */
    public List<Map<String, String>> getSharedBookmarkedQueries() throws DAOException {

        if (sharedBookmarkedQueries == null) {
            sharedBookmarkedQueries = DAOFactory.get().getDao(HelperDAO.class).getSharedSparqlBookmarks();
        }
        return sharedBookmarkedQueries;
    }

    /**
     * Returns bookmark's uri.
     *
     * @param bookmarkGraphUri
     * @param bookmarkName
     * @return
     */
    private String buildBookmarkUri(String bookmarkGraphUri, String bookmarkName) {

        return new StringBuilder(bookmarkGraphUri).append("/").append(Hashes.spoHash(bookmarkName)).toString();
    }

    /**
     * @param deleteQueries the deleteQueries to set
     */
    public void setDeleteQueries(List<String> deleteQueries) {
        this.deleteQueries = deleteQueries;
    }

    /**
     * @return the bookmarkName
     */
    public String getBookmarkName() {
        return bookmarkName;
    }

    /**
     * @return a list of export formats.
     */
    public ExportFormat[] getExportFormats() {
        return ExportFormat.values();
    }

    /**
     * @return the defaultGraphUri
     */
    public String[] getDefaultGraphUris() {
        return defaultGraphUris;
    }

    /**
     * @return the namedGraphUri
     */
    public String[] getNamedGraphUris() {
        return namedGraphUris;
    }

    /**
     * @return List<String> - user home folders
     */
    public List<String> getFolders() {
        List<String> ret = new ArrayList<String>();
        if (getUser() != null) {
            try {
                ret = FolderUtil.getUserAccessibleFolders(getUser());
            } catch (DAOException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isOverwriteDataset() {
        return overwriteDataset;
    }

    public void setOverwriteDataset(boolean overwriteDataset) {
        this.overwriteDataset = overwriteDataset;
    }

    /**
     * @return the sharedBookmark
     */
    public boolean isSharedBookmark() {
        return sharedBookmark;
    }

    /**
     * @param sharedBookmark the sharedBookmark to set
     */
    public void setSharedBookmark(boolean sharedBookmark) {
        this.sharedBookmark = sharedBookmark;
    }

    /**
     * @return the selectedBookmarkName
     */
    public String getSelectedBookmarkName() {
        return selectedBookmarkName;
    }

    /**
     * @param selectedBookmarkName the selectedBookmarkName to set
     */
    public void setSelectedBookmarkName(String selectedBookmarkName) {
        this.selectedBookmarkName = selectedBookmarkName;
    }
    /**
     * checks the exception returned by Virtuoso and sets error code and message.
     * @param virtuosoException Virtuoso JDBC exception
     * @param sparql SPARQL query
     */
    private void handleVirtuosoError(Exception virtuosoException, String sparql) {
        errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        errorMessage = virtuosoException.getMessage();

        try {
            //RepositoryConnection.prepareQuery() does not throw MalformedQueryException for some reason
            //query is parsed to throw malformedQueryException and return correct HTTP code
            parser.parseQuery(sparql, GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL) + "/sparql");

        } catch (Exception e) {
            //check also with Sesame parser if SPARQL was invalid and Virtuosos has failed because of invalid query
            //then parse error message
            //if SPARQL dos not include any SPARQL keywords "SELECT", "ASK" etc ClassCastException is thrown
            if (virtuosoException instanceof ClassCastException
                    || (e instanceof MalformedQueryException && virtuosoException.getMessage().indexOf("syntax error") != -1)) {
                addWarningMessage("Invalid SPARQL: " + e.getMessage());
                errorCode = HttpServletResponse.SC_BAD_REQUEST;

                //if sparql does not contain any keywords: SELECT, ASK etc throws ClassCastExcpetion without explanation
                if (virtuosoException instanceof ClassCastException) {
                    errorMessage += " Invalid SPARQL: " + sparql;
                }
            }
        }
    }
}
