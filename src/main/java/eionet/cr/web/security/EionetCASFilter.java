package eionet.cr.web.security;

import static eionet.cr.web.util.WebConstants.AFTER_LOGIN_EVENT;
import static eionet.cr.web.util.WebConstants.LOGIN_ACTION;
import static eionet.cr.web.util.WebConstants.USER_SESSION_ATTR;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.cr.web.util.CrCasFilterConfig;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EionetCASFilter extends CASFilter {
	
	/** */
	private static String CAS_LOGIN_URL = null;
	private static String SERVER_NAME = null;

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.cas.client.filter.CASFilter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		
		CrCasFilterConfig filterConfig = CrCasFilterConfig.getInstance(config);
		
		CAS_LOGIN_URL = filterConfig.getInitParameter(LOGIN_INIT_PARAM);
		SERVER_NAME = filterConfig.getInitParameter(SERVERNAME_INIT_PARAM);
		
		super.init(filterConfig);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.cas.client.filter.CASFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws ServletException, IOException {

		if (request.getParameter("action") != null && !request.getParameter("action").equalsIgnoreCase("list_services")) {
			fc.doFilter(request, response);
			return;
		}
		
		CASFilterChain chain = new CASFilterChain();
		super.doFilter(request, response, chain);
		if (chain.isDoNext()) {
			
			HttpServletRequest httpRequest = (HttpServletRequest)request;
			HttpSession session = httpRequest.getSession();
			if (session != null) {
				
				String userName = (String) session.getAttribute(CAS_FILTER_USER);
				session.setAttribute(USER_SESSION_ATTR, new CRUser(userName));
				
				String requestURI = httpRequest.getRequestURI();
				if (requestURI.endsWith("/login")){
					
					String redirectUrl =  httpRequest.getContextPath() + LOGIN_ACTION + "?" + AFTER_LOGIN_EVENT;
					((HttpServletResponse)response).sendRedirect(redirectUrl);
					return;
				}
			}
			
			fc.doFilter(request, response);
			return;
		}
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getCASLoginURL(HttpServletRequest request) {
		return CAS_LOGIN_URL + "?service=" + request.getScheme() + "://" + SERVER_NAME + request.getContextPath() + "/login";
	}

	/**
	 * 
	 * @param req
	 * @param forSubscription
	 * @return
	 */
	public static String getCASLoginURL(HttpServletRequest req, boolean forSubscription){
		
		StringBuffer sb = new StringBuffer(CAS_LOGIN_URL);
		sb.append("?service=");
		sb.append(req.getScheme());
		sb.append("://");
		sb.append(SERVER_NAME);
		if (!req.getContextPath().equals("")) {
			sb.append(req.getContextPath());
		}
		sb.append("/login");
		if (forSubscription) {
			sb.append("?rd=subscribe");
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getCASLogoutURL(HttpServletRequest request) {
		return CAS_LOGIN_URL.replaceFirst("/login", "/logout") + "?url="
				+ request.getScheme() + "://" + SERVER_NAME
				+ request.getContextPath();
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static boolean isCasLoggedUser(HttpServletRequest request) {
		return (request.getSession() != null && request.getSession()
				.getAttribute(CAS_FILTER_USER) != null);
	}

	/**
	 * 
	 * @param aRegexFragment
	 * @return
	 */
	public static String forRegex(String aRegexFragment) {
		final StringBuffer result = new StringBuffer();

		final StringCharacterIterator iterator = new StringCharacterIterator(
				aRegexFragment);
		char character = iterator.current();
		while (character != CharacterIterator.DONE) {
			/*
			 * All literals need to have backslashes doubled.
			 */
			if (character == '.') {
				result.append("\\.");
			} else if (character == '\\') {
				result.append("\\\\");
			} else if (character == '?') {
				result.append("\\?");
			} else if (character == '*') {
				result.append("\\*");
			} else if (character == '+') {
				result.append("\\+");
			} else if (character == '&') {
				result.append("\\&");
			} else if (character == ':') {
				result.append("\\:");
			} else if (character == '{') {
				result.append("\\{");
			} else if (character == '}') {
				result.append("\\}");
			} else if (character == '[') {
				result.append("\\[");
			} else if (character == ']') {
				result.append("\\]");
			} else if (character == '(') {
				result.append("\\(");
			} else if (character == ')') {
				result.append("\\)");
			} else if (character == '^') {
				result.append("\\^");
			} else if (character == '$') {
				result.append("\\$");
			} else {
				//the char is not a special one
				//add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

}

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
class CASFilterChain implements FilterChain {

	private boolean doNext = false;

	public void doFilter(ServletRequest request, ServletResponse response) {
		doNext = true;
	}

	public boolean isDoNext() {
		return doNext;
	}
}
