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

package eionet.cr.web.util;

import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import eionet.cr.dto.UploadDTO;

/**
 * Sitemap xml writer.
 *
 * @author Juhan Voolaid
 */
public class SitemapXmlWriter {

    private static final String ENCODING = "UTF-8";
    private static final String SCHEMA_NS_URI = "http://sw.deri.org/2007/07/sitemapextension/scschema.xsd";
    private static final String SCHEMA_NS_PREFIX = "sc";
    private static final String ROOT_ELEMENT = "urlset";
    private static final String DATASET_ELEMENT = "dataset";
    private static final String DATASET_LABEL_ELEMENT = "datasetLabel";
    private static final String DATA_DUMP_LOCATION_ELEMENT = "dataDumpLocation";

    /**
     * XMLWriter to write XML to.
     */
    private XMLStreamWriter writer = null;

    public SitemapXmlWriter(OutputStream out) throws XMLStreamException, FactoryConfigurationError {
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
    }

    /**
     * Writes sitemap xml into stream based of the uploads data.
     *
     * @param uploads
     * @throws XMLStreamException
     */
    public void writeSitemapXml(List<UploadDTO> uploads) throws XMLStreamException {
        writer.writeStartDocument(ENCODING, "1.0");
        writer.writeStartElement(ROOT_ELEMENT);
        writer.writeNamespace(SCHEMA_NS_PREFIX, SCHEMA_NS_URI);

        for (UploadDTO upload : uploads) {
            writer.writeStartElement(SCHEMA_NS_PREFIX, DATASET_ELEMENT, SCHEMA_NS_URI);
            writer.writeStartElement(SCHEMA_NS_PREFIX, DATASET_LABEL_ELEMENT, SCHEMA_NS_URI);
            writer.writeCharacters(upload.getLabel());
            writer.writeEndElement();
            writer.writeStartElement(SCHEMA_NS_PREFIX, DATA_DUMP_LOCATION_ELEMENT, SCHEMA_NS_URI);
            writer.writeCharacters(upload.getSubjectUri());
            writer.writeEndElement();
            writer.writeEndElement();
        }
        writer.writeEndDocument();
    }
}
