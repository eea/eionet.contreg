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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.web.action;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;

/**
 * An action bean that implements the CR's "ping" API. It is a RESTful API that enables other application to force an urgent harvest
 * of a source. The latter may or may not already exist in CR database and triple store.
 *
 * See also http://taskman.eionet.europa.eu/issues/10034
 *
 * @author jaanus
 */
@UrlBinding("/ping")
public class PingActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(PingActionBean.class);

    /** Template for the XML-messages to be sent as response to this API. */
    private static final String RESPONSE_XML = "<?xml version=\"1.0\"?>\r\n" + "<response>\r\n"
            + "    <message>@message@</message>\r\n" + "    <flerror>@errorCode@</flerror>\r\n" + "</response>";

    /** Hosts allowed to use CR's ping API. May contain entries with wildcards. */
    private static final HashSet<String> PING_WHITELIST = getPingWhiteList();

    /** The URI to ping. Required. */
    private String uri;

    /** If true, the source must only be harvested if it already exists in the triple store. Optional. */
    private boolean mustExist;

    /**
     * The default handler of this API's calls.
     *
     * @return
     */
    @DefaultHandler
    public Resolution defaultHandler() {

        HttpServletRequest request = getContext().getRequest();
        String ip = request.getRemoteAddr();
        String host = request.getRemoteHost();

        if (!isTrustedRequester(host, ip)) {
            LOGGER.debug("Client denied: host = " + host + ", IP = " + ip);
            return new ErrorResolution(HttpURLConnection.HTTP_FORBIDDEN);
        }

        if (StringUtils.isBlank(uri)) {
            return new ErrorResolution(HttpURLConnection.HTTP_BAD_REQUEST, "URI must not be blank!");
        }

        String message = "";
        try {
            // Ensure we have a legal and non-broken URL.
            URL url = new URL(uri);
            if (url.getRef() != null) {
                return new ErrorResolution(HttpURLConnection.HTTP_BAD_REQUEST, "URL with a fragment part not allowed!");
            }
            else if (URLUtil.isNotExisting(uri)){
                return new ErrorResolution(HttpURLConnection.HTTP_BAD_REQUEST, "URL seems to be broken!");
            }

            // Helper flag that will be raised of a harvest is indeed needed.
            boolean doHarvest = true;

            boolean exists = DAOFactory.get().getDao(HelperDAO.class).isGraphExists(uri);
            if (mustExist && !exists) {
                // Graph must exist as requested, yet it doesn't, hence no harvest will be done, response will be simply 200.
                doHarvest = false;
                message = "Harvest skipped, as no such graph exists in triple store: " + uri;
                LOGGER.debug(message);
            } else if (!exists) {
                HarvestSourceDTO source = new HarvestSourceDTO();
                source.setUrl(uri);
                source.setIntervalMinutes(GeneralConfig.getIntProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL, 60480));
                DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);
            }

            if (doHarvest) {
                UrgentHarvestQueue.addPullHarvest(uri);
                message = "URI added to the urgent harvest queue: " + uri;
                LOGGER.debug(message);
            }
        } catch (Exception e) {
            LOGGER.error("PING request failed: " + e.toString(), e);
            return new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        String response = RESPONSE_XML.replace("@message@", message);
        response = response.replace("@errorCode@", "0");
        return new StreamingResolution("text/xml", response);
    }

    /**
     * Returns true if the requester identified by the given host and/or IP address is trusted. Otherwise returns false.
     *
     * @param host The requester's host.
     * @param ip The requester's IP address.
     * @return The boolean as indicated.
     */
    private boolean isTrustedRequester(String host, String ip) {

        for (String pattern : PING_WHITELIST){

            if (StringUtils.isNotBlank(host) && Util.wildCardMatch(host.toLowerCase(), pattern)){
                return true;
            }

            if (StringUtils.isNotBlank(ip) && Util.wildCardMatch(ip, pattern)){
                return true;
            }
        }

        return false;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @param mustExist
     *            the mustExist to set
     */
    public void setMustExist(boolean mustExist) {
        this.mustExist = mustExist;
    }

    /**
     * Utility method for obtaining the set of ping whitelist (i.e. hosts allowed to call CR's ping API) from configuration.
     *
     * @return The ping whitelist as a hash-set
     */
    private static HashSet<String> getPingWhiteList(){

        HashSet<String> result = new HashSet<String>();
        result.add("localhost");
        result.add("127.0.0.1");
        result.add("0:0:0:0:0:0:0:1");
        result.add("::1");

        String property = GeneralConfig.getProperty(GeneralConfig.PING_WHITELIST);
        if (!StringUtils.isBlank(property)){
            StringTokenizer st = new StringTokenizer(property, ",");
            while (st.hasMoreTokens()){
                String token = st.nextToken().trim();
                if (!token.isEmpty()){
                    result.add(token.toLowerCase());
                }
            }
        }

        return result;
    }
}
