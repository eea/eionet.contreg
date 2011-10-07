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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.dao;

import java.io.File;
import java.util.Collection;
import java.util.List;

import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.web.security.CRUser;

/**
 * Interface to define compiled dataset related dao methods.
 *
 * @author altnyris
 */
public interface CompiledDatasetDAO extends DAO {

    /**
     *
     * @param deliveryUris
     * @return List<DeliveryFilesDTO>
     * @throws DAOException
     */
    List<DeliveryFilesDTO> getDeliveryFiles(List<String> deliveryUris) throws DAOException;

    /**
     *
     * @param homeFolder
     * @return List<String>
     * @throws DAOException
     */
    List<String> getCompiledDatasets(String homeFolder) throws DAOException;

    /**
     *
     * @param dataset
     * @return List<String>
     * @throws DAOException
     */
    List<String> getDatasetFiles(String dataset) throws DAOException;

    /**
     *
     * @param selectedFiles
     * @param fileName
     * @param userName
     * @param overwrite
     * @return File
     * @throws DAOException
     */
    File saveConstructedDataset(List<String> selectedFiles, String fileName, String userName, boolean overwrite) throws DAOException;

    /**
     * User compiled datasets.
     *
     * @param crUser CR user
     * @see eionet.cr.dao.HelperDAO#getUserCompiledDatasets(eionet.cr.web.security.CRUser)
     * @throws DAOException if query fails.
     * @return List of user compiled datasets.
     */
    Collection<UploadDTO> getUserCompiledDatasets(CRUser crUser) throws DAOException;
}
