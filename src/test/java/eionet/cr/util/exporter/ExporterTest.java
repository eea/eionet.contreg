/*
 * Created on 23.04.2010
 */
package eionet.cr.util.exporter;

import eionet.cr.ApplicationTestContext;
import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.export.XlsExporter;
import eionet.cr.util.export.XmlExporter;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS ExporterTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class ExporterTest extends TestCase {

    @Test
    public void testGetRowsLimit() {
        int limit = XlsExporter.getRowsLimit();
        assertTrue(limit > 100);

        int limit2 = XmlExporter.getRowsLimit();
        assertTrue(limit2 == -1);
    }
}
