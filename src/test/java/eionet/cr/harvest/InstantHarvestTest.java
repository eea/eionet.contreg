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

import java.net.URL;
import java.sql.Connection;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;

import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.sql.DbConnectionProvider;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class InstantHarvestTest extends CRDatabaseTestCase{

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("emptydb.xml");
	}

	@Test
	public void testInstantHarvest(){
		
		try{
			URL url = getClass().getClassLoader().getResource("taxon-over-rdf.xml");
			InstantHarvest instantHarvest = new InstantHarvest(url.toString(), null, "guest");
			instantHarvest.execute();
		}
		catch (Throwable e) {
			e.printStackTrace();
			fail("Was not expecting this exception: " + e.toString());			
		}
	}
}
