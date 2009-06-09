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

import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dao.HarvestSourceDAO;

/**
 * 
 * @author heinljab, altnyris
 *
 */
public class MySQLDAOFactory extends DAOFactory {
	

	/**
	 * 
	 */
	public MySQLDAOFactory(){
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestSourceDAO()
	 */
	public HarvestSourceDAO getHarvestSourceDAO() {
		return new MySQLHarvestSourceDAO();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestDAO()
	 */
	public HarvestDAO getHarvestDAO() {
		return new MySQLHarvestDAO();	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestMessageDAO()
	 */
	public HarvestMessageDAO getHarvestMessageDAO() {
		return new MySQLHarvestMessageDAO();
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.DAOFactory#getHarvestQueueDAO()
	 */
	public UrgentHarvestQueueDAO getUrgentHarvestQueueDAO() {
		return new MySQLUrgentHarvestQueueDAO();
	}
}
