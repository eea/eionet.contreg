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

import eionet.cr.dao.virtuoso.VirtuosoHarvestDAO;
import eionet.cr.dao.virtuoso.VirtuosoHarvestMessageDAO;
import eionet.cr.dao.virtuoso.VirtuosoHarvestSourceDAO;
import eionet.cr.dao.virtuoso.VirtuosoSpoBinaryDAO;
import eionet.cr.dao.virtuoso.VirtuosoTagsDAO;
import eionet.cr.dao.virtuoso.VirtuosoUrgentHarvestQueueDAO;
import eionet.cr.dao.virtuoso.VirtuosoUserHomeDAO;

/**
 * Tests the factory getDao methods.
 * 
 * @author altnyris
 */
public class DaoFactoryTest extends TestCase {

    @Test
    public void testFactory() {

        DAOFactory factory = DAOFactory.get();
        assertTrue(factory.getDao(HarvestSourceDAO.class) instanceof VirtuosoHarvestSourceDAO);
        assertTrue(factory.getDao(HarvestDAO.class) instanceof VirtuosoHarvestDAO);
        assertTrue(factory.getDao(HarvestMessageDAO.class) instanceof VirtuosoHarvestMessageDAO);
        assertTrue(factory.getDao(TagsDAO.class) instanceof VirtuosoTagsDAO);
        assertTrue(factory.getDao(UserHomeDAO.class) instanceof VirtuosoUserHomeDAO);
        assertTrue(factory.getDao(UrgentHarvestQueueDAO.class) instanceof VirtuosoUrgentHarvestQueueDAO);
        assertTrue(factory.getDao(SpoBinaryDAO.class) instanceof VirtuosoSpoBinaryDAO);

    }
}
