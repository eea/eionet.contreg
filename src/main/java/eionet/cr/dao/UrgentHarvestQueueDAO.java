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
package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.UrgentHarvestQueueItemDTO;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public interface UrgentHarvestQueueDAO extends DAO {

    /**
     * Adds the given items to the urgent harvest queue table.
     *
     * @param queueItems Items to add.
     * @param userName User who is adding. Might be null or blank.
     *
     * @throws DAOException Wraps any sort of excpetion that happens.
     */
    void addPullHarvests(List<UrgentHarvestQueueItemDTO> queueItems, String userName) throws DAOException;

    /**
     * Adds given item as push harvest into the urgent harvest queue.
     *
     * @param queueItem the queue item
     * @throws DAOException the DAO exception
     */
    void addPushHarvest(UrgentHarvestQueueItemDTO queueItem) throws DAOException;

    /**
     * Gets the urgent harvest queue.
     *
     * @return the urgent harvest queue
     * @throws DAOException the DAO exception
     */
    List<UrgentHarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException;

    /**
     * Poll the urgent harvest queue, returning the top-most item and removing it as well.
     *
     * @return the top-most item
     * @throws DAOException the DAO exception
     */
    UrgentHarvestQueueItemDTO poll() throws DAOException;

    /**
     * Return true if the given URL is in queue.
     *
     * @param url The URL to check.
     */
    boolean isInQueue(String url) throws DAOException;

    /**
     * Return true if the given URL requested by the given user name is in queue.
     *
     * @param url The URL.
     * @param userName The user name.
     * @return True/false.
     */
    boolean isInQueue(String url, String userName) throws DAOException;

    /**
     * Removes all urgent harvest queue entries where the URL is the one given in the method input.
     *
     * @param url The URL to remove.
     * @throws DAOException Any exception that happens within this method is wrapped into this one.
     */
    void removeUrl(String url) throws DAOException;

    /**
     * Removes urgent harvest queue items of the given ids.
     *
     * @param itemIds The given ids.
     * @throws DAOException Any exception that happens within this method is wrapped into this one.
     */
    void removeItems(List<Integer> itemIds) throws DAOException;
}
