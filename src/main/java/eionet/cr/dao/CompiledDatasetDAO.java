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

import java.util.List;

import eionet.cr.dto.DatasetDTO;
import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.SubjectDTO;

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
     * @param excludeFileUri - if provided, then compiled datasets that include this file, are not returned
     * @return List<DatasetDTO>
     * @throws DAOException
     */
    List<DatasetDTO> getCompiledDatasets(String homeFolder, String excludeFileUri) throws DAOException;

    /**
     *
     * @param dataset
     * @return List<String>
     * @throws DAOException
     */
    List<String> getDatasetFiles(String dataset) throws DAOException;

    /**
     * Returns list of compiled dataset source files. Only uri and lastModifiedDate properties are populated.
     *
     * @param dataset
     * @return
     * @throws DAOException
     */
    List<SubjectDTO> getDetailedDatasetFiles(String dataset) throws DAOException;

    /**
     * Saves compiled dataset into new graph
     *
     * @param selectedFiles
     * @param datasetUri
     * @param overwrite
     * @throws DAOException
     */
    void saveDataset(List<String> selectedFiles, String datasetUri, boolean overwrite) throws DAOException;

    /**
     * Removes all dataset triples.
     *
     * @param datasetUri
     * @param contextUri
     * @throws DAOException
     */
    void clearDataset(String datasetUri, String contextUri) throws DAOException;

    /**
     * Clears dataset graph.
     *
     * @param datasetUri
     * @throws DAOException
     */
    void clearDatasetData(String datasetUri) throws DAOException;

    /**
     * Removes triples from compiled dataset that also exists in the selectedFiles.
     *
     * @param datasetUri compiled dataset graph
     * @param selectedFiles grpahs which triples must be removed
     * @throws DAOException
     */
    void removeFiles(String datasetUri, List<String> selectedFiles) throws DAOException;

    /**
     * Check if compiled dataset exists
     *
     * @param uri dataset URI
     * @throws DAOException
     * @return boolean
     */
    boolean datasetExists(String uri) throws DAOException;

    /**
     * Returns true, if compiled datset's lastModified property is older than one of the selected files.
     *
     * @param datasetUri compiled dataset's uri
     * @param selectedFiles collections of graph uris
     * @return
     * @throws DAOException
     */
    boolean hasCompiledDatasetExpiredData(String datasetUri, List<String> selectedFiles) throws DAOException;
}
