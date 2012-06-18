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

package eionet.cr.web.action;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRException;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Util;
import eionet.cr.web.util.StripesExceptionHandler;
import eionet.cr.web.util.TriplesToOutputStream;

/**
 * 
 * @author Jaanus Heinlaid
 */
public class TabularDataServlet extends HttpServlet {

    /** */
    public static final String URL_PATTERN = "/tabularData";

    /** */
    private static final Logger LOGGER = Logger.getLogger(TabularDataServlet.class);

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        processRequest(request, response);
    }

    /**
     * 
     * @param request
     * @param response
     * @throws IOException
     * @throws ServletException
     */
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String subjectUri = request.getParameter("uri");
        if (StringUtils.isBlank(subjectUri)) {
            handleFileNotFound("Request parameter \"uri\" is missing or blank!", request, response);
            return;
        }

        ServletOutputStream outputStream = null;
        try {
            List<SubjectDTO> subjectTriples = DAOFactory.get().getDao(HelperDAO.class).getSPOsInSubject(subjectUri);
            outputStream = response.getOutputStream();
            if (isRdfXmlPreferred(request)) {
                TriplesToOutputStream.triplesToRdf(outputStream, subjectUri, subjectTriples);
            } else {
                TriplesToOutputStream.triplesToHtml(outputStream, subjectUri, subjectTriples);
            }
        } catch (DAOException e) {
            handleException(e, request, response);
            return;
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     * 
     * @param message
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void handleFileNotFound(String message, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.info(message);

        if (Util.isWebBrowser(request)) {
            request.setAttribute(StripesExceptionHandler.EXCEPTION_ATTR, new CRException(message));
            request.getRequestDispatcher(StripesExceptionHandler.ERROR_PAGE).forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * 
     * @param exception
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    private void handleException(Exception exception, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        LOGGER.error(exception);

        if (Util.isWebBrowser(request)) {
            request.setAttribute(StripesExceptionHandler.EXCEPTION_ATTR, exception);
            request.getRequestDispatcher(StripesExceptionHandler.ERROR_PAGE).forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 
     * @param httpRequest
     * @return
     */
    private boolean isRdfXmlPreferred(HttpServletRequest httpRequest) {

        String acceptHeader = httpRequest.getHeader("Accept");
        return acceptHeader != null && acceptHeader.trim().toLowerCase().startsWith("application/rdf+xml");
    }
}
