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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.FolderItemDTO;
import eionet.cr.util.Pair;

/**
 * Folder DAO.
 *
 * @author Jaanus Heinlaid
 */
public interface FolderDAO extends DAO {

    /**
     * Creates home folder for the given user name. The latter must not be null or blank! Creates also all reserved folders under
     * the newly created home folder.
     *
     * @param userName Given user name
     * @throws DAOException Thrown when a database-access error occurs.
     */
    void createUserHomeFolder(String userName) throws DAOException;

    /**
     * Creates a new folder in the given parent folder. Both given parameters must not be null or blank.
     *
     * @param parentFolderUri - URI of the new folder's parent folder.
     * @param folderName - The new folder's name.
     * @param folderLabel - The logical folder's name.
     * @param homeUri - User home URI.
     * @throws DAOException - Thrown when a database-access error occurs.
     */
    void createFolder(String parentFolderUri, String folderName, String folderLabel, String homeUri) throws DAOException;

    /**
     * Returns true if a folder or file with the given name exists in the given parent folder. If it doesn't exist, returns false.
     * Both given parameters must not be null or blank.
     *
     * @param parentFolderUri The given parent folder URI.
     * @param folderName The given folder name.
     *
     * @return See description above.
     * @throws DAOException
     */
    boolean fileOrFolderExists(String parentFolderUri, String folderName) throws DAOException;

    /**
     * Returns true if a folder or file with the given URI exists, otherwise returns false. The given URI must not be null.
     *
     * @param folderUri The given folder URI.
     *
     * @return See description above.
     * @throws DAOException
     */
    boolean fileOrFolderExists(String folderUri) throws DAOException;

    /**
     * Returns all sub folders + parent folder.
     *
     * @param uri - Parent folder uri.
     * @return List<String>.
     * @throws DAOException
     */
    List<String> getSubFolders(String uri) throws DAOException;

    /**
     * True, if folder is not empty and has files or folders.
     *
     * @param folderUri The given folder URI.
     *
     * @return
     * @throws DAOException
     */
    boolean folderHasItems(String folderUri) throws DAOException;

    /**
     * Deletes uploaded files/folders data.
     *
     * @param folderUri
     * @param subjectUris
     * @throws DAOException
     */
    void deleteFileOrFolderUris(String folderUri, List<String> subjectUris) throws DAOException;

    /**
     * Returns the contents (files and folders) of the folder with given uri. The Pair.left is the current folder and Pair.right is
     * sorted collection of folder contents.
     *
     * @param uri
     * @return
     * @throws DAOException
     */
    Pair<FolderItemDTO, List<FolderItemDTO>> getFolderContents(String uri) throws DAOException;

    /**
     * Create a bookmarks folder for the project.
     * @param projectName project name
     * @throws DAOException if creating fails
     */
    void createProjectBookmarksFolder(String projectName) throws DAOException;

}
