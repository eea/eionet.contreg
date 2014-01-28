package eionet.cr.web.action.admin;

import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;
import eionet.cr.dao.DAOException;
import eionet.cr.util.Pair;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

/**
 * An action bean for monitoring the batch-deletion of harvest sources.
 *
 * @author Jaanus
 */
@UrlBinding("/admin/sourceDeletions.action")
public class SourceDeletionsActionBean extends AbstractActionBean {

    /** The default JSP page to return to. */
    private static final String DEFAULT_JSP = "/pages/admin/sourceDeletions.jsp";

    /** The queue of source deletion requests. As pairs where left is URL and right is request timestamp. */
    private List<Pair<String, Date>> deletionQueue;

    /** URLs whose deletion the user has cancelled via POST request. */
    private List<String> cancelUrls;

    /**
     * Default event handler.
     *
     * @return The resolution to return to.
     * @throws DAOException If database access error.
     */
    @DefaultHandler
    public Resolution view() throws DAOException {
        return new ForwardResolution(DEFAULT_JSP);
    }

    /**
     * Event handler for canceling the deletion of selected URLs.
     *
     * @return The resolution to return to.
     * @throws DAOException If database access error.
     */
    public Resolution cancel() throws DAOException {
        return new ForwardResolution(DEFAULT_JSP);
    }

    /**
     * Lazy getter for the deleiton queue.
     *
     * @return the deletionQueue
     */
    public List<Pair<String, Date>> getDeletionQueue() {
        return deletionQueue;
    }

    /**
     * Validates the the user is authorised for any operations on this action bean. If user not authorised, redirects to the
     * {@link AdminWelcomeActionBean} which displays a proper error message. Will be run on any events, since no specific events
     * specified in the {@link ValidationMethod} annotation.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * @param cancelUrls the cancelUrls to set
     */
    public void setCancelUrls(List<String> cancelUrls) {
        this.cancelUrls = cancelUrls;
    }

    /**
     * Returns the class of {@link FactsheetActionBean} for refactoring-safe access from JSP.
     *
     * @return The class.
     */
    public Class<FactsheetActionBean> getSourceFactsheetBeanClass() {
        return FactsheetActionBean.class;
    }
}
