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
package eionet.cr.dao.mysql;

import java.util.HashMap;
import java.util.Map;

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.DAO;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dao.UrgentHarvestQueueDAO;



/**
 *
 * @author heinljab, altnyris
 *
 */
public final class MySQLDAOFactory extends DAOFactory{

    /** */
    private static MySQLDAOFactory instance;
    private Map<Class<? extends DAO>, Class<? extends MySQLBaseDAO>> registeredDaos;

    /**
     *
     */
    private MySQLDAOFactory() {
        init();
    }

    /**
     *
     */
    private void init() {
        registeredDaos = new HashMap<Class<? extends DAO>, Class<? extends MySQLBaseDAO>>();
        registeredDaos.put(HarvestDAO.class, MySQLHarvestDAO.class);
        registeredDaos.put(HarvestMessageDAO.class, MySQLHarvestMessageDAO.class);
        registeredDaos.put(HarvestSourceDAO.class, MySQLHarvestSourceDAO.class);
        registeredDaos.put(HelperDAO.class, MySQLHelperDAO.class);
        registeredDaos.put(SearchDAO.class, MySQLSearchDAO.class);
        registeredDaos.put(UrgentHarvestQueueDAO.class, MySQLUrgentHarvestQueueDAO.class);
        registeredDaos.put(SpoBinaryDAO.class, MySQLSpoBinaryDAO.class);
    }

    /**
     *
     * @return
     */
    public static MySQLDAOFactory get() {
        if(instance == null) {
            instance = new MySQLDAOFactory();
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.DAOFactory#getDao(java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    public <T extends DAO> T getDao(Class<T> implementedInterface) {

        //due to synchronization problems we have to create DAOs for each method invocation.
        try {
            Class implClass = registeredDaos.get(implementedInterface);
            return (T) implClass.newInstance();
        }
        catch (Exception fatal) {
            throw new RuntimeException(fatal);
        }
    }
}
