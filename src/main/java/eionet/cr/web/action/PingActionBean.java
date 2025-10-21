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

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PingActionBean.class);

    /** The Constant ERR_BLANK_URI. */
    public static final int ERR_BLANK_URI = 1;

    /** The Constant ERR_INVALID_URL. */
    public static final int ERR_INVALID_URL = 2;

    /** The Constant ERR_FRAGMENT_URL. */
    public static final int ERR_FRAGMENT_URL = 3;

    /** The Constant ERR_BROKEN_URL. */
    public static final int ERR_BROKEN_URL = 4;

    /** Template for the XML-messages to be sent as response to this API. */
    private static final String RESPONSE_XML = "<?xml version=\"1.0\"?>\r\n" + "<response>\r\n"
            + "    <message>@message@</message>\r\n" + "    <flerror>@errorCode@</flerror>\r\n" + "</response>\r\n";

    /** Hosts allowed to use CR's ping API. May contain entries with wildcards. */
    private static final HashSet<String> PING_WHITELIST = getPingWhiteList();

    /** Hosts NOT allowed to use CR's ping API. May contain entries with wildcards. */
    private static final HashSet<String> PING_BLACKLIST = getPingBlackList();

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
            LOGGER.info("Client denied: host = " + host + ", IP = " + ip);
            return new ErrorResolution(HttpURLConnection.HTTP_FORBIDDEN, "Client host denied!");
        }

        // The default result-message and error code that will be printed into XML response.
        int errorCode = 0;
        String message = "";
        try {
            // Ensure that the pinged URI is not blank, is legal URI, does not have a fragment part and is not broken.
            if (StringUtils.isBlank(uri)) {
                errorCode = ERR_BLANK_URI;
                message = "No URI given, no action taken.";
                uri = ""; // Force it to be the empty string for logging
            } else if (!URLUtil.isURL(uri)) {
                if (create) {
                    errorCode = ERR_INVALID_URL;
                    message = "Not a valid URL, source cannot be created.";
                } else {
                    message = "Not a valid URL, no action taken.";
                }
            } else if (create && new URL(uri).getRef() != null) {
                errorCode = ERR_FRAGMENT_URL;
                message = "URL with a fragment part not allowed, source cannot be created.";
            } else if (create && !URLUtil.resourceExists(uri, true)) {
                errorCode = ERR_BROKEN_URL;
                message = "Could not make a connection to this URL, source cannot be created.";
            } else {
                // Helper flag that will be raised if a harvest is indeed needed.
                boolean doHarvest = false;

                // Sanitize URL and force it HTTP for becoming harvest source URL.
                String sourceUrl = URLUtil.httpsToHttp(URLUtil.sanitizeHarvestSourceUrl(uri));

                // Check if a source by this URI exists.
                HarvestSourceDTO source = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(sourceUrl);
                if (source != null) {
                    doHarvest = true;
                } else if (create) {

                    // Graph does not exist, but must be created as indicated in request parameters
                    source = new HarvestSourceDTO();
                    source.setUrl(this.uri);

                    // If the new introduced property has not been set in the config file, use deprecated value
                    source.setIntervalMinutes(GeneralConfig.getDefaultHarvestIntervalMinutes());

                    DAOFactory.get().getDao(HarvestSourceDAO.class).addSource(source);
                    doHarvest = true;
                } else {
                    message = "URL not in catalogue of sources, no action taken: ";
                }

                if (doHarvest) {
                    UrgentHarvestQueue.addPullHarvest(this.uri, CRUser.PING_HARVEST.getUserName());
                    message = "URL added to the urgent harvest queue.";
                }
            }
        } catch (Exception e) {
            LOGGER.error("PING request failed: " + e.toString(), e);
            return new ErrorResolution(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }

        LOGGER.debug(message + " (" + uri + ")");
        String response = RESPONSE_XML.replace("@message@", message);
        response = response.replace("@errorCode@", String.valueOf(errorCode));
        return new StreamingResolution("text/xml", response);
    }

    /**
     * A handler for the "delete" event. Allows deletion of a particular source.
     *
     * @return Resolution to return to.
     */
    public Resolution delete() {

        // Get client host/IP, ensure that it's in the whitelist.
        HttpServletRequest request = getContext().getRequest();
        String ip = request.getRemoteAddr();
        String host = processClientHostName(request.getRemoteHost(), ip);
        if (!isTrustedRequester(host, ip)) {
            LOGGER.debug("Client denied: host = " + host + ", IP = " + ip);
            return new ErrorResolution(HttpURLConnection.HTTP_FORBIDDEN, "Operation not allowed!");
        }

        // The default result-message and error code that will be printed into XML response.
        int errorCode = 0;
        String message = "";
        try {
            // Ensure that the pinged URI is not blank, is legal URI, does not have a fragment part and is not broken.
            if (StringUtils.isBlank(uri)) {
                errorCode = ERR_BLANK_URI;
                message = "No URI given, no action taken.";
            } else {
                // Helper flag that will be raised the deletion should indeed be made.
                boolean doDeletion = false;

                // Check if a source by this URI exists.
                HarvestSourceDTO source = DAOFactory.get().getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);
                if (source != null) {

                    // Source exists, ensure it has an ID too.
                    Integer sourceId = source.getSourceId();
                    if (sourceId == null) {
                        throw new CRRuntimeException("Stumbled on harvest source with id=NULL: " + uri);
                    }

                    // Deletion allowed only for sources that have been pinged.
                    List<HarvestDTO> harvests = DAOFactory.get().getDao(HarvestDAO.class).getHarvestsBySourceId(sourceId);
                    if (CollectionUtils.isNotEmpty(harvests)) {
                        for (HarvestDTO harvestDTO : harvests) {
                            if (CRUser.PING_HARVEST.getUserName().equals(harvestDTO.getUser())) {
                                doDeletion = true;
                                break;
                            }
                        }
                    }

                    // If deletion now alloed (i.e. source never pinged), then return unauthorized.
                    if (!doDeletion) {
                        return new ErrorResolution(HttpURLConnection.HTTP_FORBIDDEN, "Source not allowed for ping-deletion!");
                    }

                } else {
                    // No such source found, prepare relevant feedback message.
                    message = "URL not in catalogue of sources, no action taken.";
                }

                // If deletion approved, then do it.
                if (doDeletion) {
                    DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(Collections.singletonList(uri), false);
                    message = "URL deleted: " + uri;
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

        for (String pattern : PING_BLACKLIST) {

            if (StringUtils.isNotBlank(host) && Util.wildCardMatch(host.toLowerCase(), pattern)) {
                return false;
            }
        }

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

        parseUrlList(result, property);

        return result;
    }

    /**
     * Utility method for obtaining the set of ping blacklist (i.e. hosts not allowed to call CR's ping API) from configuration.
     *
     * @return The ping blacklist as a hash-set
     */
    private static HashSet<String> getPingBlackList() {

        HashSet<String> result = new HashSet<String>();
        String property = GeneralConfig.getProperty(GeneralConfig.PING_BLACKLIST);
        parseUrlList(result, property);

        return result;
    }

    /**
     * Utility method to parse list of url from the configuration file.
     *
     * @param result
     * @param property
     */
    private static void parseUrlList(HashSet<String> result, String property) {
        if (!StringUtils.isBlank(property)) {
            String[] split = property.split("\\s*,\\s*");
            for (int i = 0; i < split.length; i++) {
                if (StringUtils.isNotBlank(split[i])) {
                    result.add(split[i].trim().toLowerCase());
                }
            }
        }
    }

    /**
     * @param create
     *            the create to set
     */
    public void setCreate(boolean create) {
        this.create = create;
    }
}
