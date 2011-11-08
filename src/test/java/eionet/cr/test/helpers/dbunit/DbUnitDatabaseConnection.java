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
package eionet.cr.test.helpers.dbunit;

import java.util.Properties;

import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;

import eionet.cr.util.sesame.SesameUtil;

/**
 *
 * @author Risto Alt
 *
 */
public abstract class DbUnitDatabaseConnection {

    /**
     *
     * @return
     * @throws Exception
     */
    public static IDatabaseConnection get() throws Exception {

        DatabaseConnection dbConn = new DatabaseConnection(SesameUtil.getSQLConnection());
        dbConn.getConfig().setPropertiesByString(getConfigProperties());
        return dbConn;
    }

    /**
     *
     * @return
     */
    private static Properties getConfigProperties() {
        Properties props = new Properties();
        props.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, "eionet.cr.test.helpers.dbunit.DbUnitVirtuosoDataTypeFactory");
        return props;
    }
}
