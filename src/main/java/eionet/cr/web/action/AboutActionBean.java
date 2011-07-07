package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;

@UrlBinding("/about.action")
public class AboutActionBean extends AbstractActionBean {

    // private long triplesCount;

    /**
     *
     * @return
     * @throws DAOException
     *             TODO
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        // triplesCount = DAOFactory.get().getDao(HelperDAO.class).getTriplesCount();
        return new ForwardResolution("/pages/about.jsp");

    }

    // public long getTriplesCount() {
    // return triplesCount;
    // }

}
