package eionet.cr.util;

import java.io.File;

import junit.framework.TestCase;

import org.junit.Test;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public class UnGZipTest extends TestCase {

    @Test
    public void testUnGZipDetect() {
        assertEquals(false, GZip.isFileGZip(new File(getClass().getClassLoader().getResource("test-rdf.xml").getFile())));

        assertEquals(true, GZip.isFileGZip(new File(getClass().getClassLoader().getResource("test-rdf.xml.gz").getFile())));
    }

}
