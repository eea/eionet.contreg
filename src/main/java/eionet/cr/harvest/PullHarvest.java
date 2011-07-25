package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.common.TempFilePathGenerator;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.harvest.util.HarvestUrlConnection;
import eionet.cr.harvest.util.MimeTypeConverter;
import eionet.cr.util.ConnectionError;
import eionet.cr.util.ConnectionError.ErrType;
import eionet.cr.util.FileUtil;
import eionet.cr.util.Hashes;
import eionet.cr.util.URLUtil;
import eionet.cr.util.UrlRedirectAnalyzer;
import eionet.cr.util.UrlRedirectionInfo;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author altnyris, heinljab
 *
 */
public class PullHarvest extends Harvest {

    /**
     * Max number of redirections. Also declared in super class.
     */
    public static final int maxRedirectionsAllowed = 4;
    private boolean rdfContentFound = false;

    /** */
    private Boolean sourceAvailable = null;
    /**
     * Last harvest of source as stored in database.
     */
    private Date lastHarvest = null;

    /** */
    private ConversionsParser convParser;

    /**
     * In case of redirected sources, each redirected source will be harvested without additional recursive redirection.
     * */
    private boolean recursiveHarvestDisabled = false;

    /**
     *
     * @param sourceUrlString
     * @param lastHarvest
     *            - date to set as last harvest.
     */
    public PullHarvest(String sourceUrlString, Date lastHarvest) {
        super(sourceUrlString);
        this.lastHarvest = lastHarvest;
    }

    /**
     * @throws HarvestException
     *
     */
    protected void doExecute() throws HarvestException {

        File downloadedFile = TempFilePathGenerator.generate();

        String contentType = null;
        int totalBytes = 0;

        HarvestUrlConnection harvestUrlConnection = null;
        needsHarvesting = true;

        try {
            // Make sure old temporary file is deleted.
            deleteFileSafely(downloadedFile);

            // Resolve redirections
            redirectedUrls = resolveRedirects();

            if (redirectedUrls != null && !redirectedUrls.isEmpty()) {

                isRedirectedSource = true;

                // Change sourceUrlString to the actual source that will be harvested.
                sourceMetadata.setUri(sourceUrlString);
                logger.setHarvestSourceUrl(sourceUrlString);
            }

            // Get DTO of source that will be harvested.
            HarvestSourceDTO source = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrlString);

            // If batch harvesting and broken source, perform necessary actions.
            if (source != null && isBatchHarvest() && !isUrgentHarvest()) {

                boolean increaseUnavailableCount = source.isPermanentError() && source.isPrioritySource();
                Integer countUnavail = source.getCountUnavail();
                boolean deleteSource = countUnavail != null && countUnavail.intValue() >= 5 && !source.isPrioritySource();

                // This is considered broken source.
                if (increaseUnavailableCount || deleteSource) {

                    handleBrokenSource(source, increaseUnavailableCount, deleteSource);
                    return;
                }
            }

            // Source shouldn't be harvested if it redirects to another source
            // and the destination source is recently harvested (within its harvesting minutes)
            if (needsHarvesting) {

                // prepare URL connection
                harvestUrlConnection = HarvestUrlConnection.getConnection(sourceUrlString);

                setConversionModified(harvestUrlConnection.getUrlConnection());

                // open connection stream
                logger.debug("Downloading");
                try {
                    harvestUrlConnection.openInputStream();
                    sourceAvailable = harvestUrlConnection.isSourceAvailable();
                } catch (Exception e) {
                    logger.warn(e.toString());
                }

                // Check if Virtuoso is up and running. If not consider it as temporary error
                boolean virtuosoAvailable = true;
                RepositoryConnection repoConn = null;
                try {
                    repoConn = SesameUtil.getRepositoryConnection();
                } catch (RepositoryException e) {
                    sourceAvailable = false;
                    virtuosoAvailable = false;
                } finally {
                    SesameUtil.close(repoConn);
                }

                ConnectionError error = harvestUrlConnection.getError();
                if (error != null && virtuosoAvailable) {
                    sourceAvailable = false;
                    if (error.getType().equals(ErrType.PERMANENT)) {
                        permanentError = true;
                        DAOFactory.get().getDao(HarvestSourceDAO.class).deleteSourceTriples(sourceUrlString);

                        DAOFactory.get().getDao(HarvestSourceDAO.class).removeAllPredicatesFromHarvesterContext(sourceUrlString);
                    }

                    // Add cr:firstSeen metadata predicate into harvester context
                    if (source != null && source.getTimeCreated() != null) {
                        String firstSeen = dateFormat.format(source.getTimeCreated());
                        ObjectDTO firstSeenObject = new ObjectDTO(firstSeen, true, XMLSchema.DATETIME);
                        DAOFactory.get().getDao(HarvestSourceDAO.class)
                        .insertUpdateSourceMetadata(sourceUrlString, Predicates.CR_FIRST_SEEN, firstSeenObject);
                    }

                    ObjectDTO errorObject = new ObjectDTO(error.getMessage(), true);
                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                    .insertUpdateSourceMetadata(sourceUrlString, Predicates.CR_ERROR_MESSAGE, errorObject);

                    String lastRefreshed = dateFormat.format(new Date(System.currentTimeMillis()));
                    ObjectDTO lastRefreshedObject = new ObjectDTO(lastRefreshed, true, XMLSchema.DATETIME);
                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                    .insertUpdateSourceMetadata(sourceUrlString, Predicates.CR_LAST_REFRESHED, lastRefreshedObject);
                }

                if (sourceAvailable && needsHarvesting) {
                    // source is available, so continue to extract it's contents and metadata

                    // extract various metadata about this harvest source from url connection object
                    setSourceMetadata(harvestUrlConnection.getConnection(), source);

                    // NOTE: If URL is redirected, content type is null. skip if unsupported content type
                    contentType = sourceMetadata.getObjectValue(Predicates.CR_MEDIA_TYPE);

                    // If MEDIA_TYPE in HARVEST_SOURCE table is set then the value from the server is ignored
                    if (source != null && !StringUtils.isBlank(source.getMediaType())) {
                        contentType = source.getMediaType();
                    }

                    if (contentType != null && !isSupportedContentType(contentType)) {
                        logger.debug("Unsupported response content type: " + contentType);
                    } else {
                        logger.debug("Response content type was " + contentType);
                        if (harvestUrlConnection.isHttpConnection()
                                && harvestUrlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {

                            handleSourceNotModified();
                            return;
                        } else {
                            logger.debug("Streaming the content to file " + downloadedFile);

                            // Save the stream to temporary file.
                            totalBytes = FileUtil.streamToFile(harvestUrlConnection.getInputStream(), downloadedFile);
                            logger.debug("Total bytes downloaded: " + totalBytes);
                            temporaryFiles.add(downloadedFile);

                            // if content-length for source metadata was previously not found, then set it to file size
                            if (sourceMetadata.getObject(Predicates.CR_BYTE_SIZE) == null) {
                                sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(String.valueOf(totalBytes), true));
                            }
                        }
                    }

                    // remove old triples
                    // FIXME: JH180511 - should it really be necessary here? Because
                    // the graph is cleared from old triples by HarvestSourceDAO.addSourceToRepository(...)
                    // anyway below.
                    DAOFactory.get().getDao(HarvestSourceDAO.class).deleteSourceTriples(sourceUrlString);

                    // remove old auto-generated metadata (i.e. the onw that's in harvster's context)
                    logger.debug("Removing old auto-generated triples about the source");
                    DAOFactory.get().getDao(HarvestSourceDAO.class).removeAllPredicatesFromHarvesterContext(sourceUrlString);

                    // Iinsert new auto generated metadata
                    logger.debug("Storing new auto-generated triples about the source");
                    DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceMetadata(sourceMetadata);

                } // if Source is available.
            } else {
                // Harvest source redirects to another source that has recently been updated therefore we can set these values
                // TRUE
                sourceAvailable = Boolean.TRUE;
                rdfContentFound = Boolean.TRUE;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new HarvestException(e.toString(), e);
        } finally {
            // close input stream
            if (harvestUrlConnection != null) {
                harvestUrlConnection.close();
            }
        }

        if (sourceAvailable && needsHarvesting && totalBytes > 0) {

            int tripleCount = harvest(downloadedFile, contentType);

            // Perform the harvest. Only extract sources if we got some triples.
            // Otherwise it is a waste of time.
            if (tripleCount > 0) {
                logger.debug("Extracting new harvest sources after fresh harvest");
                extractNewHarvestSources();
            }
        } else if (!needsHarvesting) {
            logger.debug("Source redirects to another source that has recently been harvested! Will not harvest.");
        }
    }

    /**
     * @param source
     * @param increaseUnavailableCount
     * @param deleteSource
     * @throws DAOException
     */
    private void handleBrokenSource(HarvestSourceDTO source, boolean increaseUnavailableCount, boolean deleteSource)
    throws DAOException {

        if (increaseUnavailableCount) {

            // Don't want any finishing actions to be done here.
            // TODO: sure this is right?
            daoWriter = null;

            // log intentions
            logger.warn("This priority source has permanent error, increasing its unavailability count and exiting!");

            // increase unavailable count and exit
            DAOFactory.get().getDao(HarvestSourceDAO.class).increaseUnavailableCount(source.getSourceId());
        } else if (deleteSource) {

            // Don't want any finishing actions to be done here.
            // TODO: sure this is right?
            daoWriter = null;

            // log intentions
            logger.warn("This non-priority source has unavailability count >=5, queing it for deletion and exiting!");

            // queue source for deletion and exit
            DAOFactory.get().getDao(HarvestSourceDAO.class).queueSourcesForDeletion(Collections.singletonList(sourceUrlString));
        }
    }

    /**
     *
     * @param downloadedFile
     * @param contentType
     * @return
     * @throws HarvestException
     */
    private int harvest(File downloadedFile, String contentType) throws HarvestException {

        int tripleCount = 0;

        if (downloadedFile == null || !downloadedFile.exists() || !downloadedFile.isFile()) {
            return tripleCount;
        }

        File unzippedFile = unzip(downloadedFile);
        if (unzippedFile == null) {

            // The file was not zipped, and if it's not XML-formatted either, then nothing to do here.
            if (contentType != null && !isXmlContentType(contentType)) {
                return tripleCount;
            }
        } else {
            logger.debug("The file was gzipped, going to process unzipped file");
            temporaryFiles.add(unzippedFile);
        }

        Exception exception = null;
        try {
            File processedFile = preProcess(unzippedFile == null ? downloadedFile : unzippedFile, contentType);
            if (processedFile != null) {

                temporaryFiles.add(processedFile);
                logger.debug("Loading the file contents into the triple store");
                tripleCount =
                    DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceToRepository(processedFile, sourceUrlString);
                logger.debug(tripleCount + " triples stored");
                setStoredTriplesCount(tripleCount);
            }
        } catch (IOException e) {
            exception = e;
        } catch (ParserConfigurationException e) {
            exception = e;
        } catch (SAXException e) {
            exception = e;
        }
        catch (OpenRDFException e) {
            exception = e;
        }

        if (exception!=null){
            if (this.rdfContentFound){
                throw new HarvestException(exception.toString(), exception);
            }
            else{
                logger.info("Ignoring this exception, because no RDF content found: " + exception.toString());
            }
        }

        return tripleCount;
    }

    /**
     *
     * @param sourceFile
     * @return
     */
    private File unzip(File sourceFile) {

        if (sourceFile == null) {
            return null;
        }

        File targetFile = new File(sourceFile.getAbsolutePath() + ".unzipped");

        GZIPInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new GZIPInputStream(new FileInputStream(sourceFile));
            outputStream = new FileOutputStream(targetFile);
            IOUtils.copy(inputStream, outputStream);
            return targetFile;

        } catch (IOException e) {
            logger.debug("Assuming the file is not GZipped, exception when trying unzip: " + e.toString());
            return null;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * Check if the content-type is one that is supported.
     *
     * @param contentType
     *            - value to check.
     * @return true if content-type is supported
     */
    private boolean isSupportedContentType(String contentType) {

        return contentType.startsWith("text/xml") || contentType.startsWith("application/xml")
        || contentType.startsWith("application/rdf+xml") || contentType.startsWith("application/atom+xml")
        || contentType.startsWith("application/octet-stream") || contentType.startsWith("application/x-gzip");
    }

    /**
     * Check if the content-type is one of the XML types.
     *
     * @param contentType
     * @return true if content-type is XML
     */
    private boolean isXmlContentType(String contentType) {

        return contentType.startsWith("text/xml") || contentType.startsWith("application/xml")
        || contentType.startsWith("application/rdf+xml") || contentType.startsWith("application/atom+xml");
    }

    /**
     * Stores metadata if URL content is not modified and source is not harvested.
     *
     * @throws DAOException
     *             if updating in Virtuoso fails.
     *
     */
    private void handleSourceNotModified() throws DAOException {

        SubjectDTO failedHarvestSourceMetadata = new SubjectDTO(sourceUrlString, false);
        // only cr:lastRefresh has to be stored in case of failure if source is not modified
        String lastRefreshed = dateFormat.format(new Date());
        ObjectDTO lastRefreshDate = new ObjectDTO(lastRefreshed, true, XMLSchema.DATETIME);

        // update lastRefreshed predicate for this source
        failedHarvestSourceMetadata.addObject(Predicates.CR_LAST_REFRESHED, lastRefreshDate);

        try {
            DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceMetadata(failedHarvestSourceMetadata);
        } catch (Exception e) {
            throw new DAOException("Updating failed harvest source metadata failed: " + e);
        }

        // copy the harvest's number of triples and resources from previous harvest
        if (previousHarvest != null) {
            setStoredTriplesCount(previousHarvest.getTotalStatements());
        }

        String msg = "Source not modified since " + lastHarvest.toString();
        logger.debug(msg);
        infos.add(msg);
    }

    /**
     * Checks if the last_harvested date of final source is so recent that the harvesting schedule for redirected sources wouldn't
     * have triggered
     *
     * @return boolean
     * @throws DAOException
     *             if database query fails.
     */
    private boolean shouldBeHarvested() throws DAOException {

        boolean ret = false;

        // Load full information about the final source.
        HarvestSourceDTO finalSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrlString);

        long now = System.currentTimeMillis();
        long lastHarvestTime =
            finalSource == null || finalSource.getLastHarvest() == null ? 0 : finalSource.getLastHarvest().getTime();
        long sinceLastHarvest = now - lastHarvestTime;
        long harvestIntervalMillis =
            finalSource == null || finalSource.getIntervalMinutes() == null ? 0L : finalSource.getIntervalMinutes()
                    .longValue() * 60L * 1000L;

        if (lastHarvestTime == 0 || (lastHarvestTime > 0 && sinceLastHarvest > harvestIntervalMillis)) {
            ret = true;
        }

        return ret;
    }

    /**
     * Method deals with redirections. If original source redirects to another source or there is chain of redirections between
     * original source and actual file then this method creates new record into HARVEST_SOURCE table for each redirecting URL. If
     * one already exists, then intervalMinutes is checked. If redirected source intervalMinutes is larger than originalSource then
     * latter is used.
     *
     * Method also creates new record into HARVEST table for each redirected source. Also adds info into HARVEST_MESSAGE table about
     * each redirection.
     *
     * If number of redirections exceeds VirtuosoPullHarvest.maxRedirectionsAllowed, then execution is terminated
     *
     * @return List<String>
     * @throws HarvestException
     * @throws DAOException
     *             if database query fails.
     * @throws RepositoryException
     */
    private List<String> resolveRedirects() throws HarvestException, DAOException, RepositoryException, IOException {

        List<String> ret = new ArrayList<String>();
        int redirectionsFound = -1;
        HashMap<String, String> redirections = new HashMap<String, String>();

        HarvestSourceDTO originalSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrlString);

        UrlRedirectionInfo lastUrl = UrlRedirectAnalyzer.analyzeUrlRedirection(sourceUrlString);
        while (lastUrl.isRedirected() == true) {

            // Add harvest messages about redirections
            redirections.put(lastUrl.getSourceURL(),
                    "URL " + lastUrl.getSourceURL() + " is redirected to " + lastUrl.getTargetURL());
            logger.debug("URL " + lastUrl.getSourceURL() + " is redirected to " + lastUrl.getTargetURL());

            // Remove old predicates from /harvester context for this source
            DAOFactory.get().getDao(HarvestSourceDAO.class).removeAllPredicatesFromHarvesterContext(lastUrl.getSourceURL());

            // Add last refreshed metadata into Virtuoso /harvester context
            String lastRefreshed = dateFormat.format(new Date(System.currentTimeMillis()));
            ObjectDTO lastRefreshedObject = new ObjectDTO(lastRefreshed, true, XMLSchema.DATETIME);
            DAOFactory.get().getDao(HarvestSourceDAO.class)
            .insertUpdateSourceMetadata(lastUrl.getSourceURL(), Predicates.CR_LAST_REFRESHED, lastRefreshedObject);

            // Add redirection metadata into Virtuoso /harvester context
            ObjectDTO redirectObject = new ObjectDTO(lastUrl.getTargetURL(), false);
            DAOFactory.get().getDao(HarvestSourceDAO.class)
            .insertUpdateSourceMetadata(lastUrl.getSourceURL(), Predicates.CR_REDIRECTED_TO, redirectObject);

            ret.add(lastUrl.getSourceURL());
            redirectionsFound = ret.size();
            // Checking the count of redirects.
            if (redirectionsFound > PullHarvest.maxRedirectionsAllowed) {
                throw new HarvestException("Too many redirections for url: " + sourceUrlString + ". Found " + redirectionsFound
                        + ", allowed " + PullHarvest.maxRedirectionsAllowed);
            }
            sourceUrlString = lastUrl.getTargetURL();

            lastUrl = UrlRedirectAnalyzer.analyzeUrlRedirection(lastUrl.getTargetURL());
        }

        // The code below will be only executed if source is redirected
        if (ret != null && ret.size() > 0) {
            ret.add(sourceUrlString);
        }

        // Insert sources that doesn't yet exist into postgre DB or update interval minutes
        if (ret != null && ret.size() > 0) {

            // Insert redirection harvest message for original URL
            String msg = redirections.get(originalSource.getUrl());
            if (!StringUtils.isBlank(msg)) {
                insertHarvestMessage(msg, daoWriter.getHarvestId());
            }
            // Finish original redirection harvest
            DAOFactory.get().getDao(HarvestDAO.class)
            .updateFinishedHarvest(daoWriter.getHarvestId(), Harvest.STATUS_FINISHED, 0, 0, 0);

            for (String url : ret) {
                if (!url.equals(originalSource.getUrl())) {
                    HarvestSourceDTO redirectedSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(url);
                    if (redirectedSource == null) {
                        redirectedSource = new HarvestSourceDTO();
                        redirectedSource.setPrioritySource(originalSource.isPrioritySource());
                        redirectedSource.setIntervalMinutes(originalSource.getIntervalMinutes());
                        redirectedSource.setUrl(url);
                        redirectedSource.setUrlHash(Hashes.spoHash(url));
                        redirectedSource.setOwner(originalSource.getOwner());
                        Integer sourceId = DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(redirectedSource);
                        redirectedSource.setSourceId(sourceId.intValue());
                        // Set final source ID
                        if (url.equals(sourceUrlString)) {
                            finalSourceId = sourceId;
                        }
                    } else if (redirectedSource.getIntervalMinutes() > originalSource.getIntervalMinutes()) {
                        redirectedSource.setIntervalMinutes(originalSource.getIntervalMinutes());

                        // Saving the updated source to database.
                        DAOFactory.get().getDao(HarvestSourceDAO.class).editSource(redirectedSource);
                    }

                    // Harvest for original source has already been created by DAOWriter.writeStarted() method
                    if (url.equals(sourceUrlString)) {

                        // Decide whether we should harvest this source If source redirects to another source that has recently been
                        // updated then there is no need to harvest it again
                        if (!shouldBeHarvested()) {
                            needsHarvesting = false;
                        }

                        // If final source needs harvesting, create record into HARVEST table
                        if (needsHarvesting) {
                            // Insert harvest
                            int harvestId =
                                DAOFactory
                                .get()
                                .getDao(HarvestDAO.class)
                                .insertStartedHarvest(redirectedSource.getSourceId(), Harvest.TYPE_PULL,
                                        CRUser.APPLICATION.getUserName(), Harvest.STATUS_STARTED);
                            // If final source, then update DAO Writer harvestId
                            daoWriter.setHarvestId(harvestId);
                        }
                    } else {
                        // Insert harvest
                        int harvestId =
                            DAOFactory
                            .get()
                            .getDao(HarvestDAO.class)
                            .insertStartedHarvest(redirectedSource.getSourceId(), Harvest.TYPE_PULL,
                                    CRUser.APPLICATION.getUserName(), Harvest.STATUS_FINISHED);
                        // Insert harvest message for redirected URLs
                        msg = redirections.get(redirectedSource.getUrl());
                        if (!StringUtils.isBlank(msg)) {
                            insertHarvestMessage(msg, harvestId);
                        }
                        // Finish harvest for redirected URLs
                        DAOFactory.get().getDao(HarvestDAO.class)
                        .updateFinishedHarvest(harvestId, Harvest.STATUS_FINISHED, 0, 0, 0);
                    }
                }
                // Update last_harvest for URL's
                DAOFactory.get().getDao(HarvestSourceDAO.class).updateLastHarvest(url, new Timestamp(System.currentTimeMillis()));

                // Add cr:firstSeen metadata predicate for redirected sources
                HarvestSourceDTO redirectedSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(url);
                if (redirectedSource != null && redirectedSource.getTimeCreated() != null) {
                    String firstSeen = dateFormat.format(redirectedSource.getTimeCreated());
                    ObjectDTO firstSeenObject = new ObjectDTO(firstSeen, true, XMLSchema.DATETIME);
                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                    .insertUpdateSourceMetadata(url, Predicates.CR_FIRST_SEEN, firstSeenObject);
                }
            }
        }

        return ret;
    }

    /**
     * @param message
     * @param harvestId
     * @throws DAOException
     *             if database query fails.
     */
    private void insertHarvestMessage(String message, int harvestId) throws DAOException {
        HarvestMessageDTO harvestMessageDTO = new HarvestMessageDTO();
        harvestMessageDTO.setHarvestId(new Integer(harvestId));
        harvestMessageDTO.setType(HarvestMessageType.INFO.toString());
        harvestMessageDTO.setMessage(message);
        harvestMessageDTO.setStackTrace("");
        DAOFactory.get().getDao(HarvestMessageDAO.class).insertHarvestMessage(harvestMessageDTO);
    }

    /**
     *
     * @throws ParserConfigurationException
     *             if the general config file is unparsable.
     * @throws SAXException
     *             if the XML isn't well-formed.
     * @throws IOException
     * @throws DAOException
     *             if database query fails.
     */

    private void setConversionModified(HttpURLConnection urlConnection) throws DAOException, IOException,
    ParserConfigurationException, SAXException {

        if (lastHarvest != null) {
            Boolean conversionModified = isConversionModifiedSinceLastHarvest();
            if (conversionModified == null || conversionModified.booleanValue() == false) {

                urlConnection.setIfModifiedSince(lastHarvest.getTime());
                if (conversionModified != null) {
                    logger.debug("The source's RDF conversion not modified since" + lastHarvest.toString());
                }
            } else {
                logger.debug("The source has an RDF conversion that has been modified since last harvest");
            }
        }

    }

    /**
     * Checks if the conversion script is modified since last harvest. Uses the instance variable 'sourceUrlString' and
     * 'lastHarvest'.
     *
     * @return true if conversion script is modified. Can return null, and the caller must decide what that means.
     * @throws ParserConfigurationException
     *             if the general config file is unparsable.
     * @throws SAXException
     *             if the XML isn't well-formed.
     * @throws IOException
     * @throws DAOException
     *             if database query fails.
     */
    private Boolean isConversionModifiedSinceLastHarvest() throws IOException, SAXException, ParserConfigurationException,
    DAOException {

        Boolean result = null;

        String schemaUri = daoFactory.getDao(HelperDAO.class).getSubjectSchemaUri(sourceUrlString);
        if (!StringUtils.isBlank(schemaUri)) {

            // see if schema has RDF conversion
            convParser = ConversionsParser.parseForSchema(schemaUri);
            if (!StringUtils.isBlank(convParser.getRdfConversionId())) {

                // see if the conversion XSL has changed since last harvest
                String xsl = convParser.getRdfConversionXslFileName();
                if (!StringUtils.isBlank(xsl)) {

                    String xslUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_XSL_URL);
                    xslUrl = MessageFormat.format(xslUrl, Util.toArray(xsl));
                    result = URLUtil.isModifiedSince(xslUrl, lastHarvest.getTime());
                }
            }
        }

        return result;
    }

    /**
     *
     * @param file
     * @param contentType
     * @throws IOException
     * @throws SAXException
     *             if the XML isn't well-formed.
     * @throws ParserConfigurationException
     *             if the general config file is unparsable.
     */
    private File preProcess(File file, String contentType) throws ParserConfigurationException, SAXException, IOException {

        // if content type declared to be application/rdf+xml, then believe it and go to parsing straight away
        if (contentType != null && contentType.startsWith("application/rdf+xml")) {
            this.rdfContentFound = true;
            return file;
        }

        // if conversion ID not yet detected by caller, detect it here by parsing the file as XML
        String conversionId = convParser == null ? null : convParser.getRdfConversionId();
        if (StringUtils.isBlank(conversionId)) {

            logger.debug("Trying to extract schema or DTD");

            XmlAnalysis xmlAnalysis = new XmlAnalysis();
            xmlAnalysis.parse(file);

            // get schema uri, if it's not found then fall back to dtd
            String schemaOrDtd = xmlAnalysis.getSchemaLocation();
            if (schemaOrDtd == null || schemaOrDtd.length() == 0) {
                schemaOrDtd = xmlAnalysis.getSystemDtd();
                if (schemaOrDtd == null || !URLUtil.isURL(schemaOrDtd)) {
                    schemaOrDtd = xmlAnalysis.getPublicDtd();
                }
            }

            // if no schema or DTD still found, assume the URI of the starting element to be the schema by which conversions should
            // be looked for
            if (schemaOrDtd == null || schemaOrDtd.length() == 0) {

                schemaOrDtd = xmlAnalysis.getStartElemUri();
                if (schemaOrDtd.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")) {

                    logger.debug("File seems to be RDF, going to parse like that");
                    return file;
                }
            }

            // if schema or DTD found, then get its RDF conversion ID
            if (!StringUtils.isBlank(schemaOrDtd)) {

                logger.debug("Found schema or DTD: " + schemaOrDtd);

                sourceMetadata.addObject(Predicates.CR_SCHEMA, new ObjectDTO(schemaOrDtd, false));
                convParser = ConversionsParser.parseForSchema(schemaOrDtd);
                if (convParser != null) {
                    conversionId = convParser.getRdfConversionId();
                }
            } else {
                logger.debug("No schema or DTD declared");
            }
        }

        // if no conversion found, still return the file for parsing as RDF (we know that at least it's XML, because otherwise a
        // SAXException would have been thrown above)
        if (StringUtils.isBlank(conversionId)) {
            logger.debug("No RDF conversion found!");
            return file;
        } else {
            logger.debug("Going to run the found RDF conversion (id = " + conversionId + ")");

            // prepare conversion URL
            String convertUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
            Object[] args = new String[2];
            args[0] = URLEncoder.encode(conversionId, "UTF-8");
            args[1] = URLEncoder.encode(sourceUrlString, "UTF-8");
            convertUrl = MessageFormat.format(convertUrl, args);

            // run conversion and save the response to file
            File convertedFile = new File(file.getAbsolutePath() + ".converted");
            FileUtil.downloadUrlToFile(convertUrl, convertedFile);
            this.rdfContentFound = true;

            // delete the original file
            deleteFileSafely(file);

            // return converted file
            return convertedFile;
        }
    }

    /**
     *
     * @param urlConnetion
     */
    private void setSourceMetadata(URLConnection urlConnection, HarvestSourceDTO source) {

        // set last-refreshed predicate
        long lastRefreshed = System.currentTimeMillis();
        String lastRefreshedStr = dateFormat.format(new Date(lastRefreshed));
        sourceMetadata.addObject(Predicates.CR_LAST_REFRESHED, new ObjectDTO(String.valueOf(lastRefreshedStr), true,
                XMLSchema.DATETIME));

        // detect the last-modified-date from HTTP response, if it's not >0, then take the value of last-refreshed
        sourceLastModified = urlConnection.getLastModified();
        if (sourceLastModified <= 0) {
            sourceLastModified = lastRefreshed;
        }

        // set the last-modified predicate
        String s = dateFormat.format(new Date(sourceLastModified));
        sourceMetadata.addObject(Predicates.CR_LAST_MODIFIED, new ObjectDTO(s, true, XMLSchema.DATETIME));

        int contentLength = urlConnection.getContentLength();
        if (contentLength >= 0) {
            sourceMetadata.addObject(Predicates.CR_BYTE_SIZE,
                    new ObjectDTO(String.valueOf(contentLength), true, XMLSchema.INTEGER));
        }

        // set the firstSeen predicate
        if (source != null && source.getTimeCreated() != null) {
            String firstSeen = dateFormat.format(source.getTimeCreated());
            sourceMetadata.addObject(Predicates.CR_FIRST_SEEN, new ObjectDTO(firstSeen, true, XMLSchema.DATETIME));
        }

        String contentType = urlConnection.getContentType();
        if (contentType != null && contentType.length() > 0) {
            String charset = null;
            int i = contentType.indexOf(";");
            if (i > 0) {
                int j = contentType.indexOf("charset=", i);
                if (j > i) {
                    int k = contentType.indexOf(";", j);
                    k = k < 0 ? contentType.length() : k;
                    charset = contentType.substring(j + "charset=".length(), k).trim();
                }
                contentType = contentType.substring(0, i).trim();
            }

            sourceMetadata.addObject(Predicates.CR_MEDIA_TYPE, new ObjectDTO(String.valueOf(contentType), true));
            String rdfTypeOfMediaType = MimeTypeConverter.getRdfTypeFor(contentType);
            if (!StringUtils.isBlank(rdfTypeOfMediaType)) {
                sourceMetadata.addObject(Predicates.RDF_TYPE, new ObjectDTO(String.valueOf(rdfTypeOfMediaType), false));
            }

            if (charset != null && charset.length() > 0) {
                sourceMetadata.addObject(Predicates.CR_CHARSET, new ObjectDTO(String.valueOf(charset), true));
            }
        }
    }

    /**
     * @return the sourceAvailable
     */
    public Boolean getSourceAvailable() {
        return sourceAvailable;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#doHarvestStartedActions()
     */
    protected void doHarvestStartedActions() throws HarvestException {

        logger.debug("Pull harvest started");
        super.doHarvestStartedActions();
    }

    /**
     *
     * @param sourceUrl
     * @param urgent
     * @return VirtuosoPullHarvest
     * @throws DAOException
     *             if database query fails.
     */
    public static PullHarvest createFullSetup(String sourceUrl, boolean urgent) throws DAOException {

        return createFullSetup(DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl), urgent);
    }

    /**
     * @param dto
     * @param urgent
     * @return VirtuosoPullHarvest
     * @throws DAOException
     *             if database query fails.
     */
    public static PullHarvest createFullSetup(HarvestSourceDTO dto, boolean urgent) throws DAOException {

        PullHarvest harvest = new PullHarvest(dto.getUrl(), urgent ? null : dto.getLastHarvest());

        harvest.setBatchHarvest(true);
        harvest.setUrgentHarvest(urgent);
        harvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class)
                .getLastHarvestBySourceId(dto.getSourceId().intValue()));
        harvest.setNotificationSender(new HarvestNotificationSender());

        harvest.setDaoWriter(new HarvestDAOWriter(dto.getSourceId().intValue(), Harvest.TYPE_PULL, CRUser.APPLICATION
                .getUserName()));

        return harvest;
    }

    public boolean isRecursiveHarvestDisabled() {
        return recursiveHarvestDisabled;
    }

    public void setRecursiveHarvestDisabled(boolean recursiveHarvestDisabled) {
        this.recursiveHarvestDisabled = recursiveHarvestDisabled;
    }

    public boolean isRdfContentFound() {
        return rdfContentFound;
    }

}
