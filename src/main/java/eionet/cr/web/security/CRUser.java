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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tee.uit.security.AccessControlListIF;
import com.tee.uit.security.AccessController;
import com.tee.uit.security.AclNotFoundException;
import com.tee.uit.security.SignOnException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.web.util.WebConstants;

/**
 * Class represents authenticated user.
 *
 * @author altnyris
 *
 */
public class CRUser {

    /** */
    private static Log logger = LogFactory.getLog(CRUser.class);

    /** */
    public static final CRUser APPLICATION = new CRUser("application");

    /** */
    private String userName;

    /** List of the user's folder-or-file URIs that should be reserved, i.e. not to be created or deleted by user himself! */
    private List<String> reservedFolderAndFileUris;

    /**
     * Creates CRUser.
     *
     * @param userName username
     */
    public CRUser(String userName) {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("User name must not be blank!");
        }
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
     *
     * @return boolean
     */
    public boolean isAdministrator() {
        return hasPermission("/admin", "a");
    }

    /**
     * Returns the value of {@link #hasPermission(String, String, String)}, using the given ACL path, the given permission, and the
     * name of this user.
     *
     * @param aclPath full ACL path
     * @param permission permission to check
     * @return
     */
    public boolean hasPermission(String aclPath, String permission) {
        return CRUser.hasPermission(userName, aclPath, permission);
    }

    /**
     * Returns the value of {@link #hasPermission(String, String, String)}, using the given ACL path, the given permission, and the
     * name of the user found in the given session. If no user found in session, the method will be called with user name set to
     * null.
     *
     * @param session current session
     * @param aclPath full acl path
     * @param permission permission to be checked
     * @return true if user hsa the checked permission
     */
    public static boolean hasPermission(HttpSession session, String aclPath, String permission) {

        // if no session given, simply return false
        if (session == null) {
            return false;
        }

        // get user object from session
        CRUser crUser = (CRUser) session.getAttribute(WebConstants.USER_SESSION_ATTR);

        // Return false if user is not logged in
        // if (crUser == null) {
        // return false;
        // }

        // get user name from user object, or set to null if user object null
        String userName = crUser == null ? null : crUser.getUserName();

        // check if user with this name has this permission in this ACL
        return CRUser.hasPermission(userName, aclPath, permission);
    }

    /**
     * Checks is action is allowed. If ACL does not exist, checks if user is logged in or not
     *
     * @param aclPath ACL Path
     * @param user Current user (null if not logged in)
     * @param permission permission to be checked
     * @param allowAnonymous - indicates if to allow anonymous users if ACL does not exist
     * @return true if user can perform the action
     */
    public static boolean hasPermission(String aclPath, CRUser user, String permission, boolean allowAnonymous) {

        // allow to view the folder by default if there is no ACL
        boolean allowAction = true;

        try {
            AccessControlListIF acl = AccessController.getAcl(aclPath);

            String userName = (user != null ? user.getUserName() : null);
            if (acl != null) {
                allowAction = acl.checkPermission(userName, permission);
            } else {
                // ACL does not exist - check the additional parameter
                if (!allowAnonymous) {
                    allowAction = (user != null);
                }
            }
        } catch (SignOnException e) {
            // TODO Auto-generated catch block
            logger.error("Error in ACL check " + e.toString());
        }

        return allowAction;
    }

    /**
     * Looks up an ACL with the given path, and checks if the given user has the given permission in it. If no such ACL is found,
     * the method returns false. If the ACL is found, and it has the given permission for the given user, the method returns true,
     * otherwise false.
     *
     * Situation where user name is null, is handled by the ACL library (it is treated as anonymous user).
     *
     * If the ACL library throws an exception, it is not thrown onwards, but still logged at error level.
     *
     * @param userName username
     * @param aclPath ACL Path
     * @param permission Permission to be checked
     * @return true if user has permission. If no ACL found return false
     */
    public static boolean hasPermission(String userName, String aclPath, String permission) {

        // consider missing ACL path or permission to be a programming error
        if (Util.isNullOrEmpty(aclPath) || Util.isNullOrEmpty(permission)) {
            throw new IllegalArgumentException("ACL path and permission must not be blank!");
        }

        boolean result = false;
        try {
            // get the ACL by the supplied path
            AccessControlListIF acl = AccessController.getAcl(aclPath);

            // if ACL found, check its permissions
            if (acl != null) {

                result = acl.checkPermission(userName, permission);
                //No, we do not log a very common and normal event.
                //if (!result) {
                //    logger.debug("User " + userName + " does not have permission " + permission + " in ACL \"" + aclPath + "\"");
                //}
            } else {
                logger.warn("ACL \"" + aclPath + "\" not found!");
            }
        } catch (SignOnException soe) {
            if (soe instanceof AclNotFoundException) {
                logger.warn("ACL \"" + aclPath + "\" not found!");
            } else {
                logger.error(soe.toString(), soe);
            }
        }

        return result;
    }

    /**
     * Returns the CR application URL defined in cr.proprties, such as http://cr.eionet.europa.eu.
     *
     * @return the URL.
     */
    private static String appHomeURL() {

        return GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
    }

    /**
     * Returns home URL of the user.
     *
     * @return String
     */
    public String getHomeUri() {
        return CRUser.homeUri(userName);
    }

    /**
     * Returns projects root URL.
     *
     * @return String
     */
    public String getProjectUri() {
        return CRUser.rootProjectUri();
    }

    /**
     * Returns review URL of the user.
     *
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
     *
     * @return String URI
     */
    public String getRegistrationsUri() {
        return CRUser.registrationsUri(userName);
    }

    /**
     * Bookmarks URI of the user.
     *
     * @return String URL
     */
    public String getBookmarksUri() {
        return CRUser.bookmarksUri(userName);
    }

    /**
     * History URI of the user.
     *
     * @return String URI
     */
    public String getHistoryUri() {
        return CRUser.historyUri(userName);
    }

    /**
     *
     * @return
     */
    public String getReviewsUri() {
        return CRUser.reviewsUri(userName);
    }

    /**
     * Home Item URI.
     *
     * @param uri String
     * @return String
     */
    public String getHomeItemUri(String uri) {
        return CRUser.homeItemUri(userName, uri);
    }

    /**
     * ROOT Home URI for all user folders.
     *
     * @return String
     */
    public static String rootHomeUri() {

        return new StringBuilder(appHomeURL()).append("/home").toString();
    }

    /**
     * ROOT Project URI for all project folders.
     *
     * @return String
     */
    public static String rootProjectUri() {

        return new StringBuilder(appHomeURL()).append("/project").toString();
    }

    /**
     * Home URI of the user.
     *
     * @param userName user name
     * @return String
     */
    public static String homeUri(String userName) {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("userName must not be blank");
        } else {
            return new StringBuilder(rootHomeUri()).append("/").append(userName).toString();
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
            return new StringBuilder(homeUri(userName)).append("/bookmarks/").append(Hashes.spoHash(uri)).toString();
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

    /**
     *
     * @param userName
     * @return
     */
    public static String reviewsUri(String userName) {

        return CRUser.homeUri(userName) + "/reviews";
    }

    /**
     *
     * @param uriString
     * @return
     */
    public static boolean isHomeUri(String uriString) {

        return uriString != null && uriString.startsWith(CRUser.rootHomeUri());
    }

    /**
     *
     * @param uriString
     * @return
     */
    public static String getUserNameFromUri(String uriString) {

        String userHomesPath = CRUser.rootHomeUri();
        if (!userHomesPath.endsWith("/")) {
            userHomesPath = userHomesPath + "/";
        }
        int userHomesPathLength = userHomesPath.length();

        String userName = null;

        if (uriString.startsWith(userHomesPath) && uriString.length() > userHomesPathLength) {

            int i = uriString.indexOf('/', userHomesPathLength);
            if (i > userHomesPathLength) {
                userName = uriString.substring(userHomesPathLength, i);
            }
        }

        return userName;
    }

    /**
     * @return the reservedFoldersAndFiles
     */
    public List<String> getReservedFolderAndFileUris() {

        // Lazy loading.
        if (reservedFolderAndFileUris == null) {

            reservedFolderAndFileUris = new ArrayList<String>();
            reservedFolderAndFileUris.add(getBookmarksUri());
            reservedFolderAndFileUris.add(getRegistrationsUri());
            reservedFolderAndFileUris.add(getHistoryUri());
            reservedFolderAndFileUris.add(getReviewsUri());
        }
        return reservedFolderAndFileUris;
    }

    /**
     *
     * @param userName
     * @return
     */
    public static List<String> getReservedFolderAndFileUris(String userName) {

        ArrayList<String> result = new ArrayList<String>();
        result.add(CRUser.bookmarksUri(userName));
        result.add(CRUser.registrationsUri(userName));
        result.add(CRUser.historyUri(userName));
        result.add(CRUser.reviewsUri(userName));
        return result;
    }

    /**
     * Creates default ACL's when a new user is logging in.
     *
     */
    public void createDefaultAcls() {

        // String aclPath, String owner, String description
        String mainUserAcl = "/home/" + getUserName();
        try {
            if (!AccessController.getAcls().containsKey(mainUserAcl)) {
                AccessController.addAcl(mainUserAcl, getUserName(), "Home folder for " + getUserName());
            }
            // TODO subfolders ACL's when DDC will be imple,mented in the acl mechanism
        } catch (SignOnException e) {
            // TODO Auto-generated catch block
            logger.error("Error creating ACL's " + e.toString());
        }
    }

    /**
     * Checks if user has the given privilege to the project.
     * @param session current HTTP session
     * @param projectName project which ACL is checked
     * @param privilege privilege to check
     * @return true, if user can add/delete shared SPARQL bookmars.
     */
    public boolean hasProjectPrivilege(HttpSession session, String projectName, String privilege) {
        return CRUser.hasPermission(session, "/project/" + projectName, privilege);
    }
}
