package eionet.cr.web.util;

import java.util.Map.Entry;

import eionet.cr.ApplicationTestContext;
import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Unit test for the {@link UsefulNamespaces} class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class UsefulNamespacesTest {

    /**
     * The single test of this class.
     */
    @Test
    public void testUsefulNamespaces() {

        UsefulNamespaces instance = null;
        instance = UsefulNamespaces.getInstance();
        assertNotNull(instance);

        int size = instance.size();
        assertEquals(4, size);

        // Expecting namespace prefix-uri pairs in the order they came from the file.

        int i = 0;
        for (Entry<String, String> entry : instance.entrySet()) {

            String prefix = entry.getKey();
            String uri = entry.getValue();

            if (i == 0) {
                assertEquals("rdf", prefix);
                assertEquals("http://www.w3.org/2000/01/rdf-schema#", uri);
            } else if (i == 1) {
                assertEquals("s", prefix);
                // Letter 't' as such is considered a valid URI by java.net.URI
                assertEquals("t", uri);
            } else if (i == 2) {
                assertEquals("xsd", prefix);
                assertEquals("http://www.w3.org/2001/XMLSchema#", uri);
            } else {
                assertEquals("owl", prefix);
                assertEquals("http://www.w3.org/2002/07/owl#", uri);
            }
            i++;
        }
    }
}
