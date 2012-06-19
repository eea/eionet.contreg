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

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.CRException;
import eionet.cr.util.Util;
import eionet.cr.web.util.RDFGenerator;
import eionet.cr.web.util.StripesExceptionHandler;

/**
 * Action bean that exports the triples of a given graph as RDF/XML into servlet output stream.
 *
 * @author Jaanus Heinlaid
 */
@UrlBinding("/exportTriples.action")
public class ExportTriplesActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ExportTriplesActionBean.class);

    /** */
    private String uri;

    @DefaultHandler
    public Resolution defaultHandler() {

        if (StringUtils.isBlank(uri)) {

            if (Util.isWebBrowser(getContext().getRequest())) {
                getContext().getRequest().setAttribute(StripesExceptionHandler.EXCEPTION_ATTR,
                        new CRException("Graph URI not specified in the request!"));
                return new ForwardResolution(StripesExceptionHandler.ERROR_PAGE);
            } else {
                return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        return (new StreamingResolution("application/rdf+xml") {

            public void stream(HttpServletResponse response) throws Exception {
                RDFGenerator.generate(uri, response.getOutputStream());
            }
        });
    }

    /**
     * Action event that exports properties of the given uri.
     *
     * @return rdf result.
     */
    public Resolution exportProperties() {

        if (StringUtils.isBlank(uri)) {

            if (Util.isWebBrowser(getContext().getRequest())) {
                getContext().getRequest().setAttribute(StripesExceptionHandler.EXCEPTION_ATTR,
                        new CRException("Graph URI not specified in the request!"));
                return new ForwardResolution(StripesExceptionHandler.ERROR_PAGE);
            } else {
                return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND);
            }
        }

        return (new StreamingResolution("application/rdf+xml") {

            public void stream(HttpServletResponse response) throws Exception {
                RDFGenerator.generateProperties(uri, response.getOutputStream());
            }
        });
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }
}
