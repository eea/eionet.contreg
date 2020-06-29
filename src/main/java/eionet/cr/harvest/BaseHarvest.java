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

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.*;
import eionet.cr.dto.*;
import eionet.cr.dto.HarvestScriptDTO.Phase;
import eionet.cr.dto.HarvestScriptDTO.TargetType;
import eionet.cr.dto.enums.HarvestScriptType;
import eionet.cr.harvest.load.ContentLoader;
import eionet.cr.harvest.load.FeedFormatLoader;
import eionet.cr.harvest.load.RDFFormatLoader;
import eionet.cr.harvest.service.ExternalService;
import eionet.cr.harvest.service.ExternalServiceFactory;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.harvest.util.RDFMediaTypes;
import eionet.cr.util.EMailSender;
import eionet.cr.util.FileDeletionJob;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.cleanup.HarvestSourcesCleanupJob;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.util.xml.ConversionSchema;
import eionet.cr.util.xml.ConversionSchema.Type;
import eionet.cr.web.action.admin.harvestscripts.HarvestScriptParser;
import eionet.cr.web.security.CRUser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.rdfxml.RDFXMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.io.*;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.*;

/**
 * Base abstract class for harvest-performing classes.
 *
 * @author Jaanus Heinlaid
 */
public abstract class BaseHarvest implements Harvest {

    /**
     * Static logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseHarvest.class);

    /**
     * No of latest harvests whose history is kept in the database. Used in houskeeping.
     */
    protected static final int NO_OF_LAST_HARVESTS_PRESERVED = 10;

    /**
     * Default harvesting timeout (36 hours = 129600000 ms) if no last harvest duration could be detected.
     */
    protected static final int DEFAULT_HARVEST_TIMEOUT = 129600000;

    /**
     * Minimum possible harvest timeout (10 min = 600000 ms).
     */
    protected static final int MINIMUM_HARVEST_TIMEOUT = 600000;

    /**
     * A harvest is expected to take no more than the duration of last harvest multiplied by this constnat.
     */
    protected static final double HARVEST_TIMEOUT_MULTIPLIER = 1.2;

    /**
     * Text/plain content type.
     */
    protected static final String CONTENT_TYPE_TEXT = "text/plain";

    /**
     * Turtle file extension.
     */
    protected static final String EXT_TTL = "ttl";

    /**
     * N3 file extension.
     */
    protected static final String EXT_N3 = "n3";

    /**
     * N-Triples file extension.
     */
    protected static final String EXT_NTRIPLES = "nt";

    /**
     * container for redirected source DTOs.
     */
    protected final List<HarvestSourceDTO> redirectedHarvestSources = new ArrayList<HarvestSourceDTO>();

    /** */
    private final HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
    private final HarvestDAO harvestDAO = DAOFactory.get().getDao(HarvestDAO.class);
    private final HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);
    private final HarvestMessageDAO harvestMessageDAO = DAOFactory.get().getDao(HarvestMessageDAO.class);
    private final ExternalServiceDAO externalServiceDAO = DAOFactory.get().getDao(ExternalServiceDAO.class);

    /**
     * The currently harvested URL. In case of redirections, this is the current redirected-to-URL that is baeing handled.
     */
    private String contextUrl;

    /**
     * The harvest source DTO object mnatching the {@link #contextUrl}.
     */
    private HarvestSourceDTO contextSourceDTO;

    /**
     * The metadata of the currently harvest source as in triple store.
     */
    private SubjectDTO sourceMetadata;

    /**
     * The current harvest's ID, assigned at the harvest's start.
     */
    private int harvestId;

    /**
     * List of messages collected during the harvest and saved into the DB.
     */
    private List<HarvestMessageDTO> harvestMessages = new ArrayList<HarvestMessageDTO>();

    /**
     * If true, all previously present harvest source metadata should be purged from the triple store.
     */
    private boolean cleanAllPreviousSourceMetadata;

    /**
     * If true, all triples must be removed from the graph represented by the harvested source in the harvest-finish stage.
     */
    private boolean clearTriplesInHarvestFinish;

    /**
     * The number of triples stored during this harvest. This does NOT include the generated harvest source metadata!
     */
    private int storedTriplesCount;

    /**
     * True if the current harvest was initiated by the user (as opposed to batch harvester in the background) .
     */
    protected boolean isOnDemandHarvest;

    /**
     * The user who initiated the current harvest (if this is is an on-edmand harvest).
     */
    private String harvestUser;

    /**
     * Last harvest duration in milliseconds.
     */
    private long lastHarvestDuration;

    /**
     * True if a fatal error occurred during this harvest, otherwise false.
     */
    protected boolean isFatalErrorOccured = false;

    /**
     * HTTP response code returned from the harvest source.
     */
    protected int httpResponseCode;

    /**
     * The timeout value of this harvest. Initialized at first access to the getter.
     */
    private Integer timeout;

    /**
     * Harvest status classifier for started and on-going harvests.
     */
    public static final String STATUS_STARTED = "started";

    /**
     * Harvest status classifier for properly finished harvests.
     */
    public static final String STATUS_FINISHED = "finished";

    /**
     * Harvest status classifier for abandoned (e.g. killed by Tomcat restart) harvests.
     */
    public static final String STATUS_ABANDONED = "abandoned";

    /**
     * Classifier for indicating harvests where the content is being pulled from the source.
     */
    public static final String TYPE_PULL = "pull";

    /**
     * Classifier for indicating harvests where the content is being pushed by the source.
     */
    public static final String TYPE_PUSH = "push";

    /**
     * Class constructor.
     *
     * @param contextUrl
     * @throws HarvestException
     */
    protected BaseHarvest(String contextUrl) throws HarvestException {

        changeContext(contextUrl);
    }

    /**
     * @param contextSourceDTO
     */
    protected BaseHarvest(HarvestSourceDTO contextSourceDTO) {

        if (contextSourceDTO == null || StringUtils.isEmpty(contextSourceDTO.getUrl())) {
            throw new IllegalArgumentException("Context source and its URL must not be null or empty!");
        }
        this.contextSourceDTO = contextSourceDTO;
        this.contextUrl = contextSourceDTO.getUrl();

        this.lastHarvestDuration = calculateLastHarvestDuration(contextSourceDTO);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#execute()
     */
    @Override
    public final void execute() throws HarvestException {

        startHarvest();

        boolean wasHarvestException = false;
        try {
            doHarvest();
        } catch (HarvestException e) {
            wasHarvestException = true;
            throw e;
        } catch (RuntimeException e) {
            wasHarvestException = true;
            throw e;
        } finally {
            try {
                finishHarvest(wasHarvestException);
            } finally {
                afterFinish();
            }
        }
    }

    /**
     * @throws HarvestException
     */
    private void startHarvest() throws HarvestException {

        LOGGER.debug("                                                                   ");
        LOGGER.info(loggerMsg("Starting harvest"));

        // no null checking, i.e. assuming the context source exists for sure
        int sourceId = contextSourceDTO.getSourceId();

        // fall back to default user name, if harvest user has not been set
        String user = harvestUser == null ? CRUser.APPLICATION.getUserName() : harvestUser;

        // create harvest record in the database
        try {
            harvestId = getHarvestDAO().insertStartedHarvest(sourceId, getHarvestType(), user, BaseHarvest.STATUS_STARTED);
        } catch (DAOException e) {
            throw new HarvestException(e.getMessage(), e);
        }
    }

    /**
     * @throws HarvestException
     */
    protected abstract void doHarvest() throws HarvestException;

    /**
     * @param dontThrowException
     * @throws HarvestException
     */
    private void finishHarvest(boolean dontThrowException) throws HarvestException {

        try {
            // Clear the triples if sub-classes have requested so.
            // They should do that when the harvest failed in such a way that no previous content is to be left into the graph.
            if (clearTriplesInHarvestFinish) {

                // Run pre-purge scripts.
                runHarvestScripts(Phase.PRE_PURGE, HarvestScriptType.POST_HARVEST);

                LOGGER.debug("Purging the graph of " + getContextUrl());
                DAOFactory.get().getDao(HarvestSourceDAO.class).clearGraph(getContextUrl());
                getContextSourceDTO().setStatements(0);
            }

            // Run post-harvest scripts.
            runHarvestScripts(Phase.AFTER_NEW, HarvestScriptType.POST_HARVEST);


            // Run Push scripts.
            runHarvestScripts(null, HarvestScriptType.PUSH);

            // send harvest messages
            sendHarvestMessages();

            // double-check that we're not closing a harvest whose id we don't know
            if (harvestId == 0) {
                if (dontThrowException) {
                    return;
                } else {
                    throw new HarvestException("Cannot close an un-started harvest: missing harvest id");
                }
            }

            // update harvest source DTO
            updateHarvestSourceFinished();

            // close harvest record, persist harvest messages
            updateHarvestAndMessagesClosed();

            // save source meta-data
            finishSourceMetadata();

            // derive new harvest sources from stored content
            deriveNewHarvestSources();

            // delete old harvests history
            if (!HarvestSourcesCleanupJob.CLEANUP_USERNAME.equals(harvestUser)) {
                housekeepOldHarvests();
            }

            // add source into inference if it is schema source
            addIntoInferenceRule();

            // delete sources in permanent error state
            deleteErroneousSources();

        } catch (DAOException e) {

            if (dontThrowException) {
                LOGGER.error("Error when finishing harvest: ", e);
            } else {
                if (isSendNotifications()) {
                    LOGGER.debug(loggerMsg("Sending message about harvest finishing error"));
                    sendFinishingError(e);
                }
                throw new HarvestException(e.getMessage(), e);
            }
        } finally {
            LOGGER.info(loggerMsg("Harvest finished"));
            LOGGER.debug("                                                                   ");
        }
    }

    /**
     * Called as the very last thing after {@link #finishHarvest(boolean)}. This is an abstract method that extending classes must
     * implement.
     */
    protected abstract void afterFinish();

    /**
     * Runs all post-harvest scripts relevant for this harvest.
     *
     * @param phase Only scripts meant for this phase will be run.
     */
    private void runHarvestScripts(Phase phase, HarvestScriptType type) {

        if (phase == null && HarvestScriptType.POST_HARVEST.equals(type)) {
            throw new IllegalArgumentException("Phase must be specified for post-harvest scripts!");
        }

        // If the phase is "after harvesting new content" and there was actually 0 triple harvested then don't continue.
        if (Phase.AFTER_NEW.equals(phase) || HarvestScriptType.PUSH.equals(type)) {
            if (getStoredTriplesCount() <= 0) {
                LOGGER.debug(loggerMsg("Ignoring harvest scripts, as no triples were harvested!"));
                return;
            }
        }

        String dbgLabel = HarvestScriptType.POST_HARVEST.equals(type) && phase != null ?
        phase.getShortLabel() : type.getShortLabel() + (phase != null ? " " + phase.getShortLabel() : "");
        LOGGER.debug(loggerMsg("Running \"" + dbgLabel  + "\" scripts..."));

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            HarvestScriptDAO dao = DAOFactory.get().getDao(HarvestScriptDAO.class);

            int totalScriptsFound = 0;
            // run scripts meant for all sources (i.e. all-source scripts)
            List<HarvestScriptDTO> scripts = dao.listActive(null, null, phase, type);
            totalScriptsFound += scripts.size();
            runScripts(scripts, conn);

            // run scripts meant for this source only
            scripts = dao.listActive(HarvestScriptDTO.TargetType.SOURCE, getContextUrl(), phase, type);
            totalScriptsFound += scripts.size();
            runScripts(scripts, conn);

            // run scripts meant for the types found in the freshly harvested content of this source
            SingleObjectReader<String> reader = new SingleObjectReader<String>();
            SesameUtil.executeQuery("select distinct ?type from <" + getContextUrl() + "> where {?s a ?type}", reader, conn);
            List<String> distinctTypes = reader.getResultList();
            if (distinctTypes != null && !distinctTypes.isEmpty()) {

                scripts = dao.listActiveForTypes(distinctTypes, phase, type);
                totalScriptsFound += scripts.size();
                runScripts(scripts, conn);
            }

            if (totalScriptsFound == 0) {
                LOGGER.debug(loggerMsg("No active \"" + dbgLabel  + "\" scripts were found for this source"));
            }

            // commit changes
            conn.commit();
        } catch (Exception e) {
            SesameUtil.rollback(conn);
            addHarvestMessage("Error when running \"" + dbgLabel  + "\" scripts: " + e.getMessage(),
                    HarvestMessageType.ERROR, Util.getStackTrace(e));
            LOGGER.error(loggerMsg("Error when running \"" + phase.getShortLabel() + "\" scripts: " + e.getMessage()), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * @param scriptDtos
     * @param conn
     */
    private void runScripts(List<HarvestScriptDTO> scriptDtos, RepositoryConnection conn) {

        if (scriptDtos == null || scriptDtos.isEmpty()) {
            return;
        }

        for (HarvestScriptDTO scriptDto : scriptDtos) {
            runScript(scriptDto, conn);
        }
    }

    /**
     * @param scriptDto
     * @param conn
     */
    private void runScript(HarvestScriptDTO scriptDto, RepositoryConnection conn) {

        TargetType targetType = scriptDto.getTargetType();
        String targetUrl = scriptDto.getTargetUrl();
        String query = scriptDto.getScript();
        String title = scriptDto.getTitle();
        String scriptType = targetType == null ? "all-source" : targetType.toString().toLowerCase() + "-specific";
        String associatedType = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;
        String parsedQuery = HarvestScriptParser.parseForExecution(query, getContextUrl(), associatedType);
        Phase phase = scriptDto.getPhase();
        String phaseShortLabel = phase == null ? "unknown phase" : phase.getShortLabel();

        try {
            LOGGER.debug(MessageFormat.format("Executing {0} \"{1}\" script titled \"{2}\"", scriptType, phaseShortLabel, title));

            //extralogging to be removed
            LOGGER.debug(MessageFormat.format("Executing parsedQuery: {0}", parsedQuery));

            //post-harvest
            if (scriptDto.getType().name().equals(HarvestScriptType.POST_HARVEST.name())) {
                runPostHarvestScript(parsedQuery, conn, scriptDto);
            } else if (scriptDto.getType().name().equals(HarvestScriptType.PUSH.name())) {
                runPushScript(parsedQuery, conn, scriptDto);
            }
        } catch (Exception e) {
            String message =
                    MessageFormat.format(
                            "Got exception *** {0} *** when executing the following {1} post-harvest script titled \"{2}\":\n{3}",
                            e.toString(), scriptType, title, parsedQuery);
            LOGGER.warn(message);
            addHarvestMessage(message, HarvestMessageType.WARNING);
        }
    }

    private void runPushScript(String parsedQuery, RepositoryConnection conn, HarvestScriptDTO scriptDto)
        throws  Exception {
        FileWriter writer = null;
        File file = null;

        PostMethod post = null;
        try {
            //export CONSTRUCT result to a temp file
            String fileName = System.getProperty("java.io.tmpdir") + File.separator + scriptDto.getId() + "_output.rdf";
            writer = new FileWriter(fileName);
            RDFHandler rdfxmlWriter = new RDFXMLWriter(writer);
            SesameUtil.exportGraphQuery(parsedQuery, rdfxmlWriter, conn, null);
            
            file = new File(fileName);

            //make POST
            ExternalServiceDTO serviceDTO = externalServiceDAO.fetch(scriptDto.getExternalServiceId());

            ExternalService service = ExternalServiceFactory.getService(serviceDTO.getServiceType());
            post = service.buildPost(serviceDTO, scriptDto, file);

            HttpClient httpclient = new HttpClient();
            int status = httpclient.executeMethod(post);
            LOGGER.debug("Push service returned HTTP code " + status);
            
            if (HttpStatus.SC_OK != status) {
                throw  new HarvestException("Unsuccessful response code from the remote service " + status);
            }


        } finally {
            IOUtils.closeQuietly(writer);
            if (file != null && file.exists()) {
                file.delete();
            }
            if (post != null) {
                post.releaseConnection();
            }
        }

    }
    
    private void runPostHarvestScript(String parsedQuery, RepositoryConnection conn, HarvestScriptDTO scriptDto)
        throws  Exception {
        int updateCount = SesameUtil.executeSPARUL(parsedQuery, conn);
        if (updateCount > 0 && !scriptDto.isRunOnce()) {
            // run maximum 100 times
            LOGGER.debug("Script's update count was " + updateCount
                    + ", running it until the count becomes 0, or no more than 100 times ...");
            int i = 0;
            int totalUpdateCount = updateCount;
            for (; updateCount > 0 && i < 100; i++) {
                updateCount = SesameUtil.executeSPARUL(parsedQuery, conn, getContextUrl());
                totalUpdateCount += updateCount;
            }
            LOGGER.debug("Script was run for a total of " + (i + 1) + " times, total update count = " + totalUpdateCount);
        } else {
            LOGGER.debug("Script's update count was " + updateCount);
        }
        
    }

    /**
     * @throws DAOException
     */
    private void housekeepOldHarvests() throws DAOException {
        LOGGER.debug(loggerMsg("Deleting old harvests history of the harvested source ..."));
        getHarvestDAO().deleteOldHarvests(harvestId, NO_OF_LAST_HARVESTS_PRESERVED);
    }

    /**
     * Adds source int inference rule, if source is inference rule. (It is done because rule set must be updated after the harvest
     * is done)
     *
     * @throws DAOException
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
    private void addIntoInferenceRule() throws DAOException {
        if (getHarvestSourceDAO().isSourceInInferenceRule(getContextUrl())) {
            LOGGER.debug(loggerMsg("Adding source into inference rule"));
            getHarvestSourceDAO().addSourceIntoInferenceRule(getContextUrl());
        }
    }

    /**
     * Deletes sources with permanent errors after batch harvesting.
     *
     * @throws DAOException
     *             if deleting fails
     */
    private void deleteErroneousSources() throws DAOException {
        LOGGER.debug(loggerMsg("Checking sources that need removal"));
        HashSet<String> sourcesToDelete = new HashSet<String>();

        boolean sourceInError = false;

        // if the source or redirected sources are in erroneous state, delete them while batch harvesting
        if (!isOnDemandHarvest) {
            // check only the current (last redirected) source if there were redirections.
            // If it was failed delete redirected sources as well
            if (getContextSourceDTO().isPermanentError()) {
                if (!getContextSourceDTO().isPrioritySource()) {
                    LOGGER.debug(getContextSourceDTO().getUrl() + "  will be deleted as a non-priority source "
                            + "with permanent error");
                    sourcesToDelete.add(getContextSourceDTO().getUrl());
                    sourceInError = true;

                }
            } else if (getContextSourceDTO().getCountUnavail() >= 5) {
                if (!getContextSourceDTO().isPrioritySource()) {
                    LOGGER.debug(getContextSourceDTO().getUrl() + "  will be deleted as a non-priority source "
                            + "with unavailability >= 5");
                    sourcesToDelete.add(getContextSourceDTO().getUrl());
                    sourceInError = true;
                }
            }
            if (sourceInError) {
                for (HarvestSourceDTO dto : redirectedHarvestSources) {
                    // delete redirected source if not in queue and not priority source
                    LOGGER.debug(dto.getUrl() + "  is a redirected source will be deleted.");
                    if (!dto.isPrioritySource()) {
                        sourcesToDelete.add(dto.getUrl());
                    }
                }
            }
            LOGGER.debug(loggerMsg("sources to be removed count=" + sourcesToDelete.size()));
            getHarvestSourceDAO().removeHarvestSources(sourcesToDelete);
        }

    }

    /**
     * @throws DAOException
     */
    private void finishSourceMetadata() throws DAOException {
        if (sourceMetadata == null) {
            sourceMetadata = new SubjectDTO(getContextUrl(), false);
        }

        // get number of triples in context URL, add it to the source metadata under cr:harvestedStatements
        int tripleCount = getHelperDAO().getHarvestedStatements(getContextUrl());
        addSourceMetadata(Predicates.CR_HARVESTED_STATEMENTS,
                ObjectDTO.createLiteral(String.valueOf(tripleCount), XMLSchema.INTEGER));

        // save source metadata
        String msg = "Saving " + sourceMetadata.getTripleCount() + " triples of harvest source metadata";
        if (cleanAllPreviousSourceMetadata) {
            msg = msg + ", cleaning all previous metadata first";
        }
        LOGGER.debug(loggerMsg(msg));

        // if all previous metadata should be deleted, then do so,
        if (cleanAllPreviousSourceMetadata) {
            getHarvestSourceDAO().deleteSubjectTriplesInSource(getContextUrl(), GeneralConfig.HARVESTER_URI);
        } else {
            // delete those metadata we're about to save (i.e. we're doing a replace)
            List<String> subjectUris = Collections.singletonList(getContextUrl());
            Set<String> predicateUris = sourceMetadata.getPredicateUris();
            List<String> sourceUris = Collections.singletonList(GeneralConfig.HARVESTER_URI);
            getHelperDAO().deleteSubjectPredicates(subjectUris, predicateUris, sourceUris);
        }
        getHelperDAO().addTriples(sourceMetadata);
    }

    /**
     * @throws DAOException
     */
    private void updateHarvestAndMessagesClosed() throws DAOException {

        LOGGER.debug(loggerMsg("Updating harvest record, saving harvest messages"));
        Integer noOfStatements = getContextSourceDTO().getStatements();
        getHarvestDAO().updateFinishedHarvest(harvestId, noOfStatements == null ? 0 : noOfStatements, httpResponseCode);
        for (HarvestMessageDTO messageDTO : harvestMessages) {
            getHarvestMessageDAO().insertHarvestMessage(messageDTO);
        }
    }

    /**
     * @throws DAOException
     */
    private void updateHarvestSourceFinished() throws DAOException {

        LOGGER.debug(loggerMsg("Updating harvest source record"));
        getContextSourceDTO().setLastHarvestId(harvestId);
        getHarvestSourceDAO().updateSourceHarvestFinished(getContextSourceDTO());
    }

    /**
     *
     * @return
     */
    protected abstract String getHarvestType();

    /**
     *
     * @param date
     */
    protected String formatDate(Date date) {
        return Util.virtuosoDateToString(date);
    }

    /**
     *
     * @param messageObject
     * @return
     */
    protected String loggerMsg(Object messageObject) {
        return loggerMsg(messageObject, contextUrl);
    }

    /**
     *
     * @param messageObject
     * @param contextGraphUri
     * @return
     */
    public static String loggerMsg(Object messageObject, String contextGraphUri) {
        return StringUtils.isBlank(contextGraphUri) ? messageObject + "" : messageObject + " [" + contextGraphUri + "]";
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#getContextUrl()
     */
    @Override
    public String getContextUrl() {
        return contextUrl;
    }

    /**
     * @return the harvestDAO
     */
    protected HarvestDAO getHarvestDAO() {
        return harvestDAO;
    }

    /**
     * @return the harvestSourceDAO
     */
    protected HarvestSourceDAO getHarvestSourceDAO() {
        return harvestSourceDAO;
    }

    /**
     * @return the harvestMessageDAO
     */
    protected HarvestMessageDAO getHarvestMessageDAO() {
        return harvestMessageDAO;
    }

    /**
     * @return the helperDAO
     */
    protected HelperDAO getHelperDAO() {
        return helperDAO;
    }

    /**
     *
     * @param url
     * @return
     * @throws DAOException
     */
    protected HarvestSourceDTO getHarvestSource(String url) throws DAOException {

        return getHarvestSourceDAO().getHarvestSourceByUrl(url);
    }

    /**
     *
     * @param contextUrl
     * @throws HarvestException
     */
    private void changeContext(String contextUrl) throws HarvestException {

        if (contextUrl == null || contextUrl.isEmpty()) {
            throw new IllegalArgumentException("Context URL must not be empty!");
        }

        this.contextUrl = contextUrl;
        try {
            this.contextSourceDTO = getHarvestSource(contextUrl);
        } catch (DAOException e) {
            throw new HarvestException(e.getMessage(), e);
        }

        if (this.contextSourceDTO == null) {
            throw new HarvestException("Context source must exist in the database!");
        }
    }

    /**
     *
     * @param url
     */
    protected void switchContextTo(String url) throws DAOException, HarvestException {

        LOGGER.debug(loggerMsg("Switching context to " + url));

        HarvestSourceDTO sourceDTO = getHarvestSourceDAO().getHarvestSourceByUrl(url);
        if (sourceDTO == null) {

            // Clone destination source from current context.
            sourceDTO = getContextSourceDTO().clone();
            sourceDTO.setSourceId(null);
            sourceDTO.setUrl(url);
            sourceDTO.setUrlHash(Long.valueOf(Hashes.spoHash(url)));
            sourceDTO.resetInterval();
            sourceDTO.setTimeCreated(new Date());
            Integer sourceId = getHarvestSourceDAO().addSource(sourceDTO);
            sourceDTO = getHarvestSourceDAO().getHarvestSourceById(sourceId);
        }

        this.contextUrl = url;
        this.contextSourceDTO = sourceDTO;
        if (sourceMetadata != null) {
            sourceMetadata.setUri(contextUrl);
        }

        startHarvest();
    }

    /**
     *
     * @param predicate
     * @param objectDTO
     */
    protected void addSourceMetadata(String predicate, ObjectDTO objectDTO) {

        if (sourceMetadata == null) {
            sourceMetadata = new SubjectDTO(getContextUrl(), false);
        }
        objectDTO.setSourceUri(eionet.cr.config.GeneralConfig.HARVESTER_URI);
        sourceMetadata.addObject(predicate, objectDTO);
    }

    /**
     * @return the contextSourceDTO
     */
    protected HarvestSourceDTO getContextSourceDTO() {
        return contextSourceDTO;
    }

    /**
     *
     * @param message
     * @param messageType
     */
    protected void addHarvestMessage(String message, HarvestMessageType messageType) {
        addHarvestMessage(message, messageType, null);
    }

    /**
     *
     * @param message
     * @param messageType
     * @param stackTrace
     */
    protected void addHarvestMessage(String message, HarvestMessageType messageType, String stackTrace) {

        if (harvestMessages == null) {
            harvestMessages = new ArrayList<HarvestMessageDTO>();
        }

        HarvestMessageDTO dto = HarvestMessageDTO.create(message, messageType, stackTrace);
        dto.setHarvestId(harvestId);
        harvestMessages.add(dto);
    }

    /**
     * Derives new harvest sources from stored content.
     *
     */
    private void deriveNewHarvestSources() {

        if (storedTriplesCount <= 0) {
            return;
        }
        LOGGER.debug(loggerMsg("Inferring new harvest sources"));

        try {
            int foundSourceCount = getHarvestSourceDAO().deriveNewHarvestSources(getContextUrl());
            LOGGER.debug(loggerMsg(foundSourceCount + " new harvest sources found and inserted"));
        } catch (DAOException e) {
            LOGGER.warn("Failure when extracting new harvest sources", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#getStoredTriplesCount()
     */
    @Override
    public int getStoredTriplesCount() {
        return storedTriplesCount;
    }

    /**
     * @param cleanAllPreviousSourceMetadata
     *            the cleanAllPreviousSourceMetadata to set
     */
    protected void setCleanAllPreviousSourceMetadata(boolean cleanAllPreviousSourceMetadata) {
        this.cleanAllPreviousSourceMetadata = cleanAllPreviousSourceMetadata;
    }

    /**
     * Sets the clearTriplesInHarvestFinish flag.
     *
     * @param clearTriples the clearTriplesInHarvestFinish to set
     */
    protected void setClearTriplesInHarvestFinish(boolean clearTriples) {
        this.clearTriplesInHarvestFinish = clearTriples;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#setHarvestUser(java.lang.String)
     */
    @Override
    public void setHarvestUser(String harvestUser) {
        this.harvestUser = harvestUser;
    }

    /**
     *
     * @param throwable
     */
    private void sendFinishingError(Throwable throwable) {

        if (throwable != null) {

            StringBuilder messageBody = new StringBuilder("The following error happened while finishing the harvest of\n");
            messageBody.append(contextUrl);
            messageBody.append("\n\n---\n\n").append(Util.getStackTrace(throwable));

            sendErrorMessage(messageBody.toString());
        }
    }

    /**
     *
     */
    private void sendHarvestMessages() {

        if (!isSendNotifications() || harvestMessages.isEmpty()) {
            return;
        }
        LOGGER.debug(loggerMsg("Sending harvest messages"));

        StringBuilder messageBody = null;

        for (HarvestMessageDTO messageDTO : harvestMessages) {

            String messageType = messageDTO.getType();
            if (messageType != null) {

                HarvestMessageType harvestMessageType = HarvestMessageType.parseFrom(messageType);

                // only error-messages will be notified, i.e. the message type must not be INFO
                if (harvestMessageType != null && !harvestMessageType.equals(HarvestMessageType.INFO)) {

                    if (messageBody == null) {
                        messageBody = new StringBuilder("The following error(s) happened while harvesting\n").append(contextUrl);
                    }
                    messageBody.append("\n\n---\n\n");
                    if (StringUtils.isBlank(messageDTO.getMessage()) && StringUtils.isBlank(messageDTO.getStackTrace())) {
                        messageBody.append("No error message could be found!");
                    } else {
                        if (StringUtils.isNotBlank(messageDTO.getMessage())) {
                            messageBody.append(messageDTO.getMessage());
                        }
                        if (StringUtils.isNotBlank(messageDTO.getStackTrace())) {
                            messageBody.append("\n").append(messageDTO.getStackTrace());
                        }
                    }
                }
            }
        }

        if (messageBody != null) {
            sendErrorMessage(messageBody.toString());
        }

    }

    /**
     *
     * @param messageBody
     */
    private void sendErrorMessage(String messageBody) {

        String subject = "Error(s) when harvesting " + contextUrl;

        // Send to to those listed in this source's e-mails list.
        try {
            String[] emailReceivers = getContextSourceEmailReceivers();
            if (emailReceivers != null && emailReceivers.length > 0) {
                EMailSender.send(emailReceivers, subject, messageBody, false);
            }
        } catch (AddressException e) {
            LOGGER.error("E-mail address formatting error: " + e.getMessage());
        } catch (MessagingException e) {
            LOGGER.error("E-mail sending error", e);
        }

        // Send to sys-admins.
        try {
            EMailSender.sendToSysAdmin(subject, messageBody);
        } catch (AddressException e) {
            LOGGER.error("E-mail address formatting error: " + e.getMessage());
        } catch (MessagingException e) {
            LOGGER.error("E-mail sending error", e);
        }
    }

    /**
     * Returns the list of e-mail addresses to which the error notifications of this harvest source should be sent. Does *NOT*
     * include the "default" list provided in system configuration.
     *
     * @return As indicated above.
     */
    private String[] getContextSourceEmailReceivers() {

        String emailsStr = getContextSourceDTO().getEmails();
        if (StringUtils.isNotBlank(emailsStr)) {
            return StringUtils.split(emailsStr, ";,\t\n\r ");
        } else {
            return null;
        }
    }

    /**
     * Returns true if harvest errors should be sent as notifications to selected addresses. Otherwise returns false.
     *
     * {@link BaseHarvest} always returns false for this method, as default behavior. Extending classes can override it.
     *
     * @return
     */
    protected boolean isSendNotifications() {
        return false;
    }

    /**
     * @param storedTriplesCount
     *            the storedTriplesCount to set
     */
    protected void setStoredTriplesCount(int storedTriplesCount) {
        this.storedTriplesCount = storedTriplesCount;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.harvest.Harvest#isBeingHarvested(java.lang.String)
     */
    @Override
    public boolean isBeingHarvested(String url) {

        return url != null && StringUtils.equals(url, contextUrl);
    }

    /**
     * Calculates the duration of the given source's last harvest. If the last harvest failed, the default harvest timeout is
     * returned as the result of this method. Otherwise, if the last harvest duration cannot be detected due to some reason, the
     * method returns 0.
     *
     * @param harvestSource
     *            The source in question.
     * @return The duration of the given source's last harvest.
     */
    private long calculateLastHarvestDuration(HarvestSourceDTO harvestSource) {

        // If last harvest failed, returns the default harvest timeout.
        if (harvestSource.isLastHarvestFailed()) {
            return DEFAULT_HARVEST_TIMEOUT;
        }

        long result = 0;
        try {
            HarvestDTO lastHarvest = harvestDAO.getLastRealHarvestBySourceId(harvestSource.getSourceId());
            if (lastHarvest != null) {
                Date startTime = lastHarvest.getDatetimeStarted();
                Date endTime = lastHarvest.getDatetimeFinished();
                if (startTime != null && endTime != null) {
                    result = endTime.getTime() - startTime.getTime();
                }
            }
        } catch (DAOException e) {
            LOGGER.error(loggerMsg("Failed getting the last harvest, last harvest duration fallback to 0 ms. " + e));
        }

        return result;

    }

    /**
     * Calculates timeout based on last harvests. Timeout is not greater than maximum timeout specified in HARVEST_TIMEOUT_TRESHOLD
     * If last harvest has not taken more than MINIMAL_HARVEST_TIMEOUT minimal timeout is used
     *
     * @return timeout in milliseconds
     */
    protected int getTimeout() {

        if (timeout == null) {

            String msg = "";

            // Start with default timeout, attempt to calculate a proper one.
            timeout = Integer.valueOf(DEFAULT_HARVEST_TIMEOUT);

            // Assuming lastHarvestDuration was properly set at construction-time.
            if (lastHarvestDuration > 0) {
                timeout = Integer.valueOf((int) (lastHarvestDuration * HARVEST_TIMEOUT_MULTIPLIER));
                msg = "Timeout set to " + timeout + " ms (last harvest duration * " + HARVEST_TIMEOUT_MULTIPLIER + ")";
            } else {
                msg = "Timeout set to the maximum " + DEFAULT_HARVEST_TIMEOUT + " ms, last harvest duration could not be detected";
            }

            // Use minimal if last harvest went very quickly.
            if (timeout.intValue() < MINIMUM_HARVEST_TIMEOUT) {
                timeout = Integer.valueOf(MINIMUM_HARVEST_TIMEOUT);
                msg =
                        "Timeout set to the minimum " + MINIMUM_HARVEST_TIMEOUT + " ms, last harvest duration was "
                                + lastHarvestDuration + " ms";
            }

            LOGGER.debug(loggerMsg(msg));
        }

        return timeout;
    }

    /**
     * Determines if the given throwable is fatal exception that occured during harvesting. If so, raises the fatal error flag. The
     * method is null-safe.
     *
     * @param t
     *            Throwable
     */
    protected void checkAndSetFatalExceptionFlag(Throwable t) {
        if (t != null && t instanceof TimeoutException) {
            isFatalErrorOccured = true;
        }
    }

    /**
     * @param isOnDemandHarvest
     *            the isOnDemandHarvest parameter to set
     */
    public void setOnDemandHarvest(boolean isOnDemandHarvest) {
        this.isOnDemandHarvest = isOnDemandHarvest;
    }

    /**
     * Harvests file in a local filestore.
     * Does not load it through /home servlet but takes it directly from the file system
     *
     * @param file Given file
     * @param contentType content type saved in earlier harvest
     * @return number of triples
     * @throws IOException if error in I/O
     * @throws DAOException if DAO call fails.
     * @throws SAXException if parsing fails
     * @throws RDFHandlerException if error in RDF handler
     * @throws RDFParseException if error in RDF parsing
     */
    protected int processLocalContent(File file, String contentType) throws IOException, DAOException, SAXException,
            RDFHandlerException, RDFParseException {

        // If the file can be loaded straight away as it is, then proceed to loading straight away.
        // Otherwise try to process the file into RDF format and *then* proceed to loading.

        ContentLoader contentLoader = getLocalFileContentloader(file, contentType);
        int result = loadFileContent(file, contentLoader);
        return result;
    }

    /**
     * Loads content of the given file using the given content loader. The loader may be null, in which case the method attempts
     * to further process the file into RDF format and create a loader from there.
     *
     * @param file The file whose content is to be loaded.
     * @param contentLoader The loader to used. May be null as indicated above.
     * @return Number of loaded triples.
     *
     * @throws DAOException If DAO call fails.
     * @throws IOException If error in I/O.
     * @throws SAXException If SAX parsing fails.
     * @throws RDFHandlerException If error in RDF handler.
     * @throws RDFParseException If error in RDF parsing.
     */
    protected int loadFileContent(File file, ContentLoader contentLoader) throws DAOException, IOException, SAXException,
            RDFHandlerException, RDFParseException {

        if (contentLoader != null) {

            contentLoader.setTimeout(getTimeout());
            LOGGER.debug(loggerMsg("File is in RDF or web feed format"));
            String loaderClassName = contentLoader.getClass().getSimpleName();
            LOGGER.debug(loggerMsg("Loading file into triple store, loader class is " + loaderClassName));
            return loadFiles(Collections.singletonMap(file, contentLoader));
        } else {

            LOGGER.debug(loggerMsg("File is not in RDF or web feed format, processing the file further"));
            File processedFile = null;
            try {
                // The file could be a zipped RDF, an XML with an RDF conversion, N3, or actually a completely valid RDF
                // that simply wasn't declared in the server-returned content type.
                FileToRdfProcessor fileProcessor = new FileToRdfProcessor(file, getContextUrl());
                processedFile = fileProcessor.process();

                // Add conversion schema into source metadata under cr:xmlSchema attribute, unless there is no schema information
                // or it is actually the file's root element.
                ConversionSchema convSchema = fileProcessor.getConversionSchema();
                if (convSchema != null && !ConversionSchema.Type.ROOT_ELEM.equals(convSchema.getType())) {
                    addConversionSchemaMetadata(convSchema);
                }

                if (processedFile != null && fileProcessor.getRdfFormat() != null) {
                    LOGGER.debug(loggerMsg("File processed into RDF format"));
                    ContentLoader rdfLoader = new RDFFormatLoader(fileProcessor.getRdfFormat());
                    rdfLoader.setTimeout(getTimeout());
                    return loadFiles(Collections.singletonMap(processedFile, rdfLoader));
                } else {
                    LOGGER.debug(loggerMsg("File couldn't be processed into RDF format"));
                    // File couldn't be processed into RDF, schedule its content deletion for the finish-harvest phase
                    setClearTriplesInHarvestFinish(true);
                    return 0;
                }
            } finally {
                if (processedFile != null && !file.getPath().equals(processedFile.getPath())) {
                    FileDeletionJob.register(processedFile);
                }
            }
        }
    }

    /**
     * Adds source metadata about the given conversion schema.
     *
     * @param convSchema The conversion schema object.
     */
    private void addConversionSchemaMetadata(ConversionSchema convSchema) {

        Type convSchemaType = convSchema.getType();
        String stringValue = convSchema.getStringValue();

        if (StringUtils.isNotBlank(stringValue)) {

            // We split, as the string value can contain actually multiple URIs.
            String[] tokens = StringUtils.split(stringValue);
            for (String token : tokens) {

                // A Public DTD (i.e. as in XHTML documents) is not a URI, so make it literal. Otherwise URI.
                if (convSchemaType.equals(ConversionSchema.Type.PUBLIC_DTD)) {
                    addSourceMetadata(Predicates.CR_SCHEMA, new ObjectDTO(token, true, XMLSchema.STRING));
                } else {
                    addSourceMetadata(Predicates.CR_SCHEMA, new ObjectDTO(token, false));
                }
            }
        }
    }

    /**
     * Loads given files into repository, using the given loaders.
     *
     * @param filesAndLoaders Files and corresponding loaders to use.
     * @return Total number of triples loaded.
     * @throws DAOException When repository access error happens.
     */
    protected int loadFiles(Map<File, ContentLoader> filesAndLoaders) throws DAOException {

        if (filesAndLoaders == null || filesAndLoaders.isEmpty()) {
            return 0;
        }

        HarvestSourceDAO dao = getHarvestSourceDAO();
        String url = getContextUrl();

        // Run harvest scripts meant to be run before clearing the graph before loading new content.
        runHarvestScripts(Phase.PRE_PURGE, HarvestScriptType.POST_HARVEST);

        int tripleCount = dao.loadContent(filesAndLoaders, url);
        return tripleCount;
    }

    /**
     * Returns content loader for local files.
     *
     * @param file File to re-harvest
     * @param contentType content type originally stored
     * @return ContentLoader
     */
    private ContentLoader getLocalFileContentloader(File file, String contentType) {

        ContentLoader contentLoader = null;

        if (contentType == null) {
            contentType = getContextSourceDTO().getMediaType();
        }

        // try to guess contentType
        if (contentType == null) {
            InputStream is = null;
            try {
                is = new BufferedInputStream(new FileInputStream(file));
                contentType = URLConnection.guessContentTypeFromStream(is);
            } catch (Exception e) {
                LOGGER.warn(loggerMsg("Error getting content type for " + file.getPath()));

            } finally {
                IOUtils.closeQuietly(is);
            }

        }

        if (contentType == null) {
            return null;
        }

        // content type is not null
        if (contentType.startsWith("application/rss+xml") || contentType.startsWith("application/atom+xml")) {
            contentLoader = new FeedFormatLoader();
        } else {
            // TODO refactor?
            RDFFormat rdfFormat = null;
            if (contentType.equals(CONTENT_TYPE_TEXT)) {
                String fileName = file.getName();
                String[] arr = fileName.split("\\.");
                if (arr.length > 0) {
                    String ext = arr[arr.length - 1];
                    if (StringUtils.isNotEmpty(ext)) {
                        if (ext.equalsIgnoreCase(EXT_TTL)) {
                            rdfFormat = RDFFormat.TURTLE;
                        }
                        if (ext.equalsIgnoreCase(EXT_N3)) {
                            rdfFormat = RDFFormat.N3;
                        }
                        if (ext.equalsIgnoreCase(EXT_NTRIPLES)) {
                            rdfFormat = RDFFormat.NTRIPLES;
                        }
                    }
                }
            } else {
                rdfFormat = RDFMediaTypes.toRdfFormat(contentType);
            }

            if (rdfFormat != null) {
                contentLoader = new RDFFormatLoader(rdfFormat);
            }
        }

        return contentLoader;
    }
}
