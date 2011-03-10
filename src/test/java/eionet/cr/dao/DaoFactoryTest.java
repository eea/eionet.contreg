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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.dao;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dao.mysql.MySQLHarvestDAO;
import eionet.cr.dao.mysql.MySQLHarvestMessageDAO;
import eionet.cr.dao.mysql.MySQLHarvestSourceDAO;
import eionet.cr.dao.mysql.MySQLHelperDAO;
import eionet.cr.dao.mysql.MySQLSearchDAO;
import eionet.cr.dao.mysql.MySQLUrgentHarvestQueueDAO;
import eionet.cr.dao.postgre.PostgreSQLDAOFactory;
import eionet.cr.dao.postgre.PostgreSQLHarvestDAO;
import eionet.cr.dao.postgre.PostgreSQLHarvestMessageDAO;
import eionet.cr.dao.postgre.PostgreSQLHarvestSourceDAO;
import eionet.cr.dao.postgre.PostgreSQLHelperDAO;
import eionet.cr.dao.postgre.PostgreSQLSearchDAO;
import eionet.cr.dao.postgre.PostgreSQLUrgentHarvestQueueDAO;

/**
 * Tests the factory getDao methods.
 *
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class DaoFactoryTest extends TestCase {

    @Test
    public void testFactory() {

        DAOFactory factory = MySQLDAOFactory.get();
        assertTrue(factory.getDao(HarvestDAO.class) instanceof MySQLHarvestDAO);
        assertTrue(factory.getDao(HarvestMessageDAO.class) instanceof MySQLHarvestMessageDAO);
        assertTrue(factory.getDao(HarvestSourceDAO.class) instanceof MySQLHarvestSourceDAO);
        assertTrue(factory.getDao(HelperDAO.class) instanceof MySQLHelperDAO);
        assertTrue(factory.getDao(SearchDAO.class) instanceof MySQLSearchDAO);
        assertTrue(factory.getDao(UrgentHarvestQueueDAO.class) instanceof MySQLUrgentHarvestQueueDAO);


        factory = PostgreSQLDAOFactory.get();
        assertTrue(factory.getDao(HarvestDAO.class) instanceof PostgreSQLHarvestDAO);
        assertTrue(factory.getDao(HarvestMessageDAO.class) instanceof PostgreSQLHarvestMessageDAO);
        assertTrue(factory.getDao(HarvestSourceDAO.class) instanceof PostgreSQLHarvestSourceDAO);
        assertTrue(factory.getDao(HelperDAO.class) instanceof PostgreSQLHelperDAO);
        assertTrue(factory.getDao(SearchDAO.class) instanceof PostgreSQLSearchDAO);
        assertTrue(factory.getDao(UrgentHarvestQueueDAO.class) instanceof PostgreSQLUrgentHarvestQueueDAO);
    }
}
