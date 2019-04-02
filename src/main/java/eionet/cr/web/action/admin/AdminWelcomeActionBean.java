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

    @DefaultHandler
    public Resolution view() throws DAOException {
        if (getUser() != null) {
            if (getUser().isAdministrator()) {
                adminLoggedIn = true;
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

}
