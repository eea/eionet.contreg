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
 * Enriko Käsper, Tieto Eesti
 */
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import eionet.cr.util.export.SubjectExportEvent;

/**
 * 
 * @author kaspeenr
 * 
 */
public abstract class ResultSetExportReader<T> extends ResultSetMixedReader<T> {

    /** */
    protected SubjectExportEvent exporter = null;

    /**
     * 
     * @param exporter
     */
    public ResultSetExportReader(SubjectExportEvent exporter) {
        this.exporter = exporter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.SQLResultSetReader#startResultSet(java.sql.ResultSetMetaData)
     */

    public void startResultSet(ResultSetMetaData resultSetMetaData) {
        this.resultSetMetaData = resultSetMetaData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    public abstract void readRow(ResultSet rs) throws SQLException, ResultSetReaderException;

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.readers.ResultSetReader#endResultSet()
     */

    public void endResultSet() {

        // default implementation, which does nothing, implementors can override
    }

    /*
     * (non-Javadoc)
     * 
     * @see eionet.cr.dao.readers.ResultSetReader#getResultList()
     */

    public List<T> getResultList() {
        return resultList;
    }
}
