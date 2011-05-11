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
package eionet.cr.web.security;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;
import com.tee.uit.security.SignOnException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;

/**
 * Class represents authenticated user.
 *
 * @author altnyris
 *
 */
public class CRUser {

    /**
     * ACL anonymous entry name.
     */
    private static String aclAnonymousEntryName;

    /** */
    private static Log logger = LogFactory.getLog(CRUser.class);

    /** */
    public static final CRUser APPLICATION = new CRUser("application");

    /** */
    private String userName;

    /**
     *
     */
    public CRUser() {
    }

    static {
        aclAnonymousEntryName = GeneralConfig.getProperty(GeneralConfig.ACL_ANONYMOUS_ACCESS_PROP, null);
    }

    /**
     * Creates CRUser.
     * @param userName username
     */
    public CRUser(final String userName) {
        this.userName = userName;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param username the userName to set
     */
    public void setUserName(final String username) {
        this.userName = username;
    }

    /**
     * True if user is listed as administrator in ACL.
     * @return boolean
     */
    public boolean isAdministrator() {
        return hasPermission("/", "u");
    }

    /**
     * Checks if authenticated user or anonymous user has got this permission to the ACL.
     * @param user CRUser in the HTTP Session
     * @param aclPath full ACL path
     * @param permission permission to be checked
     * @return boolean
     */
    public static boolean userHasPermission(CRUser user, String aclPath, String permission) {
        return hasPermission(user != null ? user.getUserName() : null, aclPath, permission);
    }
     /**
     * Checks if current user as the given permission to the specified ACL.
     * @param aclPath full ACL path
     * @param prm permission
     * @return boolean
     */
    public boolean hasPermission(final String aclPath, final String prm) {
        return hasPermission(userName, aclPath, prm);
    }

    /**
     * Checks authenticated or anonymous user permission.
     * @param userName Username or null for anonymous user
     * @param aclPath Full ACL path
     * @param prm permission
     * @return boolean
     */
    public static boolean hasPermission(String userName, final String aclPath, final String prm) {

        //if user not logged in check permission anonymous ACL entry if exists:
        if (Util.isNullOrEmpty(userName) && !Util.isNullOrEmpty(aclAnonymousEntryName)) {
            userName = aclAnonymousEntryName;
        }

        if (Util.isNullOrEmpty(userName) || Util.isNullOrEmpty(aclPath) || Util.isNullOrEmpty(prm))
            return false;

        boolean result = false;
        try {
            AccessControlListIF acl = AccessController.getAcl(aclPath);
            if (acl != null) {
                result = acl.checkPermission(userName, prm);
                if (!result) {
                    logger.debug("User " + userName + " does not have permission " + prm + " in acl \"" + aclPath + "\"");
                }
            } else {
                logger.warn("acl \"" + aclPath + "\" not found!");
            }
        } catch (SignOnException soe) {
            logger.error(soe.toString(), soe);
        }

        return result;
    }
    
    /**
     * 
     * @return
     */
	private static String appHomeURL(){
		
		return GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
	}

    /**
     * Returns home URL of the user.
     * @return String
     */
    public String getHomeUri() {
        return CRUser.homeUri(userName);
    }

    /**
     * Returns review URL of the user.
     * @param reviewId Id of review
     * @return String review URL
     */
    public String getReviewUri(final int reviewId) {
        return CRUser.homeUri(userName) + "/reviews/" + reviewId;
    }

    /**
     *
     * @return
     */
    public String getReviewAttachmentUri(int reviewId, String attachmentFileName) {
        return CRUser.homeUri(userName) + "/reviews/" + reviewId + "/" + attachmentFileName;
    }

    /**
     * Registrations uri.
     * @return String URI
     */
    public String getRegistrationsUri() {
        return CRUser.registrationsUri(userName);
    }

    /**
     * Bookmarks URI of the user.
     * @return String URL
     */
    public String getBookmarksUri() {
        return CRUser.bookmarksUri(userName);
    }

    /**
     * History URI of the user.
     * @return String URI
     */
    public String getHistoryUri() {
        return CRUser.historyUri(userName);
    }

    /**
     * Home Item URI.
     * @param uri String
     * @return String
     */
    public String getHomeItemUri(String uri) {
        return CRUser.homeItemUri(userName, uri);
    }

    /**
     * Home URI of the user.
     * @param userName user name
     * @return String
     */
    public static String homeUri(String userName) {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("userName must not be blank");
        } else {
        	return new StringBuilder(appHomeURL()).append("/home/").append(userName).toString();
        }
    }

    /**
     *
     * @param userName
     * @param uri
     * @return
     */
    public static String homeItemUri(String userName, String uri) {

        if (StringUtils.isBlank(userName) || StringUtils.isBlank(uri)) {
            throw new IllegalArgumentException("userName and uri must not be blank");
        } else {
        	return new StringBuilder(homeUri(userName)).append("/").append(Hashes.spoHash(uri)).toString();
        }
    }

    /**
     *
     * @param userName
     * @return
     */
    public static String bookmarksUri(String userName) {

        return CRUser.homeUri(userName) + "/bookmarks";
    }

    /**
     *
     * @param userName
     * @return
     */
    public static String registrationsUri(String userName) {

        return CRUser.homeUri(userName) + "/registrations";
    }

    /**
     *
     * @param userName
     * @return
     */
    public static String historyUri(String userName) {

        return CRUser.homeUri(userName) + "/history";
    }
}
