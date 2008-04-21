/**
 * 
 */
package eionet.cr.web.action;

import eionet.cr.web.interceptor.annotation.DontSaveLastActionEvent;
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
	@DontSaveLastActionEvent
	public Resolution login() {
		return new RedirectResolution(getContext().getCASLoginURL(), false);
	}
	
	/**
	 * Method redirects to last event occurred before logging in.
	 * 
	 * @return redirect resolution to last event occurred before logging in.
	 */
	@HandlesEvent(AFTER_LOGIN_EVENT)
	@DontSaveLastActionEvent
	public Resolution afterLogin() {
		String lastActionEventUrl = getContext().getLastActionEventUrl();
		lastActionEventUrl = lastActionEventUrl == null ? MAIN_PAGE_ACTION : lastActionEventUrl;
		//getContext().getCRUser().setLocalId(this.userService.addUserIfrequired(getUserName()));
		return new RedirectResolution(
				lastActionEventUrl,
				!lastActionEventUrl.toLowerCase().startsWith("http://"));
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
