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
package eionet.cr.harvest.persist;

import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.persist.mysql.MySQLDefaultPersister;
import eionet.cr.harvest.persist.postgresql.PostgreSQLPersister;
import eionet.cr.util.sql.DbConnectionProvider;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class PersisterFactory {

    /**
     *
     * @return
     */
    public static IHarvestPersister getPersister(PersisterConfig config) {

        //String dbUrl = GeneralConfig.getRequiredProperty(GeneralConfig.DB_URL);
        String dbUrl = DbConnectionProvider.getConnectionUrl();
        if (dbUrl.startsWith("jdbc:mysql:"))
            return new MySQLDefaultPersister(config);
        else if (dbUrl.startsWith("jdbc:postgresql:"))
            return new PostgreSQLPersister(config);
        else
            return null;
    }

    /**
     *
     * @return
     */
    public static IHarvestPersister getPersister() {

        String dbUrl = GeneralConfig.getRequiredProperty(GeneralConfig.DB_URL);
        if (dbUrl.startsWith("jdbc:mysql:"))
            return new MySQLDefaultPersister();
        else if (dbUrl.startsWith("jdbc:postgresql:"))
            return new PostgreSQLPersister();
        else
            return null;
    }
}
