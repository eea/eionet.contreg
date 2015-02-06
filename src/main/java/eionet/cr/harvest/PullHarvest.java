/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest;

import static eionet.cr.harvest.ResponseCodeUtil.isError;
import static eionet.cr.harvest.ResponseCodeUtil.isNotModified;
import static eionet.cr.harvest.ResponseCodeUtil.isPermanentError;
import static eionet.cr.harvest.ResponseCodeUtil.isRedirect;
import static eionet.cr.harvest.ResponseCodeUtil.isUnauthorized;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.common.TempFilePathGenerator;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.EndpointHarvestQueryDAO;
import eionet.cr.dao.HarvestScriptDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.EndpointHarvestQueryDTO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.UrlAuthenticationDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.harvest.load.ContentLoader;
import eionet.cr.harvest.load.FeedFormatLoader;
import eionet.cr.harvest.load.RDFFormatLoader;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.harvest.util.MediaTypeToDcmiTypeConverter;
import eionet.cr.harvest.util.RDFMediaTypes;
import eionet.cr.util.FileDeletionJob;
import eionet.cr.util.Hashes;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.xml.ConversionsParser;

/**
 * Performs a pull-harvest.
 *
 * @author Jaanus Heinlaid
 */
public class PullHarvest extends BaseHarvest {

    /** Static logger for this class. */
    private static final Logger LOGGER = Logger.getLogger(PullHarvest.class);

    /** Code reserved for saying that "no HTTP response was returned. " */
    private static final int NO_RESPONSE = -1;

    /** Number of redirections to follow before giving up. */
    private static final int MAX_REDIRECTIONS = 4;

    /** Default "Accept" HTTP header when submitting HTTP requests to sources. */
    private static final String ACCEPT_HEADER = StringUtils.join(RDFMediaTypes.collection(), ',') + ",text/xml,*/*;q=0.6";

    /** Was the source available? */
    private boolean isSourceAvailable;

    /** URL that the source redirected to. */
    private final List<String> redirectedUrls = new ArrayList<String>();

    /**
     * Instantiates a new pull harvest.
     *
     * @param contextUrl the context url
     * @throws HarvestException the harvest exception
     */
    public PullHarvest(String contextUrl) throws HarvestException {
        super(contextUrl);
    }

    /**
     * Instantiates a new pull harvest.
     *
     * @param contextSourceDTO the context source dto
     * @throws DAOException the DAO exception
     */
    public PullHarvest(HarvestSourceDTO contextSourceDTO) throws DAOException {
        super(contextSourceDTO);
    }

    /**
     * Harvests file already uploaded to a CR folder and residing in the filestore.
     *
     * @throws HarvestException if harvest fails
     */
    private void doLocalFileHarvest() throws HarvestException {
        String initialContextUrl = getContextUrl();

        httpResponseCode = NO_RESPONSE;
        File file = null;
        String responseMessage = null;

        try {

            String message = "Opening connection to local file";
            LOGGER.debug(loggerMsg(message));

            file = FileStore.getByUri(initialContextUrl);

            if (file == null) {
                finishWithError(NO_RESPONSE, "The file does not exist", new HarvestException("The file does not exist"));
                return;
            }

            isSourceAvailable = true;

            // if URL connection returned no errors and its content has been modified since last harvest,
            // proceed to downloading

            // get content type and title from previously saved triples
            SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getFactsheet(initialContextUrl, null, null);
            String contentType = (subject != null ? subject.getObjectValue(Predicates.CR_MEDIA_TYPE) : null);
            String fileTitle = (subject != null ? subject.getObjectValue(Predicates.DC_TITLE) : null);

            int noOfTriples = processLocalContent(file, contentType);

            // for local files store N/A as http response code

            setStoredTriplesCount(noOfTriples);
            LOGGER.debug(loggerMsg(noOfTriples + " triples loaded"));

            if (!StringUtils.isBlank(fileTitle)) {
                addSourceMetadata(Predicates.DC_TITLE, ObjectDTO.createLiteral(fileTitle));
            }
            if (!StringUtils.isBlank(contentType)) {
                addSourceMetadata(Predicates.CR_MEDIA_TYPE, ObjectDTO.createLiteral(contentType));
            }

            // If long size can be converted to int without loss, then do so, otherwise remain true to long.
            long size = file.length();
            ObjectDTO byteSize = ((int) size) == size ? ObjectDTO.createLiteral((int) size) : ObjectDTO.createLiteral(size);
            addSourceMetadata(Predicates.CR_BYTE_SIZE, byteSize);
            addSourceMetadata(Predicates.CR_LAST_MODIFIED, ObjectDTO.createLiteral(formatDate(new Date()), XMLSchema.DATETIME));

            httpResponseCode = 0;
            finishWithOK(null, noOfTriples);

        } catch (Exception e) {

            LOGGER.debug(loggerMsg("Exception occurred (will be further logged by caller below): " + e.toString()));

            // check what caused the DAOException - fatal flag is set to true
            checkAndSetFatalExceptionFlag(e.getCause());

            try {
                finishWithError(httpResponseCode, responseMessage, e);
            } catch (RuntimeException finishingException) {
                LOGGER.error("Error when finishing up: ", finishingException);
            }
            if (e instanceof HarvestException) {
                throw (HarvestException) e;
            } else {
                throw new HarvestException(e.getMessage(), e);
            }

        }

    }

    /**
     * Called when the source to be harvested is a SPARQL endpoint. It's the core method that does the remote endpoint harvest.
     *
     * @throws HarvestException All exception wrapped into this one.
     */
    private void doEndpointHarvest() throws HarvestException {

        httpResponseCode = NO_RESPONSE;
        String responseMessage = null;
        File downloadedFile = null;
        HashMap<File, ContentLoader> filesAndLoaders = new HashMap<File, ContentLoader>();

        try {
            // First see if this particular endpoint has any active harvest queries mapped to it at all.
            String endpointUrl = getContextUrl();
            List<EndpointHarvestQueryDTO> queries =
                    DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).listByEndpointUrl(endpointUrl, true);
            if (queries == null || queries.isEmpty()) {
                String msg = "Found no active harvest queries for this endpoint";
                LOGGER.warn(loggerMsg(msg));
                finishWithOK(null, getContextSourceDTO().getStatements(), msg);
                return;
            }

            // Loop through the harvest queries, execute each one of them on the remote repository, save each response to a local
            // temporary file. Then load all these files, and delete them afterwards.
            // If any of the submitted queries fails, then so does the whole harvest of this remote endpoint.

            HttpURLConnection endpointConn = null;
            for (EndpointHarvestQueryDTO queryDTO : queries) {

                int queryId = queryDTO.getId();
                LOGGER.debug(loggerMsg("Executing endpoint harvest query with id = " + queryId));

                try {
                    endpointConn = prepareEndpointConnection(endpointUrl, queryDTO.getQuery());
                    try {
                        httpResponseCode = endpointConn.getResponseCode();
                        responseMessage = endpointConn.getResponseMessage();
                    } catch (IOException ioe) {
                        // an error when connecting to server is considered a temporary error-
                        // don't throw it, but log in the database and exit
                        LOGGER.debug(loggerMsg("Error when connecting to server: " + ioe));
                        finishWithError(NO_RESPONSE, null, ioe);
                        return;
                    }

                    // Throws exception when the content-length indicated in HTTP response is more than the maximum allowed.
                    validateContentLength(endpointConn);

                    if (httpResponseCode == 200) {
                        LOGGER.debug(loggerMsg("Downloading response of endpoint harvest query with id = " + queryId));
                        downloadedFile = downloadFile(endpointConn);
                        ContentLoader contentLoader = createContentLoader(endpointConn);
                        if (contentLoader != null) {
                            filesAndLoaders.put(downloadedFile, contentLoader);
                        } else {
                            String msg = "Response not in RDF or web feed format, unsupported for SPARQL endpoint harvest";
                            LOGGER.warn(loggerMsg(msg));
                            finishWithOK(endpointConn, getContextSourceDTO().getStatements(), msg);
                            return;
                        }
                    } else if (isUnauthorized(httpResponseCode)) {
                        LOGGER.debug(loggerMsg("Source unauthorized!"));
                        finishWithUnauthorized();
                        return;
                    } else if (isError(httpResponseCode)) {
                        LOGGER.debug(loggerMsg("Server returned error code " + httpResponseCode));
                        finishWithError(httpResponseCode, responseMessage, null);
                        return;
                    } else {
                        String msg = "Unsupported response code for SPARQL endpoint harvest: " + httpResponseCode;
                        LOGGER.warn(loggerMsg(msg));
                        finishWithOK(endpointConn, getContextSourceDTO().getStatements(), msg);
                        return;
                    }
                } finally {
                    URLUtil.disconnect(endpointConn);
                }
            }

            if (!filesAndLoaders.isEmpty()) {
                LOGGER.debug(loggerMsg("Loading downloaded query responses into triple store"));
                int tripleCount = loadFiles(filesAndLoaders);
                setStoredTriplesCount(tripleCount);
                LOGGER.debug(loggerMsg("Total of " + tripleCount + " triples loaded"));
                finishWithOK(endpointConn, tripleCount);
            } else {
                String msg = "No files downladed in this SPARQL endpoint harvest";
                LOGGER.warn(loggerMsg(msg));
                finishWithOK(endpointConn, getContextSourceDTO().getStatements(), msg);
            }
        } catch (Exception e) {

            LOGGER.debug(loggerMsg("Exception occurred (will be further logged by caller below): " + e.toString()));

            checkAndSetFatalExceptionFlag(e.getCause());
            try {
                finishWithError(httpResponseCode, responseMessage, e);
            } catch (RuntimeException finishingException) {
                LOGGER.error("Error when finishing up: ", finishingException);
            }

            if (e instanceof HarvestException) {
                throw (HarvestException) e;
            } else {
                throw new HarvestException(e.getMessage(), e);
            }
        } finally {
            FileDeletionJob.register(downloadedFile);
            for (File file : filesAndLoaders.keySet()) {
                FileDeletionJob.register(file);
            }
        }
    }

    /**
     * Harvests external source.
     *
     * @throws HarvestException if harvest fails
     */
    private void doUrlHarvest() throws HarvestException {

        String initialContextUrl = getContextUrl();
        HttpURLConnection urlConn = null;
        httpResponseCode = NO_RESPONSE;
        String responseMessage = null;
        int noOfRedirections = 0;

        try {
            String connectUrl = getContextUrl();
            do {
                String message = "Opening URL connection";
                if (!connectUrl.equals(getContextUrl())) {
                    message = message + " to " + connectUrl;
                }
                LOGGER.debug(loggerMsg(message));

                urlConn = prepareUrlConnection(connectUrl);

                try {
                    httpResponseCode = urlConn.getResponseCode();
                    responseMessage = urlConn.getResponseMessage();
                } catch (IOException ioe) {
                    // an error when connecting to server is considered a temporary error-
                    // don't throw it, but log in the database and exit
                    LOGGER.debug("Error when connecting to server: " + ioe);
                    finishWithError(NO_RESPONSE, null, ioe);
                    return;
                }

                // Throws exception when the content-length indicated in HTTP response is more than the maximum allowed.
                validateContentLength(urlConn);

                // Handle redirection.
                if (isRedirect(httpResponseCode)) {

                    noOfRedirections++;

                    // if number of redirections more than maximum allowed, throw exception
                    if (noOfRedirections > MAX_REDIRECTIONS) {
                        throw new TooManyRedirectionsException("Too many redirections, originally started from "
                                + initialContextUrl);
                    }

                    // get redirected-to-url, throw exception if it's missing
                    String redirectedToUrl = getRedirectUrl(urlConn);
                    redirectedUrls.add(connectUrl);
                    redirectedHarvestSources.add(getContextSourceDTO());

                    if (StringUtils.isBlank(redirectedToUrl)) {
                        throw new NoRedirectLocationException("Redirection response code wihtout \"Location\" header!");
                    }
                    LOGGER.debug(loggerMsg(connectUrl + " redirects to " + redirectedToUrl));

                    // treat this as a redirection only if the context URL and the redirected-to-URL
                    // are not essentially the same
                    if (URLUtil.equalUrls(getContextUrl(), redirectedToUrl) == false) {

                        boolean startWithNew = finishRedirectedHarvest(redirectedToUrl, httpResponseCode);
                        LOGGER.debug(loggerMsg("Redirection details saved"));

                        if (startWithNew) {
                            startWithNewContext(redirectedToUrl);
                        } else {
                            isSourceAvailable = true;
                            return;
                        }
                    } else {
                        LOGGER.debug(loggerMsg("Ignoring this redirection, as it is essentially to the same URL"));
                    }

                    connectUrl = redirectedToUrl;
                    // Close redirected URL connection
                    URLUtil.disconnect(urlConn);
                }
            } while (isRedirect(httpResponseCode));

            // if URL connection returned no errors and its content has been modified since last harvest,
            // proceed to downloading
            if (!isError(httpResponseCode) && !isNotModified(httpResponseCode) && !isUnauthorized(httpResponseCode)) {

                int noOfTriples = downloadAndProcessContent(urlConn);
                setStoredTriplesCount(noOfTriples);
                LOGGER.debug(loggerMsg(noOfTriples + " triples loaded"));
                finishWithOK(urlConn, noOfTriples);

            } else if (isNotModified(httpResponseCode)) {
                LOGGER.debug(loggerMsg("Source not modified since last harvest"));
                finishWithNotModified();

            } else if (isUnauthorized(httpResponseCode)) {
                LOGGER.debug(loggerMsg("Source unauthorized!"));
                finishWithUnauthorized();

            } else if (isError(httpResponseCode)) {
                LOGGER.debug(loggerMsg("Server returned error code " + httpResponseCode));
                finishWithError(httpResponseCode, responseMessage, null);
            }
        } catch (Exception e) {

            LOGGER.debug(loggerMsg("Exception occurred (will be further logged by caller below): " + e.toString()));

            // check what caused the DAOException - fatal flag is set to true
            checkAndSetFatalExceptionFlag(e.getCause());

            try {
                finishWithError(httpResponseCode, responseMessage, e);
            } catch (RuntimeException finishingException) {
                LOGGER.error("Error when finishing up: ", finishingException);
            }
            if (e instanceof HarvestException) {
                throw (HarvestException) e;
            } else {
                throw new HarvestException(e.getMessage(), e);
            }
        } finally {
            URLUtil.disconnect(urlConn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.BaseHarvest#doHarvest()
     */
    @Override
    protected void doHarvest() throws HarvestException {

        // check if the file is on the local filestore folder
        boolean isLocalFile = FileStore.isFileStoreUri(getContextUrl());

        if (isLocalFile) {
            doLocalFileHarvest();
        } else if (getContextSourceDTO().isSparqlEndpoint()) {
            doEndpointHarvest();
        } else {
            doUrlHarvest();
        }
    }

    /**
     * Calls {@link #finishWithOK(HttpURLConnection, int, String)}.
     *
     * @param urlConn
     * @param noOfTriples
     */
    private void finishWithOK(HttpURLConnection urlConn, int noOfTriples) {
        finishWithOK(urlConn, noOfTriples, null);
    }

    /**
     * Finishing actions for a harvest that didn't fail.
     *
     * @param urlConn The {@link HttpURLConnection} that was invoked.
     * @param noOfTriples The number of triples harvested.
     * @param warnMessage A warning message if encountered.
     */
    private void finishWithOK(HttpURLConnection urlConn, int noOfTriples, String warnMessage) {

        if (StringUtils.isNotBlank(warnMessage)) {
            addHarvestMessage(warnMessage, HarvestMessageType.WARNING);
        }

        // update context source DTO with the results of this harvest
        getContextSourceDTO().setStatements(noOfTriples);
        getContextSourceDTO().setLastHarvest(new Date());
        getContextSourceDTO().setLastHarvestFailed(false);
        getContextSourceDTO().setPermanentError(false);
        getContextSourceDTO().setCountUnavail(0);

        // add source metadata resulting from this harvest
        addSourceMetadata(urlConn, 0, null, null);

        // since the harvest went OK, clean previously harvested metadata of this source
        setCleanAllPreviousSourceMetadata(true);
    }

    /**
     * Helper method for taking actions in case of "not-modified" response from source.
     */
    private void finishWithNotModified() {

        addHarvestMessage("Source not modified since last harvest", HarvestMessageType.INFO);
        isSourceAvailable = true;

        // update context source DTO (since the server returned source-not-modified,
        // the number of harvested statements stays as it already is, i.e. we're not setting it)
        getContextSourceDTO().setLastHarvest(new Date());
        getContextSourceDTO().setLastHarvestFailed(false);
        getContextSourceDTO().setPermanentError(false);
        getContextSourceDTO().setCountUnavail(0);

        // since the server returned source-not-modified, we're keeping the old metadata,
        // but still updating the cr:lastRefreshed
        setCleanAllPreviousSourceMetadata(false);
        addSourceMetadata(Predicates.CR_LAST_REFRESHED, ObjectDTO.createLiteral(formatDate(new Date()), XMLSchema.DATETIME));
    }

    /**
     * Helper method for taking actions in case of "unauthorized" response from source.
     */
    private void finishWithUnauthorized() {

        addHarvestMessage("Source unauthorized", HarvestMessageType.INFO);
        isSourceAvailable = true;

        // update context source DTO (since the server returned source-not-modified,
        // the number of harvested statements stays as it already is, i.e. we're not setting it)
        getContextSourceDTO().setLastHarvest(new Date());
        getContextSourceDTO().setLastHarvestFailed(false);
        getContextSourceDTO().setPermanentError(false);
        getContextSourceDTO().setCountUnavail(0);

        setClearTriplesInHarvestFinish(true);
        LOGGER.debug("Old harvested content will be removed, because of source unauthorized error!");

        setCleanAllPreviousSourceMetadata(false);
        addSourceMetadata(Predicates.CR_LAST_REFRESHED, ObjectDTO.createLiteral(formatDate(new Date()), XMLSchema.DATETIME));
    }

    /**
     * @param responseCode
     * @param exception
     */
    private void finishWithError(int responseCode, String responseMessage, Exception exception) {

        // source is unavailable if there was no response, or it was an error code, or the exception cause is RDFParseException
        boolean isRDFParseException = exception != null && (exception.getCause() instanceof RDFParseException);
        boolean sourceNotAvailable = responseCode == NO_RESPONSE || isError(responseCode) || isRDFParseException;

        // if source was not available, the new unavailability-count is increased by one, otherwise reset
        int countUnavail = sourceNotAvailable ? getContextSourceDTO().getCountUnavail() + 1 : 0;

        // if permanent error, the last harvest date will be set to now, otherwise special logic used
        Date now = new Date();
        Date lastHarvest = isPermanentError(responseCode) ? now : temporaryErrorLastHarvest(now);

        // if permanent error, clean previously harvested metadata of this source,
        // and if not a priority source, clean all previously harvested content of this source too
        if (isPermanentError(responseCode)) {

            setCleanAllPreviousSourceMetadata(true);
            if (!getContextSourceDTO().isPrioritySource()) {
                setClearTriplesInHarvestFinish(true);
                LOGGER.debug("Old harvested content will be removed, because of permanent error on a non-priority source!");
            }
        }

        // update context source DTO with the results of this harvest
        getContextSourceDTO().setLastHarvest(lastHarvest);
        getContextSourceDTO().setLastHarvestFailed(true);
        getContextSourceDTO().setPermanentError(isPermanentError(responseCode));
        getContextSourceDTO().setCountUnavail(countUnavail);

        // save same error parameters to parent sources where this source was redirected from
        handleRedirectedHarvestDTOs(lastHarvest, responseCode, sourceNotAvailable);

        // add harvest message about the given exception if it's not null
        if (exception != null) {
            String message = exception.getMessage() == null ? exception.toString() : exception.getMessage();
            String stackTrace = Util.getStackTrace(exception);
            stackTrace = StringUtils.replace(stackTrace, "\r", "");
            addHarvestMessage(message, HarvestMessageType.ERROR, stackTrace);
        }

        // add harvest message about the given response code, if it's an error code (because it could also be
        // a "no response" code, meaning an exception was raised before the response code could be obtained)
        if (isError(responseCode)) {
            if (responseMessage == null) {
                responseMessage = "";
            }
            addHarvestMessage("Server returned error: " + responseMessage + " (HTTP response code: " + responseCode + ")",
                    HarvestMessageType.ERROR);
        }

        // add source metadata resulting from this harvest
        addSourceMetadata(null, responseCode, responseMessage, exception);
    }

    /**
     * Marks redirected sources with error markers.
     *
     * @param lastHarvest last harvest time
     * @param responseCode http response code
     * @param sourceNotAvailable shows if source was available
     */
    private void handleRedirectedHarvestDTOs(Date lastHarvest, int responseCode, boolean sourceNotAvailable) {
        for (HarvestSourceDTO harvestSourceDTO : redirectedHarvestSources) {
            setErrorsToRedirectedHarvestDTO(harvestSourceDTO, lastHarvest, responseCode, sourceNotAvailable);
        }
    }

    /**
     * Stores error in HarvestSource DTO.
     *
     * @param harvestSourceDTO / source DTO object
     */
    private void setErrorsToRedirectedHarvestDTO(HarvestSourceDTO harvestSourceDTO, Date lastHarvest, int responseCode,
            boolean sourceNotAvailable) {

        // if source was not available, the new unavailability-count is increased by one, otherwise reset
        int countUnavail = sourceNotAvailable ? harvestSourceDTO.getCountUnavail() + 1 : 0;

        harvestSourceDTO.setStatements(0);
        harvestSourceDTO.setLastHarvest(lastHarvest);
        harvestSourceDTO.setLastHarvestFailed(true);
        harvestSourceDTO.setPermanentError(isPermanentError(responseCode));
        harvestSourceDTO.setCountUnavail(countUnavail);

    }

    /**
     * Returns the {@link Date} to which the source's last harvest time should be set in case of temporary harvest error. It should
     * be set to "now - harvest_interval + max(harvest_interval*0,1, 120 min)". The "now" is given as method input.
     *
     * @param now As indicated above.
     * @return The calculated last harvest date as indicated above.
     */
    private Date temporaryErrorLastHarvest(Date now) {

        // The source's harvesting interval in minutes.
        int intervalMinutes = getContextSourceDTO().getIntervalMinutes();

        // The new last harvest will be "now - interval + interval*0,1", but at least two hours (i.e. 120 minutes).
        // So here we calculate the value that we shall add to the "now - interval".
        int increaseMinutes = Math.max((intervalMinutes * 10) / 100, 120);

        // Get calendar instance, set it to now.
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);

        // Subtract interval and add the above-calculated increase.
        cal.add(Calendar.MINUTE, -1 * intervalMinutes);
        cal.add(Calendar.MINUTE, increaseMinutes);

        // Just make it 100% sure that the calculated time will not be after now, though the business logic should exclude it.
        Date resultingTime = cal.getTime();
        if (resultingTime.after(now)) {
            resultingTime = now;
        }

        return resultingTime;
    }

    /**
     *
     * @param urlConn
     * @param responseCode
     * @param exception
     * @throws DAOException
     */
    private void addSourceMetadata(HttpURLConnection urlConn, int responseCode, String responseMessage, Exception exception) {

        addFirstSeenPredicate();

        String lastRefreshed = formatDate(new Date());
        addSourceMetadata(Predicates.CR_LAST_REFRESHED, ObjectDTO.createLiteral(lastRefreshed, XMLSchema.DATETIME));

        if (isError(responseCode)) {
            if (responseMessage == null) {
                responseMessage = "";
            }
            addSourceMetadata(
                    Predicates.CR_ERROR_MESSAGE,
                    ObjectDTO.createLiteral("Server returned error: " + responseMessage + " (HTTP response code: " + responseCode
                            + ")"));
        } else if (exception != null) {
            addSourceMetadata(Predicates.CR_ERROR_MESSAGE, ObjectDTO.createLiteral(exception.toString()));
        }

        if (urlConn != null) {

            // content type
            String contentType = getSourceContentType(urlConn);
            if (!StringUtils.isBlank(contentType)) {

                addSourceMetadata(Predicates.CR_MEDIA_TYPE, ObjectDTO.createLiteral(contentType));

                // if content type is not "application/rdf+xml", generate rdf:type from the
                // DublinCore type mappings
                if (!contentType.toLowerCase().startsWith("application/rdf+xml")) {

                    String rdfType = MediaTypeToDcmiTypeConverter.getDcmiTypeFor(contentType);
                    if (rdfType != null) {
                        addSourceMetadata(Predicates.RDF_TYPE, ObjectDTO.createResource(rdfType));
                    }
                }
            }

            // content's last modification
            long contentLastModified = urlConn.getLastModified();
            if (contentLastModified > 0) {
                String lastModifString = formatDate(new Date(contentLastModified));
                addSourceMetadata(Predicates.CR_LAST_MODIFIED, ObjectDTO.createLiteral(lastModifString, XMLSchema.DATETIME));
            }

            // content size
            int contentLength = urlConn.getContentLength();
            if (contentLength >= 0) {
                addSourceMetadata(Predicates.CR_BYTE_SIZE, ObjectDTO.createLiteral(contentLength));
            }
        }

    }

    /**
     * Download and process content. If response content type is one of RDF, then proceed straight to loading. Otherwise process the
     * file to see if it's zipped, it's an XML with RDF conversion, or actually an RDF file.
     *
     * @param urlConn - connection to the remote source.
     * @return number of triples harvested.
     *
     * @throws IOException
     * @throws DAOException
     * @throws SAXException
     * @throws RDFParseException if RDF parsing fails while analyzing file with unknown format
     * @throws RDFHandlerException if RDF parsing fails while analyzing file with unknown format
     */
    private int downloadAndProcessContent(HttpURLConnection urlConn) throws IOException, DAOException, SAXException,
            RDFHandlerException, RDFParseException {

        File downloadedFile = null;
        try {
            LOGGER.debug(loggerMsg("Downloading file"));
            downloadedFile = downloadFile(urlConn);

            // If the downloaded file can be loaded straight away as it is, then proceed to loading straight away.
            // Otherwise try to process the file into RDF format and *then* proceed to loading.

            ContentLoader contentLoader = createContentLoader(urlConn);
            int result = loadFileContent(downloadedFile, contentLoader);
            return result;
        } finally {
            FileDeletionJob.register(downloadedFile);
        }
    }

    /**
     *
     * @param redirectedToUrl The URL which the currently harvested context URL is redirecting to.
     * @param responseCode HTTP Code from the redirected URL
     * @return true if the redirected-to-URL should also be harvested, false otherwise.
     * @throws DAOException if database eror happens
     */
    private boolean finishRedirectedHarvest(String redirectedToUrl, int responseCode) throws DAOException {

        Date redirectionSeen = new Date();

        // update the context source's last-harvest and number of statements
        getContextSourceDTO().setLastHarvest(redirectionSeen);
        getContextSourceDTO().setLastHarvestFailed(false);
        getContextSourceDTO().setStatements(0);
        getContextSourceDTO().setLastHarvestId(getHarvestId());
        getHarvestSourceDAO().updateSourceHarvestFinished(getContextSourceDTO());

        // update current harvest to finished, set its count of harvested triples to 0
        getHarvestDAO().updateFinishedHarvest(getHarvestId(), 0, responseCode);

        // insert redirection message to the current harvest
        String message = getContextUrl() + "  redirects to  " + redirectedToUrl;
        HarvestMessageDTO messageDTO = HarvestMessageDTO.create(message, HarvestMessageType.INFO, null);
        messageDTO.setHarvestId(getHarvestId());
        getHarvestMessageDAO().insertHarvestMessage(messageDTO);

        // clear context source's metadata, save new metadata about redirection
        getHarvestSourceDAO().deleteSubjectTriplesInSource(getContextUrl(), GeneralConfig.HARVESTER_URI);
        SubjectDTO subjectDTO = createRedirectionMetadata(getContextSourceDTO(), redirectionSeen, redirectedToUrl);
        getHelperDAO().addTriples(subjectDTO);

        // Indicates if the redirected-to source should be harvested at this run. Should always be true when on-demand harvest.
        // When batch-harvest, then true only if the redirected-to source does not yet exist.
        boolean shouldHarvestRedirectedToSource = isOnDemandHarvest;

        // if redirected-to source not existing, create it by copying the context source
        HarvestSourceDTO redirectedToSourceDTO = getHarvestSource(redirectedToUrl);
        if (redirectedToSourceDTO == null) {

            LOGGER.debug(loggerMsg("Creating harvest source for " + redirectedToUrl));

            // A not-yet-existing redirected-to source should always be harvested.
            shouldHarvestRedirectedToSource = true;

            // clone the redirected-to source from the context source
            // (no null-checking, i.e. assuming the context source already exists)
            redirectedToSourceDTO = getContextSourceDTO().clone();

            // set the redirected-to source's url, creation time and last harvest time
            redirectedToSourceDTO.setUrl(redirectedToUrl);
            redirectedToSourceDTO.setUrlHash(Long.valueOf(Hashes.spoHash(redirectedToUrl)));
            redirectedToSourceDTO.setTimeCreated(redirectionSeen);

            // persist the redirected-to source
            getHarvestSourceDAO().addSource(redirectedToSourceDTO);
        }

        // delete old harvests history
        LOGGER.debug(loggerMsg("Deleting old redirected harvests history"));
        getHarvestDAO().deleteOldHarvests(getHarvestId(), NO_OF_LAST_HARVESTS_PRESERVED);

        return shouldHarvestRedirectedToSource;
    }

    /**
     *
     * @param sourceDTO
     * @param redirectionSeen
     * @param redirectedToUrl
     * @return
     */
    private SubjectDTO createRedirectionMetadata(HarvestSourceDTO sourceDTO, Date redirectionSeen, String redirectedToUrl) {

        String firstSeen = formatDate(sourceDTO.getTimeCreated());
        String lastRefreshed = formatDate(redirectionSeen);

        SubjectDTO subjectDTO = new SubjectDTO(sourceDTO.getUrl(), false);

        String harvesterContextUri = eionet.cr.config.GeneralConfig.HARVESTER_URI;
        ObjectDTO object = ObjectDTO.createLiteral(firstSeen, XMLSchema.DATETIME);
        object.setSourceUri(harvesterContextUri);
        subjectDTO.addObject(Predicates.CR_FIRST_SEEN, object);

        object = ObjectDTO.createLiteral(lastRefreshed, XMLSchema.DATETIME);
        object.setSourceUri(harvesterContextUri);
        subjectDTO.addObject(Predicates.CR_LAST_REFRESHED, object);

        object = ObjectDTO.createResource(redirectedToUrl);
        object.setSourceUri(harvesterContextUri);
        subjectDTO.addObject(Predicates.CR_REDIRECTED_TO, object);

        return subjectDTO;
    }

    /**
     * Download file from remote source to a temporary file locally. Side effect: adds the file size to the metadata to save in the
     * harvester context.
     *
     * @param urlConn - connection to the remote source.
     * @return object representing the temporary file.
     * @throws IOException if the file is not downloadable.
     */
    private File downloadFile(HttpURLConnection urlConn) throws IOException {

        InputStream inputStream = null;
        OutputStream outputStream = null;
        File file = TempFilePathGenerator.generate();
        try {
            outputStream = new FileOutputStream(file);
            inputStream = urlConn.getInputStream();
            isSourceAvailable = true;
            int bytesCopied = IOUtils.copy(inputStream, outputStream);

            // add number of bytes to source metadata, unless it's already there
            addSourceMetadata(Predicates.CR_BYTE_SIZE, ObjectDTO.createLiteral(bytesCopied));

        } catch (IOException e) {
            FileDeletionJob.register(file);
            throw e;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
            URLUtil.disconnect(urlConn);
        }

        return file;
    }

    /**
     * Adds basic authentication information to URL connection
     */
    private void addBasicAuthentication(HttpURLConnection urlConnection, String username, String password) {
        String userpass = username + ":" + password;
        String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());
        urlConnection.setRequestProperty("Authorization", basicAuth);
    }

    /**
     * Prepares the {@link HttpURLConnection} to be invoked for the given URL's harvest.
     *
     * @param connectUrl URL to harvest.
     * @return The prepared {@link HttpURLConnection}.
     * @throws IOException Several IO exception can happen on the way.
     * @throws DAOException The method may also connect to the database, this can throw an error.
     * @throws SAXException When parsing the response from conversion service fails.
     * @throws ParserConfigurationException When parsing the response from conversion service fails due to parser configuration.
     */
    private HttpURLConnection prepareUrlConnection(String connectUrl) throws IOException, DAOException, SAXException,
            ParserConfigurationException {

        String sanitizedUrl = StringUtils.substringBefore(connectUrl, "#");
        sanitizedUrl = StringUtils.replace(sanitizedUrl, " ", "%20");

        HttpURLConnection connection = (HttpURLConnection) new URL(sanitizedUrl).openConnection();
        connection.setRequestProperty("Accept", ACCEPT_HEADER);
        connection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
        connection.setRequestProperty("Connection", "close");

        UrlAuthenticationDTO authentication = DAOFactory.get().getDao(HarvestSourceDAO.class).getUrlAuthentication(connectUrl);
        if (authentication != null) {
            addBasicAuthentication(connection, authentication.getUsername(), authentication.getPassword());
            LOGGER.info("Source has basic authentication. Using credentials of " + authentication.getUrlBeginning());
        }

        connection.setInstanceFollowRedirects(false);

        // Set the timeout both for establishing the connection, and reading from it once established.
        int httpTimeout = GeneralConfig.getTimePropertyMilliseconds(GeneralConfig.HARVESTER_HTTP_TIMEOUT, getTimeout());
        connection.setConnectTimeout(httpTimeout);
        connection.setReadTimeout(httpTimeout);

        // The purpose of the following if-block is to use "If-Modified-Since" header, if all conditions are met.
        // The first condition is that this is a batch harvest (i.e. not an on-demand harvest) and the last harvest did not fail
        // (because if it failed then we want to re-harvest regardless of whether the source has been modified in the meantime).

        if (isOnDemandHarvest == false && getContextSourceDTO().isLastHarvestFailed() == false) {

            // "If-Modified-Since" will be compared to this URL's last harvest
            Date lastHarvestDate = getContextSourceDTO().getLastHarvest();
            long lastHarvest = lastHarvestDate == null ? 0L : lastHarvestDate.getTime();
            if (lastHarvest > 0) {

                // Check if this URL has a conversion stylesheet, and if the latter has been modified since last harvest.
                String conversionStylesheetUrl = getConversionStylesheetUrl(getHelperDAO(), sanitizedUrl);
                boolean hasConversion = StringUtils.isNotBlank(conversionStylesheetUrl);
                boolean hasModifiedConversion = hasConversion && URLUtil.isModifiedSince(conversionStylesheetUrl, lastHarvest);

                // Check if post-harvest scripts are updated
                boolean scriptsModified =
                        DAOFactory.get().getDao(HarvestScriptDAO.class)
                                .isScriptsModified(lastHarvestDate, getContextSourceDTO().getUrl());

                // "If-Modified-Since" should only be set if there is no modified conversion or post-harvest scripts for this URL.
                // Because if there is a conversion stylesheet or post-harvest scripts, and any of them has been modified since last
                // harvest, we surely want to get the content again and run the conversion or script on the content, regardless of
                // when the content itself was last modified.
                if (!hasModifiedConversion && !scriptsModified) {
                    LOGGER.debug(loggerMsg("Using if-modified-since, compared to last harvest " + formatDate(lastHarvestDate)));
                    connection.setIfModifiedSince(lastHarvest);
                }
            }
        }

        return connection;
    }

    /**
     * Prepares a {@link HttpURLConnection} to be invoked for the given remote endpoint harvest.
     *
     * @param endpointUrl The URL of the remote SPARQL endpoint to be queried.
     * @param query The SPARQL query to be submitted.
     * @return The prepared {@link HttpURLConnection}.
     * @throws IOException Several IO exception can happen on the way.
     */
    private HttpURLConnection prepareEndpointConnection(String endpointUrl, String query) throws IOException, DAOException {

        String charset = "UTF-8";
        String queryString = String.format("query=%s", URLEncoder.encode(query, charset));

        HttpURLConnection connection = (HttpURLConnection) new URL(endpointUrl).openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Accept-Charset", charset);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
        connection.setRequestProperty("Accept", ACCEPT_HEADER);
        connection.setRequestProperty("User-Agent", URLUtil.userAgentHeader());
        connection.setRequestProperty("Connection", "close");
        connection.setInstanceFollowRedirects(false);

        UrlAuthenticationDTO authentication = DAOFactory.get().getDao(HarvestSourceDAO.class).getUrlAuthentication(queryString);
        if (authentication != null) {
            addBasicAuthentication(connection, authentication.getUsername(), authentication.getPassword());
            System.out.println("Using basic auth");
        } else {
            System.out.println("NOT Using basic auth");
        }

        // Set the timeout both for establishing the connection, and reading from it once established.
        int httpTimeout = GeneralConfig.getTimePropertyMilliseconds(GeneralConfig.HARVESTER_HTTP_TIMEOUT, getTimeout());
        connection.setConnectTimeout(httpTimeout);
        connection.setReadTimeout(httpTimeout);

        OutputStream output = null;
        try {
            output = connection.getOutputStream();
            output.write(queryString.getBytes(charset));
        } finally {
            IOUtils.closeQuietly(output);
        }

        return connection;
    }

    /**
     *
     * @param connection
     * @return
     * @throws MalformedURLException
     */
    private String getRedirectUrl(HttpURLConnection connection) throws MalformedURLException {

        String location = connection.getHeaderField("Location");
        if (location != null) {
            try {
                // If location does not seem to be an absolute URI, consider it relative to the
                // URL of this URL connection.
                if (new URI(location).isAbsolute() == false) {
                    location = new URL(connection.getURL(), location).toString();
                }
            } catch (URISyntaxException e) {
                // Ignoring on purpose.
            }

            // we want to avoid fragment parts in CR harvest source URLs
            location = StringUtils.substringBefore(location, "#");
        }

        return location;
    }

    /**
     *
     * @param harvestSourceUrl
     * @return
     * @throws DAOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static String getConversionStylesheetUrl(HelperDAO helperDAO, String harvestSourceUrl) throws DAOException,
            IOException, SAXException, ParserConfigurationException {

        String result = null;
        String schemaUri = helperDAO.getSubjectSchemaUri(harvestSourceUrl);
        if (!StringUtils.isBlank(schemaUri)) {

            // see if schema has RDF conversion
            ConversionsParser convParser = ConversionsParser.parseForSchema(schemaUri);
            if (StringUtils.isNotBlank(convParser.getRdfConversionId())) {

                String xslFileName = convParser.getRdfConversionXslFileName();
                if (StringUtils.isNotBlank(xslFileName)) {

                    String xslUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_XSL_URL);
                    result = MessageFormat.format(xslUrl, Util.toArray(xslFileName));
                }
            }
        }

        return result;
    }

    /**
     * Returns RDF format from url connection.
     *
     * @param contentType
     * @return
     */
    private RDFFormat getRdfFormat(HttpURLConnection urlConn) {
        String contentType = getSourceContentType(urlConn);

        if (contentType == null) {
            return null;
        }

        if (contentType.equals(CONTENT_TYPE_TEXT)) {
            String path = urlConn.getURL().getPath();
            String[] arr = path.split("\\.");
            if (arr.length > 0) {
                String ext = arr[arr.length - 1];
                if (StringUtils.isNotEmpty(ext)) {
                    if (ext.equalsIgnoreCase(EXT_TTL)) {
                        return RDFFormat.TURTLE;
                    }
                    if (ext.equalsIgnoreCase(EXT_N3)) {
                        return RDFFormat.N3;
                    }
                    if (ext.equalsIgnoreCase(EXT_NTRIPLES)) {
                        return RDFFormat.NTRIPLES;
                    }
                }
            }
        }

        return RDFMediaTypes.toRdfFormat(contentType);
    }

    /**
     *
     * @param urlConn
     * @return
     */
    private String getSourceContentType(HttpURLConnection urlConn) {

        // prefer content type from context source DTO over the one from URL connection
        String contentType = getContextSourceDTO().getMediaType();
        if (StringUtils.isBlank(contentType)) {
            contentType = urlConn.getContentType();
        }
        return contentType;
    }

    /**
     *
     * @param urlConn
     * @return
     */
    private ContentLoader createContentLoader(HttpURLConnection urlConn) {

        RDFFormat rdfFormat = getRdfFormat(urlConn);
        if (rdfFormat != null) {
            return new RDFFormatLoader(rdfFormat);
        }

        String contentType = getSourceContentType(urlConn);
        if (StringUtils.isBlank(contentType)) {
            return null;
        } else if (contentType.startsWith("application/rss+xml") || contentType.startsWith("application/atom+xml")) {
            return new FeedFormatLoader();
        } else {
            return null;
        }
    }

    /**
     *
     * @param urlConn
     * @throws ContentTooLongException
     */
    private void validateContentLength(HttpURLConnection urlConn) throws ContentTooLongException {

        int maxLengthAllowed = NumberUtils.toInt(GeneralConfig.getProperty(GeneralConfig.HARVESTER_MAX_CONTENT_LENGTH));
        if (maxLengthAllowed > 0) {
            int contentLength = NumberUtils.toInt(urlConn.getHeaderField("Content-Length"));
            if (contentLength > maxLengthAllowed) {
                throw new ContentTooLongException(contentLength + " is more than the allowed maximum " + maxLengthAllowed);
            }
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.BaseHarvest#getHarvestType()
     */
    @Override
    protected String getHarvestType() {

        return BaseHarvest.TYPE_PULL;
    }

    /**
     * @return the isSourceAvailable
     */
    protected boolean isSourceAvailable() {
        return isSourceAvailable;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.BaseHarvest#isSendNotifications()
     */
    @Override
    protected boolean isSendNotifications() {

        // Send notification only when this is not an on-demand harvest and the source is priority or a fatal error occurred.
        // P.S. The harvest's timeout is also considered a fatal error.
        return !isOnDemandHarvest && (getContextSourceDTO().isPrioritySource() || isFatalErrorOccured);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.BaseHarvest#isBeingHarvested(java.lang.String)
     */
    @Override
    public boolean isBeingHarvested(String url) {

        boolean result = super.isBeingHarvested(url);
        return result == true ? result : redirectedUrls.contains(url);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.BaseHarvest#afterFinish()
     */
    @Override
    protected void afterFinish() {

        // Execute a background thread that will attempt to clear the graph of all redirected sources.
        (new Thread() {
            @Override
            public void run() {
                for (String redirectedUrl : PullHarvest.this.redirectedUrls) {

                    try {
                        PullHarvest.LOGGER.debug("Clearing the graph of redirected source " + redirectedUrl);
                        DAOFactory.get().getDao(HarvestSourceDAO.class).clearGraph(redirectedUrl);
                    } catch (DAOException e) {
                        PullHarvest.LOGGER.error("Failed to clear the graph of redirected source " + redirectedUrl, e);
                    }
                }
            }
        }).start();
    }
}
