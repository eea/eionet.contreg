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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

/**
 * Purpose of this filter is to enable RESTful download of files stored at CR.
 * Since it is assumed that all these will have a URL pointing to some user
 * home directory of CR, then this filter is relevant and should be applied
 * to only URL with pattern /home/*.
 * 
 * See https://svn.eionet.europa.eu/projects/Reportnet/ticket/2464 for more
 * background.
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public class HomeContentTypeFilter implements Filter{

    /** */
    private static final Logger logger = Logger.getLogger(HomeContentTypeFilter.class);
    
    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig arg0) throws ServletException {
        // do nothing in this overridden method
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                                FilterChain filterChain) throws IOException, ServletException {

        // pass on if not a HTTP request
        if (!(servletRequest instanceof HttpServletRequest)){
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }
        
        HttpServletRequest httpRequest = (HttpServletRequest)servletRequest;
        
        logger.debug("httpRequest.getRequestURI() = " + httpRequest.getRequestURI());
        logger.debug("httpRequest.getServletPath() = " + httpRequest.getServletPath());
        
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // do nothing in this overridden method
    }
}
