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
package eionet.cr.api.feeds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.common.SubjectProcessor;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.URLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubjectsRDFWriter {

    /** */
    private static final Logger LOGGER = Logger.getLogger(SubjectsRDFWriter.class);

    /** */
    private HashMap<String, String> namespaces = new HashMap<String, String>();

    /** */
    private String xmlLang;

    /** */
    private SubjectProcessor subjectProcessor;

    /** */
    private boolean includeDerivedValues = false;

    /**
     *
     */
    public SubjectsRDFWriter() {

        addNamespace(Namespace.RDF);
        addNamespace(Namespace.RDFS);
    }

    /**
     *
     */
    public SubjectsRDFWriter(boolean includeDerivedValues) {

        this();
        this.includeDerivedValues = includeDerivedValues;
    }

    /**
     *
     * @param url
     * @param prefix
     */
    public void addNamespace(Namespace namespace) {
        namespaces.put(namespace.getUri(), namespace.getPrefix());
    }

    /**
     *
     * @return
     */
    private String getAttributes() {

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
    private String getRootElementEnd() {
        return "</rdf:RDF>";
    }

    /**
     * @param xmlLang the xmlLang to set
     */
    public void setXmlLang(String xmlLang) {
        this.xmlLang = xmlLang;
    }

    /**
     *
     * @param subjects
     * @param out
     * @throws IOException
     */
    public void write(List<SubjectDTO> subjects, OutputStream out) throws IOException {

        // if no subjects, write empty rdf:RDF tag
        if (subjects == null || subjects.isEmpty()) {
            out.write("<rdf:RDF/>".getBytes());
            return;
        }

        // start rdf:RDF element
        out.write(("<rdf:RDF" + getAttributes() + ">").getBytes());

        // loop over subjects
        for (SubjectDTO subject : subjects) {

            // initialize subject processor if not initialized yet
            if (subjectProcessor != null) {
                subjectProcessor.process(subject);
            }

            // continuing has only point if subject has at least one predicate
            if (subject.getPredicateCount() > 0) {

                String subjectUri = subject.getUri();
                if (StringUtils.isBlank(subjectUri)) {
                    LOGGER.error("Subject URI must not be blank (subject hash = " + subject.getUriHash() + ")");
                    continue;
                }

                // start rdf:Description tag
                StringBuffer buf = new StringBuffer("\n\t<rdf:Description rdf:about=\"");
                buf.append(StringEscapeUtils.escapeXml(subjectUri)).append("\">");

                // loop over this subject's predicates
                for (Entry<String, Collection<ObjectDTO>> entry : subject.getPredicates().entrySet()) {

                    String predicate = entry.getKey();
                    Collection<ObjectDTO> objects = entry.getValue();

                    // continue only if predicate has at least one object
                    if (objects != null && !objects.isEmpty()) {

                        // get namespace URI for this predicate
                        String nsUrl = extractNamespace(predicate);
                        if (nsUrl == null || nsUrl.trim().length() == 0) {
                            throw new CRRuntimeException("Could not extract namespace URL from " + predicate);
                        }

                        // include only predicates from supplied namespaces
                        if (namespaces.containsKey(nsUrl)) {

                            // extract predicate's local name
                            String localName = StringUtils.substringAfterLast(predicate, nsUrl);
                            if (localName == null || localName.trim().length() == 0) {
                                throw new CRRuntimeException("Could not extract local name from " + predicate);
                            }

                            // hash-set for remembering already written object values
                            HashSet<String> alreadyWritten = new HashSet<String>();

                            // loop over this predicate's objects
                            for (ObjectDTO object : entry.getValue()) {

                                // skip literal values of rdf:type
                                if (object.isLiteral() && predicate.equals(Predicates.RDF_TYPE)) {
                                    continue;
                                }

                                String objectValue = object.getValue();
                                boolean isDerivedObject = object.getDerivSourceHash() != 0;

                                // include only non-blank and non-derived objects
                                // that have not been written yet
                                if (!StringUtils.isBlank(objectValue) && !alreadyWritten.contains(objectValue)
                                        && (includeDerivedValues || !isDerivedObject)) {

                                    // start predicate tag
                                    buf.append("\n\t\t<").append(namespaces.get(nsUrl)).append(":").append(localName);

                                    // prepare escaped-for-XML object value
                                    String escapedValue = StringEscapeUtils.escapeXml(objectValue);

                                    // write object value, depending on whether it is literal or not
                                    // (close the predicate tag too)
                                    if (!object.isLiteral() && URLUtil.isURL(objectValue)) {

                                        buf.append(" rdf:resource=\"").append(escapedValue).append("\"/>");
                                    } else {
                                        buf.append(">").append(escapedValue).append("</").append(namespaces.get(nsUrl))
                                                .append(":").append(localName).append(">");
                                    }

                                    alreadyWritten.add(objectValue);
                                }
                            }
                        }
                    }
                }

                // close rdf:Description tag
                buf.append("\n\t</rdf:Description>");
                out.write(buf.toString().getBytes());
            }
        }

        // close rdf:RDF tag
        out.write("</rdf:RDF>\n".getBytes());
    }

    /**
     *
     * @param url
     * @return
     */
    public static String extractNamespace(String url) {

        return eionet.cr.util.NamespaceUtil.extractNamespace(url);
    }

    /**
     *
     * @param subject
     */
    protected void preProcessSubject(SubjectDTO subject) {
    }

    /**
     * @param subjectProcessor the subjectProcessor to set
     */
    public void setSubjectProcessor(SubjectProcessor subjectProcessor) {
        this.subjectProcessor = subjectProcessor;
    }
}
