package eionet.cr.util;

import org.dbunit.dataset.IDataSet;

import eionet.cr.test.helpers.CRDatabaseTestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class RDFExporterTest extends CRDatabaseTestCase {

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
    public void testGetKnownNamespace() {

        assertEquals("rdf", NamespaceUtil.getKnownNamespace("http://www.w3.org/1999/02/22-rdf-syntax-ns#"));
        assertEquals("rdfs", NamespaceUtil.getKnownNamespace("http://www.w3.org/2000/01/rdf-schema#"));
        assertEquals("owl", NamespaceUtil.getKnownNamespace("http://www.w3.org/2002/07/owl#"));
        assertEquals("dc", NamespaceUtil.getKnownNamespace("http://purl.org/dc/elements/1.1/"));
        assertEquals("eper", NamespaceUtil.getKnownNamespace("http://rdfdata.eionet.europa.eu/eper/dataflow"));
    }

    /**
     *
     */
    public void testGetPredicate() {

        assertEquals("predicate1", NamespaceUtil.extractLocalName("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate1"));
        assertEquals("predicate1", NamespaceUtil.extractLocalName("http://www.w3.org/1999/02/22-rdf-syntax-ns/predicate1"));
    }

}
