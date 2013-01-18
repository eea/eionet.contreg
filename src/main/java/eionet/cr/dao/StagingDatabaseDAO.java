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
     * Lists all staging databases in the database, ordered by name ascending.
     *
     * @return
     * @throws DAOException
     */
    public List<StagingDatabaseDTO> listAll() throws DAOException;
}
