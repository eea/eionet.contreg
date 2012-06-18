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

import eionet.cr.util.Util;
import eionet.cr.web.action.ExportTriplesActionBean;

/**
 * Forwards SPARQL url to export triples action bean.
 * 
 * @author Juhan Voolaid
 */
public class SparqlBookmarksUrlFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURL = ((HttpServletRequest) request).getRequestURL().toString();
        String uri = URLEncoder.encode(requestURL, "UTF-8");
        String redirectLocation =
                ((HttpServletRequest) request).getContextPath() + Util.getUrlBinding(ExportTriplesActionBean.class) + "?uri="
                        + uri;
        // Redirect to the location resolved above.
        ((HttpServletResponse) response).sendRedirect(redirectLocation);
    }

    @Override
    public void destroy() {
    }

}
