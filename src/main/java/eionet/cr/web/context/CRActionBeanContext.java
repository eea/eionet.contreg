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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
/**
 * 
 */
package eionet.cr.web.context;

import static eionet.cr.web.util.WebConstants.LAST_ACTION_URL_SESSION_ATTR;
import static eionet.cr.web.util.WebConstants.USER_SESSION_ATTR;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sourceforge.stripes.action.ActionBeanContext;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.security.EionetCASFilter;

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
	 * Wrapper method for {@link HttpSession#setAttribute(String, ObjectDTO)}.
	 * <p>
	 * The wrapper allows to avoid direct usage of {@link HttpSession}.
	 * @param name session attribute name.
	 * @param value session attribute value.
	 */
	public void setSessionAttribute(String name, Object value) {
		getRequest().getSession().setAttribute(name, value);
	}
	
	/**
	 * Method returns {@link CRUser} from session.
	 * 
	 * @return {@link CRUser} from session or null if user is not logged in.
	 */
	public CRUser getCRUser() {
		return (CRUser) getRequest().getSession().getAttribute(USER_SESSION_ATTR);
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
	 * 
	 * @return last action event URL.
	 */
	public String getLastActionEventUrl() {
		return (String) getRequest().getSession().getAttribute(LAST_ACTION_URL_SESSION_ATTR);
	}

	public int getSeverity() {
		return severity;
	}

	public void setSeverity(int severity) {
		this.severity = severity;
	}
}
