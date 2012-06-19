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
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.harvest.HarvestException;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class UrgentHarvestQueue {

    /** */
    private static final Logger LOGGER = Logger.getLogger(UrgentHarvestQueue.class);

    /**
     * Hide utility class constructor.
     */
    private UrgentHarvestQueue() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Adds a pull-harvest to the urgent queue.
     *
     * @param url URL to put on the queue
     * @throws HarvestException
     */
    public static synchronized void addPullHarvest(String url) throws HarvestException {

        addPullHarvests(Collections.singletonList(url));
    }

    /**
     *
     * @param urls
     * @throws HarvestException
     */
    public static synchronized void addPullHarvests(List<String> urls) throws HarvestException {

        try {
            List<UrgentHarvestQueueItemDTO> dtos = new ArrayList<UrgentHarvestQueueItemDTO>();
            for (Iterator<String> i = urls.iterator(); i.hasNext();) {
                UrgentHarvestQueueItemDTO dto = new UrgentHarvestQueueItemDTO();
                dto.setUrl(i.next());
                dtos.add(dto);
            }

            DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).addPullHarvests(dtos);

            for (Iterator<String> i = urls.iterator(); i.hasNext();) {
                LOGGER.debug("Pull harvest added to the urgent queue, url = " + i.next());
            }
        } catch (DAOException e) {
            throw new HarvestException(e.toString(), e);
        }
    }

    /**
     * Adds a push-harvest to the urgent queue. Since it is pushed, the content is already provided.
     *
     * @param pushContent
     * @param url URL to register it on
     * @throws HarvestException
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
     *
     * @return UrgentHarvestQueueItemDTO
     * @throws DAOException
     */
    public static synchronized UrgentHarvestQueueItemDTO poll() throws DAOException {

        return DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).poll();
    }

    /**
     *
     * @return boolean
     * @throws DAOException
     */
    public static synchronized boolean isInQueue(String url) {
        return DAOFactory.get().getDao(UrgentHarvestQueueDAO.class).isInQueue(url);
    }
}
