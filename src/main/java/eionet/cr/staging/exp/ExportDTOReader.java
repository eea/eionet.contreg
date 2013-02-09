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

package eionet.cr.staging.exp;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 * SQL result set reader for objects of type {@link ExportDTO}.
 *
 * @author jaanus
 */
public class ExportDTOReader extends SQLResultSetBaseReader<ExportDTO> {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ExportDTOReader.class);

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        ExportDTO exportDTO = new ExportDTO();
        exportDTO.setExportId(rs.getInt("EXPORT_ID"));
        exportDTO.setDatabaseId(rs.getInt("DATABASE_ID"));
        exportDTO.setExportName(rs.getString("EXPORT_NAME"));
        exportDTO.setQueryConf(rs.getString("QUERY_CONF"));
        exportDTO.setUserName(rs.getString("USER_NAME"));
        exportDTO.setStarted(rs.getTimestamp("STARTED"));
        exportDTO.setFinished(rs.getTimestamp("FINISHED"));
        exportDTO.setStatus(Enum.valueOf(ExportStatus.class, rs.getString("STATUS")));
        exportDTO.setExportLog(rs.getString("EXPORT_LOG"));
        exportDTO.setNoOfSubjects(rs.getInt("NOOF_SUBJECTS"));
        exportDTO.setNoOfTriples(rs.getInt("NOOF_TRIPLES"));
        exportDTO.setGraphs(rs.getString("GRAPHS"));

        String colName = "DATABASE_NAME";
        try {
            exportDTO.setDatabaseName(rs.getString(colName));
        } catch (Exception e) {
            LOGGER.info("Failed to get string value of column " + colName + ": " + e);
        }

        resultList.add(exportDTO);
    }
}
