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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.dbunit.dataset.IDataSet;
import org.junit.Test;

import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class HelperDAOTest extends CRDatabaseTestCase {

	/*
	 * (non-Javadoc)
	 * @see org.dbunit.DatabaseTestCase#getDataSet()
	 */
	@Override	
	protected IDataSet getDataSet() throws Exception {
		return getXmlDataSet("simple-db.xml");
	}
	
	/**
	 * 
	 * @throws DAOException
	 */
	public void testSubjectDataSelectSQL() throws DAOException {
		
		SubjectDTO subject =
			DAOFactory.get().getDao(HelperDAO.class).getSubject(5550937339998314774L);
		
		Collection<SubjectDTO> resultList = Collections.singleton(subject);
		
		assertEquals(
				"[SubjectDTO[uri=http://www.w3.org/2004/02/skos/core#semanticRelation," +
		"predicates={" +
		"http://www.w3.org/2000/01/rdf-schema#domain=[Concept, http://www.w3.org/2004/02/skos/core#Concept], " +
		"http://www.w3.org/1999/02/22-rdf-syntax-ns#type=[http://www.w3.org/1999/02/22-rdf-syntax-ns#Property], " +
		"http://www.w3.org/2000/01/rdf-schema#comment=[This is the super-property of all properties used to make statements about how concepts within the same conceptual scheme relate to each other.], " +
		"http://purl.org/dc/terms/issued=[2004-03-26], " +
		"http://www.w3.org/2000/01/rdf-schema#label=[semantic relation], " +
		"http://www.w3.org/2000/01/rdf-schema#range=[Concept, http://www.w3.org/2004/02/skos/core#Concept], " +
		"http://www.w3.org/2000/01/rdf-schema#isDefinedBy=[http://www.w3.org/2004/02/skos/core]}]]",
		resultList.toString());
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetSampleTriples() throws Exception {
		
		List<TripleDTO> result =
			DAOFactory.get().getDao(HelperDAO.class).getSampleTriples(
					"http://cr.eionet.europa.eu/testsource.rdf", 10);
		
		assertNotNull(result);
		assertEquals(10, result.size());
	}
	/**
	 * 
	 * @throws DAOException
	 */
	@Test
	public void testUpdateTypeDataCache() throws Exception {

		DAOFactory.get().getDao(HelperDAO.class).updateTypeDataCache();
		
	}
	@Test
	public void testGetPredicatesUsedForType() throws Exception{
		DAOFactory.get().getDao(HelperDAO.class).updateTypeDataCache();

		List<SubjectDTO> list = DAOFactory.get().getDao(HelperDAO.class).
			getPredicatesUsedForType("http://www.w3.org/2004/02/skos/core#Concept");
		
		assertEquals(0, list.size());
	}
}
