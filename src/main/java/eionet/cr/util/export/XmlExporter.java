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
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.readers.SubjectExportReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.FormatUtils;
import eionet.cr.util.Pair;

/**
 * @author Enriko Käsper, TietoEnator Estonia AS
 * XmlExporter
 */

public class XmlExporter extends Exporter implements SubjectExportEvent{

    protected static final String ENCODING = "UTF-8";
    protected static final String ROOT_ELEMENT = "root";
    protected static final String DATA_ROOT_ELEMENT = "dataroot";
    protected static final String ROW_ELEMENT = "resources";

    protected XMLStreamWriter writer = null;


    protected Map<String, XmlElementMetadata> elements=null;
    protected String[] elementKeys = null;

    @Override
    protected InputStream doExport() throws ExportException, IOException, DAOException {

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        try{
            writer = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream, ENCODING);
            writer.writeStartDocument(ENCODING, "1.0");
            //write root element
            writeDocumentStart(writer);

            //create element names Map
            parseElemNames();

            elementKeys = elements.keySet().toArray(new String[elements.size()]);

            SubjectExportReader reader = new SubjectExportReader(this);
            doExportQueryAndWriteDataIntoOutput(reader);

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
     * call-back method implements
     */
    public void writeSubjectIntoExporterOutput(SubjectDTO subject) throws ExportException {

        try{
            //	write row start element
            writer.writeStartElement(ROW_ELEMENT);

            //get uri or label value
            String uriOrLabelValue = getUriOrLabelValue(subject);

            XmlElementMetadata elementMetada = elements.get(elementKeys[0]);
            //write uri or label element
            XmlUtil.writeSimpleDataElement(writer, elementMetada.getName(), uriOrLabelValue);
            elementMetada.setMaxLength(uriOrLabelValue.length());

            //write other elements
            int elementIndex= 1;
            for(Pair<String,String> columnPair : getSelectedColumns()) {
                //label is already written
                if(Predicates.RDFS_LABEL.equals(columnPair.getLeft())) continue;

                String value = FormatUtils.getObjectValuesForPredicate(columnPair.getLeft(), subject, getLanguages());
                elementMetada = elements.get(elementKeys[elementIndex++]);
                XmlUtil.writeSimpleDataElement(writer, elementMetada.getName(), value);
                elementMetada.setMaxLength(value.length());
                elementMetada.setType(value);
            }

            writer.writeEndElement();
        }
        catch(Exception e){
            throw new ExportException(e.getMessage(), e);
        }
    }


    /**
     * Create element names map
     */
    protected void parseElemNames() {

        //create the elements map, where the key is element name in lowercase and the value is escaped element value
        elements = new LinkedHashMap<String, XmlElementMetadata>();
        //set Uri or Label element
        elements.put(getUriOrLabel().toLowerCase(),new XmlElementMetadata(getUriOrLabel()));

        //set other element names
        for(Pair<String,String> columnPair : getSelectedColumns()) {
            //label is already added to the list of elements
            if(Predicates.RDFS_LABEL.equals(columnPair.getLeft())) continue;

            String element = columnPair.getRight() != null
            ? columnPair.getRight()
                    : columnPair.getLeft();
            String elemName = getUniqueElementName(XmlUtil.getEscapedElementName(element));
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
     * returns the list of element names
     * @return
     */
    public Map<String,XmlElementMetadata> getElements() {
        return elements;
    }
    /**
     * If the given element name already exists (case insensitive) in the list of element names,
     * then append a trailing unique number.
     *
     * @param elementName
     * @return
     */
    protected String getUniqueElementName(String elementName){

        if(elementName==null || elementName.length()==0)  elementName=XmlUtil.INVALID_ELEMENT_NAME;

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

