package eionet.cr.web.util;

import javax.servlet.http.HttpServletRequest;

public class BaseUrl {

    static boolean valueSet = false;
    static String baseUrl = "";

    public static String getBaseUrl(HttpServletRequest request){
        if (!valueSet){
            valueSet = true;
            StringBuffer requestUrl = request.getRequestURL();
            String servletPath = request.getServletPath();
            baseUrl = requestUrl.substring(0, requestUrl.length() - servletPath.length())+"/";
        }
        return baseUrl;
    }
}
