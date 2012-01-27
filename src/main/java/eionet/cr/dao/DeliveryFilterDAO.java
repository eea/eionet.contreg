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
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.DeliveryFilterDTO;

/**
 * Delivery filter DAO.
 *
 * @author Juhan Voolaid
 */
public interface DeliveryFilterDAO extends DAO {

    /**
     * List of all the user's delivery filters.
     *
     * @param username
     * @return
     * @throws DAOException
     */
    List<DeliveryFilterDTO> getDeliveryFilters(String username) throws DAOException;

    /**
     * Returns delivery filter with given id.
     *
     * @param id
     * @return
     * @throws DAOException
     */
    DeliveryFilterDTO getDeliveryFilte(long id) throws DAOException;

    /**
     * Stores the delivery filter for that user, but also deletes the oldest delivery if there are more than 10 filters.
     *
     * @param username
     * @param deliveryFilter
     * @throws DAOException
     */
    void saveDeliveryFilter(DeliveryFilterDTO deliveryFilter) throws DAOException;
}
