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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SearchDAOTest extends CRDatabaseTestCase{

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
	 */
	@Override
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("types-db.xml");
	}

	/**
	 * 
	 */
	@Test
	public void testSearchByTypeAndFilters() throws Exception{
		
		//type search uses cache tables and they should be up to date
		DAOFactory.get().getDao(HelperDAO.class).updateTypeDataCache();

		Map<String, String> filters = new LinkedHashMap<String, String>();
		filters.put(Predicates.RDF_TYPE, "http://www.w3.org/2004/02/skos/core#Concept");
		List<String> selectedPredicates = null;
		
		Pair<Integer, List<SubjectDTO>> result =
			DAOFactory.get().getDao(SearchDAO.class).searchByTypeAndFilters(
					filters, null, null, null,selectedPredicates);
	}
}
