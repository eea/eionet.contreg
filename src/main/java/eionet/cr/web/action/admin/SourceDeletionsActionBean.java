package eionet.cr.web.action.admin;

import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.collections.CollectionUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SourceDeletionsDAO;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.web.action.DisplaytagSearchActionBean;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.util.CustomPaginatedList;

/**
 * An action bean for monitoring the batch-deletion of harvest sources.
 *
 * @author Jaanus
 */
@UrlBinding("/admin/sourceDeletions.action")
public class SourceDeletionsActionBean extends DisplaytagSearchActionBean {

    /** Default size of the deletion queue result list page. */
    public static final int RESULT_LIST_PAGE_SIZE = 20;

    /** The default JSP page to return to. */
    private static final String DEFAULT_JSP = "/pages/admin/sourceDeletions.jsp";

    /** The queue of source deletion requests. As pairs where left is URL and right is request timestamp. */
    private List<Pair<String, Date>> deletionQueue;

    /** Total number of sources in deletion queue matching the filter. Blank filter means all. */
    private int totalMatchCount;

    /** URLs whose deletion the user has cancelled via POST request. */
    private List<String> cancelUrls;

    /** Sub-string by which the sources in deletion queue will be filtered when queried. */
    private String filter;

    /**
     * Default event handler.
     *
     * @return The resolution to return to.
     * @throws DAOException If database access error.
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        PagingRequest pagingRequest = PagingRequest.create(getPage(), RESULT_LIST_PAGE_SIZE);
        SourceDeletionsDAO dao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
        Pair<Integer, List<Pair<String, Date>>> resultPair = dao.getDeletionQueue(filter, pagingRequest);
        deletionQueue = resultPair.getRight();
        totalMatchCount = resultPair.getLeft();
        return new ForwardResolution(DEFAULT_JSP);
    }

    /**
     * Event handler for canceling the deletion of selected URLs.
     *
     * @return The resolution to return to.
     * @throws DAOException If database access error.
     */
    public Resolution cancel() throws DAOException {

        if (CollectionUtils.isEmpty(cancelUrls)) {
            addCautionMessage("You have no URLs specified!");
        } else {
            SourceDeletionsDAO dao = DAOFactory.get().getDao(SourceDeletionsDAO.class);
            int cancelledCount = dao.unmarkForDeletion(cancelUrls);
            addSystemMessage("Successfully cancelled the deletion of " + cancelledCount + " sources!");
        }

        return new RedirectResolution(this.getClass());
    }

    /**
     * Getter for the deletion queue.
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

    /**
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(String filter) {
        this.filter = filter;
    }

    /**
     * @return the totalMatchCount
     */
    public int getTotalMatchCount() {
        return totalMatchCount;
    }

    /**
     * Dynamic getter for {@link #RESULT_LIST_PAGE_SIZE}.
     *
     * @return The value.
     */
    public int getResultListPageSize() {
        return RESULT_LIST_PAGE_SIZE;
    }

    /**
     * Constructs a {@link CustomPaginatedList} from the deletion queue and the match count.
     *
     * @return The constructed instance.
     */
    public CustomPaginatedList<Pair<String, Date>> getDeletionQueuePaginated() {

        if (deletionQueue == null) {
            return new CustomPaginatedList<Pair<String, Date>>();
        } else {
            return new CustomPaginatedList<Pair<String, Date>>(this, totalMatchCount, deletionQueue, RESULT_LIST_PAGE_SIZE);
        }
    }
}
