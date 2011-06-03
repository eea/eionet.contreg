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
package eionet.cr.web.filters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;

/**
 * Purpose of this filter is to enable RESTful download of files stored at CR. Since it is assumed that all these will have a URL
 * pointing to some user home directory of CR, then this filter is relevant and should be applied to only URL with pattern /home/*.
 * 
 * See https://svn.eionet.europa.eu/projects/Reportnet/ticket/2464 for more background.
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class HomeContentTypeFilter implements Filter {

    /** */
    private static final Logger logger = Logger.getLogger(HomeContentTypeFilter.class);

    protected static final String rdfHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    protected static final String rdfNameSpace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    protected static final String rdfSNameSpace = "http://www.w3.org/2000/01/rdf-schema#";

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // do nothing in this overridden method
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        // pass on if not a HTTP request
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String requestURL = httpRequest.getRequestURL().toString();

        // logger.debug("httpRequest.getRequestURL() = " + requestURL);
        // logger.debug("httpRequest.getRequestURI() = " + httpRequest.getRequestURI());
        // logger.debug("httpRequest.getContextPath() = " + httpRequest.getContextPath());
        // logger.debug("httpRequest.getServletPath() = " + httpRequest.getServletPath());
        // logger.debug("httpRequest.getPathInfo() = " + httpRequest.getPathInfo());

        String pathInfo = httpRequest.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {

            int i = pathInfo.indexOf('/', 1);
            if (i != -1 && pathInfo.length() > (i + 1)) {

                String userName = pathInfo.substring(1, i);
                String fileName = pathInfo.substring(i + 1);
                String id = "";
                if (!StringUtils.isBlank(fileName)) {
                    int z = fileName.indexOf("/");
                    if (z != -1 && fileName.length() > (z + 1)) {
                        id = fileName.substring(z + 1);
                        fileName = fileName.substring(0, z);
                    }
                }

                if (FileStore.getInstance(userName).get(fileName) != null) {

                    String acceptHeader = httpRequest.getHeader("accept");
                    String[] accept = null;
                    if (acceptHeader != null && acceptHeader.length() > 0) {
                        accept = acceptHeader.split(",");
                    }

                    try {
                        String fileUri = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL) + "/home/"
                                + userName + "/" + fileName;
                        // Check if file is CSV or TSV (imported by user)
                        String type = DAOFactory.get().getDao(HarvestSourceDAO.class)
                                .getSourceMetadata(fileUri, Predicates.CR_MEDIA_TYPE);
                        if (type != null && (type.equals("csv") || type.equals("tsv"))) {
                            List<SubjectDTO> triples = null;
                            if (!StringUtils.isBlank(id)) {
                                String subjectUri = fileUri + "/" + id;
                                triples = DAOFactory.get().getDao(HelperDAO.class).getSPOsInSubject(fileUri, subjectUri);
                                fileUri = subjectUri;
                            } else {
                                triples = DAOFactory.get().getDao(HelperDAO.class).getSPOsInSource(fileUri);
                            }
                            // if accept-header is "application/rdf+xml" then return RDF, otherwise return HTML
                            if (accept != null && accept.length > 0 && accept[0].equals("application/rdf+xml")) {
                                httpResponse.setContentType("application/rdf+xml;charset=utf-8");
                                triplesToRdf(httpResponse.getOutputStream(), triples, fileUri);
                            } else {
                                httpResponse.setContentType("text/html;charset=utf-8");
                                triplesToHtml(httpResponse.getOutputStream(), triples, fileUri);
                            }
                            return;
                        } else {
                            String redirectPath = httpRequest.getContextPath() + "/download?uri="
                                    + URLEncoder.encode(requestURL, "UTF-8");
                            logger.debug("URL points to stored file, so redirecting to: " + redirectPath);
                            httpResponse.sendRedirect(redirectPath);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CRRuntimeException(e.getMessage());
                    }
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private void triplesToHtml(OutputStream out, List<SubjectDTO> triples, String fileUri) {
        OutputStreamWriter writer = new OutputStreamWriter(out);
        try {
            writer.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-gb\">");
            writer.append("<head>");
            writer.append("<title>").append(fileUri).append("</title>");
            writer.append("<link rel=\"alternate\" type=\"application/rdf+xml\" href=\"").append(fileUri).append("\" />");
            writer.append("<style type=\"text/css\">" + "/*<![CDATA[*/ "
                    + "table { border: 1px solid black; border-collapse:collapse; } "
                    + "td, th { border: 1px solid black; padding: 0.3em; } " + "/*]]>*/" + "</style>");
            writer.append("</head>");
            writer.append("<body>");
            writer.append("<h1>").append(fileUri).append("</h1>");
            writer.append("<table>");
            writer.append("<tr><th>Subject</th><th>Predicate</th><th>Value</th></tr>");

            if (triples != null) {
                for (SubjectDTO subject : triples) {
                    String subjectUri = subject.getUri();
                    Map<String, Collection<ObjectDTO>> predicates = subject.getPredicates();
                    if (predicates != null) {
                        for (String predicateUri : predicates.keySet()) {
                            Collection<ObjectDTO> objects = predicates.get(predicateUri);
                            if (objects != null) {
                                for (ObjectDTO object : objects) {
                                    writer.append("<tr>");
                                    writer.append("<td>").append(subjectUri).append("</td>");
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

    private void triplesToRdf(OutputStream out, List<SubjectDTO> triples, String fileUri) {
        OutputStreamWriter writer = new OutputStreamWriter(out);
        try {
            if (triples != null) {
                writer.append(rdfHeader);
                writer.append("<rdf:RDF xmlns=\"").append(fileUri + "#").append("\" xmlns:rdf=\"").append(rdfNameSpace)
                        .append("\" ").append("xmlns:rdfs=\"").append(rdfSNameSpace).append("\">");
                for (SubjectDTO subject : triples) {
                    String subjectUri = subject.getUri();
                    writer.append("<rdf:Description rdf:about=\"").append(StringEscapeUtils.escapeXml(subjectUri)).append("\">");
                    Map<String, Collection<ObjectDTO>> predicates = subject.getPredicates();
                    if (predicates != null) {
                        for (String predicateUri : predicates.keySet()) {
                            Collection<ObjectDTO> objects = predicates.get(predicateUri);

                            // Shorten predicate URIs
                            if (predicateUri.startsWith(rdfNameSpace)) {
                                predicateUri = predicateUri.replace(rdfNameSpace, "rdf:");
                            } else if (predicateUri.startsWith(rdfSNameSpace)) {
                                predicateUri = predicateUri.replace(rdfSNameSpace, "rdfs:");
                            } else if (predicateUri.startsWith(fileUri)) {
                                predicateUri = predicateUri.replace(fileUri + "#", "");
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

    /*
     * (non-Javadoc)
     * 
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // do nothing in this overridden method
    }
}
