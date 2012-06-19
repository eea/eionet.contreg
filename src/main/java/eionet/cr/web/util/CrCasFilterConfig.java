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
package eionet.cr.web.util;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import edu.yale.its.tp.cas.client.filter.CASFilter;
import eionet.cr.config.GeneralConfig;

/**
 * An implementation of {@link FilterConfig} that reads configs from property file.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public final class CrCasFilterConfig implements FilterConfig {

    /** */
    private FilterConfig defaultConfig;
    private Hashtable<String, String> initParamMap;
    private static ConcurrentHashMap<String, String> crInitParamsMap = null;

    /**
     *
     * @param defaultConfig {@link FilterConfig} from web context.
     */
    @SuppressWarnings("unchecked")
    private CrCasFilterConfig(FilterConfig defaultConfig) {
        this.defaultConfig = defaultConfig;
        this.initParamMap = new Hashtable<String, String>();
        Enumeration<String> defaultKeys = this.defaultConfig.getInitParameterNames();

        String defaultKey;
        while (defaultKeys.hasMoreElements()) {
            defaultKey = defaultKeys.nextElement();
            initParamMap.put(defaultKey, this.defaultConfig.getInitParameter(defaultKey));
        }

        initParamMap.putAll(crInitParamsMap);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.FilterConfig#getFilterName()
     */
    public String getFilterName() {
        return this.defaultConfig.getFilterName();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String parameterKey) {
        return this.initParamMap.get(parameterKey);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.FilterConfig#getInitParameterNames()
     */
    public Enumeration<String> getInitParameterNames() {
        return this.initParamMap.keys();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.servlet.FilterConfig#getServletContext()
     */
    public ServletContext getServletContext() {
        return this.defaultConfig.getServletContext();
    }

    /**
     *
     * @param defaultConfig defaultConfig {@link FilterConfig} from web context.
     * @return an instance of {@link CrCasFilterConfig}.
     */
    public static CrCasFilterConfig getInstance(FilterConfig defaultConfig) {
        if (crInitParamsMap == null) {
            initFilterParams(defaultConfig);
        }

        return new CrCasFilterConfig(defaultConfig);
    }

    /**
     *
     * @param defaultConfig
     */
    private static synchronized void initFilterParams(FilterConfig defaultConfig) {

        crInitParamsMap = new ConcurrentHashMap<String, String>();
        ResourceBundle resourceBundle = ResourceBundle.getBundle(GeneralConfig.BUNDLE_NAME);

        for (CRInitParameterKey initParameterKey : CRInitParameterKey.values()) {
            crInitParamsMap.put(initParameterKey.getParameterKey(), resourceBundle.getString(initParameterKey.getParameterKey()));
        }
    }

    /**
     * Enum of CR web context related keys.
     *
     */
    public enum CRInitParameterKey {

        /** Specifies CAS server login URL. */
        CAS_LOGIN_URL(CASFilter.LOGIN_INIT_PARAM),
        /** Specifies CAS server validation URL. */
        CAS_VALIDATE_URL(CASFilter.VALIDATE_INIT_PARAM),
        /** Specifies application server name/URL. */
        CAS_SERVER_NAME(CASFilter.SERVERNAME_INIT_PARAM),
        /** Specifies flag wrap request. */
        CAS_WRAP_REQUEST(CASFilter.WRAP_REQUESTS_INIT_PARAM);

        private String parameterKey;

        private CRInitParameterKey(String parameterKey) {
            this.parameterKey = parameterKey;
        }

        public String getParameterKey() {
            return this.parameterKey;
        }
    }

}
