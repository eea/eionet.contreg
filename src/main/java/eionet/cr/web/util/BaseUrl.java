package eionet.cr.web.util;

import javax.servlet.http.HttpServletRequest;

/**
 *
 *
 * A single-purpose class that serves the base URL of requests coming to this application. It is referenced in JSP classes, so
 * caution when considering this class for deletion!
 *
 * @author Risto Alt
 * @author Jaanus Heinlaid
 */
public final class BaseUrl {

    /** */
    static boolean valueSet = false;
    static String baseUrl = "";

    /**
     * Hide utility class constructor.
     */
    private BaseUrl() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     *
     * @param request
     * @return
     */
    public static String getBaseUrl(HttpServletRequest request) {
        if (!valueSet) {
            valueSet = true;
            StringBuffer requestUrl = request.getRequestURL();
            String servletPath = request.getServletPath();
            baseUrl = requestUrl.substring(0, requestUrl.length() - servletPath.length()) + "/";
        }
        return baseUrl;
    }
}
