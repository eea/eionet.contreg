package eionet.cr.dao.virtuoso;

import eionet.cr.dao.BrowseVoidDatasetsDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.util.VoidDatasetsResultRow;
import eionet.cr.test.helpers.CRDatabaseTestCase;
import eionet.cr.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author George Sofianos
 */
public class VirtuosoBrowseVoidDatasetsDAOIT extends CRDatabaseTestCase {
  /**
   * Loads test file into virtuoso test database
   * @return
   */
  @Override
  protected List<String> getRDFXMLSeedFiles() {
    return Arrays.asList("test-void.rdf");
  }

  /**
   * Basic test of VoidDatasets Dao from sample file
   * @throws Exception
   */
  public void testFindDatasets() throws Exception {
    try {
      List<String> creators = new ArrayList<String>();
      creators.add("Eurostat");

      List<String> subjects = new ArrayList<String>();
      subjects.add("National accounts - ESA 2010");
      subjects.add("Main GDP aggregates");
      subjects.add("Basic breakdowns of main GDP aggregates and employment (by industry and assets)");
      subjects.add("Quarterly sector accounts (ESA 2010)");
      subjects.add("Agri-environmental indicators");
      subjects.add("Key farm variables");

      BrowseVoidDatasetsDAO dao = DAOFactory.get().getDao(BrowseVoidDatasetsDAO.class);
      Pair<Integer, List<VoidDatasetsResultRow>> pair = null;
      //finds datasets with Void that may have been imported
      pair = dao.findDatasets(creators, subjects,null,false, null,null);
      assertEquals("Size of Datasets should be 13",pair.getRight().size(),13);
      //finds only datasets with Void that have been imported
      pair = dao.findDatasets(creators, subjects,null,true, null,null);
      assertEquals("Size of Datasets should be 0",pair.getRight().size(),0);
    }
    catch(DAOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Tests for creators in database
   * @throws Exception
   */
  public void testFindCreators() throws Exception {
    try {
      BrowseVoidDatasetsDAO dao = DAOFactory.get().getDao(BrowseVoidDatasetsDAO.class);

      List<String> result = dao.findCreators(null);
      assertEquals("Size of Creators list should be 1", result.size(), 1);
    }
    catch(DAOException ex) {
      ex.printStackTrace();
    }

  }

  /**
   * Tests for subjects in database
   * @throws Exception
   */
  public void testFindSubjects() throws Exception {
    try {
      BrowseVoidDatasetsDAO dao = DAOFactory.get().getDao(BrowseVoidDatasetsDAO.class);
      List<String> result = dao.findSubjects(null);
      assertEquals("Size of Subjects list should be 6", result.size(), 6);
    }
    catch(DAOException ex) {
      ex.printStackTrace();
    }
  }
}
