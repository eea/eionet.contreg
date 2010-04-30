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
package eionet.cr.util.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eionet.cr.common.Predicates;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FormatUtils;
import eionet.cr.util.Pair;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * XmlExporter
 */

public class XmlExporter extends Exporter {

	private static final String ENCODING = "UTF-8";
	protected static final String ROOT_ELEMENT = "root";
	protected static final String DATA_ROOT_ELEMENT = "dataroot";
	protected static final String ROW_ELEMENT = "resources";

	public static final String INVALID_ELEMENT_NAME = "unknown";
	
	private Map<String, XmlElementMetadata> elements=null;
	
	@Override
	protected InputStream doExport(Pair<Integer, List<SubjectDTO>> customSearch) throws ExportException, IOException {
		
		ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		XMLStreamWriter writer = null;
		
		try{
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream, ENCODING);
			writer.writeStartDocument(ENCODING, "1.0");
			//write root element
			writeDocumentStart(writer);
			
			//create element names Map
			parseElemNames();
			
			String[] elementKeys = elements.keySet().toArray(new String[elements.size()]);
			XmlElementMetadata elementMetada = null;
			
			// write data rows
			for(SubjectDTO subject : customSearch.getRight()) {				

				//write row start element
				writer.writeStartElement(ROW_ELEMENT);

				//get uri or label value
				String uriOrLabelValue = getUriOrLabelValue(subject);
					
				elementMetada = elements.get(elementKeys[0]);
				//write uri or label element
				writeSimpleDataElement(writer, elementMetada.getName(), uriOrLabelValue);
				elementMetada.setMaxLength(uriOrLabelValue.length());

				//write other elements
				int elementIndex= 1;
				for(Pair<String,String> columnPair : getSelectedColumns()) {
					String value = FormatUtils.getObjectValuesForPredicate(columnPair.getLeft(), subject, getLanguages());
					elementMetada = elements.get(elementKeys[elementIndex++]);
					writeSimpleDataElement(writer, elementMetada.getName(), value); 
					elementMetada.setMaxLength(value.length());
					elementMetada.setType(value);
				}
				
				writer.writeEndElement();
			}
			
			writeDocumentEnd(writer);
			
			writer.flush();
		} catch (XMLStreamException e) {
			throw new ExportException(e.toString(), e);
		} catch (FactoryConfigurationError e) {
			throw new ExportException(e.toString(), e);
		}
		finally{
			if(writer!=null){
				try { writer.close();}catch (XMLStreamException e) {}
			}
		}
		//System.out.println(new String(outStream.toByteArray()));
		
		return new ByteArrayInputStream(outStream.toByteArray());
	}
	
	/**
	 * Create element names map
	 */
	private void parseElemNames() {

		//if exporting with labels no need to export RDFS_LABEL
		if (!isExportResourceUri()) {
			getSelectedColumns().remove(new Pair<String,String>(Predicates.RDFS_LABEL, null));
		}
		//create the elements map, where the key is element name in lowercase and the value is escaped element value 
		elements = new LinkedHashMap<String, XmlElementMetadata>();
		//set Uri or Label element
		elements.put(getUriOrLabel().toLowerCase(),new XmlElementMetadata(getUriOrLabel()));

		//set other element names
		for(Pair<String,String> columnPair : getSelectedColumns()) {
			String element = columnPair.getRight() != null
				? columnPair.getRight()
					: columnPair.getLeft();
			String elemName = getUniqueElementName(getEscapedElementName(element));
			elements.put(elemName.toLowerCase(), new XmlElementMetadata(elemName));
		}		
	}
	/**
	 * Write doument start element(s)
	 * @param writer
	 * @throws XMLStreamException
	 */
	protected void writeDocumentStart(XMLStreamWriter writer) throws XMLStreamException{
		writer.writeStartElement(DATA_ROOT_ELEMENT);	
	}
	/**
	 * Write document end element(s)
	 * @param writer
	 * @throws XMLStreamException
	 */
	protected void writeDocumentEnd(XMLStreamWriter writer) throws XMLStreamException{
		writer.writeEndElement();
	}
	/**
	 * writes simple XML element with start tag, textual content and end tag
	 * @param writer
	 * @param element
	 * @param value
	 * @throws XMLStreamException
	 */
	protected void writeSimpleDataElement(XMLStreamWriter writer, String element, String value) throws XMLStreamException{		
		writer.writeStartElement(element);
		writer.writeCharacters(value);
		writer.writeEndElement();
	}
	/**
	 * returns the list of element names
	 * @return
	 */
	public Map<String,XmlElementMetadata> getElements() {
		return elements;
	}
	/**
	 * Escape invalid characters that are not allowed in XML element names
	 * if the name is not still valid, then replace it with INVALID_ELEMENT_NAME 
	 *  
	 */
	protected String getEscapedElementName(String elementName){
		
		if(elementName==null || elementName.length()==0)  elementName=INVALID_ELEMENT_NAME;
		
		//replace whitespaces and other reserved characters with underscore
		elementName = elementName.replaceAll("[^A-Za-z0-9_-]", "_");

		//add leading unerscore if the name starts with invalid character or if it starts with xml (any case)
		if(!elementName.substring(0,1).matches("[A-Z]|_|[a-z]")||
				elementName.toLowerCase().startsWith("xml")){
			elementName = "_" + elementName;
		}
			
		return elementName;
	}
	/**
	 * If the given element name already exists (case insensitive) in the list of element names, 
	 * then append a trailing unique number. 
	 * 
	 * @param elementName
	 * @return
	 */
	protected String getUniqueElementName(String elementName){
		
		if(elementName==null || elementName.length()==0)  elementName=INVALID_ELEMENT_NAME;
		
		if(getElements()!=null){
			while (getElements().containsKey(elementName.toLowerCase())){
				int dashPos = elementName.lastIndexOf( "_" );
				if (dashPos>1 && dashPos<elementName.length()-1){
					String snum = elementName.substring(dashPos+1);
			          try{
			        	  int inum = Integer.parseInt(snum);
			        	  elementName = elementName.substring(0, dashPos ) + "_" + (inum+1);
			          }
			          catch(Exception e){
			        	  elementName = elementName + "_1";
			          }
			      }
			      else{
		        	  elementName = elementName + "_1";
			      }
			}
		}
		return elementName;
			
	}
}

