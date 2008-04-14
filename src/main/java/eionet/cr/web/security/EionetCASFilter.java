package eionet.cr.web.security;

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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.cr.web.util.CrCasFilterConfig;
import static eionet.cr.web.util.ICRWebConstants.*;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class EionetCASFilter extends CASFilter {
	
	/** */
	public static final String EIONET_LOGIN_COOKIE_NAME = "eionetCasLogin";
	private static final String EIONET_COOKIE_LOGIN_PATH = "eionetCookieLogin";

	/** */
	private static String CAS_LOGIN_URL = null;
	private static String SERVER_NAME = null;
	private static String EIONET_LOGIN_COOKIE_DOMAIN = null;

	/**
	 * 
	 * @return
	 */
	public static String getEionetLoginCookieName() {
		return EIONET_LOGIN_COOKIE_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.cas.client.filter.CASFilter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig config) throws ServletException {
		
		CrCasFilterConfig filterConfig = CrCasFilterConfig.getInstance(config);
		
		CAS_LOGIN_URL = filterConfig.getInitParameter(LOGIN_INIT_PARAM);
		SERVER_NAME = filterConfig.getInitParameter(SERVERNAME_INIT_PARAM);
		EIONET_LOGIN_COOKIE_DOMAIN = filterConfig.getInitParameter("eionetLoginCookieDomain");
		
		super.init(filterConfig);
	}

	/*
	 * (non-Javadoc)
	 * @see edu.yale.its.tp.cas.client.filter.CASFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain fc) throws ServletException, IOException {

		if (request.getParameter("action") != null
				&& !request.getParameter("action").equalsIgnoreCase(
						"list_services")) {
			
			fc.doFilter(request, response);
			return;
		}
		
		CASFilterChain chain = new CASFilterChain();
		super.doFilter(request, response, chain);
		if (chain.isDoNext()) {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpSession session = httpRequest.getSession();
			if (session != null) {
				String userName = (String) session
						.getAttribute(CAS_FILTER_USER);
				httpRequest.getSession().setAttribute(USER_SESSION_ATTR, new CRUser(userName));
				String requestURI = httpRequest.getRequestURI();
				HttpServletResponse httpResponse = (HttpServletResponse) response;
				if (requestURI.indexOf(EIONET_COOKIE_LOGIN_PATH) > -1) {
					redirectAfterEionetCookieLogin(httpRequest, httpResponse);
					return;
				}
				else if (requestURI.endsWith("/login")){
					attachEionetLoginCookie(httpResponse, true);
					// TODO - what should really happen here is that user must be redricetd to the page he logged in from
					/*if (session.getAttribute("afterLogin") != null)
						httpResponse.sendRedirect(session.getAttribute("afterLogin").toString());
					else
						request.getRequestDispatcher("/").forward(request, response);*/
					String redirectUrl =  httpRequest.getContextPath() + LOGIN_ACTION + "?" + AFTER_LOGIN_EVENT;
					httpResponse.sendRedirect(redirectUrl);
					return;
				}
			}
			fc.doFilter(request, response);
			return;
		}
	}

	/**
	 * 
	 * @param response
	 * @param isLoggedIn
	 */
	public static void attachEionetLoginCookie(HttpServletResponse response,
			boolean isLoggedIn) {
		Cookie eionetCookie = new Cookie(EIONET_LOGIN_COOKIE_NAME,
				isLoggedIn ? "loggedIn" : "loggedOut");
		eionetCookie.setMaxAge(-1);
		if (!EIONET_LOGIN_COOKIE_DOMAIN.equalsIgnoreCase("localhost"))
			eionetCookie.setDomain(EIONET_LOGIN_COOKIE_DOMAIN);
		eionetCookie.setPath("/");
		response.addCookie(eionetCookie);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public static String getCASLoginURL(HttpServletRequest request) {
		/*request.getSession().setAttribute(
				"afterLogin",
				request.getRequestURL().toString()
						+ (request.getQueryString() != null ? ("?" + request
								.getQueryString()) : ""));*/
		return CAS_LOGIN_URL + "?service=" + request.getScheme() + "://"
				+ SERVER_NAME + request.getContextPath() + "/login";
	}

	/**
	 * 
	 * @param req
	 * @param forSubscription
	 * @return
	 */
	public static String getCASLoginURL(HttpServletRequest req,
			boolean forSubscription) {
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
	public static String getEionetCookieCASLoginURL(HttpServletRequest request) {

		String contextPath = request.getContextPath();
		String serviceURL = request.getRequestURL().toString();
		if (request.getQueryString() != null
				&& request.getQueryString().length() > 0) {
			serviceURL = serviceURL + "?" + request.getQueryString();
		}

		String serviceURI = serviceURL.substring(serviceURL.indexOf("/",
				serviceURL.indexOf("://") + 3));

		if (contextPath.equals("")) {
			if (serviceURI.equals("/"))
				serviceURL = serviceURL + EIONET_COOKIE_LOGIN_PATH + "/";
			else
				serviceURL = serviceURL.replaceFirst(forRegex(serviceURI), "/"
						+ EIONET_COOKIE_LOGIN_PATH + serviceURI);
		} else {
			String servletPath = serviceURI.substring(contextPath.length(),
					serviceURI.length());
			if (serviceURI.equals("/"))
				serviceURL = serviceURL + EIONET_COOKIE_LOGIN_PATH + "/";
			else
				serviceURL = serviceURL.replaceFirst(forRegex(serviceURI),
						contextPath + "/" + EIONET_COOKIE_LOGIN_PATH
								+ servletPath);
		}

		try {
			serviceURL = URLEncoder.encode(serviceURL, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			//logger.error(e);
		}

		return CAS_LOGIN_URL + "?service=" + serviceURL;

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
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void redirectAfterEionetCookieLogin(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		String requestUri = request.getRequestURI()
				+ (request.getQueryString() != null ? ("?" + request
						.getQueryString()) : "");
		String realURI = null;
		if (requestUri.endsWith(EIONET_COOKIE_LOGIN_PATH + "/"))
			realURI = requestUri.replaceFirst(EIONET_COOKIE_LOGIN_PATH + "/",
					"");
		else
			realURI = requestUri.replaceFirst("/" + EIONET_COOKIE_LOGIN_PATH,
					"");
		response.sendRedirect(realURI);
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
