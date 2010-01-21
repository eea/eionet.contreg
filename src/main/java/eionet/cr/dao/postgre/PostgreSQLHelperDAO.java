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
package eionet.cr.dao.postgre;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.util.Pair;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLHelperDAO extends PostgreSQLBaseDAO implements HelperDAO{

	public void addResource(String uri, String firstSeenSourceUri)
			throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void addTriples(SubjectDTO subjectDTO) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public void deleteTriples(SubjectDTO subject) throws DAOException {
		// TODO Auto-generated method stub
		
	}

	public HashMap<String, String> getAddibleProperties(
			Collection<String> subjectTypes) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public Collection<String> getPicklistForPredicate(String predicateUri)
			throws SearchException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Pair<String, String>> getRecentlyDiscoveredFiles(int limit)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSubjectSchemaUri(String subjectUri) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

}
