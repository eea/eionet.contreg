package eionet.cr.dao.virtuoso;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.MockVirtuosoBaseDAOTest;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Bindings;

public class VirtuosoBaseDAOTest extends MockVirtuosoBaseDAOTest {

    public VirtuosoBaseDAOTest() {
        super("test-subjectsdata.nt");
    }
    @Test
    public void testSubjectsDataQuery() {
        String[] uris = {"http://rod.eionet.europa.eu/obligations/392", 
                "http://rod.eionet.europa.eu/instruments/618", 
                "http://rod.eionet.europa.eu/issues/15", 
                "http://planner.eionet.europa.eu/WorkPlan_2010/PRJ1752885689", 
                "http://planner.eionet.europa.eu/WorkPlan_2010/PRJ1607205326", 
                "http://planner.eionet.europa.eu/WorkPlan_2010/PRJ9599558008", 
                "http://planner.eionet.europa.eu/WorkPlan_2010/PRJ9193135010", 
                "http://www.eea.europa.eu/data-and-maps/figures/potential-climatic-tipping-elements", 
                "http://rod.eionet.europa.eu/obligations/171", 
                "http://rod.eionet.europa.eu/obligations/661", 
                "http://rod.eionet.europa.eu/obligations/606", 
                "http://rod.eionet.europa.eu/obligations/136", 
                "http://rod.eionet.europa.eu/obligations/520", 
                "http://rod.eionet.europa.eu/obligations/522", 
                "http://rod.eionet.europa.eu/obligations/521"};
        
        
//        String[] uris = {http://rod.eionet.europa.eu/obligations/392, http://rod.eionet.europa.eu/instruments/618, http://rod.eionet.europa.eu/issues/15, http://planner.eionet.europa.eu/WorkPlan_2010/PRJ1752885689, http://planner.eionet.europa.eu/WorkPlan_2010/PRJ1607205326, http://planner.eionet.europa.eu/WorkPlan_2010/PRJ9599558008, http://planner.eionet.europa.eu/WorkPlan_2010/PRJ9193135010, http://www.eea.europa.eu/data-and-maps/figures/potential-climatic-tipping-elements, http://rod.eionet.europa.eu/obligations/171, http://rod.eionet.europa.eu/obligations/661, "http://rod.eionet.europa.eu/obligations/606", "http://rod.eionet.europa.eu/obligations/136", "http://rod.eionet.europa.eu/obligations/520", "http://rod.eionet.europa.eu/obligations/522", "http://rod.eionet.europa.eu/obligations/521"};
        List<String> subjectUris  = Arrays.asList(uris);
//        String[] predicateUris = {"http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/2000/01/rdf-schema#label"};
        
        String[] gUris = {"http://rod.eionet.europa.eu/obligations", "http://rod.eionet.europa.eu/obligations.rdf", "http://www.eea.europa.eu/data-and-maps/figures/potential-climatic-tipping-elements/@@rdf", "http://planner.eionet.europa.eu/WorkPlan_2010/projects_rdf", "http://rod.eionet.europa.eu/issues", "http://rod.eionet.europa.eu/instruments.rdf"};
        List<String> graphUris  = Arrays.asList(gUris);
        SubjectDataReader dataReader = new SubjectDataReader(subjectUris);
        dataReader.setBlankNodeUriPrefix(VirtuosoBaseDAO.BNODE_URI_PREFIX);

        // only these predicates will be queried for
        String[] neededPredicates = { Predicates.RDF_TYPE, Predicates.RDFS_LABEL };

//        logger.trace("Free-text search, getting the data of the found subjects");

        // get the subjects data
        List <SubjectDTO> resultList = null;
        try {
            resultList = getSubjectsData(subjectUris, neededPredicates, dataReader, graphUris);
        } catch (DAOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        SubjectDTO subjectDTO = resultList.get(0);
        
        assertTrue(subjectDTO.getPredicateCount() == 1 );
       
        
//        String query = fakeDao.getSubjectsDataQ
        assertNotNull(resultList);
//        assertTrue(resultList.contains(arg0))
        assertEquals(dataReader.getSubjectsMap().size(), 15);
  
    }
    @Test
    public void testUrisToCSV() {
        ArrayList<String> uris = new ArrayList<String>();
        uris.add("http://uri1.somewhere.nonono.com");
        uris.add("http://uri2.somewhere.nonono.com");
        VirtuosoSearchDAO dao = new VirtuosoSearchDAO();
        Bindings bindings = new Bindings();
        String s = dao.urisToCSV(uris, bindings);
        
        assertEquals("?subjectValue1,?subjectValue2", s);
        assertTrue(bindings.toString().indexOf("subjectValue1=http://uri1.somewhere.nonono.com") != -1);
    }
}
