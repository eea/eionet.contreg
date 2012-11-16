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
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.filestore.FileStore;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Util;
import eionet.cr.web.action.DownloadServlet;
import eionet.cr.web.action.ExportTriplesActionBean;
import eionet.cr.web.action.TabularDataServlet;
import eionet.cr.web.action.factsheet.ViewActionBean;

/**
 * Purpose of this filter is to enable RESTful download of files stored at CR. Since it is assumed that all these will have a URL
 * pointing to some user home directory of CR, then this filter is relevant (and should be applied to) only URLs with pattern
 * /home/*.
 *
 * See https://svn.eionet.europa.eu/projects/Reportnet/ticket/2464 and https://svn.eionet.europa.eu/projects/Reportnet/ticket/2054
 * for more background.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class HomespaceUrlFilter implements Filter {

    /** */
    private static final Logger LOGGER = Logger.getLogger(HomespaceUrlFilter.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // no initialization actions required
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        // Pass on if not a HTTP request.
        if (!(servletRequest instanceof HttpServletRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // Instantiate local variables for HTTP request, response and request-URL
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        String requestURL = httpRequest.getRequestURL().toString();

        boolean isProjectFolder = FolderUtil.isProjectFolder(requestURL);

        String contextPath = httpRequest.getContextPath();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("httpRequest.getRequestURL() = " + requestURL);
            LOGGER.trace("httpRequest.getRequestURI() = " + httpRequest.getRequestURI());
            LOGGER.trace("httpRequest.getContextPath() = " + contextPath);
            LOGGER.trace("httpRequest.getServletPath() = " + httpRequest.getServletPath());
            LOGGER.trace("httpRequest.getPathInfo() = " + httpRequest.getPathInfo());
        }

        // Parse path info if it is not null and its length is greater than 1
        // (because if its equal to 1, then it is "/", meaning the application's root URI).
        String pathInfo = httpRequest.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {

            int i = pathInfo.indexOf('/', 1);
            if (i != -1 && pathInfo.length() > (i + 1)) {

                try {
                    // Prepare the default redirect location.
                    String queryString = "?uri=" + URLEncoder.encode(requestURL, "UTF-8");
                    String redirectLocation = contextPath + Util.getUrlBinding(ExportTriplesActionBean.class) + queryString;

                    // Override the prepared redirect location in the below special cases.
                    if (DAOFactory.get().getDao(HelperDAO.class).isTabularDataSubject(requestURL)) {
                        // The requested URL is a subject in tabular data (i.e. CSV or TSV) file.
                        redirectLocation = contextPath + TabularDataServlet.URL_PATTERN + queryString;
                        LOGGER.debug("URL points to tabular data subject, redirecting to: " + redirectLocation);
                        // if project file, do not check the filestore in ordinary way
                    } else if (isStoredFile(pathInfo, isProjectFolder) && !isRdfXmlPreferred(httpRequest)) {
                        // The requested URL is a stored file, and requester wants original copy (i.e. not triples)
                        redirectLocation = contextPath + DownloadServlet.URL_PATTERN + queryString;
                        LOGGER.debug("URL points to stored file, redirecting to: " + redirectLocation);
                    } else if (isSparqlBookmark(pathInfo)) {
                        if (isRdfXmlPreferred(httpRequest)) {
                            redirectLocation =
                                    contextPath + Util.getUrlBinding(ExportTriplesActionBean.class) + queryString
                                    + "&exportProperties=";
                        } else {
                            redirectLocation = contextPath + Util.getUrlBinding(ViewActionBean.class) + queryString;
                        }
                    }

                    // Redirect to the location resolved above.
                    httpResponse.sendRedirect(redirectLocation);
                    return;
                } catch (DAOException e) {
                    throw new ServletException(e.getMessage(), e);
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Checks if requestPath is like user's SPARQL bookmark.
     *
     * @param requestPathInfo
     * @return
     */
    private boolean isSparqlBookmark(String requestPathInfo) {
        String[] data = StringUtils.split(requestPathInfo, "/");
        if (data != null && data.length == 3 && data[1].equals("bookmarks")) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param requestPathInfo
     * @return
     */
    private boolean isStoredFile(String requestPathInfo, boolean isProjectUri) {
        if (isProjectUri) {
            return FileStore.getInstance("project").getFile(requestPathInfo) != null;
        } else {
            int i = requestPathInfo.indexOf('/', 1);
            String userName = requestPathInfo.substring(1, i);
            String filePath = requestPathInfo.substring(i + 1);
            return FileStore.getInstance(userName).getFile(filePath) != null;
        }
    }

    /**
     * @param httpRequest
     * @return
     */
    private boolean isRdfXmlPreferred(HttpServletRequest httpRequest) {

        String acceptHeader = httpRequest.getHeader("Accept");
        return acceptHeader != null && acceptHeader.trim().toLowerCase().startsWith("application/rdf+xml");
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
