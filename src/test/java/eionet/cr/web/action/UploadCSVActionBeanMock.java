package eionet.cr.web.action;

import eionet.cr.web.security.CRUser;

/**
 * A mock of {@link UploadCSVActionBean} in order to override some of its behavior for tests.
 *
 * @author Jaanus
 */
public class UploadCSVActionBeanMock extends UploadCSVActionBean {

    /** A dummy user for this mock. */
    private CRUser user = new CRUser("somebody");

    /**
     * Default constructor.
     */
    public UploadCSVActionBeanMock() {
        super();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.UploadCSVActionBean#uploadAllowed()
     */
    @Override
    protected boolean uploadAllowed() {
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.web.action.AbstractActionBean#getUser()
     */
    @Override
    public CRUser getUser() {
        return user;
    }
}
