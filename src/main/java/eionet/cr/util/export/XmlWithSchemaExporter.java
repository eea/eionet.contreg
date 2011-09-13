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
    protected void writeDocumentStart(XMLStreamWriter writer) throws XMLStreamException {
        writer.writeStartElement(ROOT_ELEMENT);
        writer.writeNamespace(SCHEMA_NS_PREFIX, SCHEMA_NS_URI);
        writer.writeStartElement(DATA_ROOT_ELEMENT);
    }

    @Override
    protected void writeDocumentEnd(XMLStreamWriter writer) throws XMLStreamException {
        // data root element
        writer.writeEndElement();

        // write XML schema
        new SchemaHelper(writer, getElements()).writeXmlSchema();

        // root element
        writer.writeEndElement();
    }

}
