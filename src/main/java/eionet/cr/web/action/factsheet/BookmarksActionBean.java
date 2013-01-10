/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.web.action.factsheet;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Bookmarks tab on factsheet page.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/bookmarks.action")
public class BookmarksActionBean extends AbstractActionBean {

    /** The current object URI. */
    private String uri;

    /** Factsheet page tabs. */
    private List<TabElement> tabs;

    /** Selected bookmarks. */
    private List<String> selectedBookmarks;

    /** Bookmarks to display in the table. */
    private List<UserBookmarkDTO> bookmarks;

    /**
     * View action.
     *
     * @return
     * @throws DAOException if DAO call fails
     */
    @DefaultHandler
    public Resolution view() throws DAOException {
        initTabs();
        bookmarks = DAOFactory.get().getDao(HelperDAO.class).getUserBookmarks(uri);

        return new ForwardResolution("/pages/factsheet/bookmarks.jsp");
    }

    /**
     * Delete action.
     *
     * @return
     * @throws DAOException if DAO call fails
     */
    public Resolution delete() throws DAOException {
        if (!isDeletePermission()) {
            return new RedirectResolution(BookmarksActionBean.class).addParameter("uri", uri);
        }
        if (selectedBookmarks == null || selectedBookmarks.isEmpty()) {
            addCautionMessage("No bookmarks selected for deletion.");
            return new RedirectResolution(BookmarksActionBean.class).addParameter("uri", uri);
        }

        HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
        for (String bookmark : selectedBookmarks) {
            if (isProjectBookmarks()) {
                helperDAO.deleteProjectBookmark(bookmark);
            } else {
                helperDAO.deleteUserBookmark(getUser(), bookmark);
            }
        }
        addSystemMessage("Selected bookmarks were deleted from the bookmark list.");
        return new RedirectResolution(BookmarksActionBean.class).addParameter("uri", uri);
    }

    /**
     * Initializes tabs.
     *
     * @throws DAOException if DAO call fails
     */
    private void initTabs() throws DAOException {
        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
            SubjectDTO subject = helperDAO.getFactsheet(uri, null, null);

            FactsheetTabMenuHelper helper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
            tabs = helper.getTabs(FactsheetTabMenuHelper.TabTitle.BOOKMARKS);
        }
    }

    /**
     * Checks if the bookmarks reside in a user home folder.
     * @return true if the folder is in users home
     */
    public boolean isUsersBookmarks() {
        if (isUserLoggedIn()) {
            String homeUri = CRUser.homeUri(getUserName());
            if (uri.startsWith(homeUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns user or project name of the bookmarks file.
     * @return owner name
     */
    public String getOwnerName() {
        // if project bookmarks extract project name:
        if (isProjectBookmarks()) {
            return FolderUtil.extractPathInSpecialFolder(uri, "project");
        } else {
            return FolderUtil.extractUserName(uri);
        }
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @param tabs
     *            the tabs to set
     */
    public void setTabs(List<TabElement> tabs) {
        this.tabs = tabs;
    }

    /**
     * @return the selectedBookmarks
     */
    public List<String> getSelectedBookmarks() {
        return selectedBookmarks;
    }

    /**
     * @param selectedBookmarks
     *            the selectedBookmarks to set
     */
    public void setSelectedBookmarks(List<String> selectedBookmarks) {
        this.selectedBookmarks = selectedBookmarks;
    }

    /**
     * @return the bookmarks
     */
    public List<UserBookmarkDTO> getBookmarks() {
        return bookmarks;
    }

    /**
     * @param bookmarks
     *            the bookmarks to set
     */
    public void setBookmarks(List<UserBookmarkDTO> bookmarks) {
        this.bookmarks = bookmarks;
    }

    /**
     * Check from ACL if user has permission to delete bookmarks.
     *
     * @return boolean
     */
    public boolean isDeletePermission() {
        String aclPath = FolderUtil.extractAclPath(uri);
        return CRUser.hasPermission(aclPath, getUser(), "d", false);
    }

    /**
     * True if project bookmarks file.
     * @return boolean
     */
    public boolean isProjectBookmarks() {
        return FolderUtil.isProjectFolder(uri);
    }

    /**
     * Returns selected projet name where the user wants to add the bookmark.
     * @return project dropdown value
     */
    public String getProjectName() {
        if (isProjectBookmarks()) {
            return StringUtils.substringBeforeLast(FolderUtil.extractPathInFolder(uri), "/");
        }

        return "";
    }
}
