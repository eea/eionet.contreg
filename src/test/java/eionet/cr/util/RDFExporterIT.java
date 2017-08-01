package eionet.cr.util;

import java.util.Arrays;
import java.util.List;

import eionet.cr.test.helpers.CRDatabaseTestCase;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RDFExporterIT extends CRDatabaseTestCase {

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.test.helpers.CRDatabaseTestCase#getXMLDataSetFiles()
     */
    @Override
    protected List<String> getXMLDataSetFiles() {
        return Arrays.asList("emptydb.xml");
    }

    /**
     *
     */
    @Test
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
    @Test
    public void testGetPredicate() {

        assertEquals("predicate1", NamespaceUtil.extractLocalName("http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate1"));
        assertEquals("predicate1", NamespaceUtil.extractLocalName("http://www.w3.org/1999/02/22-rdf-syntax-ns/predicate1"));
    }

}
