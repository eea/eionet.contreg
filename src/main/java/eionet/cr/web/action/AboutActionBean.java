package eionet.cr.web.action;

import java.text.NumberFormat;
import java.util.Locale;

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
    public String getTriplesCount() throws DAOException {
        int totalStatements = DAOFactory.get().getDao(HarvestSourceDAO.class).getTotalStatementsCount();
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.UK);
        return nf.format(totalStatements);
    }

}
