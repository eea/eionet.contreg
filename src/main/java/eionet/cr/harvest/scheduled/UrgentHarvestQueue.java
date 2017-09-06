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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.harvest.scheduled;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;


import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.web.security.CRUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for common operations with urgent harvest queue.
 * Basically, these methods act as services, though this is not a good pattern for service design.
 *
 * @author jaanus
 *
 */
public final class UrgentHarvestQueue {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UrgentHarvestQueue.class);

    /**
     * Hide utility class constructor.
     */
    private UrgentHarvestQueue() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Calls {@link #addPullHarvests(List, String)} with a singleton-list from the given URL and the given user name.
     *
     * @param url The URL.
     * @param userName The user name.
     * @throws HarvestException HarvestException Wraps any sort of exception that happens.
     */
    public static synchronized void addPullHarvest(String url, String userName) throws HarvestException {

        addPullHarvests(Collections.singletonList(url), userName);
    }

    /**
     * Adds a pull-harvest to the urgent harvest queue.
     *
     * @param urls The URL to harvest.
     * @param userName User who is adding. Might be null or blank, in which case {@link CRUser#APPLICATION} is used as default.
     *
     * @throws HarvestException Wraps any sort of exception that happens.
     */
    public static synchronized void addPullHarvests(List<String> urls, String userName) throws HarvestException {

        if (CollectionUtils.isEmpty(urls)) {
            return;
        }

        if (StringUtils.isBlank(userName)) {
            userName = CRUser.APPLICATION.getUserName();
        }

        boolean isPingHarvest = CRUser.PING_HARVEST.getUserName().equals(userName);

        try {
            UrgentHarvestQueueDAO dao = DAOFactory.get().getDao(UrgentHarvestQueueDAO.class);
            List<UrgentHarvestQueueItemDTO> dtos = new ArrayList<UrgentHarvestQueueItemDTO>();

            for (String url : urls) {

                if (isPingHarvest && UrgentHarvestQueue.isInQueue(url, userName)) {
                    LOGGER.info("Skipping pinged URL that is already in queue: " + url);
                } else {
                    UrgentHarvestQueueItemDTO dto = new UrgentHarvestQueueItemDTO();
                    dto.setUrl(url);
                    dtos.add(dto);
                }
            }
            dao.addPullHarvests(dtos, userName);

            // Log success for every URL individually.
            for (UrgentHarvestQueueItemDTO dto : dtos) {
                LOGGER.debug("Pull harvest added to the urgent queue, url = " + dto.getUrl());
            }
        } catch (DAOException e) {
            throw new HarvestException(e.toString(), e);
        }
    }

    /**
     * Adds a push-harvest to the urgent queue. Since it is pushed, the content is already provided.
     *
     * @param pushContent the push content
     * @param url URL to register it on
     * @throws HarvestException the harvest exception
     */
    public static synchronized void addPushHarvest(String pushContent, String url) throws HarvestException {

        UrgentHarvestQueueItemDTO dto = new UrgentHarvestQueueItemDTO();
        dto.setUrl(url);
        dto.setPushedContent(pushContent);

        try {
            DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).addPushHarvest(dto);
            LOGGER.debug("Push harvest added to the urgent queue, url = " + url);
        } catch (DAOException e) {
            throw new HarvestException(e.toString(), e);
        }
    }

    /**
     * Poll the urgent harvest queue, returning the top-most item and removing it as well.
     *
     * @return the top-most item
     * @throws DAOException the DAO exception
     */
    public static synchronized UrgentHarvestQueueItemDTO poll() throws DAOException {

        return DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).poll();
    }

    /**
     * Returns true if the given URL is already in queue.
     *
     * @param url The URL.
     * @return boolean True/false.
     * @throws DAOException In case of DAO error.
     */
    public static synchronized boolean isInQueue(String url) throws DAOException {
        return DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).isInQueue(url);
    }

    /**
     * Returns true if the given URL requested by the given user name is already in queue.
     *
     * @param url The URL.
     * @param userName The user name.
     * @return boolean True/false.
     * @throws DAOException In case of DAO error.
     */
    public static synchronized boolean isInQueue(String url, String userName) throws DAOException {
        return DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).isInQueue(url, userName);
    }
}
