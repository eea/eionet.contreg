package eionet.cr.web.action.admin;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.web.action.AbstractActionBean;

/**
 * Action bean that lists the top N most urgent harvest sources. It differs from the
 * {@link HarvestSourceDAO#getNextScheduledSources(int)} functionality in that it also lists sources where the harvest urgency score
 * is less than 1.
 *
 * @author Jaak Kapten
 * @author Jaanus Heinlaid
 */

@UrlBinding("/admin/nhus")
public class MostUrgentHarvestSourcesActionBean extends AbstractActionBean {

    /** */
    private static final String JSP = "/pages/admin/mostUrgentSources.jsp";

    /** */
    private int limit = 20;
    private boolean adminLoggedIn = false;
    private int noOfSourcesAboveUrgencyThreshold;

    /** */
    private List<HarvestSourceDTO> sources;

    /** */
    private double urgencyThreshold = 1d;

    /**
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (getUser() != null && getUser().isAdministrator()) {

            setAdminLoggedIn(true);
            HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
            sources = dao.getMostUrgentHarvestSources(limit);
            noOfSourcesAboveUrgencyThreshold = dao.getNumberOfSourcesAboveUrgencyThreshold(urgencyThreshold);
        } else {
            setAdminLoggedIn(false);
        }

        return new ForwardResolution(JSP);
    }

    /**
     * @return
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param urgencyScoreLimit
     */
    public void setLimit(int urgencyScoreLimit) {
        this.limit = urgencyScoreLimit;
    }

    /**
     * @return
     */
    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    /**
     * @param adminLoggedIn
     */
    public void setAdminLoggedIn(boolean adminLoggedIn) {
        this.adminLoggedIn = adminLoggedIn;
    }

    /**
     * @return the nrOfUrgentSources
     */
    public int getNoOfSourcesAboveUrgencyThreshold() {
        return noOfSourcesAboveUrgencyThreshold;
    }

    /**
     * @return the sources
     */
    public List<HarvestSourceDTO> getSources() {
        return sources;
    }

    /**
     * @return the urgencyThreshold
     */
    public double getUrgencyThreshold() {
        return urgencyThreshold;
    }
}
