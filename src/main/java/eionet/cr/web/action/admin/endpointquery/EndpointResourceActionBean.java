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
 *        jaanus
 */

package eionet.cr.web.action.admin.endpointquery;

import java.util.Collection;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.Statement;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.EndpointHarvestQueryDAO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * An action bean for getting the properties of a resource at a remote SPARQL endpoint.
 *
 * @author jaanus
 */
@UrlBinding("/admin/endpointResource.action")
public class EndpointResourceActionBean extends AbstractActionBean {

    /** */
    private static final String QUERY_TEMPLATE = "CONSTRUCT {<URL> ?property ?object} WHERE {<URL> ?property ?object}";

    /** */
    private static final String REMOTE_RESOURCE_JSP = "/pages/admin/endpointquery/remoteResource.jsp";

    /** */
    private String url;
    private String endpoint;

    /** */
    private Collection<Statement> queryResult;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        if (StringUtils.isBlank(url) || StringUtils.isBlank(endpoint)) {
            return new ErrorResolution(400, "Missing URL of resource or endpoint!");
        }
        else{
            String query = StringUtils.replace(QUERY_TEMPLATE, "URL", url);
            queryResult = DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).testConstructQuery(query, endpoint);
            return new ForwardResolution(REMOTE_RESOURCE_JSP);
        }
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url the url to set
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the queryResult
     */
    public Collection<Statement> getQueryResult() {
        return queryResult;
    }

    /**
     * Validates the the user is authorised for any operations on this action bean. If user not authorised, redirects to the
     * {@link AdminWelcomeActionBean} which displays a proper error message. Will be run on any events, since no specific events
     * specified in the {@link ValidationMethod} annotation.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }
}
