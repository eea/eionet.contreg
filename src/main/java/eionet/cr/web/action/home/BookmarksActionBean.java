package eionet.cr.web.action.home;

import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/bookmark")
public class BookmarksActionBean extends AbstractHomeActionBean {


    private List<UserBookmarkDTO> bookmarks;
    List<String> selectedBookmarks;

    /*
     * (non-Javadoc)
     * @see eionet.cr.web.action.AbstractSearchActionBean#search()
     */
    @DefaultHandler
    public Resolution view() throws DAOException {
        setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_BOOKMARK, true);
        if (isUserAuthorized()) {
            bookmarkActions();
        }
        return new ForwardResolution("/pages/home/bookmark.jsp");
    }

    private void bookmarkActions() {
        if (this.getContext().getRequest().getParameter("deletebookmarks") != null) {
            if (selectedBookmarks != null && !selectedBookmarks.isEmpty()) {
                try {
                    for (int i = 0; i < selectedBookmarks.size(); i++) {
                        DAOFactory.get().getDao(HelperDAO.class).deleteUserBookmark(getUser(), selectedBookmarks.get(i));
                    }
                    addSystemMessage("Selected bookmarks were deleted from your personal bookmark list.");
                } catch (DAOException ex) {
                    addWarningMessage("Error occured during bookmark deletion.");
                }
            } else {
                addCautionMessage("No bookmarks selected for deletion.");
            }
        }
    }

    /**
     * @param sourceUrl the sourceUrl to set
     */
    public void setBookmarkUrl(List<String> bookmarkUrl) {
        selectedBookmarks = bookmarkUrl;
    }

    public List<UserBookmarkDTO> getBookmarks() {
        try {
            if (this.isUserAuthorized() && this.getUser() != null) {
                bookmarks = DAOFactory.get().getDao(HelperDAO.class).getUserBookmarks(this.getUser());
            } else {
                bookmarks = DAOFactory.get().getDao(HelperDAO.class).getUserBookmarks(new CRUser(this.getAttemptedUserName()));
            }
        } catch (DAOException ex) {

        }
        return bookmarks;
    }

    public void setBookmarks(List<UserBookmarkDTO> bookmarks) {
        this.bookmarks = bookmarks;
    }

}
