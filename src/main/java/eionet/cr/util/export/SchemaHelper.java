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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.util.export;

import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * Helper for writing Microsoft Office compatible xml schema into xml stream.
 * 
 * @author Juhan Voolaid
 */
public class SchemaHelper {

    protected static final String SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    protected static final String SCHEMA_NS_PREFIX = "xsd";
    protected static final String ROOT_ELEMENT = "root";
    protected static final String DATA_ROOT_ELEMENT = "dataroot";
    protected static final String ROW_ELEMENT = "resources";

    /** Xml stream writer. */
    private XMLStreamWriter writer;

    /** Map of element names and its metadata. */
    private Map<String, XmlElementMetadata> elements;

    /**
     * Class constructor.
     * 
     * @param writer
     * @param elements
     */
    public SchemaHelper(XMLStreamWriter writer, Map<String, XmlElementMetadata> elements) {
        this.writer = writer;
        this.elements = elements;
    }

    public void writeXmlSchema() throws XMLStreamException {
        writer.writeStartElement(SCHEMA_NS_URI, "schema");

        // data root element definition
        writer.writeStartElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", DATA_ROOT_ELEMENT);
        writer.writeStartElement(SCHEMA_NS_URI, "complexType");
        writer.writeStartElement(SCHEMA_NS_URI, "sequence");
        writer.writeEmptyElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("ref", ROW_ELEMENT);
        writer.writeAttribute("minOccurs", "0");
        writer.writeAttribute("maxOccurs", "unbounded");
        writer.writeEndElement(); // sequence
        writer.writeEndElement(); // complexType
        writer.writeEndElement(); // element

        // table element definition
        writer.writeStartElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", ROW_ELEMENT);
        writer.writeStartElement(SCHEMA_NS_URI, "complexType");
        writer.writeStartElement(SCHEMA_NS_URI, "sequence");

        // write elements
        for (XmlElementMetadata element : elements.values()) {
            if (element.getType() == XmlElementMetadata.Type.DOUBLE) {
                writeDoubleElement(element);
            } else {
                writeStringElement(element);
            }
        }
        // end of table element definition
        writer.writeEndElement(); // sequence
        writer.writeEndElement(); // complexType
        writer.writeEndElement(); // element

        // end of schema
        writer.writeEndElement();
    }

    private void writeDoubleElement(XmlElementMetadata element) throws XMLStreamException {
        writer.writeEmptyElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", element.getName());
        writer.writeAttribute("minOccurs", "0");
        writer.writeAttribute("type", "xsd:double");

    }

    private void writeStringElement(XmlElementMetadata element) throws XMLStreamException {
        writer.writeStartElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", element.getName());
        writer.writeAttribute("minOccurs", "0");
        // write simple Type info
        writer.writeStartElement(SCHEMA_NS_URI, "simpleType");
        writer.writeStartElement(SCHEMA_NS_URI, "restriction");
        writer.writeAttribute("base", "xsd:string");
        writer.writeEmptyElement(SCHEMA_NS_URI, "maxLength");
        writer.writeAttribute("value", String.valueOf((element.getMaxLength() > 255) ? element.getMaxLength() : 255));
        writer.writeEndElement(); // restriction
        writer.writeEndElement(); // simpleType
        writer.writeEndElement(); // element
    }
}
