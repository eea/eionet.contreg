/*
 * Created on 23.04.2010
 */
package eionet.cr.util.exporter;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.export.Exporter;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * ExporterTest
 */

public class ExporterTest extends TestCase {

	@Test
	public void testGetXlsRowsLimit(){
		int limit = Exporter.getXlsRowsLimit();
		assertTrue(limit>100);
	}
}
