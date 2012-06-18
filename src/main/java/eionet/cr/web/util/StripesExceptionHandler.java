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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.util;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.ExceptionHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author altnyris
 * 
 */
public class StripesExceptionHandler implements ExceptionHandler {

    /** */
    public static final String ERROR_PAGE = "/pages/error.jsp";
    public static final String EXCEPTION_ATTR = "exception";

    /** */
    private static Log logger = LogFactory.getLog(StripesExceptionHandler.class);

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.stripes.config.ConfigurableComponent#init(net.sourceforge.stripes.config.Configuration)
     */
    public void init(Configuration configuration) throws Exception {
    }

    /*
     * (non-Javadoc)
     * 
     * @see net.sourceforge.stripes.exception.ExceptionHandler#handle(java.lang.Throwable, javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    public void handle(Throwable t, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Throwable newThrowable = (t instanceof ServletException) ? getRootCause((ServletException) t) : t;
        if (newThrowable == null)
            newThrowable = t;

        logger.error(newThrowable.getMessage(), newThrowable);
        request.setAttribute(EXCEPTION_ATTR, newThrowable);
        request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
    }

    /**
     * 
     * @param servletException
     * @return
     */
    private Throwable getRootCause(ServletException servletException) {

        Throwable rootCause = servletException.getRootCause();
        if (rootCause instanceof ServletException)
            return getRootCause((ServletException) rootCause);
        else
            return rootCause;
    }
}
