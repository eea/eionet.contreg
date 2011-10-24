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

import nl.bitwalker.useragentutils.Browser;
import nl.bitwalker.useragentutils.BrowserType;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRException;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.web.util.StripesExceptionHandler;
import eionet.cr.web.util.TriplesToOutputStream;

/**
 *
 * @author Jaanus Heinlaid
 */
public class TabularDataServlet extends HttpServlet {

    /** */
    private static final Logger LOGGER = Logger.getLogger(TabularDataServlet.class);

    /** */
    private Boolean isWebBrowser;

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

        String fileUri = request.getParameter("fileUri");
        if (StringUtils.isBlank(fileUri)) {
            handleFileNotFound("No file URI supplied in the request!", request, response);
            return;
        }
        String idInFile = request.getParameter("idInFile");

        ServletOutputStream outputStream = null;
        try {
            if (isTabularDataFile(fileUri)) {

                HelperDAO dao = DAOFactory.get().getDao(HelperDAO.class);
                List<SubjectDTO> triplesInFile = null;
                if (StringUtils.isBlank(idInFile)) {
                    triplesInFile = dao.getSPOsInSource(fileUri);
                } else {
                    triplesInFile = dao.getSPOsInSubject(fileUri + "/" + idInFile);
                }

                outputStream = response.getOutputStream();
                if (isContentTypeAccepted(request, "application/rdf+xml")) {

                    response.setContentType("application/rdf+xml;charset=utf-8");
                    TriplesToOutputStream.triplesToRdf(outputStream, fileUri, triplesInFile);
                } else if (!StringUtils.isBlank(idInFile)) {
                    TriplesToOutputStream.triplesToHtml(outputStream, fileUri + "/" + idInFile, triplesInFile);
                } else {
                    // TODO probably a more detailed error message needed here
                    handleFileNotFound("Found no content!", request, response);
                    return;
                }
            } else {
                handleFileNotFound("No tabular data file by this URI found!", request, response);
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
     * @param request
     * @param contentType
     * @return
     */
    private static boolean isContentTypeAccepted(HttpServletRequest request, String contentType) {

        String acceptHeader = request.getHeader("Accept");
        return ArrayUtils.contains(acceptHeader.split(","), contentType);
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

        if (isWebBrowser(request)) {
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

        if (isWebBrowser(request)) {
            request.setAttribute(StripesExceptionHandler.EXCEPTION_ATTR, exception);
            request.getRequestDispatcher(StripesExceptionHandler.ERROR_PAGE).forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * @param request
     * @return
     */
    private boolean isWebBrowser(HttpServletRequest request) {

        // Lazy-loading.
        if (isWebBrowser == null) {

            isWebBrowser = false;
            String userAgentString = request.getHeader("User-Agent");
            if (userAgentString != null && userAgentString.trim().length() > 0) {

                Browser browser = Browser.parseUserAgentString(userAgentString);
                if (browser != null) {

                    BrowserType browserType = browser.getBrowserType();
                    if (browserType != null) {

                        if (browserType.equals(BrowserType.WEB_BROWSER) || browserType.equals(BrowserType.MOBILE_BROWSER)) {
                            isWebBrowser = true;
                        }
                    }
                }
            }
        }

        return isWebBrowser;
    }

    /**
     *
     * @param fileUri
     * @return
     * @throws DAOException
     */
    public static boolean isTabularDataFile(String fileUri) throws DAOException {

        String mediaType =
                DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceMetadata(fileUri, Predicates.CR_MEDIA_TYPE);
        return mediaType != null && (mediaType.equals("csv") || mediaType.equals("tsv"));
    }

    /**
     *
     * @param fileUri
     * @param idInFile
     * @param request
     * @return
     * @throws DAOException
     */
    public static boolean willHandle(String fileUri, String idInFile, HttpServletRequest request) throws DAOException {

        return isTabularDataFile(fileUri)
                && (isContentTypeAccepted(request, "application/rdf+xml") || !StringUtils.isBlank(idInFile));
    }
}
