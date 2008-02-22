/**
 * 
 */
package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import static eionet.cr.web.util.ICRWebConstants.*;

/**
 * Action bean that deals with user login/logout.
 * <p>
 * In CAS context Action bean simply delegates user login/logout to CAS.
 * 
 * @author gerasvad, altnyris
 *
 */
@UrlBinding(LOGIN_ACTION)
public class LoginActionBean extends AbstractCRActionBean {
	
	/**
	 * Action method deals with user logging in.
	 * 
	 * @return {@link RedirectResolution} to CAS login URL.
	 */
	@DefaultHandler
	@HandlesEvent(LOGIN_EVENT)
	public Resolution login() {
		return new RedirectResolution(getContext().getCASLoginURL(), false);
	}
	
	/**
	 * Action method deals with user logging out.
	 * 
	 * @return {@link RedirectResolution} to CAS logout URL.
	 */
	@HandlesEvent(LOGOUT_EVENT)
	public Resolution logout() {
		getContext().setSessionAttribute(USER_SESSION_ATTR, null);
		getContext().attachEionetLoginCookie(false);
		return new RedirectResolution(getContext().getCASLogoutURL(), false);
	}
}
