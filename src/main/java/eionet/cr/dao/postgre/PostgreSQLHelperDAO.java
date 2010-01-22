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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.RawTripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.util.PredicateLabels;
import eionet.cr.search.util.SubProperties;
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

	public List<Pair<String, String>> getLatestFiles(int limit)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSubjectSchemaUri(String subjectUri) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#isAllowLiteralSearch(java.lang.String)
	 */
	public boolean isAllowLiteralSearch(String predicateUri) throws SearchException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getPredicatesUsedForType(java.lang.String)
	 */
	public List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.HelperDAO#getSampleTriples(java.lang.String, int)
	 */
	public Pair<Integer, List<RawTripleDTO>> getSampleTriples(String url, int limit) throws DAOException {
		return null;
	}

	public List<String> getSpatialSources() throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public SubjectDTO getSubject(Long subjectHash) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public PredicateLabels getPredicateLabels(Set<Long> subjectHashes)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public SubProperties getSubProperties(Set<Long> subjectHashes)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}

	public List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit)
			throws DAOException {
		// TODO Auto-generated method stub
		return null;
	}
}
