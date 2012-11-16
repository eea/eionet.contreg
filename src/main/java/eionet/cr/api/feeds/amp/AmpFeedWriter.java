/*
 * The contents of this file are subject to the Mozilla Public
 *
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
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.api.feeds.amp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Literal;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.NamespaceUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.export.ExportException;
import eionet.cr.util.sesame.SPARQLResultSetReader;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class AmpFeedWriter implements SPARQLResultSetReader {

    /** */
    private OutputStream outputStream;

    /** */
    private int rowCount;

    /** */
    private HashMap<String, String> namespaces = new HashMap<String, String>();

    /** */
    private String xmlLang;

    /** */
    private String currSubjUri;
    private String currPredUri;

    /** */
    private StringBuilder currSubjBuf;

    /** */
    private HashSet<String> currPredWrittenObjects;

    /** */
    private int writtenTriplesCount = 0;
    private int writtenSubjectsCount = 0;

    /**
     *
     * @param outputStream
     */
    public AmpFeedWriter(OutputStream outputStream) {

        this.outputStream = outputStream;

        addNamespace(Namespace.RDF);
        addNamespace(Namespace.RDFS);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        rowCount++;
        try {
            if (rowCount == 1) {
                // write RDF header
                outputStream.write("<?xml version=\"1.0\"?>".getBytes());
                outputStream.write(("\n<rdf:RDF" + getHeaderAttributes() + ">").getBytes());
                outputStream.flush();
            }

            Value subjectValue = bindingSet.getValue("s");
            String subjUri = subjectValue.stringValue();
            if (currSubjUri == null || !currSubjUri.equals(subjUri)) {

                // new subject, so close previous one, unless it's the first time
                if (rowCount > 1) {

                    // close the subject
                    currSubjBuf.append("\n\t</rdf:Description>");
                    outputStream.write(currSubjBuf.toString().getBytes());
                    outputStream.flush();
                }

                // start new subject
                currSubjBuf = new StringBuilder("\n\t<rdf:Description rdf:about=\"");
                currSubjBuf.append(StringEscapeUtils.escapeXml(subjUri)).append("\">");

                // set current subject to the new one
                currSubjUri = subjUri;

                writtenSubjectsCount++;
            }

            Value predicateValue = bindingSet.getValue("p");
            String predUri = predicateValue.stringValue();

            // if new predicate, clear the set of already written objects
            if (currPredUri == null || !currPredUri.equals(predUri)) {

                currPredWrittenObjects = new HashSet<String>();
                currPredUri = predUri;
            }

            Value objectValueObject = bindingSet.getValue("o");
            boolean isLitObject = objectValueObject instanceof Literal;

            // skip literal values of rdf:type
            if (predUri.equals(Predicates.RDF_TYPE) && isLitObject) {
                return;
            }

            String objValue = objectValueObject.stringValue();
            boolean objAlreadyWritten = currPredWrittenObjects.contains(objValue);

            // skip if object value is blank, or object has already been written
            if (StringUtils.isBlank(objValue) || objAlreadyWritten) {
                return;
            }

            // get namespace URI for this predicate
            String predNsUri = NamespaceUtil.extractNamespace(predUri);
            if (StringUtils.isBlank(predNsUri)) {
                throw new CRRuntimeException("Could not extract namespace URL from " + predUri);
            }

            // include only predicates from supplied namespaces
            if (namespaces.containsKey(predNsUri)) {

                // extract predicate's local name
                String predLocalName = StringUtils.substringAfterLast(predUri, predNsUri);
                if (StringUtils.isBlank(predLocalName)) {
                    throw new CRRuntimeException("Could not extract local name from " + predUri);
                }

                // start predicate tag
                currSubjBuf.append("\n\t\t<").append(namespaces.get(predNsUri)).append(":").append(predLocalName);

                // prepare escaped-for-XML object value
                String objValueEscaped = StringEscapeUtils.escapeXml(objValue);

                // write object value, depending on whether it is literal or not
                // (and close the predicate tag too!)
                if (!isLitObject && URLUtil.isURL(objValue)) {

                    currSubjBuf.append(" rdf:resource=\"").append(objValueEscaped).append("\"/>");
                } else {
                    currSubjBuf.append(">").append(objValueEscaped).append("</").append(namespaces.get(predNsUri)).append(":")
                    .append(predLocalName).append(">");
                }

                writtenTriplesCount++;
                currPredWrittenObjects.add(objValue);
            }
        } catch (IOException e) {
            throw new ExportException(e.toString(), e);
        }
    }

    /**
     * @throws IOException
     *
     */
    public void closeRDF() throws IOException {

        currSubjBuf.append("\n\t</rdf:Description>");
        outputStream.write(currSubjBuf.toString().getBytes());
        outputStream.write("</rdf:RDF>\n".getBytes());
        outputStream.flush();
    }

    /**
     * @throws IOException
     *
     */
    public void writeEmptyHeader() throws IOException {
        outputStream.write("<rdf:RDF/>".getBytes());
        outputStream.flush();
    }

    /**
     *
     * @return
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     *
     * @param namespace
     */
    public void addNamespace(Namespace namespace) {
        namespaces.put(namespace.getUri(), namespace.getPrefix());
    }

    /**
     *
     * @param xmlLang
     */
    public void setXmlLang(String xmlLang) {
        this.xmlLang = xmlLang;
    }

    /**
     *
     * @return
     */
    private String getHeaderAttributes() {

        StringBuffer buf = new StringBuffer("");
        if (xmlLang != null && xmlLang.trim().length() > 0) {
            buf.append(" xml:lang=\"").append(xmlLang).append("\"");
        }

        if (!namespaces.isEmpty()) {
            for (Entry<String, String> entry : namespaces.entrySet()) {
                buf.append("\n   xmlns:").append(entry.getValue()).append("=\"").append(entry.getKey()).append("\"");
            }
        }

        return buf.toString();
    }

    /**
     *
     * @return
     */
    public int getWrittenTriplesCount() {
        return writtenTriplesCount;
    }

    /**
     *
     * @return
     */
    public int getWrittenSubjectsCount() {
        return writtenSubjectsCount;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetReader#getResultList()
     */
    @Override
    public List getResultList() {
        return null;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.readers.ResultSetReader#endResultSet()
     */
    @Override
    public void endResultSet() {
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#startResultSet(java.util.List)
     */
    @Override
    public void startResultSet(List bindingNames) {
    }
}
