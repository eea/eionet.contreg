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
import java.text.SimpleDateFormat;
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
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.EMailSender;
import eionet.cr.util.Hashes;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author Jaanus Heinlaid
 */
public abstract class BaseHarvest implements Harvest {

    /** */
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** */
    private static final Logger LOGGER = Logger.getLogger(BaseHarvest.class);

    protected static final int PRESERVED_HARVEST_COUNT = 10;

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
            // run post-harvest scripts (TODO not to be run yet, as the scripts functionality is not entirely finished)
            // runPostHarvestScripts();

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
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     * @throws RepositoryException
     *
     */
    private void runPostHarvestScripts() {

        LOGGER.debug("Running post-harvest scripts ...");

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            conn.setAutoCommit(false);
            PostHarvestScriptDAO dao = DAOFactory.get().getDao(PostHarvestScriptDAO.class);

            // run scripts meant for all sources (i.e. all-source scripts)
            runScripts(dao.listActive(null, null), conn);

            // run scripts meant for this source only
            runScripts(dao.listActive(PostHarvestScriptDTO.TargetType.SOURCE, getContextUrl()), conn);

            // run scripts meant for the types found in the freshly harvested content of this source
            SingleObjectReader<String> reader = new SingleObjectReader<String>();
            SesameUtil.executeQuery("select distinct ?type from <" + getContextUrl() + "> where {?s a ?type}", reader, conn);
            List<String> distinctTypes = reader.getResultList();
            if (distinctTypes != null && !distinctTypes.isEmpty()) {
                runScripts(dao.listActiveForTypes(distinctTypes), conn);
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
    private void runScripts(List<PostHarvestScriptDTO> scriptDtos, RepositoryConnection conn) throws OpenRDFException {

        if (scriptDtos != null && !scriptDtos.isEmpty()) {
            for (PostHarvestScriptDTO scriptDto : scriptDtos) {

                String scriptType =
                    scriptDto.getTargetType() == null ? "all-source" : scriptDto.getTargetType().toString().toLowerCase()
                            + "-specific";
                LOGGER.debug(MessageFormat.format("Executing {0} post-harvest script with title: {1}", scriptType,
                        scriptDto.getTitle()));

                String updateSparql = "WITH <" + getContextUrl() + "> " + scriptDto.getScript();
                SesameUtil.executeUpdate(updateSparql, null, conn);
            }
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
     * Adds source int inference rule, if source is inference rule.
     * (It is done because rule set must be updated after the harvest is done)
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

        // add harvest statements
        int harvestedStatements = getHelperDAO().getHarvestedStatements(getContextUrl());
        ObjectDTO harvestedStatementsObject =
            new ObjectDTO(Integer.toString(harvestedStatements), null, true, false, XMLSchema.INTEGER);
        harvestedStatementsObject.setSourceUri(GeneralConfig.HARVESTER_URI);
        sourceMetadata.addObject(Predicates.CR_HARVESTED_STATEMENTS, harvestedStatementsObject);

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
        getHarvestDAO().updateFinishedHarvest(harvestId, storedTriplesCount);
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
        return DATE_FORMATTER.format(date);
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
     * @throws DAOException
     */
    private void deriveNewHarvestSources() throws DAOException {

        if (storedTriplesCount <= 0) {
            return;
        }
        LOGGER.debug(loggerMsg("Deriving new harvest sources"));

        // get the default harvest interval minutes
        int defaultHarvestIntervalMinutes =
            Integer.parseInt(GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
                    String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL)));

        // derive URLs of new harvest sources from content in this context
        List<String> newSources = getHarvestSourceDAO().getNewSources(getContextUrl());
        LOGGER.debug((newSources == null ? 0 : newSources.size()) + " new harvest sources found");

        // loop over derived URIs, create new harvest source for each one
        if (newSources!=null){
            for (String sourceUrl : newSources) {

                // sanitize URL by removing fragment part and escaping spaces
                sourceUrl = StringUtils.substringBefore(sourceUrl, "#");
                sourceUrl = URLUtil.replaceURLSpaces(sourceUrl);

                // create new source DTO
                HarvestSourceDTO sourceDTO = new HarvestSourceDTO();
                sourceDTO.setUrl(sourceUrl);
                sourceDTO.setUrlHash(Hashes.spoHash(sourceUrl));
                sourceDTO.setIntervalMinutes(defaultHarvestIntervalMinutes);

                // persist the new harvest source DTO
                getHarvestSourceDAO().addSource(sourceDTO);
            }
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
     * @param cleanAllPreviousSourceMetadata
     *            the cleanAllPreviousSourceMetadata to set
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
}
