package eionet.cr.web.action.home;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;

/**
 * Action bean class for projects.
 *
 * @author kaido
 */
@UrlBinding("/project")
public class ProjectsActionBean extends AbstractActionBean {
    /**
     * Default handler.
     * redirects to view.action
     * @return resolution
     */
    @DefaultHandler
    public Resolution noEvent() {
        return new RedirectResolution("/view.action?uri=" + CRUser.rootProjectUri());
    }

}
