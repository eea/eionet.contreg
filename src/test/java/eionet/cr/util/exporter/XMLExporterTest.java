/*
 * Created on 26.04.2010
 */
package eionet.cr.util.exporter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.junit.Test;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.Util;
import eionet.cr.util.export.ExportException;
import eionet.cr.util.export.XmlElementMetadata;
import eionet.cr.util.export.XmlExporter;
import eionet.cr.util.export.XmlUtil;

/**
 * @author Enriko Käsper, Tieto Estonia AS XMLExporterTest
 */

public class XMLExporterTest extends TestCase {

    @Test
    public void testGetUniqueElementName() {
        MockXmlExporter exporter = new MockXmlExporter();
        Map<String, XmlElementMetadata> elements = new HashMap<String, XmlElementMetadata>();
        elements.put("elemone", new XmlElementMetadata("elemOne"));
        elements.put("elemtwo", new XmlElementMetadata("elemTwo"));
        elements.put("elemthree", new XmlElementMetadata("elemThree"));
        elements.put("elemtwo_1", new XmlElementMetadata("elemTwo_1"));
        elements.put("elemthree_1", new XmlElementMetadata("elemThree_1"));
        elements.put("elemthree_2", new XmlElementMetadata("elemThree_2"));
        exporter.setElements(elements);

        assertEquals(exporter.getUniqueElementNameTest("elemFour"), "elemFour");
        assertEquals(exporter.getUniqueElementNameTest("elemone"), "elemone_1");
        assertEquals(exporter.getUniqueElementNameTest("elemtwo"), "elemtwo_2");
        assertEquals(exporter.getUniqueElementNameTest("elemthree"), "elemthree_3");
        assertEquals(exporter.getUniqueElementNameTest("ELEMONE"), "ELEMONE");
        assertEquals(exporter.getUniqueElementNameTest("ELEMTWO"), "ELEMTWO");
        assertEquals(exporter.getUniqueElementNameTest(""), XmlUtil.INVALID_ELEMENT_NAME);

    }

    @Ignore
    public void testDoExport() throws Exception {

        // fill in search data
        SubjectDTO subject = new SubjectDTO("http://www.google.com", true);
        subject.addObject(Predicates.RDFS_LABEL, new ObjectDTO("labelValue", false));
        subject.addObject("comment", new ObjectDTO("commentValue", false));
        List<Pair<String, String>> selectedColumns = new ArrayList<Pair<String, String>>();
        selectedColumns.add(new Pair<String, String>("comment", null));

        // execute export
        MockXmlExporter exporter = new MockXmlExporter();
        exporter.setSelectedColumns(selectedColumns);
        exporter.setExportResourceUri(true);
        // exporter.setSelectedFilters(searchParameters);

        InputStream in = exporter.writeSubjectIntoExporterOutputTest(subject);

        List<String> xmlElements = new ArrayList<String>();
        List<String> xmlCharacters = new ArrayList<String>();

        // check if the result is valid XML
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader parser = factory.createXMLStreamReader(in);

        while (true) {
            int event = parser.next();
            if (event == XMLStreamConstants.END_DOCUMENT) {
                parser.close();
                break;
            }
            if (event == XMLStreamConstants.START_ELEMENT) {
                xmlElements.add(parser.getLocalName());
            }
            if (event == XMLStreamConstants.CHARACTERS) {
                xmlCharacters.add(parser.getText());
            }
        }
        parser.close();
        // assert xml elements
        assertEquals(xmlElements, Arrays.asList("dataroot", "resources", "Uri", "comment"));
        // assert xml element values
        assertEquals(xmlCharacters, Arrays.asList("http://www.google.com", "commentValue"));

    }

    /**
     * XmlError mocked class for setting private data MockXmlExporter
     * 
     * @author Enriko Käsper, TietoEnator Estonia AS
     */
    class MockXmlExporter extends XmlExporter {

        public MockXmlExporter() {
            super();
        }

        public void setElements(Map<String, XmlElementMetadata> elements) {
            this.elements = elements;
        }

        public String getUniqueElementNameTest(String elementName) {
            return Util.getUniqueElementName(elementName, elements.keySet());
        }

        public InputStream writeSubjectIntoExporterOutputTest(SubjectDTO subject) throws ExportException {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            try {
                writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream, ENCODING);
                writer.writeStartDocument(ENCODING, "1.0");
                // write root element
                writeDocumentStart(writer);

                // create element names Map
                parseElemNames();

                elementKeys = elements.keySet().toArray(new String[elements.size()]);

                // test this method
                super.writeSubjectIntoExporterOutput(subject);

                writeDocumentEnd(writer);

                writer.flush();
            } catch (XMLStreamException e) {
                throw new ExportException(e.toString(), e);
            } catch (FactoryConfigurationError e) {
                throw new ExportException(e.toString(), e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (XMLStreamException e) {
                    }
                }
            }
            return new ByteArrayInputStream(outStream.toByteArray());

        }
    }
}
