/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util.xml;

import java.io.File;

import junit.framework.TestCase;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class XmlAnalysisTest extends TestCase {

    /**
     *
     */
    public void testXmlAnalysis() {

        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        try {
            xmlAnalysis.parse(new File(getClass().getClassLoader().getResource("test-xml.xml").getFile()));
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Was not expecting this exception: " + t.toString());
        }

        assertNotNull(xmlAnalysis.getSchemaLocation() != null);
        assertEquals(xmlAnalysis.getSchemaLocation(), "http://biodiversity.eionet.europa.eu/schemas/dir9243eec/habitats.xsd");

        assertTrue(xmlAnalysis.getSchemaNamespace() == null || xmlAnalysis.getSchemaNamespace().length() == 0);

        assertNotNull(xmlAnalysis.getStartElemLocalName());
        assertEquals(xmlAnalysis.getStartElemLocalName(), "habitat");

        assertTrue(xmlAnalysis.getStartElemNamespace() == null || xmlAnalysis.getStartElemNamespace().length() == 0);

        assertNull(xmlAnalysis.getSystemDtd());
        assertNull(xmlAnalysis.getPublicDtd());
    }
}
