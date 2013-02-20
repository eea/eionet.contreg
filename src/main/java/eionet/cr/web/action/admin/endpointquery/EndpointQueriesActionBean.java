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

import java.util.HashSet;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.EndpointHarvestQueryDAO;
import eionet.cr.dto.EndpointHarvestQueryDTO;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.HarvestSourceActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * Action bean for operations with a SPARQL endpoint's harvest queries.
 *
 * @author jaanus
 */
@UrlBinding("/admin/endpointQueries.action")
public class EndpointQueriesActionBean extends AbstractActionBean {

    /** */
    private static final String QUERIES_JSP = "/pages/admin/endpointquery/queries.jsp";

    /** */
    private String endpointUrl;

    /** */
    private List<EndpointHarvestQueryDTO> queries;

    /** */
    private List<String> endpoints;

    /** */
    private List<Integer> selectedIds;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution list() throws DAOException {

        EndpointHarvestQueryDAO dao = DAOFactory.get().getDao(EndpointHarvestQueryDAO.class);
        queries = dao.listByEndpointUrl(endpointUrl);
        endpoints = dao.getEndpoints();
        return new ForwardResolution(QUERIES_JSP);
    }

    /**
     * Handler for the "delete" event.
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {

        if (selectedIds != null && !selectedIds.isEmpty()) {
            DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).delete(selectedIds);
            addSystemMessage("Selected queries successfully deleted!");
        }

        return list();
    }

    /**
     * Handler for the "activateDeactivate" event.
     * @return
     * @throws DAOException
     */
    public Resolution activateDeactivate() throws DAOException {

        if (selectedIds != null && !selectedIds.isEmpty()) {
            DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).activateDeactivate(selectedIds);
            addSystemMessage("Selected queries successfully activated/deactivated!");
        }

        return list();
    }

    /**
     * Handler for the "moveUp" event.
     * @return
     * @throws DAOException
     */
    public Resolution moveUp() throws DAOException {

        DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).move(endpointUrl, new HashSet(selectedIds), -1);
        addSystemMessage("Moving up completed!!");
        return list();
    }

    /**
     * Handler for the "moveDown" event.
     * @return
     * @throws DAOException
     */
    public Resolution moveDown() throws DAOException {

        DAOFactory.get().getDao(EndpointHarvestQueryDAO.class).move(endpointUrl, new HashSet(selectedIds), 1);
        addSystemMessage("Moving down completed!!");
        return list();
    }

    /**
     * @return the endpointUrl
     */
    public String getEndpointUrl() {
        return endpointUrl;
    }

    /**
     * @param endpointUrl the endpointUrl to set
     */
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    /**
     * @return the queries
     */
    public List<EndpointHarvestQueryDTO> getQueries() {
        return queries;
    }

    /**
     * @return the endpoints
     */
    public List<String> getEndpoints() {
        return endpoints;
    }

    /**
     * Gets the endpoint query action bean class.
     *
     * @return the endpoint query action bean class
     */
    public Class getEndpointQueryActionBeanClass() {
        return EndpointQueryActionBean.class;
    }

    /**
     * Gets the harvest source action bean class.
     *
     * @return the harvest source action bean class
     */
    public Class getHarvestSourceActionBeanClass() {
        return HarvestSourceActionBean.class;
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

    /**
     * @param selectedIds the selectedIds to set
     */
    public void setSelectedIds(List<Integer> selectedIds) {
        this.selectedIds = selectedIds;
    }
}
