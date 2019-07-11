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
 *        Juhan Voolaid
 */

package eionet.cr.web.action.factsheet;

import eionet.acl.AccessController;
import eionet.acl.SignOnException;
import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.*;
import eionet.cr.dto.FolderItemDTO;
import eionet.cr.dto.RenameFolderItemDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.cleanup.FoldersAndFilesRestorer;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.home.HomesActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;
import net.sourceforge.stripes.action.*;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

/**
 * Folder tab on factsheet page.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/folder.action")
public class FolderActionBean extends AbstractActionBean implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolderActionBean.class);

    /** Temporary space placeholder for some parsing below. */
    private static final String TEMP_SPACE_REPLACEMENT = "---TEMP_SPACE_REPLACEMENT---";

    /** The current folder URI. */
    private String uri;

    /** Factsheet page tabs. */
    private List<TabElement> tabs;

    /** The folder. */
    private FolderItemDTO folder;

    /** Items that are listed in parent folder. */
    private List<FolderItemDTO> folderItems;

    /** Objects of selected folder items data. */
    private List<RenameFolderItemDTO> selectedItems;

    /** Objects with renaming data. */
    private List<RenameFolderItemDTO> renameItems;

    // File upload properties
    /** Title of newly uploaded file. */
    private String title;

    /** Label of newly uploaded file. */
    private String label;

    /** ACL path. */
    private String aclPath;

    /** Uploaded file. */
    private FileBean uploadedFile;

    /** True, if newly uploaded file should replace the existing one. */
    private boolean replaceExisting;

    /** Exception that is thrown in file upload/harvest thread. */
    private Exception saveAndHarvestException = null;

    /** True, if file exists with the same name as currently uploaded file. */
    private boolean fileExists;

    /** If uploaded file's content is saved. */
    private boolean contentSaved;

    /**
     * Action event for displaying folder contents.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {
        aclPath = FolderUtil.extractAclPath(uri);

        // allow to view the folder if there is no ACL
        boolean allowFolderView = CRUser.hasPermission(aclPath, getUser(), CRUser.VIEW_PERMISSION, true);

        if (!allowFolderView) {
            addSystemMessage("Viewing content of this folder is prohibited.");
            String redirectUri = StringUtils.substringBeforeLast(uri, "/");
            if (FolderUtil.isHomeFolder(redirectUri)) {
                return new RedirectResolution(HomesActionBean.class);
            } else {
                return new RedirectResolution(FolderActionBean.class).addParameter("uri", redirectUri);
            }
        }
        initTabs();
        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        Pair<FolderItemDTO, List<FolderItemDTO>> result = folderDAO.getFolderContents(uri);
        folder = result.getLeft();
        folderItems = result.getRight();

        return new ForwardResolution("/pages/folder/viewItems.jsp");
    }

    /**
     * Displays the renaming form.
     *
     * @return
     * @throws DAOException
     */
    public Resolution renameForm() throws DAOException {

        if (!isUserLoggedIn()) {
            addSystemMessage("Only logged in users can rename files.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        // When returning from submit handler, renameItems are valued
        if (renameItems == null) {
            if (itemsNotSelected()) {
                addSystemMessage("Select files or folders to rename.");
                return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
            }

            String check = selectedItemsEmpty();
            if (check != null) {
                addSystemMessage("Cannot rename. Folder is not empty: " + check);
                return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
            }
            check = selectedItemsReserved();
            if (check != null) {
                addSystemMessage("Cannot rename. File or folder is reserved: " + check);
                return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
            }

            renameItems = new ArrayList<RenameFolderItemDTO>();

            for (RenameFolderItemDTO item : selectedItems) {
                if (item.isSelected()) {
                    renameItems.add(item);
                }
            }
        }

        initTabs();
        return new ForwardResolution("/pages/folder/renameItems.jsp");
    }

    /**
     * Handles the renaming event.
     *
     * @return stripes resolution
     * @throws DAOException if renam in the repositry fails
     * @throws SignOnException if rename ACLs fails
     */
    public Resolution rename() throws DAOException, SignOnException {
        aclPath = FolderUtil.extractAclPath(uri);

        // Rename files
        Set<String> uniqueNewNames = new HashSet<String>();
        HashMap<String, String> uriRenamings = new HashMap<String, String>();
        HashMap<String, String> fileRenamings = new HashMap<String, String>();

        HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        for (RenameFolderItemDTO item : renameItems) {
            if (FolderUtil.isUserReservedUri(item.getUri())) {
                addSystemMessage("Cannot rename items. File or folder is reserved: " + item.getName());
                return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
            }
            if (FolderItemDTO.Type.FOLDER.equals(item.getType())) {
                if (folderDAO.folderHasItems(item.getUri())) {
                    addSystemMessage("Cannot rename items. Folder is not empty: " + item.getName());
                    return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
                }
            }

            if (StringUtils.isNotEmpty(item.getNewName())) {
                String oldUri = item.getUri();

                String newUri = uri + "/";
                String newName = StringUtils.replace(item.getNewName(), " ", TEMP_SPACE_REPLACEMENT);
                try {
                    newName = URLEncoder.encode(newName, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                newName = StringUtils.replace(newName, TEMP_SPACE_REPLACEMENT, "%20");

                newUri += newName;

                if (!uniqueNewNames.add(item.getNewName())) {
                    addSystemMessage("Cannot name multiple items with the same name: " + item.getNewName());
                    return renameForm();
                }

                if (helperDAO.isExistingSubject(newUri)) {
                    addSystemMessage("Object with such a name already exists in system: " + item.getNewName());
                    return renameForm();
                }

                if (!oldUri.equals(newUri)) {
                    uriRenamings.put(oldUri, newUri);
                }

                if (FolderItemDTO.Type.FILE.equals(item.getType())) {
                    String oldFileName = StringUtils.replace(URIUtil.extractURILabel(item.getUri()), "%20", " ");
                    String newFileName = item.getNewName();

                    String folderPath = FolderUtil.extractPathInUserHome(uri);
                    if (StringUtils.isNotEmpty(folderPath)) {
                        oldFileName = folderPath + "/" + oldFileName;
                        newFileName = folderPath + "/" + newFileName;
                    }

                    if (!newFileName.equals(oldFileName)) {
                        fileRenamings.put(oldFileName, newFileName);
                    }
                }
            }
        }

        String check = renameableItemsHaveUpdatePermission();
        if (check != null) {
            addSystemMessage("Not authorized to rename items: " + check);
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }
        if (uriRenamings.size() > 0) {
            helperDAO.renameUserUploads(uriRenamings);
            renameAcls(uriRenamings);
        }
        if (fileRenamings.size() > 0) {

            FileStore.getInstance(FolderUtil.getUserDir(uri, getUserNameOrAnonymous())).rename(fileRenamings);
            addSystemMessage("Files renamed successfully!");
        }
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    /**
     * Handles deletion event.
     *
     * @return Stripes resolution
     * @throws DAOException if delete files and filders fails in RDF store
     * @throws SignOnException if deleting ACL fails
     */
    public Resolution delete() throws DAOException, SignOnException {

        aclPath = FolderUtil.extractAclPath(uri);

        if (itemsNotSelected()) {
            addSystemMessage("Select files or folders to delete.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        String check = selectedItemsEmpty();
        if (check != null) {
            addSystemMessage("Cannot delete. Folder is not empty: " + check);
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }
        check = selectedItemsReserved();
        if (check != null) {
            addSystemMessage("Cannot delete. File or folder is reserved: " + check);
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        check = selectedItemsHaveDeletePermission();
        if (check != null) {
            addSystemMessage("Cannot delete. Not authorized to delete: " + check);
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        List<String> fileOrFolderUris = new ArrayList<String>();

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);

        FileStore fileStore = FileStore.getInstance(FolderUtil.getUserDir(uri, getUserNameOrAnonymous()));

        // Delete folders.
        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected() && FolderItemDTO.Type.FOLDER.equals(item.getType())) {

                boolean folderDeleted = fileStore.deleteFolder(FolderUtil.extractPathInFolder(item.getUri()));
                if (!folderDeleted) {
                    LOGGER.warn("Failed to delete folder from filestore for uri: " + item.getUri());
                }
            }
            if (item.isSelected()
                    && (FolderItemDTO.Type.FILE.equals(item.getType()) || FolderItemDTO.Type.FOLDER.equals(item.getType()))) {
                fileOrFolderUris.add(item.getUri());
            }
        }

        // Delete files.
        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected() && FolderItemDTO.Type.FILE.equals(item.getType())) {
                String filePath = FolderUtil.extractPathInFolder(item.getUri());
                fileStore.delete(StringUtils.replace(filePath, "%20", " "));
            }
        }

        if (fileOrFolderUris.size() > 0) {
            folderDAO.deleteFileOrFolderUris(FolderUtil.folderContext(uri), fileOrFolderUris);
            DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(fileOrFolderUris);
            deleteAcls(fileOrFolderUris);
        }

        addSystemMessage("Files/folders deleted successfully.");
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    /**
     * Create new folder.
     *
     * @return Resolution
     * @throws DAOException
     *             if DAO method execution fails
     * @throws SignOnException
     *             if adding ACL to the DB fails
     * @throws URIException
     * @throws UnsupportedEncodingException
     */
    public Resolution createFolder() throws DAOException, SignOnException, URIException, UnsupportedEncodingException {

        aclPath = FolderUtil.extractAclPath(uri);

        // Check if user has permission to add entries in the parent.
        boolean actionAllowed = CRUser.hasPermission(aclPath, getUser(), CRUser.INSERT_PERMISSION, false);
        if (!actionAllowed) {
            addSystemMessage("No permission to add folder.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        if (StringUtils.isEmpty(title)) {
            addCautionMessage("Folder name must be valued.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        title = StringUtils.replace(title, " ", TEMP_SPACE_REPLACEMENT);
        title = URLEncoder.encode(title, "UTF-8");

        String titleSpacesUnescaped = StringUtils.replace(title, TEMP_SPACE_REPLACEMENT, " ");
        title = StringUtils.replace(title, TEMP_SPACE_REPLACEMENT, "%20");

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        if (folderDAO.fileOrFolderExists(uri, title)) {
            addCautionMessage("File or folder with the same name already exists.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        String context = FolderUtil.folderContext(uri);

        folderDAO.createFolder(uri, title, label, context);
        AccessController.addAcl(aclPath + "/" + titleSpacesUnescaped, getUserName(), titleSpacesUnescaped, true);

        if (FolderUtil.isProjectRootFolder(uri)) {
            AccessController.addAcl(aclPath + "/" + titleSpacesUnescaped + "/bookmarks", getUserName(), "Bookmarks for "
                    + titleSpacesUnescaped, true);
        }

        addSystemMessage("Folder created successfully.");
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    /**
     * Displays the upload form.
     *
     * @return
     * @throws DAOException
     * @throws SignOnException
     */
    public Resolution uploadForm() throws DAOException, SignOnException {
        if (!hasPermission(uri, CRUser.INSERT_PERMISSION)) {
            addSystemMessage("No permission to upload file.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        initTabs();
        return new ForwardResolution("/pages/folder/uploadFile.jsp");
    }

    /**
     * Upload file to CR folder.
     *
     * @return Stripes resolution
     * @throws DAOException if harvesting or any other DB operation fails
     * @throws IOException if I/O error
     * @throws SignOnException if creating ACL fails
     */
    public Resolution upload() throws DAOException, IOException, SignOnException {
        aclPath = FolderUtil.extractAclPath(uri);
        boolean actionAllowed =
                CRUser.hasPermission(aclPath, getUser(), replaceExisting ? CRUser.UPDATE_PERMISSION : CRUser.INSERT_PERMISSION,
                        false);
        if (!actionAllowed) {
            addWarningMessage("Not authorized to upload files");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }
        if (uploadedFile == null) {
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        // if file content is empty (e.f. 0 KB file), no point in continuing
        if (uploadedFile.getSize() <= 0) {
            addWarningMessage("The file must not be empty!");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        String fileUri = getUploadedFileSubjectUri();

        // check if the file exists
        fileExists = DAOFactory.get().getDao(FolderDAO.class).fileOrFolderExists(fileUri);

        // if file exists and replace not requested, report a warning
        if (!replaceExisting && fileExists) {
            addWarningMessage("A file with such a name already exists! Use \"replace existing\" checkbox to overwrite.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        if (fileUri != null && (CurrentHarvests.contains(fileUri) || UrgentHarvestQueue.isInQueue(fileUri))) {
            addWarningMessage("A file with such name is currently being harvested!");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        // save the file's content and try to harvest it
        saveAndHarvest();

        // redirect to the uploads list
        // String urlBinding = getUrlBinding();
        // resolution = new RedirectResolution(StringUtils.replace(urlBinding, "{username}", getUserName()));

        // add file ACL if not existing
        if (!replaceExisting) {
            AccessController.addAcl(aclPath + "/" + uploadedFile.getFileName(), getUserName(), "");
        }

        addSystemMessage("File successfully uploaded.");
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    private void saveAndHarvest() throws IOException, DAOException {
        // start the thread that saves the file's content and attempts to harvest it
        Thread thread = new Thread(this);
        thread.start();

        // check the thread after every second, exit loop if it hasn't finished in 15 seconds
        for (int loopCount = 0; thread.isAlive() && loopCount < 15; loopCount++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new CRRuntimeException(e.toString(), e);
            }
        }

        // if the the thread reported an exception, throw it
        if (saveAndHarvestException != null) {
            if (saveAndHarvestException instanceof DAOException) {
                throw (DAOException) saveAndHarvestException;
            } else if (saveAndHarvestException instanceof IOException) {
                throw (IOException) saveAndHarvestException;
            } else if (saveAndHarvestException instanceof RuntimeException) {
                throw (RuntimeException) saveAndHarvestException;
            } else {
                throw new CRRuntimeException(saveAndHarvestException.getMessage(), saveAndHarvestException);
            }
        }

        // add feedback message to the bean's context
        if (!thread.isAlive()) {
            addSystemMessage("File saved and harvested!");
        } else {
            if (!contentSaved) {
                addSystemMessage("Saving and harvesting the file continues in the background!");
            } else {
                addSystemMessage("File content saved, but harvest continues in the background!");
            }
        }
    }

    /**
     * File upload thread run method.
     */
    @Override
    public void run() {

        try {
            runBody();
        } catch (RuntimeException e) {
            if (saveAndHarvestException == null) {
                saveAndHarvestException = e;
            }
            throw e;
        } catch (Error e) {
            if (saveAndHarvestException == null) {
                saveAndHarvestException = new CRRuntimeException(e);
            }
            throw e;
        }
    }

    /**
     * Run method body.
     */
    private void runBody() {

        // at this stage, the uploaded file must not be null
        if (uploadedFile == null) {
            throw new CRRuntimeException("Uploaded file object must not be null");
        }

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        try {
            folderDAO.createFileSubject(uri, getUploadedFileSubjectUri(), title, getUserNameOrAnonymous(), fileExists);
        } catch (DAOException e) {
            saveAndHarvestException = e;
            return;
        }

        // save the file's content into database
        File file = null;
        try {
            LOGGER.debug("Going to save the uploaded file's content into database");
            file = folderDAO.saveFileContent(
                    uri, getUploadedFileSubjectUri(), uploadedFile, getUserNameOrAnonymous(), replaceExisting);
            contentSaved = true;
        } catch (DAOException e) {
            saveAndHarvestException = e;
            return;
        } catch (IOException e) {
            saveAndHarvestException = e;
            return;
        }

        // attempt to harvest the uploaded file
        folderDAO.harvestUploadedFile(
                getUploadedFileSubjectUri(), file, null, getUserNameOrAnonymous(), uploadedFile.getContentType());
    }

    private String getUploadedFileSubjectUri() {
        return uri + "/" + StringUtils.replace(uploadedFile.getFileName(), " ", "%20");
    }

    /**
     * Initializes tabs.
     *
     * @throws DAOException
     */
    private void initTabs() throws DAOException {
        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
            SubjectDTO subject = helperDAO.getFactsheet(uri, null, null);

            FactsheetTabMenuHelper helper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
            tabs = helper.getTabs(FactsheetTabMenuHelper.TabTitle.FOLDER);
        }
    }

    /**
     * Returns null, if all the folders from the items are empty. Folder name, if it is not.
     *
     * @return
     * @throws DAOException
     */
    private String selectedItemsEmpty() throws DAOException {
        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);

        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected() && FolderItemDTO.Type.FOLDER.equals(item.getType())) {
                boolean hasItems = folderDAO.folderHasItems(item.getUri());
                if (hasItems) {
                    return item.getName();
                }
            }
        }

        return null;
    }

    /**
     * Returns null, if all the selected items are not reserved. Returns file or folder name, if it is reserved.
     *
     * @return
     * @throws DAOException
     */
    private String selectedItemsReserved() throws DAOException {
        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected() && FolderUtil.isUserReservedUri(uri)) {
                return item.getName();
            }
        }

        return null;
    }

    /**
     * checks selected items delete permission.
     *
     * @return list of declined items
     * @throws SignOnException if acl check fails
     */
    private String selectedItemsHaveDeletePermission() throws SignOnException {
        StringBuilder result = new StringBuilder();
        for (RenameFolderItemDTO item : selectedItems) {
            String acl = aclPath + "/" + item.getName();
            if (item.isSelected() && AccessController.getAcls().containsKey(acl)
                    && !CRUser.hasPermission(getUserName(), acl, CRUser.DELETE_PERMISSION)) {
                result.append(item.getName()).append(" ");
            }

        }

        return StringUtils.isEmpty(result.toString()) ? null : result.toString();

    }

    /**
     * checks if items selected to be renamed have update permission.
     *
     * @return list of declined items
     * @throws SignOnException if acl check fails
     */
    private String renameableItemsHaveUpdatePermission() throws SignOnException {
        StringBuilder result = new StringBuilder();
        for (RenameFolderItemDTO item : renameItems) {
            String acl = aclPath + "/" + item.getName();
            if (item.isSelected() && AccessController.getAcls().containsKey(acl)
                    && !CRUser.hasPermission(getUserName(), acl, CRUser.UPDATE_PERMISSION)) {
                result.append(item.getName()).append(" ");
            }
        }

        return StringUtils.isEmpty(result.toString()) ? null : result.toString();
    }

    /**
     * Returns true, if none of the items are marked selected.
     *
     * @return
     */
    private boolean itemsNotSelected() {
        if (selectedItems == null) {
            return true;
        }
        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected()) {
                return false;
            }
        }
        return true;
    }

    /**
     * True, if currently logged in user is viewing his home folder or one of sub folders.
     *
     * @return
     */
    public boolean isUsersFolder() {
        if (isUserLoggedIn()) {
            String homeUri = CRUser.homeUri(getUserName());
            if (uri.startsWith(homeUri)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Renames corresponding ACLs of the renamed folders.
     *
     * @param renamings
     *            renamings hash keys: old urls, values: new urls
     */
    private void renameAcls(HashMap<String, String> renamings) {
        String appHome = GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL);
        for (String oldUri : renamings.keySet()) {
            String path = StringUtils.substringAfter(oldUri, appHome);
            try {
                if (AccessController.getAcls().containsKey(path)) {
                    String newAclPath = StringUtils.substringAfter(renamings.get(oldUri), appHome);
                    AccessController.renameAcl(path, newAclPath);
                }
            } catch (SignOnException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deletes corresponding ACLs of the deleted items.
     *
     * @param itemUris URIs of items whose ACLs are to be deleted.
     * @throws SignOnException If problem with deleting ACLs.
     */
    @SuppressWarnings("rawtypes")
    private void deleteAcls(List<String> itemUris) throws SignOnException {

        for (String uri : itemUris) {

            String aclPath = FolderUtil.extractAclPath(uri);
            HashMap acls = AccessController.getAcls();
            if (acls.containsKey(aclPath)) {
                AccessController.removeAcl(aclPath);
            } else {
                // ACL names do NOT actually have the spaces escaped.
                aclPath = StringUtils.replace(aclPath, "%20", " ");
                if (acls.containsKey(aclPath)) {
                    AccessController.removeAcl(aclPath);
                }
            }
        }
    }

    /**
     * Returns current username or "anonymous" if not authenticated. Can be used if username needs to be stored and anonymous user
     * has been given permissions
     *
     * @return username or anonymous
     */
    private String getUserNameOrAnonymous() {
        if (getUser() != null) {
            return getUserName();
        }

        return "anonymous";
    }

    /**
     * True, if the folder uri is user's home folder.
     *
     * @return
     */
    public boolean isHomeFolder() {
        return FolderUtil.isHomeFolder(uri);
    }

    /**
     * True, if the folder uri is project root folder.
     *
     * @return
     */
    public boolean isProjectFolder() {
        return FolderUtil.isProjectRootFolder(uri);
    }

    /**
     * Returns parent folder uri. If it is home folder uri, current uri is returned.
     *
     * @return
     */
    public String getParentUri() {
        if (isHomeFolder()) {
            return uri;
        }
        return StringUtils.substringBeforeLast(uri, "/");
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the folder
     */
    public FolderItemDTO getFolder() {
        return folder;
    }

    /**
     * @return the folderItems
     */
    public List<FolderItemDTO> getFolderItems() {
        return folderItems;
    }

    /**
     * @param folderItems
     *            the folderItems to set
     */
    public void setFolderItems(List<FolderItemDTO> folderItems) {
        this.folderItems = folderItems;
    }

    /**
     * @return the selectedItems
     */
    public List<RenameFolderItemDTO> getSelectedItems() {
        return selectedItems;
    }

    /**
     * @param selectedItems
     *            the selectedItems to set
     */
    public void setSelectedItems(List<RenameFolderItemDTO> selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the uploadedFile
     */
    public FileBean getUploadedFile() {
        return uploadedFile;
    }

    /**
     * @param uploadedFile
     *            the uploadedFile to set
     */
    public void setUploadedFile(FileBean uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    /**
     * @return the replaceExisting
     */
    public boolean isReplaceExisting() {
        return replaceExisting;
    }

    /**
     * @param replaceExisting
     *            the replaceExisting to set
     */
    public void setReplaceExisting(boolean replaceExisting) {
        this.replaceExisting = replaceExisting;
    }

    /**
     * @return the renameItems
     */
    public List<RenameFolderItemDTO> getRenameItems() {
        return renameItems;
    }

    /**
     * @param renameItems
     *            the renameItems to set
     */
    public void setRenameItems(List<RenameFolderItemDTO> renameItems) {
        this.renameItems = renameItems;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    public String getAclPath() {
        return aclPath;
    }

    /**
     * checks if user has permission for this acl.
     *
     * @param folderUri folder full URI
     * @param permission permission to check
     * @return tru if user has permission
     * @throws SignOnException if check fails
     */
    private boolean hasPermission(String folderUri, String permission) throws SignOnException {
        String acl = FolderUtil.extractAclPath(folderUri);
        return CRUser.hasPermission(getUserName(), acl, permission);
    }

    /**
     *
     * @return
     */
    public Resolution restoreFoldersAndFiles() throws DAOException, SignOnException {

        FoldersAndFilesRestorer restorer = new FoldersAndFilesRestorer(getUser());
        restorer.restore();

        addSystemMessage("Restoration done! Created " +
                restorer.getCreatedFoldersCount() + " folders and " + restorer.getCreatedFilesCount() + " files!");
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", FolderUtil.getProjectsFolder());
    }
}
