/*
 * Created on 26.04.2010
 */
package eionet.cr.util.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.junit.Test;

import eionet.cr.common.Predicates;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.export.ExportException;
import eionet.cr.util.export.XmlElementMetadata;
import eionet.cr.util.export.XmlExporter;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * XMLExporterTest
 */

public class XMLExporterTest  extends TestCase {

	@Test
	public void testEscapeElementName(){
		MockXmlExporter exporter = new MockXmlExporter();
		assertEquals(exporter.getEscapedElementNameTest("1invalidElem"),"_1invalidElem");
		assertEquals(exporter.getEscapedElementNameTest("xmlElem"),"_xmlElem");
		assertEquals(exporter.getEscapedElementNameTest(".Elem"),"_Elem");

		assertEquals(exporter.getEscapedElementNameTest("test:Elem1"),"test_Elem1");
		assertEquals(exporter.getEscapedElementNameTest("test.Elem"),"test_Elem");
		assertEquals(exporter.getEscapedElementNameTest("elem#"),"elem_");
		assertEquals(exporter.getEscapedElementNameTest("elem?"),"elem_");
		assertEquals(exporter.getEscapedElementNameTest("elem1  and  elem2"),"elem1__and__elem2");

		assertEquals(exporter.getEscapedElementNameTest("elem***"),"elem___");
		assertEquals(exporter.getEscapedElementNameTest(""),XmlExporter.INVALID_ELEMENT_NAME);
	}
	@Test
	public void testGetUniqueElementName(){
		MockXmlExporter exporter = new MockXmlExporter();
		Map<String, XmlElementMetadata> elements = new HashMap<String,XmlElementMetadata>();
		elements.put("elemone", new XmlElementMetadata("elemOne"));
		elements.put("elemtwo", new XmlElementMetadata("elemTwo"));
		elements.put("elemthree", new XmlElementMetadata("elemThree"));
		elements.put("elemtwo_1", new XmlElementMetadata("elemTwo_1"));
		elements.put("elemthree_1", new XmlElementMetadata("elemThree_1"));
		elements.put("elemthree_2", new XmlElementMetadata("elemThree_2"));
		exporter.setElements(elements);
		
		assertEquals(exporter.getUniqueElementNameTest("elemFour"),"elemFour");
		assertEquals(exporter.getUniqueElementNameTest("elemOne"),"elemOne_1");
		assertEquals(exporter.getUniqueElementNameTest("elemTwo"),"elemTwo_2");
		assertEquals(exporter.getUniqueElementNameTest("elemThree"),"elemThree_3");
		assertEquals(exporter.getUniqueElementNameTest("ELEMONE"),"ELEMONE_1");
		assertEquals(exporter.getUniqueElementNameTest("ELEMTWO"),"ELEMTWO_2");
		assertEquals(exporter.getUniqueElementNameTest(""),XmlExporter.INVALID_ELEMENT_NAME);
		
	}
	@Test
	public void testDoExport() throws XMLStreamException, ExportException, IOException{
		
		//fill in search data
		List<SubjectDTO> subjectsList = new ArrayList<SubjectDTO>();
		SubjectDTO subject = new SubjectDTO("http://www.google.com",true);
		subject.addObject(Predicates.RDFS_LABEL, new ObjectDTO("labelValue", false));
		subject.addObject("comment", new ObjectDTO("commentValue", false));
		subjectsList.add(subject);
		Pair<Integer,List<SubjectDTO>> searchParameters = new Pair<Integer,List<SubjectDTO>>(subjectsList.size(),subjectsList);
		List<Pair<String, String>> selectedColumns = new ArrayList<Pair<String,String>>();
		selectedColumns.add(new Pair<String,String>("comment", null));

		// execute export
		MockXmlExporter exporter = new MockXmlExporter();
		exporter.setSelectedColumns(selectedColumns);
		exporter.setExportResourceUri(true);
		InputStream in = exporter.doExport(searchParameters);
		
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
		//assert xml elements
		assertEquals(xmlElements, Arrays.asList("dataroot", "resources", "Uri", "comment"));
		//assert xml element values
		assertEquals(xmlCharacters, Arrays.asList("http://www.google.com", "commentValue"));
		
	}

	
	/**
	 * XmlError mocked class for setting private data
	 * MockXmlExporter
	 *
	 * @author Enriko Käsper, TietoEnator Estonia AS
	 */
	class MockXmlExporter extends XmlExporter {

		/**
		 * Override getDataset and construct the result of xml-rpc method (DDServiceClient.getDataset())
		 * 
		 */

		Map<String, XmlElementMetadata> elements;
		public MockXmlExporter() {
			super();
		}
		public Map<String, XmlElementMetadata> getElements(){
			return elements;
		}
		public void setElements(Map<String,XmlElementMetadata> elements){
			this.elements = elements;
		}
		public InputStream doExport(Pair<Integer, List<SubjectDTO>> customSearch) throws ExportException, IOException {
			return super.doExport(customSearch);
		}
		public String getEscapedElementNameTest(String elementName){
			return super.getEscapedElementNameTest(elementName);
		}
		public String getUniqueElementNameTest(String elementName){
			return super.getUniqueElementNameTest(elementName);
		}
	}
}
