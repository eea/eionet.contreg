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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.EMailSender;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.action.admin.postHarvest.PostHarvestScriptParser;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author Jaanus Heinlaid
 */
public abstract class BaseHarvest implements Harvest {

    /** */
    private static final Logger LOGGER = Logger.getLogger(BaseHarvest.class);

    protected static final int PRESERVED_HARVEST_COUNT = 10;

    /**
     * Use timeout checking only if last harvest greater than this.
     * (1hr)
     */
    protected static final int DEFAULT_HARVEST_TIMEOUT = 3600000;


    /**
     * Minimal value if harvest timeout is taken into account.
     */
    protected static final int MINIMAL_HARVEST_TIMEOUT = 300000;

    /**
     * harvesting cancelled if harvesting takes more than last harvest duration multiplied with this constant.
     */
    protected static final double HARVEST_TIMEOUT_MULTIPLIER = 1.2;

    /** */
    private final HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
    private final HarvestDAO harvestDAO = DAOFactory.get().getDao(HarvestDAO.class);
    private final HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);
    private final HarvestMessageDAO harvestMessageDAO = DAOFactory.get().getDao(HarvestMessageDAO.class);

    /** */
    private String contextUrl;
    private HarvestSourceDTO contextSourceDTO;

    /** */
    private int harvestId;
    private List<HarvestMessageDTO> harvestMessages = new ArrayList<HarvestMessageDTO>();

    /** */
    private SubjectDTO sourceMetadata;

    /** */
    private int storedTriplesCount;

    /** */
    private boolean cleanAllPreviousSourceMetadata;

    /** */
    private String harvestUser;

    /**
     * Last harvest duration in millis.
     */
    private Long lastHarvestDuration;

    /**
     *
     * Class constructor.
     *
     * @param contextUrl
     * @throws HarvestException
     */
    protected BaseHarvest(String contextUrl) throws HarvestException {

        changeContext(contextUrl);
    }

    /**
     *
     * @param contextSourceDTO
     */
    protected BaseHarvest(HarvestSourceDTO contextSourceDTO) {

        if (contextSourceDTO == null || StringUtils.isEmpty(contextSourceDTO.getUrl())) {
            throw new IllegalArgumentException("Context source and its URL must not be null or empty!");
        }
        this.contextSourceDTO = contextSourceDTO;
        this.contextUrl = contextSourceDTO.getUrl();

        this.lastHarvestDuration = calculateLastHarvestDuration(contextSourceDTO.getSourceId());
    }

    /**
     *
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
            finishHarvest(wasHarvestException);
            afterFinish();
        }
    }

    /**
     * @throws HarvestException
     *
     */
    private void startHarvest() throws HarvestException {

        LOGGER.debug("                                                                   ");
        LOGGER.debug(loggerMsg("Starting harvest"));

        // no null checking, i.e. assuming the context source exists for sure
        int sourceId = contextSourceDTO.getSourceId();

        // fall back to default user name, if harvest user has not been set
        String user = harvestUser == null ? CRUser.APPLICATION.getUserName() : harvestUser;

        // create harvest record in the database
        try {
            harvestId = getHarvestDAO().insertStartedHarvest(sourceId, getHarvestType(), user, HarvestConstants.STATUS_STARTED);
        } catch (DAOException e) {
            throw new HarvestException(e.getMessage(), e);
        }
    }

    /**
     *
     * @throws HarvestException
     */
    protected abstract void doHarvest() throws HarvestException;

    /**
     *
     * @param dontThrowException
     * @throws HarvestException
     */
    private void finishHarvest(boolean dontThrowException) throws HarvestException {

        try {
            // run post-harvest scripts
            runPostHarvestScripts();

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

            // update harvest source dto
            updateHarvestSourceFinished();

            // close harvest record, persist harvest messages
            updateHarvestAndMessagesClosed();

            // save source meta-data
            finishSourceMetadata();

            // derive new harvest sources from stored content
            deriveNewHarvestSources();

            // delete old harvests history
            housekeepOldHarvests();

            // add source into inference if it is schema source
            addIntoInferenceRule();
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
            LOGGER.debug(loggerMsg("Harvest finished"));
            LOGGER.debug("                                                                   ");
        }
    }

    /**
     * Called as the very last thing after {@link #finishHarvest(boolean)}. This is an abstract method that extending classes must
     * implement.
     */
    protected abstract void afterFinish();

    /**
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws RepositoryException
     *
     */
    private void runPostHarvestScripts() {

        if (getStoredTriplesCount() <= 0) {
            LOGGER.debug(loggerMsg("Ignoring post-harvest scripts - no triples added from"));
            return;
        }

        LOGGER.debug(loggerMsg("Running post-harvest scripts"));

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            PostHarvestScriptDAO dao = DAOFactory.get().getDao(PostHarvestScriptDAO.class);

            int totalScriptsFound = 0;
            // run scripts meant for all sources (i.e. all-source scripts)
            List<PostHarvestScriptDTO> scripts = dao.listActive(null, null);
            totalScriptsFound += scripts.size();
            runScripts(scripts, conn);

            // run scripts meant for this source only
            scripts = dao.listActive(PostHarvestScriptDTO.TargetType.SOURCE, getContextUrl());
            totalScriptsFound += scripts.size();
            runScripts(scripts, conn);

            // run scripts meant for the types found in the freshly harvested content of this source
            SingleObjectReader<String> reader = new SingleObjectReader<String>();
            SesameUtil.executeQuery("select distinct ?type from <" + getContextUrl() + "> where {?s a ?type}", reader, conn);
            List<String> distinctTypes = reader.getResultList();
            if (distinctTypes != null && !distinctTypes.isEmpty()) {

                scripts = dao.listActiveForTypes(distinctTypes);
                totalScriptsFound += scripts.size();
                runScripts(scripts, conn);
            }

            if (totalScriptsFound == 0) {
                LOGGER.debug(loggerMsg("No active post-harvest scripts were found relevant for this source"));
            }

            // commit changes
            conn.commit();
        } catch (Exception e) {
            SesameUtil.rollback(conn);
            addHarvestMessage("Error when running post-harvest scripts: " + e.getMessage(), HarvestMessageType.ERROR,
                    Util.getStackTrace(e));
            LOGGER.error(loggerMsg("Error when running post-harvest scripts: " + e.getMessage()), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     *
     * @param scriptDtos
     * @param conn
     * @throws OpenRDFException
     */
    private void runScripts(List<PostHarvestScriptDTO> scriptDtos, RepositoryConnection conn) {

        if (scriptDtos == null || scriptDtos.isEmpty()) {
            return;
        }

        for (PostHarvestScriptDTO scriptDto : scriptDtos) {
            runScript(scriptDto, conn);
        }
    }

    /**
     * @param scriptDto
     * @param conn
     */
    private void runScript(PostHarvestScriptDTO scriptDto, RepositoryConnection conn) {

        TargetType targetType = scriptDto.getTargetType();
        String targetUrl = scriptDto.getTargetUrl();
        String query = scriptDto.getScript();
        String title = scriptDto.getTitle();
        String scriptType = targetType == null ? "all-source" : targetType.toString().toLowerCase() + "-specific";
        String associatedType = targetType != null && targetType.equals(TargetType.TYPE) ? targetUrl : null;
        String parsedQuery = PostHarvestScriptParser.parseForExecution(query, getContextUrl(), associatedType);

        try {
            LOGGER.debug(MessageFormat.format("Executing {0} script titled \"{1}\":\n{2}", scriptType, title, parsedQuery));

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
        } catch (Exception e) {
            String message =
                    MessageFormat.format(
                            "Got exception *** {0} *** when executing the following {1} post-harvest script titled \"{2}\":\n{3}",
                            e.toString(), scriptType, title, parsedQuery);
            LOGGER.warn(message);
            addHarvestMessage(message, HarvestMessageType.WARNING);
        }
    }

    /**
     * @throws DAOException
     */
    private void housekeepOldHarvests() throws DAOException {
        LOGGER.debug(loggerMsg("Deleting old harvests history"));
        getHarvestDAO().deleteOldHarvests(harvestId, PRESERVED_HARVEST_COUNT);
    }

    /**
     * Adds source int inference rule, if source is inference rule. (It is done because rule set must be updated after the harvest
     * is done)
     *
     * @throws DAOException
     */
    private void addIntoInferenceRule() throws DAOException {
        if (getHarvestSourceDAO().isSourceInInferenceRule(getContextUrl())) {
            LOGGER.debug(loggerMsg("Adding source into inference rule"));
            getHarvestSourceDAO().addSourceIntoInferenceRule(getContextUrl());
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
        getHarvestDAO().updateFinishedHarvest(harvestId, noOfStatements == null ? 0 : noOfStatements);
        for (HarvestMessageDTO messageDTO : harvestMessages) {
            getHarvestMessageDAO().insertHarvestMessage(messageDTO);
        }
    }

    /**
     * @throws DAOException
     */
    private void updateHarvestSourceFinished() throws DAOException {
        LOGGER.debug(loggerMsg("Updating harvest source record"));
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
        return messageObject + " [" + contextUrl + "]";
    }

    /**
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
     * @param contextUrl
     * @throws HarvestException
     * @throws DAOException
     */
    protected void startWithNewContext(String contextUrl) throws HarvestException, DAOException {

        changeContext(contextUrl);
        startHarvest();
    }

    /**
     * @return the harvestId
     */
    protected int getHarvestId() {
        return harvestId;
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

    @Override
    public Long getLastHarvestDuration() {
        return lastHarvestDuration;
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
        LOGGER.debug(loggerMsg("Deriving new harvest sources"));

        try {
            int foundSourceCount = getHarvestSourceDAO().deriveNewHarvestSources(getContextUrl());
            LOGGER.debug(loggerMsg(foundSourceCount + " new harvest sources found and inserted"));
        } catch (DAOException e) {
            LOGGER.warn("Failure when extracting new harvest sources", e);
        }
    }

    /**
     * @see eionet.cr.harvest.Harvest#getStoredTriplesCount()
     */
    @Override
    public int getStoredTriplesCount() {
        return storedTriplesCount;
    }

    /**
     * @param cleanAllPreviousSourceMetadata the cleanAllPreviousSourceMetadata to set
     */
    protected void setCleanAllPreviousSourceMetadata(boolean cleanAllPreviousSourceMetadata) {
        this.cleanAllPreviousSourceMetadata = cleanAllPreviousSourceMetadata;
    }

    /**
     * @see eionet.cr.harvest.Harvest#setHarvestUser(java.lang.String)
     */
    @Override
    public void setHarvestUser(String harvestUser) {
        this.harvestUser = harvestUser;
    }

    /**
     * @return the harvestUser
     */
    protected String getHarvestUser() {
        return harvestUser;
    }

    /**
     *
     * @param message
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
                    messageBody.append("\n\n---\n\n").append(messageDTO.getStackTrace());
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

        try {
            EMailSender.sendToSysAdmin("Error(s) when harvesting " + contextUrl, messageBody);
        } catch (AddressException e) {
            LOGGER.error("E-mail address formatting error: " + e.getMessage());
        } catch (MessagingException e) {
            LOGGER.error("E-mail sending error", e);
        }
    }

    /**
     *
     * @return
     */
    protected boolean isSendNotifications() {
        return false;
    }

    /**
     * @param storedTriplesCount the storedTriplesCount to set
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
     * calculates last harvest duration.
     * @param harvestSourceId source id in the harvest_source
     * @return time in millis, null if cannot be calculated
     */
    private Long calculateLastHarvestDuration(int harvestSourceId) {
        HarvestDTO lastHarvest = null;
        Long result = null;

        try {
            lastHarvest = harvestDAO.getLastHarvestBySourceId(harvestSourceId);
        } catch (DAOException e) {
            LOGGER.error(loggerMsg("Error getting last harvest time, returning null " + e));
        }
        if (lastHarvest != null) {
            Date startTime = lastHarvest.getDatetimeStarted();
            Date endTime = lastHarvest.getDatetimeFinished();
            if (startTime != null && endTime != null) {
                result = endTime.getTime() - startTime.getTime();
            }
        }

        return result;

    }

    /**
     * Calculates timeout based on last harvests.
     * Timeout is not greater than maximum timeout specified in HARVEST_TIMEOUT_TRESHOLD
     * If last harvest has not taken more than MINIMAL_HARVEST_TIMEOUT minimal timeout is used
     *
     * @return timeout in milliseconds
     */
    protected int getTimeout() {
        Long lastHarvestDuration = getLastHarvestDuration();

        //no last harvest, use default timeout
        int timeout = DEFAULT_HARVEST_TIMEOUT;

        if (lastHarvestDuration != null) {
            //big sources - last harvest * 1.2
            timeout = (int)(lastHarvestDuration * HARVEST_TIMEOUT_MULTIPLIER );
        }
        //use minimal if last harvest went very quickly
        if (timeout < MINIMAL_HARVEST_TIMEOUT) {
            timeout = MINIMAL_HARVEST_TIMEOUT;
        }
        LOGGER.info(loggerMsg("Timeout=" + timeout));
        return timeout;

    }
}
