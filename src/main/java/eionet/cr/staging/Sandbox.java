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

package eionet.cr.staging;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Set;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.staging.imp.ImportException;
import eionet.cr.util.sql.SQLUtil;

/**
 * Just a sandbox class for playing around with the staging databases functionality.
 *
 * @author jaanus
 */
public class Sandbox {

    /**
     *
     * @param file
     * @throws ImportException
     */
    private void test(File file) throws ImportException{

        Database database = null;
        try {
            database = openDatabase(file);

            Set<String> tableNames = getTableNames(database);
            if (tableNames == null || tableNames.isEmpty()) {
                System.out.println("No tables!");
            }

            int i = 0;
            for (String tableName : tableNames) {
                Table table = getTable(database, tableName);
                i = i + table.getRowCount();
            }

            System.out.println("Total row count = " + i);
        }
        finally{
            close(database);
        }

    }

    /**
     *
     * @param database
     * @param tableName
     * @return
     * @throws ImportException
     */
    private Table getTable(Database database, String tableName) throws ImportException {
        try {
            return database.getTable(tableName);
        } catch (IOException e) {
            throw new ImportException("Failed to get table " + tableName, e);
        }
    }

    /**
     *
     * @param database
     * @return
     * @throws ImportException
     */
    private Set<String> getTableNames(Database database) throws ImportException {

        try {
            return database.getTableNames();
        } catch (IOException e) {
            throw new ImportException("Failed to get the table names of the database", e);
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws ImportException
     */
    private Database openDatabase(File file) throws ImportException {
        try {
            Database database = Database.open(file);
            if (database == null) {
                throw new ImportException("Failed to open database at this file: " + file);
            }
            return database;
        } catch (IOException e) {
            throw new ImportException("Failed to open database at this file: " + file, e);
        }
    }

    /**
     *
     * @param database
     */
    private void close(Database database) {
        if (database != null) {
            try {
                database.close();
            } catch (IOException e) {
                // Deliberately ignore closing exceptions
            }
        }
    }

    /**
     *
     * @return
     * @throws SQLException
     */
    private static Connection getConnection() throws SQLException {

        String drv = "virtuoso.jdbc3.Driver";
        String url = "jdbc:virtuoso://localhost:1111/charset=UTF-8/log_enable=2";
        String usr = "cr3user";
        String pwd = "xxx";

        try {
            Class.forName(drv);
            return DriverManager.getConnection(url, usr, pwd);
        } catch (ClassNotFoundException e) {
            throw new CRRuntimeException("Failed to get connection, driver class not found: " + drv, e);
        }
    }

    /**
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //        long started = System.currentTimeMillis();
        //        Sandbox sandbox = new Sandbox();
        //        sandbox.test(new File("C:/dev/projects/DigitalAgendaScoreboard/data/ENT2.mdb"));
        //        System.out.println("Total time: " + (System.currentTimeMillis() - started) + " ms");
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement("select top(50) concat(IdYear,IdCountry,IdVariable) as id, DataWithAggregates.IdBrkDwn, IdUnit as Unit from ENT2.cr3user.DataWithAggregates");
            ResultSetMetaData metaData = pstmt.getMetaData();
            int colCount = metaData.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String label = metaData.getColumnLabel(i);
                String name = metaData.getColumnName(i);
                System.out.println("col #" + i + ", name = " + name + ", label = " + label);
            }

            System.out.println("Success!");
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }
    }
}
