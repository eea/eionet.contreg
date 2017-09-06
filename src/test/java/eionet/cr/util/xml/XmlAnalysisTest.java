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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.File;

import eionet.cr.ApplicationTestContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { ApplicationTestContext.class })
public class XmlAnalysisTest {

    /**
     * Parsing from a file with xsi:noNamespaceSchemaLocation.
     */
    @Test
    public void simpleXmlAnalysis() throws Exception {

        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        xmlAnalysis.parse(new File(getClass().getClassLoader().getResource("test-xml.xml").getFile()));

        assertEquals("http://biodiversity.eionet.europa.eu/schemas/dir9243eec/habitats.xsd", xmlAnalysis.getSchemaLocation());

        assertTrue(xmlAnalysis.getSchemaNamespace() == null || xmlAnalysis.getSchemaNamespace().length() == 0);

        assertEquals("habitat", xmlAnalysis.getStartElemLocalName());

        assertTrue(xmlAnalysis.getStartElemNamespace() == null || xmlAnalysis.getStartElemNamespace().length() == 0);

        assertNull(xmlAnalysis.getSystemDtd());
        assertNull(xmlAnalysis.getPublicDtd());
        assertEquals("Unexpected root element", "habitat", xmlAnalysis.getStartElemUri());

        ConversionSchema convSchema = xmlAnalysis.getConversionSchema();
        assertNotNull("Expected a conversion schema", convSchema);
        assertEquals("Unexpected type of conversion schema", ConversionSchema.Type.XML_SCHEMA, convSchema.getType());
        assertEquals("Unexpected conversion schema value", "http://biodiversity.eionet.europa.eu/schemas/dir9243eec/habitats.xsd",
                convSchema.getStringValue());
    }

    /**
     * Parsing from an inputstream. The xsi:schemaLocation has extra whitespace.
     */
    @Test
    public void nsXmlAnalysis() throws Exception {
        String inlineXml =
                "<gml:FeatureCollection " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                        + "xmlns:gml=\"http://www.opengis.net/gml/3.2\" " + "xmlns:aqd=\"http://aqd.ec.europa.eu/aqd/0.3.6b\" "
                        + "xmlns:swe=\"http://www.opengis.net/swe/2.0\" "
                        + "xsi:schemaLocation=\"http://aqd.ec.europa.eu/aqd/0.3.6b "
                        + "http://dd.eionet.europa.eu/schemas/id2011850eu/AirQualityReporting_0.3.6b.xsd \n\n"
                        + "http://www.opengis.net/swe/2.0  http://schemas.opengis.net/sweCommon/2.0/swe.xsd\">\n"
                        + "</gml:FeatureCollection>\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(inlineXml.getBytes("UTF-8"));
        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        xmlAnalysis.parse(inputStream);
        String expected =
                "http://dd.eionet.europa.eu/schemas/id2011850eu/AirQualityReporting_0.3.6b.xsd "
                        + "http://schemas.opengis.net/sweCommon/2.0/swe.xsd";
        assertEquals(expected, xmlAnalysis.getSchemaLocation());

        ConversionSchema convSchema = xmlAnalysis.getConversionSchema();
        assertNotNull("Expected a conversion schema", convSchema);
        assertEquals("Unexpected type of conversion schema", ConversionSchema.Type.XML_SCHEMA, convSchema.getType());
        assertEquals("Unexpected conversion schema value", expected, convSchema.getStringValue());
    }

    /**
     * Parsing from an inputstream. The file has no schema - only namespaces.
     * getConversionSchema() shall return something based on namespace of first element
     * getSchemaLocation() shall not return a namespace.
     *
     */
    @Test
    public void onlyGetSchemas1() throws Exception {
        String inlineXml =
                "<gml:FeatureCollection " + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
                        + "xmlns:gml=\"http://www.opengis.net/gml/3.2\" " + "xmlns:aqd=\"http://aqd.ec.europa.eu/aqd/0.3.6b\" "
                        + "xmlns:swe=\"http://www.opengis.net/swe/2.0\">\n" + "</gml:FeatureCollection>\n";

        ByteArrayInputStream inputStream = new ByteArrayInputStream(inlineXml.getBytes("UTF-8"));
        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        xmlAnalysis.parse(inputStream);
        String expected = "http://www.opengis.net/gml/3.2FeatureCollection";

        assertEquals(null, xmlAnalysis.getSchemaLocation());

        ConversionSchema convSchema = xmlAnalysis.getConversionSchema();
        assertNotNull("Expected a conversion schema", convSchema);
        assertEquals("Unexpected type of conversion schema", ConversionSchema.Type.ROOT_ELEM, convSchema.getType());
        assertEquals("Unexpected conversion schema value", expected, convSchema.getStringValue());
    }

    /**
     * Check the system ID. It must not have trailing spaces.
     */
    @Test
    public void onlyGetSchemas2() throws Exception {

        String inlineXml =
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd \">\n"
                        + "<html>\n" + "</html>\n";
        ByteArrayInputStream inputStream = new ByteArrayInputStream(inlineXml.getBytes("UTF-8"));
        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        xmlAnalysis.parse(inputStream);

        String expectedSystemDTD = "http://www.w3.org/TR/html4/loose.dtd";
        assertEquals(expectedSystemDTD, xmlAnalysis.getSystemDtd());

        String expectedPublicDTD = "-//W3C//DTD HTML 4.01 Transitional//EN";
        assertEquals(expectedPublicDTD, xmlAnalysis.getPublicDtd());

        ConversionSchema convSchema = xmlAnalysis.getConversionSchema();
        assertNotNull("Expected a conversion schema", convSchema);
        assertEquals("Unexpected type of conversion schema", ConversionSchema.Type.SYSTEM_DTD, convSchema.getType());
        assertEquals("Unexpected conversion schema value", expectedSystemDTD, convSchema.getStringValue());
    }
}
