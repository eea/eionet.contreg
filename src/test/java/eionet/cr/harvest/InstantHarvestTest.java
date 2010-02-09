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
package eionet.cr.harvest;

import java.sql.Connection;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eionet.cr.util.sql.ConnectionUtil;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class InstantHarvestTest extends DatabaseTestCase{

	/**
	 * 
	 */
	static{
		ConnectionUtil.setReturnSimpleConnection(true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DBTestCase#getConnection()
	 */
	protected IDatabaseConnection getConnection() throws Exception {
		Connection conn = ConnectionUtil.getConnection();
		return new DatabaseConnection(ConnectionUtil.getConnection());
	}

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(getClass().getClassLoader().getResourceAsStream("emptydb.xml"));
	}

//	/*
//	 * (non-Javadoc)
//	 * @see org.dbunit.DatabaseTestCase#getSetUpOperation()
//	 */
//	protected DatabaseOperation getSetUpOperation() throws Exception{
//		return DatabaseOperation.REFRESH;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * @see org.dbunit.DatabaseTestCase#getTearDownOperation()
//	 */
//	protected DatabaseOperation getTearDownOperation()throws Exception{
//		return DatabaseOperation.NONE;
//	}

	@Test
	public void testInstantHarvest(){
		
		try{
		InstantHarvest instantHarvest = new InstantHarvest("http://rod.eionet.europa.eu/obligations", null, "guest");
		instantHarvest.execute();
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());			
		}
	}
}
