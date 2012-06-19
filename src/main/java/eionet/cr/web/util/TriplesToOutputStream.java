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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.web.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;

/**
 * @author Risto Alt
 * @author Jaanus Heinlaid
 */
public final class TriplesToOutputStream {

    /** */
    private static final String RDF_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String RDF_NAMESPACE = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDFS_NAMESPACE = "http://www.w3.org/2000/01/rdf-schema#";

    /**
     * Hide utility class constructor.
     */
    private TriplesToOutputStream() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param out
     * @param subjectUri
     * @param triples
     */
    public static void triplesToHtml(OutputStream out, String subjectUri, List<SubjectDTO> triples) {

        OutputStreamWriter writer = new OutputStreamWriter(out);
        try {
            writer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-gb\">");
            writer.append("<head>");
            writer.append("<title>").append(subjectUri).append("</title>");
            writer.append("<link rel=\"alternate\" type=\"application/rdf+xml\" href=\"").append(subjectUri).append("\" />");
            writer.append("<style type=\"text/css\">" + "/*<![CDATA[*/ "
                    + "table { border: 1px solid black; border-collapse:collapse; } "
                    + "td, th { border: 1px solid black; padding: 0.3em; } " + "/*]]>*/" + "</style>");
            writer.append("</head>");
            writer.append("<body>");
            writer.append("<h1>").append(subjectUri).append("</h1>");
            writer.append("<table>");
            writer.append("<tr><th>Subject</th><th>Predicate</th><th>Value</th></tr>");

            if (triples != null) {
                for (SubjectDTO subject : triples) {
                    Map<String, Collection<ObjectDTO>> predicates = subject.getPredicates();
                    if (predicates != null) {
                        for (String predicateUri : predicates.keySet()) {
                            Collection<ObjectDTO> objects = predicates.get(predicateUri);
                            if (objects != null) {
                                for (ObjectDTO object : objects) {
                                    writer.append("<tr>");
                                    writer.append("<td>");
                                    writer.append("<a href=\"").append(subject.getUri()).append("\">");
                                    writer.append(subject.getUri());
                                    writer.append("</a>");
                                    writer.append("</td>");
                                    writer.append("<td>").append(predicateUri).append("</td>");
                                    writer.append("<td>");
                                    if (object.isLiteral()) {
                                        writer.append(object.getValue());
                                    } else {
                                        writer.append("<a href=\"").append(object.getValue()).append("\">")
                                        .append(object.getValue()).append("</a>");
                                    }
                                    writer.append("</td>");
                                    writer.append("</tr>");
                                }
                            }
                        }
                    }
                }
                /*
                 * for (TripleDTO triple : triples) { writer.append("<tr>");
                 * writer.append("<td>").append(triple.getSubjectUri()).append("</td>");
                 * writer.append("<td>").append(triple.getPredicateUri()).append("</td>");
                 * writer.append("<td>").append(triple.getObject()).append("</td>"); writer.append("</tr>"); }
                 */
            }
            writer.append("</table>");
            writer.append("</body>");
            writer.append("</html>");
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @param out
     * @param subjectUri
     * @param triples
     */
    public static void triplesToRdf(OutputStream out, String subjectUri, List<SubjectDTO> triples) {
        OutputStreamWriter writer = new OutputStreamWriter(out);
        try {
            if (triples != null) {
                writer.append(RDF_HEADER);
                writer.append("<rdf:RDF xmlns=\"").append(subjectUri + "#").append("\" xmlns:rdf=\"").append(RDF_NAMESPACE)
                .append("\" ").append("xmlns:rdfs=\"").append(RDFS_NAMESPACE).append("\">");
                for (SubjectDTO subject : triples) {

                    writer.append("<rdf:Description rdf:about=\"").append(StringEscapeUtils.escapeXml(subject.getUri()))
                    .append("\">");
                    Map<String, Collection<ObjectDTO>> predicates = subject.getPredicates();
                    if (predicates != null) {
                        for (String predicateUri : predicates.keySet()) {
                            Collection<ObjectDTO> objects = predicates.get(predicateUri);

                            // Shorten predicate URIs
                            if (predicateUri.startsWith(RDF_NAMESPACE)) {
                                predicateUri = predicateUri.replace(RDF_NAMESPACE, "rdf:");
                            } else if (predicateUri.startsWith(RDFS_NAMESPACE)) {
                                predicateUri = predicateUri.replace(RDFS_NAMESPACE, "rdfs:");
                            } else if (predicateUri.startsWith(subjectUri)) {
                                predicateUri = predicateUri.replace(subjectUri + "#", "");
                            }

                            if (objects != null) {
                                for (ObjectDTO object : objects) {
                                    if (object.isLiteral()) {
                                        writer.append("<").append(predicateUri).append(">")
                                        .append(StringEscapeUtils.escapeXml(object.getValue())).append("</")
                                        .append(predicateUri).append(">");
                                    } else {
                                        writer.append("<").append(predicateUri).append(" rdf:resource=\"")
                                        .append(StringEscapeUtils.escapeXml(object.getValue())).append("\"/>");
                                    }
                                }
                            }
                        }
                    }
                    writer.append("</rdf:Description>");
                }
                writer.append("</rdf:RDF>");
                writer.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
