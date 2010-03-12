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
package eionet.cr.dao;

import java.sql.SQLException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.mysql.MySQLDAOFactory;
import eionet.cr.dao.postgre.PostgreSQLDAOFactory;
import eionet.cr.util.sql.DbConnectionProvider;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class DAOFactory {

	/**
	 * 
	 * @return
	 * @throws DAOException 
	 */
	public static DAOFactory get(){
		
//		String dbUrl = GeneralConfig.getRequiredProperty(GeneralConfig.DB_URL);
		String dbUrl = DbConnectionProvider.getConnectionUrl();
		if (dbUrl.startsWith("jdbc:mysql:"))
			return MySQLDAOFactory.get();
		else if (dbUrl.startsWith("jdbc:postgresql:"))
			return PostgreSQLDAOFactory.get();
		else
			return null;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param implementedInterface
	 * @return
	 */
	public abstract <T extends DAO> T getDao(Class<T> implementedInterface);
}
