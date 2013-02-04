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

package eionet.cr.staging.util;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

// TODO: Auto-generated Javadoc
/**
 *
 * @author jaanus
 *
 */
public final class MySQLUtil {

    /** */
    public static final Map<DataType, String> JACKCESS_TO_MYSQL_DATATYPES = new HashMap<DataType, String>();

    /** */
    static {
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.BYTE, "TINYINT");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.INT, "SMALLINT");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.LONG, "INT");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.FLOAT, "FLOAT");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.DOUBLE, "DOUBLE PRECISION");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.BOOLEAN, "BOOL");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.TEXT, "VARCHAR");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.MEMO, "TEXT");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.BINARY, "BLOB");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.MONEY, "DECIMAL");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.NUMERIC, "DECIMAL");
        JACKCESS_TO_MYSQL_DATATYPES.put(DataType.SHORT_DATE_TIME, "DATETIME");
    }

    /**
     * Private constructor: do not allow instantiation of utility classes.
     */
    private MySQLUtil() {
        // Just a private constructor.
    }

    /**
     * Creates the table statement.
     *
     * @param table the table
     * @return the string
     */
    public static String createTableStatement(Table table) {

        StringBuilder sb = new StringBuilder("CREATE TABLE \u0060");
        sb.append(sanitizeTableOrColumnName(table.getName())).append("\u0060 (\n");

        LinkedHashSet<String> primKeyColumns = new LinkedHashSet<String>();
        try {
            Index pkIndex = table.getPrimaryKeyIndex();
            if (pkIndex != null) {
                List<ColumnDescriptor> columns = pkIndex.getColumns();
                for (ColumnDescriptor colDescriptor : columns) {
                    primKeyColumns.add(colDescriptor.getColumn().getName());
                }
            }
        } catch (IllegalArgumentException e) {
            // This exception means no primary key index set on this table
        }

        int i = 0;
        List<Column> columns = table.getColumns();
        for (Column column : columns) {

            sb.append("    \u0060").append(sanitizeTableOrColumnName(column.getName())).append("\u0060 ");
            sb.append(jackcessToMySQLDataType(column));
            if (++i < columns.size() || !primKeyColumns.isEmpty()) {
                sb.append(",");
            }
            sb.append("\n");
        }

        if (!primKeyColumns.isEmpty()) {
            sb.append("    PRIMARY KEY (");
            i = 0;
            for (String pkCol : primKeyColumns) {
                if (i++ > 0) {
                    sb.append(", ");
                }
                sb.append("\u0060").append(sanitizeTableOrColumnName(pkCol)).append("\u0060");
            }
            sb.append(")\n");
        }

        sb.append(") ENGINE=MyISAM;");

        return sb.toString();
    }

    /**
     * Parameterized insert statement.
     *
     * @param tableName the table name
     * @param columnNames the column names
     * @return the string
     */
    public static String parameterizedInsertStatement(String tableName, List<String> columnNames) {

        StringBuilder columnsStr = new StringBuilder();
        StringBuilder valuesStr = new StringBuilder();

        int i = 0;
        for (String column : columnNames) {

            if (i++ > 0) {
                columnsStr.append(", ");
                valuesStr.append(", ");
            }
            columnsStr.append("\u0060").append(sanitizeTableOrColumnName(column)).append("\u0060");
            valuesStr.append("?");
        }

        return "INSERT INTO \u0060" + sanitizeTableOrColumnName(tableName) + "\u0060 (" + columnsStr + ") values (" + valuesStr
                + ")";
    }

    /**
     * Sanitize table or column name.
     *
     * @param name the name
     * @return the string
     */
    private static String sanitizeTableOrColumnName(String name) {
        return name.trim().replaceAll("\\s+", "_");
    }

    /**
     * Jackcess to my sql data type.
     *
     * @param column the column
     * @return the string
     */
    public static String jackcessToMySQLDataType(Column column) {

        String result = JACKCESS_TO_MYSQL_DATATYPES.get(column.getType());
        if (result != null) {
            if (result.equalsIgnoreCase("VARCHAR")) {
                if (column.getLengthInUnits() > 0) {
                    result += "(" + column.getLengthInUnits() + ")";
                }
            }
        }

        return result == null ? "VARCHAR(255)" : result;
    }
}
