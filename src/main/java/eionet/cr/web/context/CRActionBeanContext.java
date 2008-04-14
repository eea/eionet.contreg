/**
 * 
 */
package eionet.cr.web.context;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import eionet.cr.web.security.EionetCASFilter;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.ActionBeanContext;

import static eionet.cr.web.util.ICRWebConstants.*;

/**
 * Extension of stripes ActionBeanContext.
 * 
 * @author altnyris
 *
 */
public class CRActionBeanContext extends ActionBeanContext {
	
	private int severity;
	
	/**
	 * Wrapper method for {@link ServletRequest#getParameter(String)}.
	 * <p>
	 * The wrapper allows to avoid direct usage of {@link HttpServletRequest}.
	 * @param parameterName parameter name.
	 * @return corresponding parameter value from {@link HttpServletRequest}.
	 */
	public String getRequestParameter(String parameterName) {
		return getRequest().getParameter(parameterName);
	}
	
	/**
	 * Wrapper method for {@link HttpSession#setAttribute(String, Object)}.
	 * <p>
	 * The wrapper allows to avoid direct usage of {@link HttpSession}.
	 * @param name session attribute name.
	 * @param value session attribute value.
	 */
	public void setSessionAttribute(String name, Object value) {
		getRequest().getSession(true).setAttribute(name, value);
	}
	
	/**
	 * Method returns {@link CRUser} from session.
	 * 
	 * @return {@link CRUser} from session or null if user is not logged in.
	 */
	public CRUser getCRUser() {
		return (CRUser) getRequest().getSession(true).getAttribute(USER_SESSION_ATTR);
	}
	
	/**
	 * A wrapper for {@link EionetCASFilter#getCASLoginURL(javax.servlet.http.HttpServletRequest)}.
	 * 
	 * @return central authentication system login URL.
	 */
	public String getCASLoginURL() {
		return EionetCASFilter.getCASLoginURL(getRequest());
	}
	
	/**
	 * A wrapper for {@link EionetCASFilter#getCASLogoutURL(javax.servlet.http.HttpServletRequest)}.
	 * 
	 * @return central authentication system logout URL.
	 */
	public String getCASLogoutURL() {
		return EionetCASFilter.getCASLogoutURL(getRequest());
	}
	
	/**
	 * Wrapper method for
	 * {@link EionetCASFilter#attachEionetLoginCookie(javax.servlet.http.HttpServletResponse, boolean)}.
	 * 
	 * @param isLoggedIn
	 *            if value is true the cookie is added with some value that in
	 *            CAS context means logged in user otherwise cookie is added
	 *            that means not logged in user.
	 */
	public void attachEionetLoginCookie(boolean isLoggedIn) {
		EionetCASFilter.attachEionetLoginCookie(getResponse(), isLoggedIn);
	}
	
	/**
	 * 
	 * @return last action event URL.
	 */
	public String getLastActionEventUrl() {
		return (String) getRequest().getSession(true).getAttribute(LAST_ACTION_URL_SESSION_ATTR);
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}
}
