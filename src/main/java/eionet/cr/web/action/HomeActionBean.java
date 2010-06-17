package eionet.cr.web.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.web.security.BadUserHomeUrlException;
import eionet.cr.web.util.UserHomeUrlExtractor;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home")
public class HomeActionBean extends AbstractActionBean{

	private String attemptedUserName="NoValue";
	
	private static final String TYPE_BOOKMARK = "bookmark";
	private static final String TYPE_HISTORY = "history";
	private static final String TYPE_WORKSPACE = "workspace";
	
	/** */
	private String tabType;
	
	private String baseHomeUrl;
	private String section;
	
	private List<UserBookmarkDTO> bookmarks;
	private List<UserHistoryDTO> history;
	
	private boolean userAuthorized;
	private String authenticationMessage;
	
	/** */
	private static List<Map<String, String>> tabs;
	private static final Map<String,List<SearchResultColumn>> typesColumns;
	
	List<String> selectedBookmarks;
	
	static {
		tabs = new ArrayList<Map<String,String>>();
		
		Map<String,String> tabType;

		tabType = new HashMap<String,String>();
		tabType.put("title", "Workspace");
		tabType.put("tabType", "workspace");
		tabs.add(tabType);
		
		tabType = new HashMap<String,String>();
		tabType.put("title", "Bookmarks");
		tabType.put("tabType", "bookmark");
		tabs.add(tabType);

		tabType = new HashMap<String,String>();
		tabType.put("title", "History");
		tabType.put("tabType", "history");
		tabs.add(tabType);

		typesColumns = new HashMap<String,List<SearchResultColumn>>();
		
		/* columns for bookmarks */
		List<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("Bookmark", false, Predicates.RDFS_LABEL));
		typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);
		
		/* columns for history */
		list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("URL", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("Last Update", false, Predicates.RDFS_LABEL));
		typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {

		setUrlParams();
		
		return new ForwardResolution("/pages/home.jsp");
	}
	
	private void setUrlParams() throws DAOException  {
		String requestUrl = this.getContext().getRequest().getRequestURI(); 
		
		try {
			attemptedUserName = UserHomeUrlExtractor.extractUserNameFromHomeUrl(requestUrl);
			section = UserHomeUrlExtractor.extractSectionFromHomeUrl(requestUrl);
			
			if (this.isUserLoggedIn()){
				if (attemptedUserName.toLowerCase().equals(this.getUser().getUserName().toLowerCase())){
					userAuthorized = true;
				} else {
					userAuthorized = false;
					authenticationMessage = "Logged in username and home url don't match.";
				}
			} else {
				userAuthorized = false;
				authenticationMessage = "User must be logged in to access his home";
			}
			
		} catch (BadUserHomeUrlException ex) {
			userAuthorized = false;
			authenticationMessage = "Bad user home url";
		}
		baseHomeUrl = requestUrl.split(attemptedUserName)[0];
		setDefaultSection();
		
		performSectionSpecificActions();
	}
	
	private void performSectionSpecificActions(){
		if (section.equals(TYPE_BOOKMARK)){
			bookmarkActions();
		}
		if (section.equals(TYPE_HISTORY)){
		}
		if (section.equals(TYPE_WORKSPACE)){
		}
	}
	
	private void bookmarkActions(){
		if (this.getContext().getRequest().getParameter("deletebookmarks") != null){
			if (selectedBookmarks != null && !selectedBookmarks.isEmpty()) {
				try {
					for (int i=0; i<selectedBookmarks.size(); i++){
						DAOFactory.get().getDao(HelperDAO.class).deleteUserBookmark(getUser(), selectedBookmarks.get(i));
					}
					addSystemMessage("Selected bookmarks were deleted from your personal bookmark list.");
				} catch (DAOException ex){
					addWarningMessage("Error occured during bookmark deletion.");
				}
			} else {
				addCautionMessage("No bookmarks selected for deletion.");
			}
		}
	}
	
	private void setDefaultSection(){
		if (section == null ||
			!section.equals(TYPE_BOOKMARK)&&
			!section.equals(TYPE_WORKSPACE)&&
			!section.equals(TYPE_HISTORY)
		){
			section = TYPE_WORKSPACE;
		}
	}
	
	/**
	 * @param sourceUrl the sourceUrl to set
	 */
	public void setBookmarkUrl(List<String> bookmarkUrl) {
		selectedBookmarks = bookmarkUrl;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Map<String, String>> getTabs(){
		
		return tabs;
	}
	
	/**
	 * @return the tabType
	 */
	public String getTabType() {
		
		if (tabType == null){
			tabType = TYPE_WORKSPACE;
		}
		
		return tabType;
	}
	
	public List<UserBookmarkDTO> getBookmarks() {
		try {
			bookmarks = DAOFactory.get().getDao(HelperDAO.class).getUserBookmarks(this.getUser());
		} catch (DAOException ex){
			
		}
		return bookmarks;
	}
	
	public List<UserHistoryDTO> getHistory() {
		try {
			history = DAOFactory.get().getDao(HelperDAO.class).getUserHistory(this.getUser());
		} catch (DAOException ex){
			
		}
		return history;
	}

	public void setBookmarks(List<UserBookmarkDTO> bookmarks) {
		this.bookmarks = bookmarks;
	}

	public String getAttemptedUserName() {
		return attemptedUserName;
	}

	public void setAttemptedUserName(String attemptedUserName) {
		this.attemptedUserName = attemptedUserName;
	}

	public String getBaseHomeUrl() {
		return baseHomeUrl;
	}

	public void setBaseHomeUrl(String baseHomeUrl) {
		this.baseHomeUrl = baseHomeUrl;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public boolean isSectionBookmarks() {
		if (section.equals(TYPE_BOOKMARK)){
			return true;
		} else {
			return false;
		}
	}

	public boolean isSectionHistory() {
		if (section.equals(TYPE_HISTORY)){
			return true;
		} else {
			return false;
		}
	}

	public boolean isSectionWorkspace() {
		if (section.equals(TYPE_WORKSPACE)){
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isUserAuthorized() {
		return userAuthorized;
	}

	public void setUserAuthorized(boolean userAuthorized) {
		this.userAuthorized = userAuthorized;
	}

	public String getAuthenticationMessage() {
		return authenticationMessage;
	}

	public void setAuthenticationMessage(String authenticationMessage) {
		this.authenticationMessage = authenticationMessage;
	}



	public void setHistory(List<UserHistoryDTO> history) {
		this.history = history;
	}


}
