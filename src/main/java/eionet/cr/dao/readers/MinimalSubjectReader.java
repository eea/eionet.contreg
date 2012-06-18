/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class MinimalSubjectReader extends SQLResultSetBaseReader<SubjectDTO> {

    /** */
    private Map<Long, SubjectDTO> subjectsMap;

    /**
     * 
     * @param subjectsMap
     */
    public MinimalSubjectReader(Map<Long, SubjectDTO> subjectsMap) {
        this.subjectsMap = subjectsMap;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
     */
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {

        Long hash = Long.valueOf(rs.getLong("SUBJECT"));
        if (subjectsMap.get(hash) == null) {
            subjectsMap.put(hash, new SubjectDTO(rs.getString("URI"), YesNoBoolean.parse(rs.getString("ANON_SUBJ"))));
        }
    }
}
