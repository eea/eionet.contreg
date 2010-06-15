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

import java.sql.Connection;
import java.sql.SQLException;

import org.dbunit.dataset.IDataSet;

import junit.framework.TestCase;
import eionet.cr.common.Predicates;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.DbConnectionProvider;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.DbConnectionProvider.ConnectionType;
import eionet.cr.web.security.CRUser;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class AddDeleteTriplpesTest extends TestCase{
	
	/**
	 * @throws DAOException 
	 * @throws SQLException 
	 * 
	 */
	public void test() throws DAOException, SQLException{

		DbConnectionProvider.setConnectionType(ConnectionType.SIMPLE);
		CRUser user = new CRUser("heinlja");
		String url = "http://eionet.europa.eu";
		
		DAOFactory.get().getDao(HelperDAO.class).addUserBookmark(user, url);
		assertEquals(1, getCount(user, url));
		
		DAOFactory.get().getDao(HelperDAO.class).deleteUserBookmark(user, url);
		assertEquals(0, getCount(user, url));
	}
	
	/**
	 * 
	 * @param user
	 * @param url
	 * @return
	 * @throws SQLException
	 */
	private int getCount(CRUser user, String url) throws SQLException{
		
		StringBuffer buf = new StringBuffer("select count(*) from SPO where OBJECT_HASH=").
		append(Hashes.spoHash(url)).append(" and LIT_OBJ='N' and PREDICATE=").
		append(Hashes.spoHash(Predicates.CR_BOOKMARK)).append(" and SOURCE=").
		append(Hashes.spoHash(user.getBookmarksUri()));
		
		Connection conn = null;
		try{
			Object o = SQLUtil.executeSingleReturnValueQuery(buf.toString(),
					DbConnectionProvider.getConnection());
			assertNotNull(o);
			return Integer.parseInt(o.toString());
		}
		finally{
			SQLUtil.close(conn);
		}
	}
}
