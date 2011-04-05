package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.xml.sax.SAXException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.persist.PersisterFactory;
import eionet.cr.harvest.util.HarvestUrlConnection;
import eionet.cr.harvest.util.MimeTypeConverter;
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
 * @author heinljab
 * 
 */
public class VirtuosoPullHarvest extends Harvest {

    /**
     *
     */
    public static final int maxRedirectionsAllowed = 4;
    private boolean fullSetupMode = false;
    private boolean fullSetupModeUrgent = false;
    private boolean rdfContentFound = false;

    /** */
    private Boolean sourceAvailable = null;
    private Date lastHarvest = null;

    /** */
    private ConversionsParser convParser;

    /**
     * In case of redirected sources, each redirected source will be harvested
     * without additional recursive redirection.
     * */
    private boolean recursiveHarvestDisabled = false;

    /**
     * 
     * @param sourceUrlString
     * @param lastHarvest
     */
    public VirtuosoPullHarvest(String sourceUrlString, Date lastHarvest) {
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
        try {

            // reuse the file if it exists and the configuration allows to do it
            // (e.g. for debugging purposes) */
            if (file.exists()
                    && Boolean.parseBoolean(GeneralConfig.getProperty(
                            GeneralConfig.HARVESTER_USE_DOWNLOADED_FILES,
                            "false"))) {

                sourceAvailable = Boolean.TRUE;
                logger.debug("Harvesting the already downloaded file");
            } else {
                // delete the old file, should it exist
                if (file.exists()) {
                    file.delete();
                }
                // prepare URL connection
                harvestUrlConnection = HarvestUrlConnection
                        .getConnection(sourceUrlString);

                setConversionModified(harvestUrlConnection.getUrlConnection());

                // open connection stream
                logger.debug("Downloading");
                try {
                    harvestUrlConnection.openInputStream();
                    sourceAvailable = harvestUrlConnection.isSourceAvailable();
                } catch (Exception e) {
                    logger.warn(e.toString());
                }

                // FIXME: Last line of SELF-DEFENSE: The isSourceAvailable()
                // returns FALSE on *ALL* errors. That's way too agressive.
                // Until this is fixed, we don't delete sources.
                /*
                 * if (!harvestUrlConnection.isSourceAvailable()) { try {
                 * HarvestSourceDTO source =
                 * DAOFactory.get().getDao(HarvestSourceDAO
                 * .class).getHarvestSourceByUrl(sourceUrlString); if(source !=
                 * null && source.isPrioritySource()){ //Priority sources will
                 * not be deleted by system. Instead email will be sent to
                 * administrator.
                 * logger.debug(harvestUrlConnection.getSourceNotExistMessage()
                 * +
                 * ", will not delete the source because it is Priority source"
                 * ); throw new
                 * HarvestException(harvestUrlConnection.getSourceNotExistMessage
                 * (), new Throwable()); } else { daoWriter = null; // we dont't
                 * want finishing actions to be done
                 * logger.debug(harvestUrlConnection.getSourceNotExistMessage()
                 * + ", going to delete the source");
                 * DAOFactory.get().getDao(HarvestSourceDAO
                 * .class).queueSourcesForDeletion
                 * (Collections.singletonList(sourceUrlString)); } } catch
                 * (DAOException e) {
                 * logger.warn("Failure when deleting the source", e); } return;
                 * }
                 */

                // if source not available (i.e. link broken) then just set the
                // last-refreshed metadata
                if (harvestUrlConnection.isSourceAvailable() == false) {
                    setLastRefreshed(harvestUrlConnection.getConnection(),
                            System.currentTimeMillis());
                } else {
                    // source is available, so continue to extract it's contents
                    // and metadata

                    // extract various metadata about this harvest source from
                    // url connection object
                    setSourceMetadata(harvestUrlConnection.getConnection());

                    // if Url is redirected, take action.

                    if (harvestUrlConnection.getRedirectionInfo()
                            .isRedirected()
                            && harvestUrlConnection.isHttpConnection()
                            && recursiveHarvestDisabled == false) {
                        redirectedSourceHarvest(harvestUrlConnection);
                    }

                    // NOTE: If URL is redirected, content type is null.
                    // skip if unsupported content type

                    contentType = sourceMetadata
                            .getObjectValue(Predicates.CR_MEDIA_TYPE);
                    if (contentType != null
                            && !isSupportedContentType(contentType)) {
                        logger.debug("Unsupported response content type: "
                                + contentType);
                    } else {
                        logger.debug("Response content type was " + contentType);
                        if (harvestUrlConnection.isHttpConnection()
                                && harvestUrlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {

                            handleSourceNotModified();
                            return;
                        } else {
                            logger.debug("Streaming the content to file "
                                    + file);
                            // save the stream to file
                            totalBytes = FileUtil
                                    .streamToFile(harvestUrlConnection
                                            .getInputStream(), file);

                            // if content-length for source metadata was
                            // previously not found, then set it to file size
                            if (sourceMetadata
                                    .getObject(Predicates.CR_BYTE_SIZE) == null) {
                                sourceMetadata.addObject(
                                        Predicates.CR_BYTE_SIZE, new ObjectDTO(
                                                String.valueOf(totalBytes),
                                                true));
                            }
                        }
                    }
                } // if Source is available.
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new HarvestException(e.toString(), e);
        } finally {
            // close input stream
            try {
                // TODO: Provide close() method for harvestUrlConnection
                if (harvestUrlConnection != null
                        && harvestUrlConnection.getInputStream() != null)
                    harvestUrlConnection.getInputStream().close();
            } catch (IOException e) {
            }
        }

        // perform the harvest
        harvest(file, contentType, totalBytes);
    }

    /**
     * Harvest the given file.
     * 
     * @param file
     * @param contentType
     * @param fileSize
     * @throws HarvestException
     */
    private void harvest(File file, String contentType, int fileSize)
            throws HarvestException {

        // remember the file's absolute path, so we can later detect if a new
        // file was created during the pre-processing.
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
                    addToRepository(file);
                }
            } catch (Exception e) {
                throw new HarvestException(e.toString(), e);
            } finally {
                deleteDownloadedFile(file);
                deleteDownloadedFile(originalPath);
                deleteDownloadedFile(unGZipped);
            }
        }
    }

    /**
     * Loads the file into the repository (triple store). File is required to be
     * RDF.
     * 
     * @param file
     * @throws IOException
     * @throws RepositoryException
     * @throws RDFParseException
     */
    private void addToRepository(File file) throws RDFParseException,
            RepositoryException, IOException {

        String repoUrl = GeneralConfig
                .getRequiredProperty(GeneralConfig.VIRTUOSO_DB_URL);
        String repoUsr = GeneralConfig
                .getRequiredProperty(GeneralConfig.VIRTUOSO_DB_USR);
        String repoPwd = GeneralConfig
                .getRequiredProperty(GeneralConfig.VIRTUOSO_DB_PWD);

        boolean isSuccess = false;
        Repository repository = null;
        RepositoryConnection conn = null;
        try {
            repository = new VirtuosoRepository(repoUrl, repoUsr, repoPwd);
            repository.initialize();
            conn = repository.getConnection();

            // see http://www.openrdf.org/doc/sesame2/users/ch08.html#d0e1218
            // for what's a context
            org.openrdf.model.URI context = repository.getValueFactory()
                    .createURI(sourceUrlString);

            // start transaction
            conn.setAutoCommit(false);

            // clear previous triples of this context
            conn.clear(context);

            // add the file's contents into repository and under this context
            conn.add(file, sourceUrlString, RDFFormat.RDFXML, context);

            long tripleCount = conn.size(context);

            // add CR's auto-generated metadata
            int autoGenTripleCount = 0;
            // FIXME: Files that have no triples still generate harvesting
            // metadata.
            // The user must be able to see that the file is known.
            if (sourceMetadata.getPredicateCount() > 0) {
                logger.debug("Storing auto-generated triples for the source");
                autoGenTripleCount = addSourceMetadata(sourceMetadata, conn,
                        repository, context);
            }

            // commit transaction
            conn.commit();

            // set total stored triples count
            // FIXME: The autoGenTripleCount is not part of the harvested
            // content
            setStoredTriplesCount(Long.valueOf(tripleCount).intValue()
                    + autoGenTripleCount);

            // no transaction rollback needed, when reached this point
            isSuccess = true;
        } finally {
            if (!isSuccess && conn != null) {
                try {
                    conn.rollback();
                } catch (RepositoryException e) {
                }
            }

            if (conn != null) {
                try {
                    conn.close();
                } catch (RepositoryException e) {
                }
            }

            if (repository != null) {
                try {
                    repository.shutDown();
                } catch (RepositoryException e) {
                }
            }
        }
    }

    /**
     * Adds the meta information the harvester has collected about the source.
     * The meta data is considered part of the harvester and not the source.
     * Therefore the meta data is stored in the harvester's named graph (or
     * context)
     * 
     * @param subjectDTO
     * @param conn
     * @param rep
     * @param contextURI
     * @return
     * @throws RepositoryException
     * @throws Exception
     */
    private int addSourceMetadata(SubjectDTO subjectDTO,
            RepositoryConnection conn, Repository rep, URI contextURI)
            throws RepositoryException {

        // FIXME: The contextURI is always the harvester URI
        // (which is generated from the deployment hostname).
        int statementsAdded = 0;
        if (subjectDTO != null && subjectDTO.getPredicateCount() > 0) {

            URI subject = rep.getValueFactory().createURI(subjectDTO.getUri());
            for (String predicateUri : subjectDTO.getPredicates().keySet()) {

                Collection<ObjectDTO> objects = subjectDTO
                        .getObjects(predicateUri);
                if (objects != null && !objects.isEmpty()) {

                    URI predicate = rep.getValueFactory().createURI(
                            predicateUri);
                    for (ObjectDTO object : objects) {
                        if (object.isLiteral()) {
                            Literal literalObject = rep.getValueFactory()
                                    .createLiteral(object.toString());
                            conn.add(subject, predicate, literalObject,
                                    contextURI);
                        } else {
                            URI resourceObject = rep.getValueFactory()
                                    .createURI(object.toString());
                            conn.add(subject, predicate, resourceObject,
                                    contextURI);
                        }

                        statementsAdded++;
                    }
                }
            }

            // if (statementsAdded > 0) {
            // addResource(Harvest.HARVESTER_URI,
            // Hashes.spoHash(Harvest.HARVESTER_URI), true);
            // }
        }
        return statementsAdded;
    }

    /**
     * Check if the content-type is one that is supported.
     * 
     * @param contentType
     * @return true if content-type is supported
     */
    private boolean isSupportedContentType(String contentType) {

        return contentType.startsWith("text/xml")
                || contentType.startsWith("application/xml")
                || contentType.startsWith("application/rdf+xml")
                || contentType.startsWith("application/atom+xml")
                || contentType.startsWith("application/octet-stream")
                || contentType.startsWith("application/x-gzip");
    }

    /**
     * Check if the content-type is one of the XML types.
     * 
     * @param contentType
     * @return true if content-type is XML
     */
    private boolean isXmlContentType(String contentType) {

        return contentType.startsWith("text/xml")
                || contentType.startsWith("application/xml")
                || contentType.startsWith("application/rdf+xml")
                || contentType.startsWith("application/atom+xml");
    }

    /**
     * @throws SQLException
     * 
     */
    private void handleSourceNotModified() throws SQLException {

        // update lastRefreshed predicate for this source
        PersisterFactory.getPersister().updateLastRefreshed(
                Hashes.spoHash(sourceUrlString), lastRefreshedDateFormat);

        // copy the harvest's number of triples and resources from previous
        // harvest
        if (previousHarvest != null) {
            setStoredTriplesCount(previousHarvest.getTotalStatements());
            setDistinctSubjectsCount(previousHarvest.getTotalResources());
        }

        String msg = "Source not modified since " + lastHarvest.toString();
        logger.debug(msg);
        infos.add(msg);
    }

    /**
     * Performs the harvesting for redirected harvests.
     * 
     * @throws DAOException
     * @throws HarvestException
     * @throws MalformedURLException
     */
    private void redirectedSourceHarvest(
            HarvestUrlConnection harvestUrlConnection) throws DAOException,
            HarvestException, MalformedURLException {

        logger.debug("Going to handle redirected harvest");

        int redirectionsFound = -1;

        // Load full information about the initial source.
        HarvestSourceDTO originalSource = DAOFactory.get()
                .getDao(HarvestSourceDAO.class)
                .getHarvestSourceByUrl(sourceUrlString);

        // The case when the original URL has been redirected to a new source.
        // Before continuing with harvesting, the potential chain of further
        // redirections
        // is going to be analyzed and limited if necessary.

        String targetUrlNormalized = StringUtils.substringBefore(
                harvestUrlConnection.getRedirectionInfo().getTargetURL(), "#");
        String directedUrl = new URL(targetUrlNormalized).toString();
        UrlRedirectionInfo lastUrl = UrlRedirectAnalyzer
                .analyzeUrlRedirection(directedUrl);

        List<UrlRedirectionInfo> redirectedUrls = new ArrayList<UrlRedirectionInfo>();
        redirectedUrls.add(lastUrl);

        while (lastUrl.isRedirected() == true) {

            lastUrl = UrlRedirectAnalyzer.analyzeUrlRedirection(lastUrl
                    .getTargetURL());
            redirectedUrls.add(lastUrl);
            redirectionsFound = redirectedUrls.size();

            // Checking the count of redirects.
            if (redirectionsFound > VirtuosoPullHarvest.maxRedirectionsAllowed) {
                throw new HarvestException("Too many redirections for url: "
                        + sourceUrlString + ". Found " + redirectionsFound
                        + ", allowed "
                        + VirtuosoPullHarvest.maxRedirectionsAllowed);
            }
        }

        // Going to harvest all directed url's
        for (int i = 0; i < redirectedUrls.size(); i++) {

            UrlRedirectionInfo current = redirectedUrls.get(i);
            HarvestSourceDTO directedSource = DAOFactory.get()
                    .getDao(HarvestSourceDAO.class)
                    .getHarvestSourceByUrl(current.getSourceURL());

            if (directedSource != null) {

                // Checking if directedSource has larger interval_minutes value
                // compared to original and updating accordingly.
                if (directedSource.getIntervalMinutes() > originalSource
                        .getIntervalMinutes()) {

                    directedSource.setIntervalMinutes(originalSource
                            .getIntervalMinutes());

                    // Saving the updated source to database.
                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                            .editSource(directedSource);
                }
            } else {
                directedSource = new HarvestSourceDTO();
                directedSource.setPrioritySource(true);
                directedSource.setIntervalMinutes(originalSource
                        .getIntervalMinutes());
                directedSource.setUrl(current.getSourceURL());

                Integer sourceId = DAOFactory.get()
                        .getDao(HarvestSourceDAO.class)
                        .addSource(directedSource);

                directedSource.setSourceId(sourceId.intValue());
            }

            long now = System.currentTimeMillis();
            long lastHarvestTime = directedSource.getLastHarvest() == null ? 0
                    : directedSource.getLastHarvest().getTime();
            long sinceLastHarvest = now - lastHarvestTime;
            long harvestIntervalMillis = directedSource.getIntervalMinutes() == null ? 0L
                    : directedSource.getIntervalMinutes().longValue() * 60L * 1000L;

            // The conditions applies to current url only.
            // If "current" is not harvested, the one following the "current" is
            // still attempted.

            if (lastHarvestTime == 0
                    || this instanceof VirtuosoInstantHarvest
                    || (lastHarvestTime > 0 && sinceLastHarvest > harvestIntervalMillis)) {

                VirtuosoPullHarvest harvest = null;

                // The flag for fullSetupMode is set in createFullSetup
                // when initializing the harvest that way. The value for Urgent
                // is also received there.
                if (this.isFullSetupMode()) {
                    harvest = createFullSetup(directedSource,
                            this.isFullSetupModeUrgent());
                } else {
                    harvest = new VirtuosoPullHarvest(directedSource.getUrl(),
                            null);
                }

                // Setting the flag to not allow it recursively harvest their
                // followers.
                // During the harvest of this instance, the code won't reach
                // this block.
                harvest.setRecursiveHarvestDisabled(true);
                harvest.execute();
            }
        }
    }

    /**
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws DAOException
     */

    private void setConversionModified(HttpURLConnection urlConnection)
            throws DAOException, IOException, ParserConfigurationException,
            SAXException {

        if (lastHarvest != null) {
            Boolean conversionModified = isConversionModifiedSinceLastHarvest();
            if (conversionModified == null
                    || conversionModified.booleanValue() == false) {

                urlConnection.setIfModifiedSince(lastHarvest.getTime());
                if (conversionModified != null) {
                    logger.debug("The source's RDF conversion not modified since"
                            + lastHarvest.toString());
                }
            } else {
                logger.debug("The source has an RDF conversion that has been modified since last harvest");
            }
        }

    }

    /**
     * 
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     * @throws DAOException
     */
    private Boolean isConversionModifiedSinceLastHarvest() throws IOException,
            SAXException, ParserConfigurationException, DAOException {

        Boolean result = null;

        String schemaUri = daoFactory.getDao(HelperDAO.class)
                .getSubjectSchemaUri(sourceUrlString);
        if (!StringUtils.isBlank(schemaUri)) {

            // see if schema has RDF conversion
            convParser = ConversionsParser.parseForSchema(schemaUri);
            if (!StringUtils.isBlank(convParser.getRdfConversionId())) {

                // see if the conversion XSL has changed since last harvest
                String xsl = convParser.getRdfConversionXslFileName();
                if (!StringUtils.isBlank(xsl)) {

                    String xslUrl = GeneralConfig
                            .getRequiredProperty(GeneralConfig.XMLCONV_XSL_URL);
                    xslUrl = MessageFormat.format(xslUrl, Util.toArray(xsl));
                    result = URLUtil.isModifiedSince(xslUrl,
                            lastHarvest.getTime());
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
     * @throws ParserConfigurationException
     */
    private File preProcess(File file, String contentType)
            throws ParserConfigurationException, SAXException, IOException {

        // if content type declared to be application/rdf+xml, then believe it
        // and go to parsing
        // straight away
        if (contentType != null
                && contentType.startsWith("application/rdf+xml")) {
            return file;
        }

        // if conversion ID not yet detected by caller, detect it here by
        // parsing the
        // file as XML
        String conversionId = convParser == null ? null : convParser
                .getRdfConversionId();
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

            // if no schema or DTD still found, assume the URI of the starting
            // element
            // to be the schema by which conversions should be looked for
            if (schemaOrDtd == null || schemaOrDtd.length() == 0) {

                schemaOrDtd = xmlAnalysis.getStartElemUri();
                if (schemaOrDtd
                        .equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")) {

                    logger.debug("File seems to be RDF, going to parse like that");
                    return file;
                }
            }

            // if schema or DTD found, then get its RDF conversion ID
            if (!StringUtils.isBlank(schemaOrDtd)) {

                logger.debug("Found schema or DTD: " + schemaOrDtd);

                sourceMetadata.addObject(Predicates.CR_SCHEMA, new ObjectDTO(
                        schemaOrDtd, false));
                convParser = ConversionsParser.parseForSchema(schemaOrDtd);
                if (convParser != null) {
                    conversionId = convParser.getRdfConversionId();
                }
            } else {
                logger.debug("No schema or DTD declared");
            }
        }

        // if no conversion found, still return the file for parsing as RDF
        // (we know that at least it's XML, because otherwise a SAXException
        // would have been thrown above)
        if (StringUtils.isBlank(conversionId)) {

            logger.debug("No RDF conversion found, trying to parse as RDF");
            return file;
        } else {
            logger.debug("Going to run the found RDF conversion (id = "
                    + conversionId + ")");

            // prepare conversion URL

            String convertUrl = GeneralConfig
                    .getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
            Object[] args = new String[2];
            args[0] = URLEncoder.encode(conversionId);
            args[1] = URLEncoder.encode(sourceUrlString);
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
     * @param urlConnection
     *            is not used TODO
     */
    private void setLastRefreshed(URLConnection urlConnection,
            long lastRefreshedTime) {

        String lastRefreshed = lastRefreshedDateFormat.format(new Date(
                lastRefreshedTime));
        sourceMetadata.addObject(Predicates.CR_LAST_REFRESHED, new ObjectDTO(
                String.valueOf(lastRefreshed), true));
    }

    /**
     * 
     * @param urlConnetion
     */
    private void setSourceMetadata(URLConnection urlConnection) {

        // set last-refreshed predicate
        long lastRefreshed = System.currentTimeMillis();
        setLastRefreshed(urlConnection, lastRefreshed);

        // detect the last-modified-date from HTTP response, if it's not >0,
        // then take the value of last-refreshed
        sourceLastModified = urlConnection.getLastModified();
        if (sourceLastModified <= 0) {
            sourceLastModified = lastRefreshed;
        }

        // set the last-modified predicate
        String s = lastRefreshedDateFormat.format(new Date(sourceLastModified));
        sourceMetadata.addObject(Predicates.CR_LAST_MODIFIED, new ObjectDTO(s,
                true));

        int contentLength = urlConnection.getContentLength();
        if (contentLength >= 0) {
            sourceMetadata.addObject(Predicates.CR_BYTE_SIZE, new ObjectDTO(
                    String.valueOf(contentLength), true));
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
                    charset = contentType.substring(j + "charset=".length(), k)
                            .trim();
                }
                contentType = contentType.substring(0, i).trim();
            }

            sourceMetadata.addObject(Predicates.CR_MEDIA_TYPE, new ObjectDTO(
                    String.valueOf(contentType), true));
            String rdfTypeOfMediaType = MimeTypeConverter
                    .getRdfTypeFor(contentType);
            if (!StringUtils.isBlank(rdfTypeOfMediaType)) {
                sourceMetadata.addObject(Predicates.RDF_TYPE, new ObjectDTO(
                        String.valueOf(rdfTypeOfMediaType), false));
            }

            if (charset != null && charset.length() > 0) {
                sourceMetadata.addObject(Predicates.CR_CHARSET, new ObjectDTO(
                        String.valueOf(charset), true));
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
            if (GeneralConfig.getProperty(
                    GeneralConfig.HARVESTER_DELETE_DOWNLOADED_FILES, "true")
                    .equals("true")) {
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
    public static VirtuosoPullHarvest createFullSetup(String sourceUrl,
            boolean urgent) throws DAOException {

        return createFullSetup(DAOFactory.get().getDao(HarvestSourceDAO.class)
                .getHarvestSourceByUrl(sourceUrl), urgent);
    }

    /**
     * 
     * @param dto
     * @param urgent
     * @return VirtuosoPullHarvest
     * @throws DAOException
     */
    public static VirtuosoPullHarvest createFullSetup(HarvestSourceDTO dto,
            boolean urgent) throws DAOException {

        VirtuosoPullHarvest harvest = new VirtuosoPullHarvest(dto.getUrl(),
                urgent ? null : dto.getLastHarvest());

        harvest.setFullSetupMode(true);
        harvest.setFullSetupModeUrgent(urgent);
        harvest.setPreviousHarvest(DAOFactory.get().getDao(HarvestDAO.class)
                .getLastHarvestBySourceId(dto.getSourceId().intValue()));
        harvest.setNotificationSender(new HarvestNotificationSender());

        int numOfResources = dto.getResources() == null ? 0 : dto
                .getResources().intValue();
        harvest.setDaoWriter(new HarvestDAOWriter(dto.getSourceId().intValue(),
                Harvest.TYPE_PULL, numOfResources, CRUser.application
                        .getUserName()));

        return harvest;
    }

    public boolean isRecursiveHarvestDisabled() {
        return recursiveHarvestDisabled;
    }

    public void setRecursiveHarvestDisabled(boolean recursiveHarvestDisabled) {
        this.recursiveHarvestDisabled = recursiveHarvestDisabled;
    }

    public boolean isFullSetupMode() {
        return fullSetupMode;
    }

    public void setFullSetupMode(boolean fullSetupMode) {
        this.fullSetupMode = fullSetupMode;
    }

    public boolean isFullSetupModeUrgent() {
        return fullSetupModeUrgent;
    }

    public void setFullSetupModeUrgent(boolean fullSetupModeUrgent) {
        this.fullSetupModeUrgent = fullSetupModeUrgent;
    }

    public boolean isRdfContentFound() {
        return rdfContentFound;
    }
}
