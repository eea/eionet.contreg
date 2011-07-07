package eionet.cr.util;

import java.util.List;

import org.dbunit.dataset.IDataSet;

import eionet.cr.test.helpers.CRDatabaseTestCase;

public class SubjectDTOOptimizerTest extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getDataSet()
     */
    protected IDataSet getDataSet() throws Exception {
        return getXmlDataSet("emptydb.xml");
    }

    /**
     *
     */
    public void testAcceptedLanguagePriority() {
        assertEquals(1.000, Util.getHTTPAcceptedLanguageImportance("en"));
        assertEquals(0.5000, Util.getHTTPAcceptedLanguageImportance("en;q=0.5"));
        assertEquals(0.55, Util.getHTTPAcceptedLanguageImportance("en;q=0.55"));
    }

    /**
     *
     */
    public void testOrdering() {
        List<String> languages = Util.getAcceptedLanguagesByImportance("et,pl;q=0.5,dk,ru;q=0.7");

        assertEquals("et", languages.get(0));
        assertEquals("dk", languages.get(1));
        assertEquals("ru", languages.get(2));
        assertEquals("pl", languages.get(3));
        assertEquals("en", languages.get(4));
        assertEquals("", languages.get(5));

    }

}
