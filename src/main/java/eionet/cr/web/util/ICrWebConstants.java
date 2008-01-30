package eionet.cr.web.util;

/**
 * Interface that holds CR web part related constants.
 * 
 * @author gerasvad
 *
 */
public interface ICrWebConstants {
	/** CR user session attribute name. */
	public static final String USER_SESSION_ATTR = "crUser";
	
	/** Specifies login action constant value. */
	public static final String LOGIN_ACTION = "/login.action";
	
	public static final String MAIN_PAGE_ACTION = "/main.action";
	
	/** Specifies login event name. */
	public static final String LOGIN_EVENT = "login";
	
	/** Specifies logout event name. */
	public static final String LOGOUT_EVENT = "logout";
	
	/** Specifies presentation value for not logged in user. */
	public static final String ANONYMOUS_USER_NAME = "anononymous";
}
