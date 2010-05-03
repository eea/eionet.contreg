/*
 * Created on 26.04.2010
 */
package eionet.cr.util.exporter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import eionet.cr.util.export.XmlWithSchemaExporter;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * XMLExporterTest
 */

public class XmlWithSchemaExporterTest  extends TestCase {


	/**
	 * validates the result of "Xml With XML Schema" export
	 * @throws XMLStreamException
	 * @throws ExportException
	 * @throws IOException
	 */
	@Test
	public void testDoExport() throws XMLStreamException, ExportException, IOException{
		
		//fill in search data
		List<SubjectDTO> subjectsList = new ArrayList<SubjectDTO>();
		SubjectDTO subject = new SubjectDTO("http://www.google.com",true);
		subject.addObject(Predicates.RDFS_LABEL, new ObjectDTO("labelValue", false));
		subject.addObject("comment", new ObjectDTO("commentValue", false));
		subject.addObject("altitude", new ObjectDTO("123", false));
		String longStringValue = generateLongStringValue(300);
		subject.addObject("verylongtext", new ObjectDTO(longStringValue, false));
		subjectsList.add(subject);
		Pair<Integer,List<SubjectDTO>> searchParameters = new Pair<Integer,List<SubjectDTO>>(subjectsList.size(),subjectsList);
		List<Pair<String, String>> selectedColumns = new ArrayList<Pair<String,String>>();
		selectedColumns.add(new Pair<String,String>("comment", null));
		selectedColumns.add(new Pair<String,String>("altitude", null));
		selectedColumns.add(new Pair<String,String>("verylongtext", null));
		
		// execute export
		MockXmlWithSchemaExporter exporter = new MockXmlWithSchemaExporter();
		exporter.setSelectedColumns(selectedColumns);
		exporter.setExportResourceUri(true);
		InputStream in = exporter.doExport(searchParameters);
		
		List<String> xmlElements = new ArrayList<String>();
		List<String> xmlCharacters = new ArrayList<String>();
		List<String> xmlAttributeValues = new ArrayList<String>();
		
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
		        for(int i=0; i<parser.getAttributeCount();i++){
		        	xmlAttributeValues.add(parser.getAttributeValue(i));
		        }
		    }
		    if (event == XMLStreamConstants.CHARACTERS) {
		    	xmlCharacters.add(parser.getText());
		    }
		}
		parser.close();
		//assert xml elements - schema elements should come after data elements
		assertEquals(xmlElements.get(0),"root"); 
		assertEquals(xmlElements.get(1),"dataroot"); 
		assertEquals(xmlElements.get(2),"resources"); 
		assertEquals(xmlElements.get(3),"Uri"); 
		assertEquals(xmlElements.get(4),"comment"); 
		assertEquals(xmlElements.get(5),"altitude"); 
		assertEquals(xmlElements.get(6),"verylongtext"); 
		assertEquals(xmlElements.get(7),"schema"); 
		assertEquals(xmlElements.get(8),"element"); 
		assertEquals(xmlElements.get(9),"complexType"); 
		assertEquals(xmlElements.get(10),"sequence"); 
		assertEquals(xmlElements.get(xmlElements.size()-1),"maxLength"); 
		//assert xml element values
		assertEquals(xmlCharacters, Arrays.asList("http://www.google.com", "commentValue", "123", longStringValue));
		// assert xml attribute values
		assertTrue(xmlAttributeValues.contains("xsd:double"));
		assertTrue(xmlAttributeValues.contains("xsd:string"));
		assertTrue(xmlAttributeValues.contains("300"));
	}
	private String generateLongStringValue(int l){
		StringBuilder s = new StringBuilder("");
		for(int i=0;i<l;i++){
			s.append("a");
		}
		return s.toString();		
	}
	
	/**
	 * XmlError mocked class for setting private data
	 * MockXmlExporter
	 *
	 * @author Enriko Käsper, TietoEnator Estonia AS
	 */
	class MockXmlWithSchemaExporter extends XmlWithSchemaExporter {

		/**
		 * Override getDataset and construct the result of xml-rpc method (DDServiceClient.getDataset())
		 * 
		 */

		public MockXmlWithSchemaExporter() {
			super();
		}
		public InputStream doExport(Pair<Integer, List<SubjectDTO>> customSearch) throws ExportException, IOException {
			return super.doExport(customSearch);
		}
		public String getEscapedElementNameTest(String elementName){
			return super.getEscapedElementName(elementName);
		}
		public String getUniqueElementNameTest(String elementName){
			return super.getUniqueElementName(elementName);
		}
	}
}
