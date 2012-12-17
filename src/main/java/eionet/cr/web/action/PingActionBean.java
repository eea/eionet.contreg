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
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;

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

    /** */
    private static final int ERR_BLANK_URI = 1;
    private static final int ERR_INVALID_URL = 2;
    private static final int ERR_FRAGMENT_URL = 3;
    private static final int ERR_BROKEN_URL = 4;

    /** Template for the XML-messages to be sent as response to this API. */
    private static final String RESPONSE_XML = "<?xml version=\"1.0\"?>\r\n" + "<response>\r\n"
            + "    <message>@message@</message>\r\n" + "    <flerror>@errorCode@</flerror>\r\n" + "</response>";

    /** Hosts allowed to use CR's ping API. May contain entries with wildcards. */
    private static final HashSet<String> PING_WHITELIST = getPingWhiteList();

    /** The URI to ping. Required. */
    private String uri;

    /** If true, the source must be created if not existing yet. */
    private boolean create;

    /**
     * The default handler of this API's calls.
     *
     * @return
     */
    @DefaultHandler
    public Resolution defaultHandler() {

        // Get client host/IP, ensure that it's in the whitelist.
        HttpServletRequest request = getContext().getRequest();
        String ip = request.getRemoteAddr();
        String host = processClientHostName(request.getRemoteHost(), ip);
        if (!isTrustedRequester(host, ip)) {
            LOGGER.debug("Client denied: host = " + host + ", IP = " + ip);
            return new ErrorResolution(HttpURLConnection.HTTP_FORBIDDEN);
        }

        // The default result-message and error code that will be printed into XML response.
        int errorCode = 0;
        String message = "";
        try {
            // Ensure that the pinged URI is not blank, is legal URI, does not have a fragment part and is not broken.
            if (StringUtils.isBlank(uri)) {
                errorCode = ERR_BLANK_URI;
                message = "No URI given, no action taken.";
            } else if (!URLUtil.isURL(uri)) {
                if (create) {
                    errorCode = ERR_INVALID_URL;
                    message = "Not a valid URL, source cannot be created.";
                } else {
                    message = "URL not in catalogue of sources, no action taken.";
                }
            } else if (create && new URL(uri).getRef() != null) {
                errorCode = ERR_FRAGMENT_URL;
                message = "URL with a fragment part not allowed, source cannot be created.";
            } else if (create && URLUtil.isNotExisting(uri)) {
                errorCode = ERR_BROKEN_URL;
                message = "Could not make a connection to this URL, source cannot be created.";
            } else {
                // Helper flag that will be raised if a harvest is indeed needed.
                boolean doHarvest = false;

                // Check if a graph by this URI exists.
                boolean exists = DAOFactory.get().getDao(HelperDAO.class).isGraphExists(uri);
                if (exists) {
                    doHarvest = true;
                } else if (create) {

                    // Graph does not exist, but must be created as indicated in request parameters
                    HarvestSourceDTO source = new HarvestSourceDTO();
                    source.setUrl(uri);
                    source.setIntervalMinutes(GeneralConfig.getIntProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL, 60480));
                    DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);
                    doHarvest = true;
                } else {
                    message = "URL not in catalogue of sources, no action taken.";
                }

                if (doHarvest) {
                    UrgentHarvestQueue.addPullHarvest(uri);
                    message = "URL added to the urgent harvest queue: " + uri;
                }
            }
        } catch (Exception e) {
            LOGGER.error("PING request failed: " + e.toString(), e);
            return new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        LOGGER.debug(message);
        String response = RESPONSE_XML.replace("@message@", message);
        response = response.replace("@errorCode@", String.valueOf(errorCode));
        return new StreamingResolution("text/xml", response);
    }

    /**
     * Returns true if the requester identified by the given host and/or IP address is trusted. Otherwise returns false.
     *
     * @param host
     *            The requester's host.
     * @param ip
     *            The requester's IP address.
     * @return The boolean as indicated.
     */
    private boolean isTrustedRequester(String host, String ip) {

        for (String pattern : PING_WHITELIST) {

            if (StringUtils.isNotBlank(host) && Util.wildCardMatch(host.toLowerCase(), pattern)) {
                return true;
            }

            if (StringUtils.isNotBlank(ip) && Util.wildCardMatch(ip, pattern)) {
                return true;
            }
        }

        return false;
    }

    /**
     * If the given hostName differs from the given IP address then this method returns the hostName as it is. Otherwise it uses
     * {@link InetAddress} to detect the given IP address's true host name. If that still fails, the method returns the IP address
     * as given.
     *
     * @param hostName
     *            As indicated above.
     * @param ip
     *            As indicated above.
     * @return As indicated above.
     */
    private String processClientHostName(String hostName, String ip) {

        // If the IP address is blank we can only return the host name as it is.
        if (StringUtils.isBlank(ip)) {
            return hostName;
        }

        // If the hostName is blank or it equals with the IP, then try to obtain proper host name from java.net.InetAddress.
        if (StringUtils.isBlank(hostName) || hostName.equals(ip)) {
            try {
                return InetAddress.getByName(ip).getCanonicalHostName();
            } catch (UnknownHostException e) {
                // Fallback to IP address.
                return ip;
            }
        } else {
            // The host name was not blank and it differs from IP, so return it as it is.
            return hostName;
        }
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * Utility method for obtaining the set of ping whitelist (i.e. hosts allowed to call CR's ping API) from configuration.
     *
     * @return The ping whitelist as a hash-set
     */
    private static HashSet<String> getPingWhiteList() {

        HashSet<String> result = new HashSet<String>();
        result.add("localhost");
        result.add("127.0.0.1");
        result.add("0:0:0:0:0:0:0:1");
        result.add("::1");

        String property = GeneralConfig.getProperty(GeneralConfig.PING_WHITELIST);
        if (!StringUtils.isBlank(property)) {
            String[] split = property.split("\\s*,\\s*");
            for (int i = 0; i < split.length; i++) {
                if (StringUtils.isNotBlank(split[i])) {
                    result.add(split[i].trim().toLowerCase());
                }
            }
        }

        return result;
    }

    /**
     * @param create
     *            the create to set
     */
    public void setCreate(boolean create) {
        this.create = create;
    }
}
