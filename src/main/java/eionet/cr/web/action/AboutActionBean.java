package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;

/**
 * About page controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/about.action")
public class AboutActionBean extends AbstractActionBean {

    /**
     * Handles view page.
     *
     * @return
     */
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution("/pages/about.jsp");

    }

    /**
     * Returns triple count.
     *
     * @return
     * @throws DAOException
     */
    public long getTriplesCount() throws DAOException {
        return DAOFactory.get().getDao(HarvestSourceDAO.class).getTotalStatementsCount();
    }

}
