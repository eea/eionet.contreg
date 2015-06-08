package eionet.cr.web.action;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.*;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.export.ExportFormat;
import eionet.cr.util.sesame.SPARQLQueryUtil;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.interceptor.annotation.DontSaveLastActionEvent;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.sparqlClient.helpers.*;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.util.CRSPARQLCSVWriter;
import eionet.cr.web.util.CRSPARQLTSVWriter;
import eionet.cr.web.util.ServletOutputLazyStream;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.*;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.n3.N3WriterFactory;
import org.openrdf.rio.ntriples.NTriplesWriterFactory;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.openrdf.rio.turtle.TurtleWriterFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

/**
 *
 * @author altnyris
 *
 */
@UrlBinding("/sparql")
public class SPARQLEndpointActionBean extends AbstractActionBean {

    /** Message to notify about missing administrator permissions */
    public static final String VALIDATION_ERROR_MUST_BE_ADMINISTRATOR =
            "Must have administrator permissions to insert bulk sources through Sparql endpoint";

    /** */
    private static final Logger LOGGER = Logger.getLogger(SPARQLEndpointActionBean.class);

    /** */
    private static final int DEFAULT_NUMBER_OF_HITS = 20;

    /** */
    private static final String DEFAULT_STREAMING_RESPONSE_MIME_TYPE = "application/sparql-results+xml";

    /** endpoint output file name. */
    private static final String DEFAULT_SPARQL_FILENAME = "sparql-result";

    /** The endpoint's internal conventional output formats. */
    private static final String FORMAT_XML = "xml";
    private static final String FORMAT_XML_SCHEMA = "xml_schema";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_CSV = "csv";
    private static final String FORMAT_TSV = "tsv";
    private static final String FORMAT_HTML = "html";
    private static final String FORMAT_HTML_PLUS = "html+";
    private static final String FORMAT_N3 = "n3";
    private static final String FORMAT_TURTLE = "turtle";
    private static final String FORMAT_NTRIPLES = "ntriples";

    /** A set of all supported MIME types requested in "Accept" header. */
    private static final HashSet<String> SUPPORTED_MIME_TYPES = createSupportedMimeTypes();

    /** Supported MIME types mapped to the corresponding internal format names. */
    private static final Map<String, String> STREAMING_MIME_TYPES_TO_INTERNAL_FORMATS = createMimeTypesToInternalFormats();

    /** */
    private static final String FORM_PAGE = "/pages/sparqlClient.jsp";
    private static final String BOOKMARK_PAGE = "/pages/bookmarkQuery.jsp";
    private static final String AJAX_PAGE = "/pages/sparqlClientAjax.jsp";

    /**
     * Option value in the folder dropdown for Shared bookmarks.
     */
    private final static String SHARED_BOOKMARKS_FOLDER = "_shared_bookmarks";

    /**
     * Option value in the folder dropdown for my bookmarks.
     */
    private final static String MY_BOOKMARKS_FOLDER = "_my_bookmarks";

    /** The request parameter name for the default graph URI. */
    private static final String DEFAULT_GRAPH_URI = "default-graph-uri";

    /** The request parameter name for the names graph URI. */
    private static final String NAMED_GRAPH_URI = "named-graph-uri";

    /** Internal variable for HTTP error code. */
    private int errorCode;

    /** HTTP status error message to be returned to external client. */
    private String errorMessage;

    /** Defines AJAX component to be loaded */
    private Integer ajaxRequestId = 0;

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
    private boolean useOwlSameAs;

    /** */
    private QueryResult result;
    private String resultAsk;

    /** */
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

    /**
     * List of bookmarked queries, each represented by a Map. Relevant only when user is known.
     */
    private List<Map<String, String>> bookmarkedQueries;

    /**
     * List of shared bookmarked queries, each represented by a Map. Relevant only when user is known.
     */
    private List<Map<String, String>> sharedBookmarkedQueries;

    /**
     * List of project bookmarked queries, each represented by a Map. Relevant only if the user is known and has View permission to
     * some project.
     */
    private List<Map<String, String>> projectBookmarkedQueries;

    /** */
    private List<String> deleteQueries;

    /** Projects for the user with insert permission. */
    private List<String> userProjects;

    /** Selected project for the bookmark. */
    private String bookmarkFolder;

    /** Keeps the state of the bulk actions visibility across requests */
    private boolean displayBulkActions;

    /**
     *
     * @return
     * @throws OpenRDFException
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution noEvent() throws OpenRDFException, DAOException {

        // If "fillfrom" is specified then fill the bean from bookmarked query and send to form page without executing the query.
        // If "queryfrom" is specified then fill the bean from bookmarked query and execute the query.
        // In all other cases just execute the query.

        if (!StringUtils.isBlank(fillfrom)) {

            setDefaultAndNamedGraphs();
            fillFromBookmark(fillfrom);
            return new ForwardResolution(FORM_PAGE);
        } else {
            if (!StringUtils.isBlank(queryfrom)) {
                fillFromBookmark(queryfrom);
            }
            return execute();
        }
    }

    /**
     * Fills the bean's following properties from the bookmarked query:
     * - the query itself - output format - hits per page - whether
     * to use same-as "yes".
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
        String flagString = subjectDTO.getObjectValue(Predicates.CR_USE_OWLSAMEAS);
        this.useOwlSameAs = Util.toBooolean(flagString);
        this.bookmarkName = subjectDTO.getObjectValue(Predicates.RDFS_LABEL);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    @HandlesEvent("ajaxrequest")
    @DontSaveLastActionEvent
    public Resolution ajaxrequest() throws DAOException {
        return new ForwardResolution(AJAX_PAGE);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution bookmark() throws DAOException {
        setDefaultAndNamedGraphs();
        CRUser user = getUser();
        userProjects = FolderUtil.getUserAccessibleProjectFolderNames(user, "i");

        if (user == null) {
            addWarningMessage("Cannot bookmark for anonymous user!");
        } 
        else if (StringUtils.isBlank(query)) {
            addGlobalValidationError("Query is missing!");
        }  
        else {
            return new ForwardResolution(BOOKMARK_PAGE);
        }

        return new ForwardResolution(FORM_PAGE);
    }
    
    public Resolution saveBookmark() throws DAOException {
        setDefaultAndNamedGraphs();
        CRUser user = getUser();
        
        if (user == null) {
            addWarningMessage("Cannot bookmark for anonymous user!");
        }
        else if (StringUtils.isBlank(query)) {
            addGlobalValidationError("Query is missing!");
        }
        else if (StringUtils.isBlank(bookmarkName)) {
            addGlobalValidationError("Bookmark name is missing!");
        }
        else {
            if (bookmarkFolder != null && bookmarkFolder.equals(SHARED_BOOKMARKS_FOLDER)) {
                if (!isSharedBookmarkPrivilege()) {
                    addGlobalValidationError("No privilege to update shared SPARQL bookmark.");
                    return new ForwardResolution(FORM_PAGE);
                }
                
                storeSharedBookmark();
                // store to project folder
            } 
            else if (bookmarkFolder != null && !bookmarkFolder.equals(MY_BOOKMARKS_FOLDER)) {
                // bookmarkFolder = project name
                if (!hasProjectPrivilege(bookmarkFolder)) {
                    addGlobalValidationError("No privilege to add SPARQL bookmark to the selected project.");
                    return new ForwardResolution(FORM_PAGE);
                }
                
                storeProjectBookmark();
            } 
            else {
                storePersonalBookmark();
            }
            // log and display message about successful operation
            addSystemMessage("Successfully bookmarked query: " + bookmarkName);
            selectedBookmarkName = bookmarkName;
        }
        
        return new ForwardResolution(FORM_PAGE);
    }

    /**
     * common method for user and project bookmark.
     *
     * @param bookmarksUri
     *            full url for the parent bookmark folder
     * @throws DAOException
     *             if db operation fails
     */
    private void storeBookmark(String bookmarksUri) throws DAOException {
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

        objectDTO = new ObjectDTO(Boolean.toString(useOwlSameAs), true, XMLSchema.BOOLEAN);
        objectDTO.setSourceUri(bookmarksUri);
        subjectDTO.addObject(Predicates.CR_USE_OWLSAMEAS, objectDTO);

        objectDTO = new ObjectDTO(bookmarkName, true, XMLSchema.STRING);
        objectDTO.setSourceUri(bookmarksUri);
        subjectDTO.addObject(Predicates.RDFS_LABEL, objectDTO);

        // first remove previous triples of this bookmark (i.e. always overwrite bookmark with the same name)
        ArrayList<String> predicateUris = new ArrayList<String>();
        predicateUris.add(Predicates.RDF_TYPE);
        predicateUris.add(Predicates.CR_SPARQL_QUERY);
        predicateUris.add(Predicates.DC_FORMAT);
        predicateUris.add(Predicates.CR_USE_OWLSAMEAS);
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
     * Stores project bookmark.
     *
     * @throws DAOException
     *             if query fails
     */
    private void storeProjectBookmark() throws DAOException {
        String bookmarksUri = FolderUtil.getProjectFolder(getBookmarkFolder()) + "/bookmarks";

        // create bookmarks folder for the project if it does not exist
        FolderDAO dao = DAOFactory.get().getDao(FolderDAO.class);
        if (!dao.fileOrFolderExists("bookmarks", FolderUtil.getProjectFolder(getBookmarkFolder()))) {
            // dao.createFolder(parentFolderUri, folderName, folderLabel, homeUri);
            dao.createProjectBookmarksFolder(getBookmarkFolder());
        }

        storeBookmark(bookmarksUri);
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
        storeBookmark(bookmarksUri);

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

        objectDTO = new ObjectDTO(Boolean.toString(useOwlSameAs), true, XMLSchema.BOOLEAN);
        objectDTO.setSourceUri(bookmarksUri);
        subjectDTO.addObject(Predicates.CR_USE_OWLSAMEAS, objectDTO);

        objectDTO = new ObjectDTO(bookmarkName, true, XMLSchema.STRING);
        objectDTO.setSourceUri(bookmarksUri);
        subjectDTO.addObject(Predicates.RDFS_LABEL, objectDTO);

        // first remove previous triples of this bookmark (i.e. always overwrite bookmark with the same name)
        ArrayList<String> predicateUris = new ArrayList<String>();
        predicateUris.add(Predicates.RDF_TYPE);
        predicateUris.add(Predicates.CR_SPARQL_QUERY);
        predicateUris.add(Predicates.DC_FORMAT);
        predicateUris.add(Predicates.CR_USE_OWLSAMEAS);
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
     * Executes the SparqlEndpoint query
     *
     * @return Resolution
     */
    public Resolution execute() {

        Resolution resolution = executeSparqlQuery();

        return resolution;
    }

    /**
     * Executes the SparqlEndpoint query and adds result to sources.
     *
     * @return Resolution to return to.
     * @throws DAOException If DAO access error.
     */
    public Resolution executeAddSources() throws DAOException {
        Resolution resolution = executeSparqlQuery(false);

        if (isAdminPrivilege()) {
            String resultValidation = QueryResultValidator.isProperBulkSourceResult(result);
            if (resultValidation.equals(QueryResultValidator.PROPER_BULK_SOURCE_OK)) {
                DAOFactory.get().getDao(HarvestSourceDAO.class).addBulkSourcesFromSparql(result);

                addSystemMessage(result.getRows().size() + " sources scheduled for urgent batch harvest!");
                LOGGER.info("Successfully added " + result.getRows().size() + " sources from Sparql query.");
            } else {
                addGlobalValidationError(resultValidation);
                LOGGER.info("Sparql endpoint add bulk sources validation error: " + resultValidation);
            }
        } else {
            addGlobalValidationError(VALIDATION_ERROR_MUST_BE_ADMINISTRATOR);
            LOGGER.info("Sparql endpoint add bulk sources validation error: Must have administrator permissions to insert bulk "
                    + "sources through Sparql endpoint");
        }

        return resolution;
    }

    /**
     * Schedules background deletion for sources identified by URLs returned in the 1st column of executed SPARQL results.
     *
     * @return Resolution to return to.
     * @throws DAOException If DAO access error.
     */
    public Resolution executeDeleteSources() throws DAOException {

        Resolution resolution = new ForwardResolution(FORM_PAGE);
        if (!isAdminPrivilege()) {
            addWarningMessage("You are not authorized for this operation!");
        } else if (StringUtils.isBlank(query)) {
            addCautionMessage("Found no SPARQL query in the reqeust!");
        } else {
            int markedCount = DAOFactory.get().getDao(SourceDeletionsDAO.class).markForDeletionSparql(query);
            addSystemMessage("A total of " + markedCount + " sources were scheduled for background deletion!");
        }

        return resolution;
    }

    /**
     *
     *
     * @return
     */
    private Resolution executeSparqlQuery() {
        return executeSparqlQuery(true);
    }

    /**
     *
     *
     * @return
     */
    private Resolution executeSparqlQuery(boolean limitResultCount) {
        // If query is blank then send HTTP 400 if not a browser, otherwise take the browser to the SPARQL endpoint form page.
        if (StringUtils.isBlank(query)) {
            if (isWebBrowser()) {
                return new ForwardResolution(FORM_PAGE);
            } else {
                return new ErrorStreamingResolution(HttpServletResponse.SC_BAD_REQUEST,
                        "Query missing or blank in request parameters");
            }
        }

        // Set the default-graph-uri and named-graph-uri (see SPARQL protocol specifications).
        setDefaultAndNamedGraphs();

        // If user has requested use of same-as "yes", then ensure that the relevant command is present in the query.
        String sameasyesCommand = SPARQLQueryUtil.getCrOwlSameAsDefinitionStr();
        if (useOwlSameAs) {
            if (query.indexOf(sameasyesCommand) == -1) {
                query = sameasyesCommand + "\n" + query;
            }
        } else {
            if (query.contains(sameasyesCommand)) {
                query = query.replace(sameasyesCommand, "");
            }
        }

        // Content negotiation: prefer the value of "format" request parameter. If it's blank, fall back to the request's "Accept"
        // header. If that one is blank too, fall back to the default.
        // TODO Currently the header's parsing does not parse quality weights as stated in the HTTP standard. Instead, the quality
        // weights (separated by ';') are ignored, and the very first MIME type supported is the one used.

        String mimeType = format;
        if (StringUtils.isBlank(mimeType)) {

            String acceptHeader = getContext().getRequest().getHeader("Accept");
            LOGGER.trace("Accept header: " + acceptHeader);
            if (StringUtils.isNotBlank(acceptHeader)) {

                String[] split = StringUtils.split(acceptHeader, ',');
                for (int i = 0; i < split.length; i++) {

                    String s = StringUtils.substringBefore(split[i], ";").trim();
                    if (SUPPORTED_MIME_TYPES.contains(s)) {
                        mimeType = s;
                        LOGGER.trace("Going for this MIME type (from Accept header): " + mimeType);
                        break;
                    }
                }
            }
        }
        if (StringUtils.isBlank(mimeType)) {
            mimeType = DEFAULT_STREAMING_RESPONSE_MIME_TYPE;
        }

        // If export to home space requested, then do so, otherwise run the query and either stream results into requested mime
        // type, or send back normal SPARQL endpoint HTML form page. The default resolution is the one to the form page.

        Resolution resolution = new ForwardResolution(FORM_PAGE);
        if ("HOMESPACE".equalsIgnoreCase(exportType)) {

            String dataset = folder + "/" + StringUtils.replace(datasetName, " ", "%20");
            int nrOfTriples = exportToHomespace(dataset);
            if (nrOfTriples > 0) {
                resolution = new RedirectResolution(FactsheetActionBean.class).addParameter("uri", dataset);
            }
        } else if (STREAMING_MIME_TYPES_TO_INTERNAL_FORMATS.containsKey(mimeType)) {
            resolution = executeStreamingQuery(mimeType);
        } else {
            executeQuery(mimeType.equals("text/html+") ? FORMAT_HTML_PLUS : FORMAT_HTML, null, getContext().getResponse(),
                    limitResultCount);
        }

        // In case an error has been raised and the client is not a browser, then set the resolution to HTTP error
        if (errorCode != 0 && !isWebBrowser()) {
            resolution = new ErrorStreamingResolution(errorCode, errorMessage);
        }

        return resolution;
    }

    /**
     * Gets the default-graph-uri and named-graph-uri parameters from request and stores them into ActionBean properties.
     * See SPARQL protocol specifications for more.
     */
    private void setDefaultAndNamedGraphs() {
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
     * Checks if user has update rights to "admin" ACL.
     *
     * @return true, if user is in admin group.
     */
    public boolean isAdminPrivilege() {
        if (getUser() != null && getUser().isAdministrator()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if user has insert right to the project ACL.
     *
     * @param projectName
     *            name
     * @return true, if user can add resources to the project.
     */
    public boolean hasProjectPrivilege(String projectName) {
        if (getUser() != null && CRUser.hasPermission(getContext().getRequest().getSession(), "/project/" + projectName, "i")) {
            return true;
        }

        return false;
    }

    /**
     * Execute the {@link #query} and streams the result into the servlet's output stream, using the given MIME type.
     *
     * @param mimeType
     *            The given MIME type.
     * @return The {@link StreamingResolution}.
     */
    private Resolution executeStreamingQuery(String mimeType) {

        final String internalFormat = STREAMING_MIME_TYPES_TO_INTERNAL_FORMATS.get(mimeType);
        StreamingResolution resolution = new StreamingResolution(mimeType) {
            @Override
            public void stream(HttpServletResponse response) throws Exception {

                ServletOutputLazyStream outputStream = null;
                try {
                    // Look at ServletOutputLazyStream JavaDoc for why use it here.
                    outputStream = new ServletOutputLazyStream(response);
                    executeQuery(internalFormat == null ? FORMAT_XML : internalFormat, outputStream, response);

                    if (errorCode > 0) {
                        if (!isWebBrowser()) {
                            // send plain text errors to external clients
                            outputStream.setContentType("text/plain");
                            response.setStatus(errorCode);
                            if (!StringUtils.isBlank(errorMessage)) {
                                outputStream.write(errorMessage.getBytes());
                            }
                        } else {
                            if (StringUtils.isBlank(errorMessage)) {
                                response.sendError(errorCode);
                            } else {
                                response.sendError(errorCode, errorMessage);
                            }

                        }
                    } else {
                        String fileName = "sparql-result." + StringUtils.substringBefore(internalFormat, "_");
                        this.setFilename(fileName);
                    }
                } finally {
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        return resolution;
    }

    private void executeQuery(String outputFormat, OutputStream outputStream, HttpServletResponse response) {
        executeQuery(outputFormat, outputStream, response, true);
    }

    /**
     * Executes the {@link #query}, using the given output format and stream, and possibly setting some headers
     * of the given servlet response.
     *
     * TODO: the execution of the query and communication with Sesame should really not be in controller.
     *
     * @param outputFormat
     *            Output format to generate.
     * @param outputStream
     *            Output stream to use, may be null, in which the bean simply sets the {@link #result}.
     * @param response
     *            Servlet response.
     */
    private void executeQuery(String outputFormat, OutputStream outputStream, HttpServletResponse response,
            boolean limitResultCount) {

        // If outputFormat is blank, fall back to "XML".
        if (StringUtils.isBlank(outputFormat)) {
            outputFormat = FORMAT_XML;
        }

        RepositoryConnection conn = null;
        try {
            conn = SesameConnectionProvider.getReadOnlyRepositoryConnection();
            Query queryObject = conn.prepareQuery(QueryLanguage.SPARQL, query);
            SesameUtil.setDatasetParameters(queryObject, conn, defaultGraphUris, namedGraphUris);

            TupleQueryResult queryResult = null;

            try {
                if (queryObject instanceof BooleanQuery) {

                    // Evaluate ASK query.

                    isAskQuery = true;
                    Boolean askResult = ((BooleanQuery) queryObject).evaluate();
                    if (outputFormat.equals(FORMAT_XML)) {

                        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                        writer.write("<?xml version=\"1.0\"?>");
                        writer.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
                        writer.write("<head></head>");
                        writer.write("<boolean>");
                        writer.write(askResult.toString());
                        writer.write("</boolean>");
                        writer.write("</sparql>");
                        writer.flush();

                    } else if (outputFormat.equals(FORMAT_XML_SCHEMA)) {

                        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                        writer.write("<?xml version=\"1.0\"?>");
                        writer.write("<sparql xmlns=\"http://www.w3.org/2005/sparql-results#\">");
                        writer.write("<head></head>");
                        writer.write("<boolean>");
                        writer.write(askResult.toString());
                        writer.write("</boolean>");
                        writer.write("</sparql>");
                        writer.flush();

                    } else if (outputFormat.equals(FORMAT_JSON)) {

                        OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                        writer.write("{  \"head\": { \"link\": [] }, \"boolean\": ");
                        writer.write(askResult.toString());
                        writer.write("}");
                        writer.flush();

                    } else if (outputFormat.equals(FORMAT_HTML)) {
                        resultAsk = askResult.toString();
                    }
                } else if (queryObject instanceof GraphQuery) {

                    // Evaluate CONSTRUCT query.
                    if (outputFormat.equals(FORMAT_HTML)) {
                        long startTime = System.currentTimeMillis();
                        TupleQuery resultsTable = conn.prepareTupleQuery(QueryLanguage.SPARQL, query);
                        TupleQueryResult bindings = resultsTable.evaluate();
                        executionTime = System.currentTimeMillis() - startTime;
                        if (bindings != null) {
                            result = new QueryResult(bindings, false, limitResultCount);
                        }

                    } else if (outputFormat.equals(FORMAT_XML)) {
                        RDFXMLWriter writer = new RDFXMLWriter(outputStream);
                        ((GraphQuery) queryObject).evaluate(writer);

                    } else if (outputFormat.equals(FORMAT_N3)) {
                        RDFWriter n3Writer = new N3WriterFactory().getWriter(outputStream);
                        ((GraphQuery) queryObject).evaluate(n3Writer);

                    } else if (outputFormat.equals(FORMAT_TURTLE)) {
                        RDFWriter turtleWriter = new TurtleWriterFactory().getWriter(outputStream);
                        ((GraphQuery) queryObject).evaluate(turtleWriter);

                    } else if (outputFormat.equals(FORMAT_NTRIPLES)) {
                        RDFWriter nTriplesWriter = new NTriplesWriterFactory().getWriter(outputStream);
                        ((GraphQuery) queryObject).evaluate(nTriplesWriter);
                    }
                } else {

                    // Evaluate SELECT query.
                    if (outputFormat.equals(FORMAT_XML)) {
                        setFileNameToHeader(response, "xml");
                        CRXmlWriter sparqlWriter = new CRXmlWriter(outputStream);
                        ((TupleQuery) queryObject).evaluate(sparqlWriter);

                    } else if (outputFormat.equals(FORMAT_XML_SCHEMA)) {
                        setFileNameToHeader(response, "xml");
                        CRXmlSchemaWriter sparqlWriter = new CRXmlSchemaWriter(outputStream);
                        ((TupleQuery) queryObject).evaluate(sparqlWriter);

                    } else if (outputFormat.equals(FORMAT_JSON)) {
                        setFileNameToHeader(response, "json");
                        CRJsonWriter sparqlWriter = new CRJsonWriter(outputStream);
                        ((TupleQuery) queryObject).evaluate(sparqlWriter);

                    } else if (outputFormat != null && outputFormat.equals(FORMAT_CSV)) {
                        setFileNameToHeader(response, "csv");
                        addBOM(outputStream, "UTF-8");

                        // as main consumer of the result is Excel use ";" because otherwise Excel does not handle CSV correctly
                        CRSPARQLCSVWriter sparqlWriter = new CRSPARQLCSVWriter(outputStream, ';');
                        ((TupleQuery) queryObject).evaluate(sparqlWriter);
                    } else if (outputFormat != null && outputFormat.equals(FORMAT_TSV)) {
                        setFileNameToHeader(response, "csv");
                        // MS Excel expects TSV to be UTF-16Little Endian
                        response.setCharacterEncoding("UTF-16LE");
                        addBOM(outputStream, "utf-16le");
                        CRSPARQLTSVWriter sparqlWriter = new CRSPARQLTSVWriter(outputStream);
                        ((TupleQuery) queryObject).evaluate(sparqlWriter);

                    } else if (outputFormat != null && (outputFormat.equals(FORMAT_HTML)
                            || outputFormat.equals(FORMAT_HTML_PLUS))) {
                        response.setContentType("text/html");
                        long startTime = System.currentTimeMillis();
                        queryResult = ((TupleQuery) queryObject).evaluate();
                        executionTime = System.currentTimeMillis() - startTime;
                        if (queryResult != null) {
                            result = new QueryResult(queryResult, outputFormat.equals(FORMAT_HTML_PLUS), limitResultCount);
                        }
                    }

                }
            } finally {
                SesameUtil.close(queryResult);
            }
        } catch (Exception e) {

            // Add feedback message to user (ignored when client is not a browser)
            addWarningMessage("Failure when executing the query: " + e);

            // Syntax error in query: http code - 400 - check query syntax with sesame parser
            handleQueryExecutionException(e);

        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * Exports CONSTRUCT query results into user's home space.
     *
     * @param dataset
     *            The user's home space identifier.
     *
     * @return Number of triples exported.
     */
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
     * @param deleteSharedBookmark
     *            true, if shared bookmark will be deleted, otherwise personal bookmark
     * @return
     * @throws DAOException
     */
    private Resolution deleteBookmarked(boolean deleteSharedBookmark) throws DAOException {

        setDefaultAndNamedGraphs();

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
    public boolean isUseOwlSameAs() {
        return useOwlSameAs;
    }

    /**
     * @param useOwlSameAs
     */
    public void setUseOwlSameAs(boolean useOwlSameAs) {
        this.useOwlSameAs = useOwlSameAs;
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
     * Lazy getter that returns the shared bookmarked queries.
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
     * Lazy getter that returns the project bookmarked queries.
     *
     * @return project bookmarked queries for the user.
     * @throws DAOException
     *             if query fails
     */
    public List<Map<String, String>> getProjectBookmarkedQueries() throws DAOException {

        if (projectBookmarkedQueries == null) {
            projectBookmarkedQueries = DAOFactory.get().getDao(HelperDAO.class).getProjectSparqlBookmarks(getUser());
        }
        return projectBookmarkedQueries;

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
     * @param deleteQueries
     *            the deleteQueries to set
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
     * @return the selectedBookmarkName
     */
    public String getSelectedBookmarkName() {
        return selectedBookmarkName;
    }

    /**
     * @param selectedBookmarkName
     *            the selectedBookmarkName to set
     */
    public void setSelectedBookmarkName(String selectedBookmarkName) {
        this.selectedBookmarkName = selectedBookmarkName;
    }

    /**
     * Handles the given exception that was thrown while trying to execute the {@link #query}. Sets a proper values to
     * {@link #errorCode} and {@link #errorMessage}.
     *
     * @param exception
     *            The given exception.
     */
    private void handleQueryExecutionException(Exception exception) {

        errorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        errorMessage = exception.getMessage();

        // Detect if the exception resulted from bad query, by parsing the query here. If that throws an exception, the query must
        // be bad, and necessary actions are taken.

        try {
            parser.parseQuery(query, GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL) + "/sparql");
        } catch (Exception e) {

            // Query is definitely bad if the exception is MalformedQueryException, and message contains "syntax error".
            boolean isBadQuery = e instanceof MalformedQueryException && exception.getMessage().indexOf("syntax error") != -1;

            // A ClassCastException means bad query too, because that's what is thrown when the query dos not start with
            // "SELECT", "CONSTRUCT" or "ASK".
            if (!isBadQuery) {
                isBadQuery = exception instanceof ClassCastException;
            }

            // If recognized as bad query, add warning message to user (ignored when the client is not a browser) and set proper
            // HTTP status code and message.
            if (isBadQuery) {

                errorCode = HttpServletResponse.SC_BAD_REQUEST;
                String msg = "Invalid SPARQL: " + e.getMessage();
                errorMessage = " Invalid SPARQL: " + query;
                addWarningMessage(msg);
            }
        }
    }

    /**
     *
     * @return
     */
    public List<String> getUserProjects() {
        return userProjects;
    }

    /**
     *
     * @param userProjects
     */
    public void setUserProjects(List<String> userProjects) {
        this.userProjects = userProjects;
    }

    /**
     *
     * @return
     */
    public String getBookmarkFolder() {
        return this.bookmarkFolder;
    }

    /**
     *
     * @param folder
     */
    public void setBookmarkFolder(String folder) {
        this.bookmarkFolder = folder;
    }

    /**
     *
     * @param id
     */
    public void setAjaxRequestId(Integer id) {
        this.ajaxRequestId = id;
    }

    /**
     *
     * @return
     */
    public Integer getAjaxRequestId() {
        return this.ajaxRequestId;
    }

    /**
     * @return
     */
    private static Map<String, String> createMimeTypesToInternalFormats() {

        HashMap<String, String> map = new HashMap<String, String>();
        map.put("application/sparql-results+xml", FORMAT_XML);
        map.put("application/rdf+xml", FORMAT_XML);
        map.put("application/xml", FORMAT_XML);
        map.put("text/xml", FORMAT_XML);
        map.put("application/x-binary-rdf-results-table", FORMAT_XML);
        map.put("text/csv", FORMAT_CSV);
        map.put("application/csv", FORMAT_CSV);
        map.put("application/tsv", FORMAT_TSV);
        map.put("text/tab-separated-values", FORMAT_TSV);
        map.put("text/comma-separated-values", FORMAT_CSV);
        map.put("text/boolean", FORMAT_XML);
        map.put("application/x-ms-access-export+xml", FORMAT_XML_SCHEMA);
        map.put("application/sparql-results+json", FORMAT_JSON);
        map.put("application/json", FORMAT_JSON);
        map.put("text/n3", FORMAT_N3);
        map.put("text/turtle", FORMAT_TURTLE);
        map.put("application/x-turtle", FORMAT_TURTLE);
        map.put("text/plain", FORMAT_NTRIPLES);
        return Collections.unmodifiableMap(map);
    }

    /**
     * @return
     */
    private static HashSet<String> createSupportedMimeTypes() {

        HashSet<String> set = new HashSet<String>();
        set.addAll(createMimeTypesToInternalFormats().keySet());
        set.add("text/html");
        return set;
    }

    /**
     * adds BOM at the beginning of the outputStream.
     *
     * @param os
     *            current outputstream
     * @throws IOException
     *             if connection fails
     */
    private static void addBOM(OutputStream os, String encoding) throws IOException {
        int[] bytes = Util.getBOM(encoding);

        for (int b : bytes) {
            os.write(b);
        }

    }

    /**
     * Checks if is bulk actions panel should be available for the current user.
     *
     * @return true, if is bulk actions available
     */
    public boolean isBulkActionsAllowed() {
        return isAdminPrivilege();
    }

    /**
     * @return the displayBulkActions
     */
    public boolean isDisplayBulkActions() {
        return displayBulkActions;
    }

    /**
     * @param displayBulkActions the displayBulkActions to set
     */
    public void setDisplayBulkActions(boolean displayBulkActions) {
        this.displayBulkActions = displayBulkActions;
    }

    /**
     * adds file name header to response.
      * @param response Http Response
     * @param fileExtension file extension without dot
     */
    private void setFileNameToHeader(HttpServletResponse response, String fileExtension) {
        response.setHeader("Content-Disposition", "attachment; filename=\""
                + DEFAULT_SPARQL_FILENAME + "." + fileExtension + "\"");
    }
}
