package eionet.cr.web.filters.cas;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.cr.config.GeneralConfig;
import eionet.cr.web.security.CRUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import static eionet.cr.web.util.WebConstants.AFTER_LOGIN_EVENT;
import static eionet.cr.web.util.WebConstants.LOGIN_ACTION;
import static eionet.cr.web.util.WebConstants.USER_SESSION_ATTR;

/**
 * CAS Login Filter.
 *
 */
public class CASLoginFilter extends CASFilter {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CASLoginFilter.class);
    /** FQN of this class. */
    private static final String CLASS_NAME = CASLoginFilter.class.getName();

    /*
     * (non-Javadoc)
     *
     * @see edu.yale.its.tp.cas.client.filter.CASFilter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
        LOGGER.info("Initializing " + CLASS_NAME + " ...");
        super.init(CASFilterConfig.getInstance(config));
    }

    /*
     * (non-Javadoc)
     *
     * @see edu.yale.its.tp.cas.client.filter.CASFilter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse,
     * javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain fc) throws ServletException, IOException {
        LOGGER.trace(CLASS_NAME + ".doFilter() invoked ...");
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession();
        if (request.getParameter("action") != null && !request.getParameter("action").equalsIgnoreCase("list_services")) {
            fc.doFilter(request, response);
            return;
        }
        if (session != null) {
            String userName = ((String) session.getAttribute(CAS_FILTER_USER));
            if (userName != null) {
                session.setAttribute(USER_SESSION_ATTR, new CRUser(userName));
            }
        }
        super.doFilter(request, response, fc);


//        super.doFilter(request, response, fc);
         /*if (request.getParameter("action") != null && !request.getParameter("action").equalsIgnoreCase("list_services")) {
            fc.doFilter(request, response);
            return;
        }

        CASFilterChain chain = new CASFilterChain();
        super.doFilter(request, response, chain);
        if (chain.isDoNext()) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpSession session = httpRequest.getSession();
            if (session != null) {

                String userName = ((String) session.getAttribute(CAS_FILTER_USER)).toLowerCase();
                session.setAttribute(USER_SESSION_ATTR, new CRUser(userName));

                String requestURI = httpRequest.getRequestURI();
                if (requestURI.endsWith("/login")) {

                    String redirectUrl = httpRequest.getContextPath() + LOGIN_ACTION + "?" + AFTER_LOGIN_EVENT;
                    ((HttpServletResponse) response).sendRedirect(redirectUrl);
                    return;
                }
            }

            fc.doFilter(request, response);
            return;
        }*/
    }
    class CASFilterChain implements FilterChain {

        private boolean doNext = false;

        public void doFilter(ServletRequest request, ServletResponse response) {
            doNext = true;
        }

        public boolean isDoNext() {
            return doNext;
        }
    }

    public static String getCASLoginURL(HttpServletRequest request) {
        String casLoginUrl = GeneralConfig.getProperty(CASFilter.LOGIN_INIT_PARAM);
        String serverName = GeneralConfig.getProperty(CASFilter.SERVERNAME_INIT_PARAM);
        return casLoginUrl + "?service=" + request.getScheme() + "://" + serverName + request.getContextPath() + "/login";
    }

    public static String getCASLogoutURL(HttpServletRequest request) {
        String casLoginUrl = GeneralConfig.getProperty(CASFilter.LOGIN_INIT_PARAM);
        String serverName = GeneralConfig.getProperty(CASFilter.SERVERNAME_INIT_PARAM);
        return casLoginUrl.replaceFirst("/login", "/logout") + "?url=" + request.getScheme() + "://" + serverName
                + request.getContextPath();
    }
}
