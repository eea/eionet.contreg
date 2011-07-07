/*
 * Created on 23.04.2010
 */
package eionet.cr.util.exporter;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.export.XlsExporter;
import eionet.cr.util.export.XmlExporter;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS ExporterTest
 */

public class ExporterTest extends TestCase {

    @Test
    public void testGetRowsLimit() {
        int limit = XlsExporter.getRowsLimit();
        assertTrue(limit > 100);

        int limit2 = XmlExporter.getRowsLimit();
        assertTrue(limit2 == -1);
    }
}
