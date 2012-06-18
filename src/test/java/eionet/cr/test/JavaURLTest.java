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
 * Søren Roug, European Environment Agency
 */
package eionet.cr.test;

import java.net.URL;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class JavaURLTest extends TestCase {

    /**
     * This method tests the rdf:about="..." when the base URL ends with a "/"
     * 
     * @throws Exception
     */
    public void testURLWithSlash() throws Exception {
        URL basename = new URL("http://rs.tdwg.org/dwc/terms/");
        assertEquals(basename.toString(), "http://rs.tdwg.org/dwc/terms/");

        URL taxon = new URL(basename, "Taxon");
        assertEquals(taxon.toString(), "http://rs.tdwg.org/dwc/terms/Taxon");

        taxon = new URL(basename, "sub/Taxon");
        assertEquals(taxon.toString(), "http://rs.tdwg.org/dwc/terms/sub/Taxon");

        URL samesame = new URL(basename, "");
        assertEquals(samesame.toString(), "http://rs.tdwg.org/dwc/terms/");

        // Test rdf:ID="kingdom" == rdf:about="#kingdom"
        URL rdfid = new URL(basename, "#kingdom");
        assertEquals(rdfid.toString(), "http://rs.tdwg.org/dwc/terms/#kingdom");
    }

    /**
     * This method tests the rdf:about="..." when the base url ends with a real file.
     * 
     * @throws Exception
     */
    public void testURLWithHash() throws Exception {
        URL basename = new URL("http://rs.tdwg.org/dwc/terms/something.rdf#");
        assertEquals(basename.toString(), "http://rs.tdwg.org/dwc/terms/something.rdf#");

        URL taxon = new URL(basename, "Taxon");
        assertEquals(taxon.toString(), "http://rs.tdwg.org/dwc/terms/Taxon");

        // Test rdf:about="sub/Taxon"
        taxon = new URL(basename, "sub/Taxon");
        assertEquals(taxon.toString(), "http://rs.tdwg.org/dwc/terms/sub/Taxon");

        // Test rdf:about=""
        URL samesame = new URL(basename, "");
        assertEquals(samesame.toString(), "http://rs.tdwg.org/dwc/terms/something.rdf#");

        // Test rdf:ID="kingdom" == rdf:about="#kingdom"
        URL rdfid = new URL(basename, "#kingdom");
        assertEquals(rdfid.toString(), "http://rs.tdwg.org/dwc/terms/something.rdf#kingdom");

    }

}
