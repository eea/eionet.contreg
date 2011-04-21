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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>, Tieto Estonia
 */

public class XmlWithSchemaExporter extends XmlExporter {

    protected static final String SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    protected static final String SCHEMA_NS_PREFIX = "xsd";

    @Override
    protected void writeDocumentStart(XMLStreamWriter writer) throws XMLStreamException{
        writer.writeStartElement(ROOT_ELEMENT);
        writer.writeNamespace(SCHEMA_NS_PREFIX, SCHEMA_NS_URI);
        writer.writeStartElement(DATA_ROOT_ELEMENT);
    }

    @Override
    protected void writeDocumentEnd(XMLStreamWriter writer) throws XMLStreamException{
        //data root element
        writer.writeEndElement();

        // write XML schema
        writeXmlSchema(writer);

        // root element
        writer.writeEndElement();
    }

    protected void writeXmlSchema(XMLStreamWriter writer) throws XMLStreamException{
        writer.writeStartElement(SCHEMA_NS_URI, "schema");

        //data root element definition
        writer.writeStartElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", DATA_ROOT_ELEMENT);
        writer.writeStartElement(SCHEMA_NS_URI, "complexType");
        writer.writeStartElement(SCHEMA_NS_URI, "sequence");
        writer.writeEmptyElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("ref", ROW_ELEMENT);
        writer.writeAttribute("minOccurs", "0");
        writer.writeAttribute("maxOccurs", "unbounded");
        writer.writeEndElement(); //sequence
        writer.writeEndElement(); //complexType
        writer.writeEndElement(); //element

        //table element definition
        writer.writeStartElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", ROW_ELEMENT);
        writer.writeStartElement(SCHEMA_NS_URI, "complexType");
        writer.writeStartElement(SCHEMA_NS_URI, "sequence");

        //write elements
        for(XmlElementMetadata element : getElements().values()) {
            if(element.getType() == XmlElementMetadata.Type.DOUBLE) {
                writeDoubleElement(writer, element);
            }
            else {
                writeStringElement(writer, element);
            }
        }
        //end of table element definition
        writer.writeEndElement(); //sequence
        writer.writeEndElement(); //complexType
        writer.writeEndElement(); //element

        //end of schema
        writer.writeEndElement();
    }
    private void writeDoubleElement(XMLStreamWriter writer, XmlElementMetadata element) throws XMLStreamException{
        writer.writeEmptyElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", element.getName());
        writer.writeAttribute("minOccurs", "0");
        writer.writeAttribute("type", "xsd:double");

    }
    private void writeStringElement(XMLStreamWriter writer, XmlElementMetadata element) throws XMLStreamException{
        writer.writeStartElement(SCHEMA_NS_URI, "element");
        writer.writeAttribute("name", element.getName());
        writer.writeAttribute("minOccurs", "0");
        //write simple Type info
        writer.writeStartElement(SCHEMA_NS_URI, "simpleType");
        writer.writeStartElement(SCHEMA_NS_URI, "restriction");
        writer.writeAttribute("base", "xsd:string");
        writer.writeEmptyElement(SCHEMA_NS_URI, "maxLength");
        writer.writeAttribute("value", String.valueOf((element.getMaxLength()>255)?element.getMaxLength():255));
        writer.writeEndElement();//restriction
        writer.writeEndElement();//simpleType
        writer.writeEndElement();//element
    }
}
