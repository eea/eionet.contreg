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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author heinljab
 *
 */
public final class MySQLUtil {

    /**
     * Hide utility class constructor.
     */
    private MySQLUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param tableName
     * @param valueMap
     * @return
     * @throws SQLException
     */
    public static int insertRow(String tableName, HashMap<String, Object> valueMap, Connection conn) throws SQLException {

        if (tableName == null || tableName.trim().length() == 0 || valueMap == null || valueMap.size() == 0)
            return -1;

        StringBuffer sqlStringBuffer = new StringBuffer("insert into ");
        sqlStringBuffer.append(tableName).append(" (");

        int i;
        StringBuffer questionMarksBuffer = new StringBuffer();
        Iterator<String> colNamesIter = valueMap.keySet().iterator();
        for (i = 0; colNamesIter.hasNext(); i++) {
            String colName = colNamesIter.next();
            if (i > 0) {
                sqlStringBuffer.append(", ");
                questionMarksBuffer.append(", ");
            }
            sqlStringBuffer.append(colName);
            questionMarksBuffer.append("?");
        }

        sqlStringBuffer.append(") values (").append(questionMarksBuffer.toString()).append(")");

        return SQLUtil.executeUpdate(sqlStringBuffer.toString(), new ArrayList<Object>(valueMap.values()), conn);
    }

    /**
     *
     * @param tableName
     * @param valueMap
     * @param criteriaMap
     * @param conn
     * @return
     * @throws SQLException
     */
    public static int updateRow(String tableName, HashMap<String, Object> valueMap, HashMap<String, Object> criteriaMap,
            Connection conn) throws SQLException {

        if (tableName == null || tableName.trim().length() == 0 || valueMap == null || valueMap.size() == 0 || criteriaMap == null
                || criteriaMap.size() == 0)
            return -1;

        StringBuffer sqlStringBuffer = new StringBuffer("update ");
        sqlStringBuffer.append(tableName).append(" set ");

        int i;
        Iterator<String> colNamesIter = valueMap.keySet().iterator();
        for (i = 0; colNamesIter.hasNext(); i++) {
            if (i > 0)
                sqlStringBuffer.append(", ");
            String colName = colNamesIter.next();
            sqlStringBuffer.append(colName).append("=?");
        }

        sqlStringBuffer.append(" where ");
        Iterator<String> criteriaIter = criteriaMap.keySet().iterator();
        for (i = 0; colNamesIter.hasNext(); i++) {
            if (i > 0)
                sqlStringBuffer.append(" and ");
            String critName = criteriaIter.next();
            sqlStringBuffer.append(critName).append("=?");
        }

        List<Object> values = new ArrayList<Object>(valueMap.values());
        values.addAll(criteriaMap.values());

        return SQLUtil.executeUpdate(sqlStringBuffer.toString(), values, conn);
    }

    /**
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    public static Integer getLastInsertID(Connection conn) throws SQLException {

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select last_insert_id()");
            return (rs != null && rs.next()) ? new Integer(rs.getInt(1)) : null;
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
        }
    }

    /**
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    public static Integer getTotalRowCount(Connection conn) throws SQLException {

        ResultSet rs = null;
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select found_rows()");
            return (rs != null && rs.next()) ? new Integer(rs.getInt(1)) : null;
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
        }
    }
}
