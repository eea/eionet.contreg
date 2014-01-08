package eionet.cr.web.action.mock;

import eionet.cr.web.action.SPARQLEndpointActionBean;

/**
 *
 * Class to override SPARQLEndpointActionBean methods for testing purposes.
 *
 * @author Jaak Kapten
 */
public class SPARQLEndpointActionBeanMock extends SPARQLEndpointActionBean {

    /**
     * Overrides the admin privileges check to return always true.
     *
     * @return always true.
     */
    @Override
    public boolean isAdminPrivilege() {
        return true;
    }

}
