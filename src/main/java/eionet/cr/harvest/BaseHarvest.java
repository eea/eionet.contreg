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
import eionet.cr.util.EMailSender;
import eionet.cr.util.Hashes;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
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
    private HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
    private HarvestDAO harvestDAO = DAOFactory.get().getDao(HarvestDAO.class);
    private HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);
    private HarvestMessageDAO harvestMessageDAO = DAOFactory.get().getDao(HarvestMessageDAO.class);

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

        if (contextSourceDTO==null || StringUtils.isEmpty(contextSourceDTO.getUrl())) {
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
        try{
            doHarvest();
        }
        catch (HarvestException e){
            wasHarvestException = true;
            throw e;
        }
        catch (RuntimeException e){
            wasHarvestException = true;
            throw e;
        }
        finally {
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
        String user = harvestUser==null ? CRUser.APPLICATION.getUserName() : harvestUser;

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
    private void finishHarvest(boolean dontThrowException) throws HarvestException{

        try{
            if (isSendNotifications() && !harvestMessages.isEmpty()){
                LOGGER.debug(loggerMsg("Sending harvest messages"));
                sendHarvestMessages();
            }

            if (harvestId == 0) {
                if (dontThrowException){
                    return;
                }
                else{
                    throw new HarvestException("Cannot close an un-started harvest: missing harvest id");
                }
            }

            // update harvest source dto
            LOGGER.debug(loggerMsg("Updating harvest source record"));
            getHarvestSourceDAO().updateSourceHarvestFinished(getContextSourceDTO());

            // close harvest record, persist harvest messages
            LOGGER.debug(loggerMsg("Updating harvest record, saving harvest messages"));
            getHarvestDAO().updateFinishedHarvest(harvestId, storedTriplesCount);
            for (HarvestMessageDTO messageDTO : harvestMessages){
                getHarvestMessageDAO().insertHarvestMessage(messageDTO);
            }

            // save source metadata
            if (sourceMetadata != null && sourceMetadata.getPredicateCount() > 0) {

                String msg = "Saving " + sourceMetadata.getTripleCount() + " triples of harvest source metadata";
                if (cleanAllPreviousSourceMetadata){
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

            // derive new harvest sources from stored content, unless no content was stored
            if (storedTriplesCount>0){
                LOGGER.debug(loggerMsg("Deriving new harvest sources"));
                deriveNewHarvestSources();
            }

            // delete old harvests history
            LOGGER.debug(loggerMsg("Deleting old harvests history"));
            getHarvestDAO().deleteOldHarvests(harvestId, PRESERVED_HARVEST_COUNT);
        }
        catch (DAOException e){

            if (dontThrowException){
                LOGGER.error("Error when finishing harvest: ", e);
            }
            else{
                if (isSendNotifications()){
                    LOGGER.debug(loggerMsg("Sending message about harvest finishing error"));
                    sendFinishingError(e);
                }
                throw new HarvestException(e.getMessage(), e);
            }
        }
        finally{
            LOGGER.debug(loggerMsg("Harvest finished"));
            LOGGER.debug("                                                                   ");
        }
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

        if (this.contextSourceDTO==null){
            throw new HarvestException("Context source must exist in the database!");
        }
    }

    /**
     *
     * @param contextUrl
     * @throws HarvestException
     * @throws DAOException
     */
    protected void startWithNewContext(String contextUrl) throws HarvestException, DAOException{

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
    protected void addHarvestMessage(String message, HarvestMessageType messageType){
        addHarvestMessage(message, messageType, null);
    }

    /**
     *
     * @param message
     * @param messageType
     * @param stackTrace
     */
    protected void addHarvestMessage(String message, HarvestMessageType messageType, String stackTrace){

        if (harvestMessages==null){
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
    private void deriveNewHarvestSources() throws DAOException{

        // get the default harvest interval minutes
        int defaultHarvestIntervalMinutes =
            Integer.parseInt(GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
                    String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL)));

        // derive URLs of new harvest sources from content in this context
        List<String> newSources = getHarvestSourceDAO().getNewSources(getContextUrl());
        LOGGER.debug((newSources==null ? 0 : newSources.size()) + " new harvest sources found");

        // loop over derived URIs, create new harvest source for each one
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
    public void setHarvestUser(String harvestUser){
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
    private void sendFinishingError(Throwable throwable){

        if (throwable!=null){

            StringBuilder messageBody = new StringBuilder("The following error happened while finishing the harvest of\n");
            messageBody.append(contextUrl);
            messageBody.append("\n\n---\n\n").append(Util.getStackTrace(throwable));

            sendErrorMessage(messageBody.toString());
        }
    }

    /**
     *
     */
    private void sendHarvestMessages(){

        StringBuilder messageBody = null;

        for (HarvestMessageDTO messageDTO : harvestMessages){

            String messageType = messageDTO.getType();
            if (messageType != null){

                HarvestMessageType harvestMessageType = HarvestMessageType.parseFrom(messageType);

                // only error-messages will be notified, i.e. the message type must not be INFO
                if (harvestMessageType!=null && !harvestMessageType.equals(HarvestMessageType.INFO)){

                    if (messageBody==null){
                        messageBody = new StringBuilder("The following error(s) happened while harvesting\n").append(contextUrl);
                    }
                    messageBody.append("\n\n---\n\n").append(messageDTO.getStackTrace());
                }
            }
        }

        if (messageBody!=null){
            sendErrorMessage(messageBody.toString());
        }

    }

    /**
     *
     * @param messageBody
     */
    private void sendErrorMessage(String messageBody){

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
    protected boolean isSendNotifications(){
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
     * @see eionet.cr.harvest.Harvest#isBeingHarvested(java.lang.String)
     */
    @Override
    public boolean isBeingHarvested(String url){

        return url!=null && StringUtils.equals(url, contextUrl);
    }
}
