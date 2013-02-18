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
import eionet.cr.dto.EndpointHarvestQueryDTO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * Action bean for operations with a SPARQL endpoint's harvest queries.
 *
 * @author jaanus
 */
@UrlBinding("/admin/endpointQuery.action")
public class EndpointQueryActionBean extends AbstractActionBean {

    /** */
    private static final String QUERY_JSP = "/pages/admin/endpointquery/query.jsp";

    /** */
    private EndpointHarvestQueryDTO query;

    /** */
    private Collection<Statement> testResult;

    /**
     * The bean's default handler event. Basically handles all GET requests to this bean.
     *
     * @return The resolution.
     * @throws DAOException If any sort of data access error happens.
     */
    @DefaultHandler
    public Resolution defaultHandler() throws DAOException {

        if (query != null && query.getId() > 0) {
            query = DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).fetchById(query.getId());
        }

        return new ForwardResolution(QUERY_JSP);
    }

    /**
     * Handles the "save" event.
     *
     * @return The resolution.
     * @throws DAOException If any sort of data access error happens.
     */
    public Resolution save() throws DAOException {

        doSave();
        return new RedirectResolution(getClass()).addParameter("query.id", query.getId());
    }

    /**
     * Handles the "save & close" event.
     *
     * @return The resolution.
     * @throws DAOException If any sort of data access error happens.
     */
    public Resolution saveAndClose() throws DAOException {

        doSave();

        RedirectResolution resolution = new RedirectResolution(EndpointQueriesActionBean.class);
        if (query != null && StringUtils.isNotBlank(query.getEndpointUrl())) {
            resolution.addParameter("endpointUrl", query.getEndpointUrl());
        }
        return resolution;
    }

    /**
     * A utility method that does the save, regardless of whether the event was "save" or "save & close".
     * @throws DAOException If any sort of data access error happens.
     */
    private void doSave() throws DAOException {

        if (query != null && query.getId() > 0) {
            DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).update(query);
        }
        else{
            int queryId = DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).create(query);
            if (query == null) {
                query = new EndpointHarvestQueryDTO();
            }
            query.setId(queryId);
        }

        addSystemMessage("Query successfully saved!");
    }

    /**
     * Handles the "test" event.
     *
     * @return The resolution.
     * @throws DAOException If any sort of data access error happens.
     */
    public Resolution test() throws DAOException {

        String testQuery = query.getQuery();
        String testEndpoint = query.getEndpointUrl();

        EndpointHarvestQueryDAO dao = DAOFactory.get().getDao(EndpointHarvestQueryDAO.class);
        if (query.getId() > 0) {
            query = dao.fetchById(query.getId());
        }
        testResult = dao.testConstructQuery(testQuery, testEndpoint);

        return new ForwardResolution(QUERY_JSP);
    }

    /**
     * Handles the "cancel" event.
     *
     * @return The resolution.
     * @throws DAOException If any sort of data access error happens.
     */
    public Resolution cancel() throws DAOException {

        RedirectResolution resolution = new RedirectResolution(EndpointQueriesActionBean.class);
        if (query != null && StringUtils.isNotBlank(query.getEndpointUrl())) {
            resolution.addParameter("endpointUrl", query.getEndpointUrl());
        }
        return resolution;
    }

    /**
     *
     * @return
     */
    public Class getEndpointQueriesActionBeanClass() {
        return EndpointQueriesActionBean.class;
    }

    /**
     * @return the query
     */
    public EndpointHarvestQueryDTO getQuery() {
        return query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(EndpointHarvestQueryDTO query) {
        this.query = query;
    }

    /**
     * @return the testResult
     */
    public Collection<Statement> getTestResult() {
        return testResult;
    }

    /**
     *
     * @return
     */
    public Class getEndpointResourceActionBeanClass() {
        return EndpointResourceActionBean.class;
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
