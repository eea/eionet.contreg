/*
 * Created on 23.04.2010
 */
package eionet.cr.util.exporter;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.export.ExportFormat;
import eionet.cr.util.export.Exporter;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * ExporterTest
 */

public class ExporterTest extends TestCase {

	@Test
	public void testGetRowsLimit(){
		Exporter exporter = Exporter.getExporter(ExportFormat.XLS);
		int limit = exporter.getRowsLimit();
		assertTrue(limit>100);

		Exporter exporter2 = Exporter.getExporter(ExportFormat.XML);
		int limit2 = exporter2.getRowsLimit();
		assertTrue(limit2==-1);
	}
}
