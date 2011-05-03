package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryException;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
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
import eionet.cr.harvest.persist.PersisterFactory;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.harvest.util.HarvestUrlConnection;
import eionet.cr.harvest.util.MimeTypeConverter;
import eionet.cr.util.ConnectionError;
import eionet.cr.util.ConnectionError.ErrType;
import eionet.cr.util.FileUtil;
import eionet.cr.util.GZip;
import eionet.cr.util.Hashes;
import eionet.cr.util.URLUtil;
import eionet.cr.util.UrlRedirectAnalyzer;
import eionet.cr.util.UrlRedirectionInfo;
import eionet.cr.util.Util;
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

        File file = fullFilePathForSourceUrl(sourceUrlString);

        String contentType = null;
        int totalBytes = 0;

        HarvestUrlConnection harvestUrlConnection = null;
        needsHarvesting = true;

        try {
            // reuse the file if it exists and the configuration allows to do it (e.g. for debugging purposes) */
            if (file.exists()
                    && Boolean.parseBoolean(GeneralConfig.getProperty(GeneralConfig.HARVESTER_USE_DOWNLOADED_FILES, "false"))) {

                sourceAvailable = Boolean.TRUE;
                logger.debug("Harvesting the already downloaded file");
            } else {
                // delete the old file, should it exist
                if (file.exists()) {
                    file.delete();
                }

                // Resolve redirections
                redirectedUrls = resolveRedirects();

                if (redirectedUrls != null && redirectedUrls.size() > 0) {
                    isRedirectedSource = true;

                    // Change sourceUrlString to the actual source that will be harvested
                    sourceMetadata.setUri(sourceUrlString);
                    logger.setHarvestSourceUrl(sourceUrlString);
                }

                // Source that will be harvested
                HarvestSourceDTO source = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrlString);

                if (isBatchHarvest() && !isUrgentHarvest()) {
                    try {
                        if (source != null) {
                            if (source.isPermanentError()) {
                                daoWriter = null; // we dont't want finishing actions to be done
                                if (source.isPrioritySource()) {
                                    DAOFactory.get().getDao(HarvestSourceDAO.class).increaseUnavailableCount(source.getSourceId());
                                    String err = "Source: " + source.getUrl() + " has permanent error. Will not delete the source because it is Priority source";
                                    logger.debug(err);
                                    throw new HarvestException(err, new Throwable());
                                } else {
                                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                                            .queueSourcesForDeletion(Collections.singletonList(sourceUrlString));
                                }
                                return;
                            } else if (source.getCountUnavail() >= 5) {
                                if (!source.isPrioritySource()) {
                                    daoWriter = null; // we dont't want finishing actions to be done
                                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                                            .queueSourcesForDeletion(Collections.singletonList(sourceUrlString));
                                    return;
                                }
                            }
                        }
                    } catch (DAOException e) {
                        logger.warn("Failure when deleting the source", e);
                    }
                }

                // Source shouldn't be harvested if it redirects to another source and the destination source is recently harvested
                // (within its harvesting minutes)
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

                    ConnectionError error = harvestUrlConnection.getError();
                    if (error != null) {
                        sourceAvailable = false;
                        if (error.getType().equals(ErrType.PERMANENT)) {
                            permanentError = true;
                            DAOFactory.get().getDao(HarvestSourceDAO.class).deleteSourceTriples(sourceUrlString);

                            DAOFactory.get().getDao(HarvestSourceDAO.class).removeAllPredicatesFromHarvesterContext(sourceUrlString);
                        }

                        ObjectDTO errorObject = new ObjectDTO(error.getMessage(), true);
                        DAOFactory.get().getDao(HarvestSourceDAO.class)
                                .insertUpdateSourceMetadata(sourceUrlString, Predicates.CR_ERROR_MESSAGE, errorObject);

                        String lastRefreshed = lastRefreshedDateFormat.format(new Date(System.currentTimeMillis()));
                        ObjectDTO lastRefreshedObject = new ObjectDTO(lastRefreshed, true, XMLSchema.DATETIME);
                        DAOFactory.get().getDao(HarvestSourceDAO.class)
                                .insertUpdateSourceMetadata(sourceUrlString, Predicates.CR_LAST_REFRESHED, lastRefreshedObject);
                    }

                    if (sourceAvailable && needsHarvesting) {
                        // source is available, so continue to extract it's contents and metadata

                        // extract various metadata about this harvest source from url connection object
                        setSourceMetadata(harvestUrlConnection.getConnection());

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
                                logger.debug("Streaming the content to file " + file);
                                // save the stream to file
                                totalBytes = FileUtil.streamToFile(harvestUrlConnection.getInputStream(), file);

                                // if content-length for source metadata was previously not found, then set it to file size
                                if (sourceMetadata.getObject(Predicates.CR_BYTE_SIZE) == null) {
                                    sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(String.valueOf(totalBytes),
                                            true));
                                }
                            }
                        }

                        // Remove old triples
                        DAOFactory.get().getDao(HarvestSourceDAO.class).deleteSourceTriples(sourceUrlString);

                        // Remove old predicates from /harvester context
                        DAOFactory.get().getDao(HarvestSourceDAO.class).removeAllPredicatesFromHarvesterContext(sourceUrlString);

                        // Insert auto generated metadata into repository
                        DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceMetadata(sourceMetadata);

                    } // if Source is available.
                } else {
                    // Harvest source redirects to another source that has recently been updated therefore we can set these values
                    // TRUE
                    sourceAvailable = Boolean.TRUE;
                    rdfContentFound = Boolean.TRUE;
                }
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

        if (sourceAvailable && needsHarvesting) {
            // Perform the harvest. Only extract sources if we got some triples.
            // Otherwise it is a waste of time.
            if (harvest(file, contentType, totalBytes) > 0) {
                extractNewHarvestSources();
            }
        } else if (!needsHarvesting) {
            logger.debug("Source redirects to another source that has recently been harvested! Will not harvest.");
        }
    }

    /**
     * Harvest the given file.
     *
     * @param file - file on filesystem containing the data.
     * @param contentType
     * @param fileSize
     * @return the number of triples in source
     * @throws HarvestException
     */
    private int harvest(File file, String contentType, int fileSize) throws HarvestException {

        int tripleCount = 0;
        // remember the file's absolute path, so we can later detect if a new file was created during the pre-processing.
        String originalPath = file.getAbsolutePath();

        File unGZipped = null;
        if (fileSize == 0) {
            file = null;
            logger.debug("File size = 0");
        } else {
            // see if file is zipped, and if yes, then unzip
            unGZipped = unCompressGZip(file);
            if (unGZipped != null) {
                logger.debug("The file was gzipped, going to process unzipped file now");
                file = unGZipped;
            } else if (contentType != null && !isXmlContentType(contentType)) {
                file = null;
            }
        }

        if (file != null && file.exists()) {

            rdfContentFound = true;

            try {
                file = preProcess(file, contentType);
                if (file != null) {
                    tripleCount = DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceToRepository(file, sourceUrlString);
                    setStoredTriplesCount(tripleCount);
                }
            } catch (Exception e) {
                // We try to harvest XML files also, but if it fails, we do not throw an error.
            } finally {
                deleteDownloadedFile(file);
                deleteDownloadedFile(originalPath);
                deleteDownloadedFile(unGZipped);
            }
        }
        return tripleCount;
    }

    /**
     * Searches new harvest sources from harvested file. If resource is subclass of cr:File and doesn't yet exist, then it is
     * considered as new source.
     *
     * @throws HarvestException
     */
    private void extractNewHarvestSources() throws HarvestException {

        try {
            // calculate harvest interval for tracked files
            Integer interval = Integer.valueOf(GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
                    String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL)));

            List<String> newSources = DAOFactory.get().getDao(HarvestSourceDAO.class).getNewSources(sourceUrlString);

            for (String sourceUrl : newSources) {
                // escape spaces in URLs
                sourceUrl = URLUtil.replaceURLSpaces(sourceUrl);
                
                HarvestSourceDTO source = new HarvestSourceDTO();
                source.setUrl(sourceUrl);
                source.setUrlHash(Hashes.spoHash(sourceUrl));
                source.setIntervalMinutes(interval);

                DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);
            }

        } catch (Exception e) {
            throw new HarvestException(e.toString(), e);
        }
    }

    /**
     * Check if the content-type is one that is supported.
     *
     * @param contentType
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
     * @throws SQLException
     *
     */
    private void handleSourceNotModified() throws SQLException {

        // update lastRefreshed predicate for this source
        PersisterFactory.getPersister().updateLastRefreshed(Hashes.spoHash(sourceUrlString), lastRefreshedDateFormat);

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
     */
    private boolean shouldBeHarvested() throws DAOException {

        boolean ret = false;

        // Load full information about the final source.
        HarvestSourceDTO finalSource = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrlString);

        long now = System.currentTimeMillis();
        long lastHarvestTime = finalSource.getLastHarvest() == null ? 0 : finalSource.getLastHarvest().getTime();
        long sinceLastHarvest = now - lastHarvestTime;
        long harvestIntervalMillis = finalSource.getIntervalMinutes() == null ? 0L
                : finalSource.getIntervalMinutes().longValue() * 60L * 1000L;

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
            String lastRefreshed = lastRefreshedDateFormat.format(new Date(System.currentTimeMillis()));
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
                            int harvestId = DAOFactory
                                    .get()
                                    .getDao(HarvestDAO.class)
                                    .insertStartedHarvest(redirectedSource.getSourceId(), Harvest.TYPE_PULL,
                                            CRUser.APPLICATION.getUserName(), Harvest.STATUS_STARTED);
                            // If final source, then update DAO Writer harvestId
                            daoWriter.setHarvestId(harvestId);
                        }
                    } else {
                        // Insert harvest
                        int harvestId = DAOFactory
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
            }
        }

        return ret;
    }

    /**
     * @param message
     * @param harvestId
     * @throws DAOException
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
     * @throws ParserConfigurationException if the general config file is unparsable.
     * @throws SAXException
     * @throws IOException
     * @throws DAOException
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
     * Checks if the conversion script is modified since last harvest. Uses the instance variable 'sourceUrlString'
     * and 'lastHarvest'.
     *
     * @return true if conversion script is modified. Can return null, and the caller must decide what that means.
     * @throws ParserConfigurationException if the general config file is unparsable.
     * @throws SAXException
     * @throws IOException
     * @throws DAOException
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

    private File unCompressGZip(File file) {

        File unPackedFile = null;

        // Testing whether the input file is GZip or not.
        if (GZip.isFileGZip(file)) {
            try {
                unPackedFile = GZip.unPack(file);
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }
        return unPackedFile;
    }

    /**
     *
     * @param file
     * @param contentType
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException if the general config file is unparsable.
     */
    private File preProcess(File file, String contentType) throws ParserConfigurationException, SAXException, IOException {

        // if content type declared to be application/rdf+xml, then believe it and go to parsing straight away
        if (contentType != null && contentType.startsWith("application/rdf+xml")) {
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
            rdfContentFound = false;
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

            // delete the original file
            deleteDownloadedFile(file);

            // return converted file
            return convertedFile;
        }
    }

    /**
     *
     * @param urlConnetion
     */
    private void setSourceMetadata(URLConnection urlConnection) {

        // set last-refreshed predicate
        long lastRefreshed = System.currentTimeMillis();
        String lastRefreshedStr = lastRefreshedDateFormat.format(new Date(lastRefreshed));
        sourceMetadata.addObject(Predicates.CR_LAST_REFRESHED, new ObjectDTO(String.valueOf(lastRefreshedStr), true, XMLSchema.DATETIME));

        // detect the last-modified-date from HTTP response, if it's not >0, then take the value of last-refreshed
        sourceLastModified = urlConnection.getLastModified();
        if (sourceLastModified <= 0) {
            sourceLastModified = lastRefreshed;
        }

        // set the last-modified predicate
        String s = lastRefreshedDateFormat.format(new Date(sourceLastModified));
        sourceMetadata.addObject(Predicates.CR_LAST_MODIFIED, new ObjectDTO(s, true, XMLSchema.DATETIME));

        int contentLength = urlConnection.getContentLength();
        if (contentLength >= 0) {
            sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(String.valueOf(contentLength), true, XMLSchema.INTEGER));
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
     *
     * @param path
     */
    private void deleteDownloadedFile(String path) {
        deleteDownloadedFile(new File(path));
    }

    /**
     *
     * @param file
     */
    private void deleteDownloadedFile(File file) {

        if (file == null || !file.exists())
            return;

        try {
            // delete unless the configuration requires otherwise
            if (GeneralConfig.getProperty(GeneralConfig.HARVESTER_DELETE_DOWNLOADED_FILES, "true").equals("true")) {
                file.delete();
            }
        } catch (RuntimeException e) {
            logger.error(e.toString(), e);
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
     */
    public static PullHarvest createFullSetup(String sourceUrl, boolean urgent) throws DAOException {

        return createFullSetup(DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl), urgent);
    }

    /**
     *
     * @param dto
     * @param urgent
     * @return VirtuosoPullHarvest
     * @throws DAOException
     */
    public static PullHarvest createFullSetup(HarvestSourceDTO dto, boolean urgent) throws DAOException {

        PullHarvest harvest = new PullHarvest(dto.getUrl(), urgent ? null : dto.getLastHarvest());

        harvest.setBatchHarvest(true);
        harvest.setUrgentHarvest(urgent);
        harvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class).getLastHarvestBySourceId(dto.getSourceId().intValue()));
        harvest.setNotificationSender(new HarvestNotificationSender());

        harvest.setDaoWriter(new HarvestDAOWriter(dto.getSourceId().intValue(), Harvest.TYPE_PULL, CRUser.APPLICATION.getUserName()));

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
