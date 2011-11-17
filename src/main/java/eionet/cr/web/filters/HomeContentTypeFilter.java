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
import eionet.cr.filestore.FileStore;
import eionet.cr.web.action.TabularDataServlet;

/**
 * Purpose of this filter is to enable RESTful download of files stored at CR. Since it is assumed that all these will have a URL
 * pointing to some user home directory of CR, then this filter is relevant (and should be applied to) only URLs with pattern
 * /home/*.
 *
 * See https://svn.eionet.europa.eu/projects/Reportnet/ticket/2464 for more background.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class HomeContentTypeFilter implements Filter {

    /** */
    private static final Logger LOGGER = Logger.getLogger(HomeContentTypeFilter.class);

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // no initialization actions required
    }

    /**
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

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("httpRequest.getRequestURL() = " + requestURL);
            LOGGER.trace("httpRequest.getRequestURI() = " + httpRequest.getRequestURI());
            LOGGER.trace("httpRequest.getContextPath() = " + httpRequest.getContextPath());
            LOGGER.trace("httpRequest.getServletPath() = " + httpRequest.getServletPath());
            LOGGER.trace("httpRequest.getPathInfo() = " + httpRequest.getPathInfo());
        }

        // Parse path info if it is not null and its length is greater than 1
        // (because if its equal to 1, then it is "/", meaning the application's root URI).
        String pathInfo = httpRequest.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {

            int i = pathInfo.indexOf('/', 1);
            if (i != -1 && pathInfo.length() > (i + 1)) {

                // Extract user name and file name from the path info.
                String userName = pathInfo.substring(1, i);
                String filePath = pathInfo.substring(i + 1);
                String fileUri = new String(requestURL);

                // if is tabular data and (idInFile!=null || rdf not accepted)

                // Extract id of a resource/object/tag/code/... inside the file.
                // For example if http://cr.eionet.europa.eu/home/userName/fileName identifies
                // a file that contains a code-list (e.g. country-codes), then
                // http://cr.eionet.europa.eu/home/userName/fileName/AT identifies code "AT"
                // inside that file.
                String idInFile = "";
                String fileName = "";
                String fileUriWithoutCode = fileUri;
                if (!StringUtils.isBlank(filePath)) {
                    if (filePath.contains("/")) {
                        idInFile = StringUtils.substringAfterLast(filePath, "/");
                        fileName = StringUtils.substringBeforeLast(filePath, "/");
                        fileUriWithoutCode = StringUtils.substringBeforeLast(requestURL, "/" + idInFile);
                    }
                }

                try {
                    if (TabularDataServlet.willHandle(fileUriWithoutCode, idInFile, httpRequest)) {
                        String redirectPath =
                                httpRequest.getContextPath() + "/tabularData?fileUri=" + URLEncoder.encode(fileUriWithoutCode, "UTF-8");
                        if (!StringUtils.isBlank(fileName)) {
                            redirectPath = redirectPath + "&idInFile=" + URLEncoder.encode(idInFile, "UTF-8");
                        }
                        LOGGER.debug("URL points to tabular data, so redirecting to: " + redirectPath);
                        httpResponse.sendRedirect(redirectPath);
                        return;
                    } else if (FileStore.getInstance(userName).get(filePath) != null) {

                        String redirectPath =
                                httpRequest.getContextPath() + "/download?uri=" + URLEncoder.encode(requestURL, "UTF-8");
                        LOGGER.debug("URL points to stored file, so redirecting to: " + redirectPath);
                        httpResponse.sendRedirect(redirectPath);
                        return;
                    } else if (httpRequest.getHeader("Accept")!=null
                            && httpRequest.getHeader("Accept").trim().toLowerCase().startsWith("application/rdf+xml")) {

                        httpResponse.sendRedirect(httpRequest.getContextPath() + "/exportTriples.action?uri=" + URLEncoder.encode(requestURL, "UTF-8"));
                        return;
                    } else {
                        // If no file is found and is not "application/rdf+xml" request
                        httpResponse.sendRedirect(httpRequest.getContextPath() + "/exportTriples.action?uri=" + URLEncoder.encode(requestURL, "UTF-8"));
                        return;
                    }
                } catch (DAOException e) {
                    throw new ServletException(e.getMessage(), e);
                }
            }
        }

        filterChain.doFilter(servletRequest, servletResponse);
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
