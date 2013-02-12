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

package eionet.cr.dto;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * A DTO describing briefly a column in a table in a staging database.
 * @author jaanus
 */
public class StagingDatabaseTableColumnDTO {

    /** */
    private String table;

    /** */
    private String column;

    /** */
    private String dataType;

    /**
     * Construct a DTO for the given column in the given table, with the given data type of the column.
     *
     * @param table the table
     * @param column the column
     * @param dataType the data type
     */
    public StagingDatabaseTableColumnDTO(String table, String column, String dataType) {
        super();
        this.table = table;
        this.column = column;
        this.dataType = dataType;
    }

    /**
     * @return the table
     */
    public String getTable() {
        return table;
    }

    /**
     * @return the column
     */
    public String getColumn() {
        return column;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
