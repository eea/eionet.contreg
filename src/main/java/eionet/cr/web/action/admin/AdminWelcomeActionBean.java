package eionet.cr.web.action.admin;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.web.action.AbstractActionBean;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/admin")
public class AdminWelcomeActionBean extends AbstractActionBean {

    private boolean adminLoggedIn = false;
    private String userLdapRole = "";
    private boolean crAdmin = false;
    private boolean sdsAdmin = false;

    @DefaultHandler
    public Resolution view() throws DAOException {
        if (getUser() != null) {
            if (getUser().isAdministrator() || getUser().isCrAdmin() || getUser().isSdsAdmin()) {
                adminLoggedIn = true;
                if (getUser().isCrAdmin()) {
                    crAdmin = true;
                    userLdapRole = "extranet-cr-admin";
                } else if (getUser().isSdsAdmin()) {
                    userLdapRole = "extranet-sds-admin";
                    sdsAdmin = true;
                }
            } else {
                adminLoggedIn = false;
            }
        } else {
            adminLoggedIn = false;
        }
        return new ForwardResolution("/pages/admin/adminWelcome.jsp");
    }

    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    public String getUserLdapRole() {
        return userLdapRole;
    }

    public boolean isCrAdmin() {
        return crAdmin;
    }

    public boolean isSdsAdmin() {
        return sdsAdmin;
    }
}
