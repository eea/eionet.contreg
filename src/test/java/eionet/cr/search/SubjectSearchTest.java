package eionet.cr.search;

import java.io.InputStream;
import java.util.Collection;

import org.dbunit.DatabaseTestCase;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.sql.ConnectionUtil;

public class SubjectSearchTest extends DatabaseTestCase {

	@Override
	protected IDatabaseConnection getConnection() throws Exception {
		ConnectionUtil.setReturnSimpleConnection(true);
		return new DatabaseConnection(ConnectionUtil.getConnection());
	}

	@Override
	protected IDataSet getDataSet() throws Exception {
		return new FlatXmlDataSet(getFileAsStream("simple-db.xml"));
	}
	
	public void testSubjectDataSelectSQL() throws SearchException {
		UriSearch us = new UriSearch(5550937339998314774L);
		us.execute();
		Collection<SubjectDTO> resultList = us.getResultList();
		assertEquals("[SubjectDTO[uri=http://www.w3.org/2004/02/skos/core#semanticRelation," +
				"predicates={" +
				"http://www.w3.org/2000/01/rdf-schema#domain=[Concept, http://www.w3.org/2004/02/skos/core#Concept], " +
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#type=[http://www.w3.org/1999/02/22-rdf-syntax-ns#Property], " +
				"http://www.w3.org/2000/01/rdf-schema#comment=[This is the super-property of all properties used to make statements about how concepts within the same conceptual scheme relate to each other.], " +
				"http://purl.org/dc/terms/issued=[2004-03-26], " +
				"http://www.w3.org/2000/01/rdf-schema#label=[semantic relation], " +
				"http://www.w3.org/2000/01/rdf-schema#range=[Concept, http://www.w3.org/2004/02/skos/core#Concept], " +
				"http://www.w3.org/2000/01/rdf-schema#isDefinedBy=[http://www.w3.org/2004/02/skos/core]}]]",resultList.toString());
	}

	
	private InputStream getFileAsStream(String filename) {
		return this.getClass().getClassLoader().getResourceAsStream(filename);
	}


}
