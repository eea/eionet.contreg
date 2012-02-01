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
package eionet.cr.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author altnyris
 *
 */
public class FolderUtil {

    /**
     *
     * @param uri
     * @return String
     */
    public static String extractProjectAclPath(String uri) {

        String aclPath = "/project";
        String path = extractPathInProjectFolder(uri);
        if (!StringUtils.isBlank(path)) {
            String[] tokens = path.split("/");
            if (tokens != null && tokens.length > 0) {
                aclPath = "/project/" + tokens[0];
            }
        }
        return aclPath;
    }

    /**
     *
     * @return String
     */
    public static String getProjectsFolder() {

        String appHome = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        return appHome + "/project";
    }


    /**
     * Detects if the given URI starts with a CR user home.
     *
     * @param uri
     *            The given URI.
     * @return See description above..
     */
    public static boolean startsWithUserHome(String uri) {

        if (StringUtils.isBlank(uri)) {
            return false;
        }

        String appHomeUrl = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        if (!uri.startsWith(appHomeUrl) || uri.equals(appHomeUrl)) {
            return false;
        }

        String afterAppHomeUrl = StringUtils.substringAfter(uri, appHomeUrl);
        String homeString = "/home/";
        return afterAppHomeUrl.startsWith(homeString) && afterAppHomeUrl.length() > homeString.length();
    }

    /**
     * Returns true if {@link URIUtil#startsWithUserHome(String)} true and it is a reserved URI. For resreved URIs, see
     * {@link CRUser#getReservedFolderAndFileUris(String)}. Otherwise returns false.
     *
     * @param uri
     *            The given URI.
     * @return See method description.
     */
    public static boolean isUserReservedUri(String uri) {

        String userName = extractUserName(uri);
        return !StringUtils.isBlank(userName) && CRUser.getReservedFolderAndFileUris(userName).contains(uri);
    }

    /**
     * Extracts user name from the given URI. A user name is returned only if the given URI returns true for
     * {@link URIUtil#startsWithUserHome(String)}. Otherwise null is returned.
     *
     * @param uri
     *            The given URI.
     * @return See method description.
     */
    public static String extractUserName(String uri) {

        if (!startsWithUserHome(uri)) {
            return null;
        }

        String str =
            StringUtils.substringAfter(uri, GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL) + "/home/");
        return StringUtils.substringBefore(str, "/");
    }

    /**
     * Returns the path after the user home folder. For example
     * if uri is "http://127.0.0.1:8080/cr/home/heinlja/newFolder/newFile.txt" the result is "newFolder/newFile.txt".
     * @param uri
     * @return
     */
    public static String extractPathInUserHome(String uri) {
        if (!startsWithUserHome(uri)) {
            return null;
        }

        String userName = extractUserName(uri);

        String result =
            StringUtils.substringAfter(uri, GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL) + "/home/"
                    + userName + "/");
        return result;
    }

    /**
     * Returns the path after the project folder. For example
     * if uri is "http://127.0.0.1:8080/cr/project/newFolder/newFile.txt" the result is "newFolder/newFile.txt".
     * @param uri
     * @return String
     */
    public static String extractPathInProjectFolder(String uri) {

        String result = null;
        String appHome = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        if (uri.startsWith(appHome + "/project/")) {
            result = StringUtils.substringAfter(uri, appHome + "/project/");
        }
        return result;
    }

    public static String extractPathInFolder(String uri) {

        if (uri == null) {
            return null;
        }

        String appHome = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        if (uri.startsWith(appHome + "/project/")) {
            return extractPathInProjectFolder(uri);
        } else if (startsWithUserHome(uri)) {
            return extractPathInUserHome(uri);
        } else {
            return null;
        }
    }

    /**
     * Return all folders where user can store data
     *
     * @param user
     * @return String
     */
    public static List<String> getUserAccessibleFolders(CRUser user) throws DAOException {

        List<String> folders = null;
        if (user != null) {
            folders = DAOFactory.get().getDao(FolderDAO.class).getSubFolders(user.getHomeUri());

            // Get project folders where user can insert content
            if (CRUser.hasPermission(user.getUserName(), "/project", "i")) {
                List<String> projectFolders = DAOFactory.get().getDao(FolderDAO.class).getSubFolders(FolderUtil.getProjectsFolder());
                if (projectFolders != null && projectFolders.size() > 0) {
                    for (String furi : projectFolders) {
                        String aclPath = FolderUtil.extractProjectAclPath(furi);
                        if (!StringUtils.isBlank(aclPath) && CRUser.hasPermission(user.getUserName(), aclPath, "i")) {
                            folders.add(furi);
                        }
                    }
                }
            }
        }
        return folders;
    }
}
