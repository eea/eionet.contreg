package eionet.cr.web.action.admin;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.web.action.AbstractActionBean;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/admin")
public class AdminWelcomeActionBean extends AbstractActionBean {

    private boolean adminLoggedIn = false;
    private String userLdapRole = "";

    @DefaultHandler
    public Resolution view() throws DAOException {
        if (getUser() != null) {
            if (getUser().isAdministrator() || getUser().isCrAdmin() || getUser().isSdsAdmin()) {
                adminLoggedIn = true;
                if (getUser().isCrAdmin() || getUser().isSdsAdmin()) {
                    userLdapRole = GeneralConfig.getProperty( "config.admin-group");
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
}
