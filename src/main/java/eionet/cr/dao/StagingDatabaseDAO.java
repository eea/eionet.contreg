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
 *        jaanus
 */

package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.imp.ImportStatus;

/**
 * DAO interface for operations with staging databases.
 *
 * @author jaanus
 */
public interface StagingDatabaseDAO extends DAO {

    /**
     * Creates the staging database in the underlying RDBMS. Simply creates the database, does nothing further. i.e. the result
     * of this method will be a new empty staging database.
     *
     * @param databaseName
     * @throws DAOException
     */
    void createDatabase(String databaseName) throws DAOException;

    /**
     * Creates a record in the staging databases table for the given database DTO. Returns the newly created record's surrogate id.
     *
     * @param databaseDTO
     * @param userName
     * @return
     * @throws DAOException
     */
    int createRecord(StagingDatabaseDTO databaseDTO, String userName) throws DAOException;

    /**
     * Gets a staging database by the given id.
     *
     * @param id
     * @return
     * @throws DAOException
     */
    StagingDatabaseDTO getDatabaseById(int id) throws DAOException;

    /**
     * Gets a staging database by the given name.
     *
     * @param name
     * @return
     * @throws DAOException
     */
    StagingDatabaseDTO getDatabaseByName(String name) throws DAOException;

    /**
     *
     * @param databaseId
     * @param importStatus
     * @throws DAOException
     */
    void updateImportStatus(int databaseId, ImportStatus importStatus) throws DAOException;

    /**
     *
     * @param databaseId
     * @param message
     * @throws DAOException
     */
    void addImportLogMessage(int databaseId, String message) throws DAOException;

    /**
     * Lists all staging databases in the database, ordered by name ascending.
     *
     * @return
     * @throws DAOException
     */
    public List<StagingDatabaseDTO> listAll() throws DAOException;

    /**
     * Deletes the given staging databases.
     *
     * @param dbNames
     * @throws DAOException
     */
    public void delete(List<String> dbNames) throws DAOException;

    /**
     * Returns true if there is at least one table under the given Virtuoso database (i.e. qualifier). Otherwise returns false.
     * The database is looked only within the current SQL connection's user space.
     *
     * @param dbName
     * @return
     * @throws DAOException
     */
    public boolean exists(String dbName) throws DAOException;

    /**
     * Returns the import log of the staging database by the given integer id.
     *
     * @param databaseId
     * @return
     * @throws DAOException
     */
    public String getImportLog(int databaseId) throws DAOException;
}
