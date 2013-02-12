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

package eionet.cr.dao.readers;

import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.imp.ImportStatus;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 * SQL result set reader for objects of type {@link StagingDatabaseDTO}.
 *
 * @author jaanus
 */
public class StagingDatabaseDTOReader extends SQLResultSetBaseReader<StagingDatabaseDTO> {

    /** */
    private static final Logger LOGGER = Logger.getLogger(StagingDatabaseDTOReader.class);

    /* (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        StagingDatabaseDTO databaseDTO = new StagingDatabaseDTO();
        databaseDTO.setId(rs.getInt("DATABASE_ID"));
        databaseDTO.setName(rs.getString("NAME"));
        databaseDTO.setCreator(rs.getString("CREATOR"));
        databaseDTO.setCreated(rs.getTimestamp("CREATED"));
        databaseDTO.setDescription(rs.getString("DESCRIPTION"));
        databaseDTO.setImportStatus(Enum.valueOf(ImportStatus.class, rs.getString("IMPORT_STATUS")));
        databaseDTO.setImportLog(rs.getString("IMPORT_LOG"));

        try {
            Blob blob = rs.getBlob("DEFAULT_QUERY");
            String query = blob == null ? null : blob.length() == 0 ? "" : IOUtils.toString(blob.getBinaryStream());
            databaseDTO.setDefaultQuery(query);
        } catch (Exception e) {
            LOGGER.warn("Failed to read column: DEFAULT_QUERY", e);
        }

        resultList.add(databaseDTO);
    }

}
