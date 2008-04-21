package eionet.cr.web.util;

/**
 * 
 * @author heinljab
 *
 */
public interface ICRWebConstants {
	
	/** CR user session attribute name. */
	public static final String USER_SESSION_ATTR = "crUser";
	
	/** Specifies login action constant value. */
	public static final String LOGIN_ACTION = "/login.action";
	
	public static final String MAIN_PAGE_ACTION = "/";
	
	/** Specifies login event name. */
	public static final String LOGIN_EVENT = "login";
	
	/** Specifies logout event name. */
	public static final String LOGOUT_EVENT = "logout";
	
	/** Specifies presentation value for not logged in user. */
	public static final String ANONYMOUS_USER_NAME = "anononymous";
	
	/** Specifies session attribute name where last action URL is kept. */
	String LAST_ACTION_URL_SESSION_ATTR = "ActionEventInterceptor#lastActionUrl";
	
	/** Specifies after login event name. */
	String AFTER_LOGIN_EVENT = "afterLogin";
}
