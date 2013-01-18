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

package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eionet.cr.common.CRException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dao.readers.StagingDatabaseDTOReader;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.util.sql.SQLUtil;

/**
 * Virtuoso-specific implementation of {@link StagingDatabaseDAO}.
 *
 * @author jaanus
 */
public class VirtuosoStagingDatabaseDAO extends VirtuosoBaseDAO implements StagingDatabaseDAO {

    /** */
    private static final String ADD_NEW_DB_SQL = "insert into STAGING_DB (NAME,CREATOR,CREATED,DESCRIPTION) values (?,?,?,?)";

    /** */
    private static final String LIST_ALL_SQL = "select * from STAGING_DB order by NAME asc";

    /* (non-Javadoc)
     * @see eionet.cr.dao.StagingDatabaseDAO#createDatabase()
     */
    @Override
    public void createDatabase(String databaseName) throws DAOException {

        // We do nothing here, because in Virtuoso there's actually no such thing as "create a database". Instead, tables in
        // Virtuoso's RDBMS are sectioned into so-called qualifiers. A qualifier/database becomes "created" the moment a first
        // table is created with that qualifier.
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#createRecord(eionet.cr.dto.StagingDatabaseDTO, java.lang.String)
     */
    @Override
    public int createRecord(StagingDatabaseDTO databaseDTO, String userName) throws DAOException {

        ArrayList<Object> params = new ArrayList<Object>();
        params.add(databaseDTO.getName());
        params.add(userName);
        params.add(new Date());
        params.add(databaseDTO.getDescription());

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdateReturnAutoID(ADD_NEW_DB_SQL, params, conn);
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        } catch (CRException e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.StagingDatabaseDAO#listAll()
     */
    @Override
    public List<StagingDatabaseDTO> listAll() throws DAOException {

        return executeSQL(LIST_ALL_SQL, null, new StagingDatabaseDTOReader());
    }
}
