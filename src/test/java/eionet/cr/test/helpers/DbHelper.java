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
package eionet.cr.test.helpers;

import java.io.IOException;

import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import eionet.cr.config.GeneralConfig;
import eionet.cr.test.helpers.dbunit.DbUnitDatabaseConnection;

/**
 *
 */
public class DbHelper {

    public static void handleDbSetUpOperation(String datasetName) throws Exception {
        IDatabaseConnection conn = getConnection();
        IDataSet data = getXmlDataSet(datasetName);
        try {
            DatabaseOperation.CLEAN_INSERT.execute(conn, data);
        } finally {
            conn.close();
        }
    }

    public static IDataSet getXmlDataSet(String fileName) throws DataSetException, IOException {
        return new FlatXmlDataSetBuilder().build(CRDatabaseTestCase.class.getClassLoader().getResourceAsStream(fileName));
    }

    public static IDatabaseConnection getConnection() throws Exception {
        return DbUnitDatabaseConnection.get();
    }

    /**
     *
     */
    public static void createConnectionPropertiesInSystem() {

        String drv = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_DRV);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, drv);

        String url = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_URL);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, url);

        String usr = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_USR);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, usr);

        String pwd = GeneralConfig.getProperty(GeneralConfig.VIRTUOSO_DB_PWD);
        System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, pwd);
    }
}
