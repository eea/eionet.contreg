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
package eionet.cr.dao;

import eionet.cr.dao.mysql.MySQLDAOFactory;

public abstract class DAOFactory {

	/** */
	public static final int MYSQL = 1;
	
	/**
	 * @return
	 */
	public abstract HarvestSourceDAO getHarvestSourceDAO();
	
	/**
	 * 
	 * @return
	 */
	public abstract HarvestDAO getHarvestDAO();
	
	/**
	 * 
	 * @return
	 */
	public abstract HarvestMessageDAO getHarvestMessageDAO();

	/**
	 * 
	 * @return
	 */
	public abstract UrgentHarvestQueueDAO getUrgentHarvestQueueDAO();

	/**
	 * 
	 * @param whichFactory
	 * @return
	 */
	public static DAOFactory getDAOFactory(int whichFactory) {
		
		switch (whichFactory) {
	      case MYSQL: 
	          return new MySQLDAOFactory();
	      default : 
	          return null;
	    }
	}
	
	/**
	 * Convenience method that always returns DAO factory for MySQL.  
	 * @return
	 */
	public static DAOFactory getDAOFactory() {
		return getDAOFactory(MYSQL);
	}
}
