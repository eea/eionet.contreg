package eionet.cr.web.action.home;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.context.CRActionBeanContext;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public abstract class AbstractHomeActionBean extends AbstractActionBean {

	
	protected static final String TYPE_BOOKMARK = "bookmark";
	protected static final String TYPE_HISTORY = "history";
	protected static final String TYPE_WORKSPACE = "workspace";
	protected static final String TYPE_REGISTRATIONS = "registrations";
	protected static final String TYPE_QARAPORTS = "qaraports";
	
	protected static final String SHOWPUBLIC_YES = "Y"; 
	protected static final String SHOWPUBLIC_NO = "N";
	
	/** */
	protected static List<Map<String, String>> tabs;
	private static final Map<String,List<SearchResultColumn>> typesColumns;
	
	// Note: attemptedUserName might be used in some situations where showPublic = true and content of that user is visible to everyone.
	private String attemptedUserName;
	
	private String section;	
	private String baseHomeUrl;
	
	private boolean userAuthorized;
	private String authenticationMessage;
	/** */
	private String tabType;
	
	private String authenticatedUserName;
	
	private boolean showPublic;
	
	static {
		tabs = new ArrayList<Map<String,String>>();
		
		Map<String,String> tabType;

		tabType = new HashMap<String,String>();
		tabType.put("title", "Workspace");
		tabType.put("tabType", "workspace");
		tabType.put("showPublic", SHOWPUBLIC_NO);
		tabs.add(tabType);
		
		tabType = new HashMap<String,String>();
		tabType.put("title", "Bookmarks");
		tabType.put("tabType", "bookmark");
		tabType.put("showPublic", SHOWPUBLIC_NO);
		tabs.add(tabType);

		tabType = new HashMap<String,String>();
		tabType.put("title", "Registrations");
		tabType.put("tabType", "registrations");
		tabType.put("showPublic", SHOWPUBLIC_YES);
		tabs.add(tabType);

		tabType = new HashMap<String,String>();
		tabType.put("title", "History");
		tabType.put("tabType", "history");
		tabType.put("showPublic", SHOWPUBLIC_NO);
		tabs.add(tabType);

		tabType = new HashMap<String,String>();
		tabType.put("title", "QA Raports");
		tabType.put("tabType", "qaraports");
		tabType.put("showPublic", SHOWPUBLIC_NO);
		tabs.add(tabType);
		
		typesColumns = new HashMap<String,List<SearchResultColumn>>();
		
		/* columns for bookmarks */
		List<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("Bookmark", false, Predicates.RDFS_LABEL));
		typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);
		
		/* columns for registrations */
		list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("Subject", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("Predicate", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("Object", false, Predicates.RDFS_LABEL));
		typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);

		/* columns for history */
		list = new ArrayList<SearchResultColumn>();
		list.add(new SubjectPredicateColumn("URL", false, Predicates.RDFS_LABEL));
		list.add(new SubjectPredicateColumn("Last Update", false, Predicates.RDFS_LABEL));
		typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);
		
	}
	
	public AbstractHomeActionBean(){
		setHomeContext(true);
		//setUrlParams();
	}
	
	protected void setEnvironmentParams(CRActionBeanContext context, String activeSection) {
		attemptedUserName = context.getRequest().getParameter("username");
		section = activeSection;
		setDefaultSection();
		if (this.isUserLoggedIn()){
			if (attemptedUserName.toLowerCase().equals(this.getUser().getUserName().toLowerCase())){
				userAuthorized = true;
				authenticatedUserName = attemptedUserName;
			} else {
				userAuthorized = false;
				authenticationMessage = "Logged in username and home url don't match.";
			}
		} else {
			userAuthorized = false;
			authenticationMessage = "User must be logged in to access his home";
		}
		
		baseHomeUrl = context.getRequest().getRequestURI().split(attemptedUserName)[0];
	}

	private void setDefaultSection(){
		if (section == null ||
			(!section.equals(TYPE_BOOKMARK)&&
			!section.equals(TYPE_WORKSPACE)&&
			!section.equals(TYPE_HISTORY)&&
			!section.equals(TYPE_REGISTRATIONS)&&
			!section.equals(TYPE_QARAPORTS))
		){
			section = TYPE_WORKSPACE;
		}
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
	
	public String getAttemptedUserName() {
		return attemptedUserName;
	}

	public void setAttemptedUserName(String attemptedUserName) {
		this.attemptedUserName = attemptedUserName;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
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

	public void setTabType(String tabType) {
		this.tabType = tabType;
	}

	public List<Map<String, String>> getTabs() {
		return tabs;
	}

	public void setTabs(List<Map<String, String>> tabs) {
		AbstractHomeActionBean.tabs = tabs;
	}

	public Map<String, List<SearchResultColumn>> getTypescolumns() {
		return typesColumns;
	}

	public String getBaseHomeUrl() {
		return baseHomeUrl;
	}

	public void setBaseHomeUrl(String baseHomeUrl) {
		this.baseHomeUrl = baseHomeUrl;
	}

	public String getAuthenticatedUserName() {
		return authenticatedUserName;
	}

	public void setAuthenticatedUserName(String authenticatedUserName) {
		this.authenticatedUserName = authenticatedUserName;
	}

	public boolean isShowPublic() {
		return showPublic;
	}

	public void setShowPublic(boolean showPublic) {
		this.showPublic = showPublic;
	}

	public String getShowpublicYes() {
		return SHOWPUBLIC_YES;
	}

	public String getShowpublicNo() {
		return SHOWPUBLIC_NO;
	}

}
