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

import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author altnyris
 *
 */
public class StripesExceptionHandler implements ExceptionHandler {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(StripesExceptionHandler.class);

    /** */
    public static final String ERROR_PAGE = "/pages/common/error.jsp";
    public static final String EXCEPTION_ATTR = "exception";

    /*
     * (non-Javadoc)
     *
     * @see net.sourceforge.stripes.config.ConfigurableComponent#init(net.sourceforge.stripes.config.Configuration)
     */
    @Override
    public void init(Configuration configuration) throws Exception {
        // No init actions.
    }

    /**
     * An implementation of {@link ExceptionHandler#handle(Throwable, HttpServletRequest, HttpServletResponse)} whose purpose is
     * to forward the request to a user-friendly error page that displays the unexpected exception that got us here.
     */
    @Override
    public void handle(Throwable t, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // If response already committed, then we can neither forward nor redirect anywhere, so we might just as well leave.
        if (response.isCommitted()) {
            return;
        }

        // Showing root cause only should be enough.
        Throwable rootCause = ExceptionUtils.getRootCause(t);
        if (rootCause == null) {
            rootCause = t;
        }

        // Log the exception with Log4j
        LOGGER.error(rootCause.getMessage(), rootCause);

        // Forward the request to the error page.
        request.setAttribute(EXCEPTION_ATTR, rootCause);
        request.getRequestDispatcher(ERROR_PAGE).forward(request, response);
    }
}
