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
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.util.exporter;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.util.export.XmlElementMetadata;
import eionet.cr.util.export.XmlElementMetadata.Type;

/**
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>, Tieto Estonia
 */

public class XmlElementMetadaTest extends TestCase {

    @Test
    public void testXmlElementMetada() {
        XmlElementMetadata elem = new XmlElementMetadata("nameValue");
        // get default type
        assertEquals(elem.getType(), XmlElementMetadata.Type.STRING);

        elem.setMaxLength(100);
        assertEquals(elem.getMaxLength(), 100);
        elem.setMaxLength(300);
        assertEquals(elem.getMaxLength(), 300);
        elem.setMaxLength(100);
        assertEquals(elem.getMaxLength(), 300);

        elem.setType("111");
        assertEquals(elem.getType(), XmlElementMetadata.Type.DOUBLE);

        elem.setType("some string");
        assertEquals(elem.getType(), XmlElementMetadata.Type.STRING);

        elem.setType(Type.STRING);
        assertEquals(elem.getType(), XmlElementMetadata.Type.STRING);

        // if element is already a string data type, then it's impossible to change it to numeric
        elem.setType(Type.DOUBLE);
        assertEquals(elem.getType(), XmlElementMetadata.Type.STRING);

    }
}
