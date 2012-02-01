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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import com.tee.uit.security.AccessController;
import com.tee.uit.security.SignOnException;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dto.FolderItemDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.RenameFolderItemDTO;
import eionet.cr.dto.SpoBinaryDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.UploadHarvest;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Hashes;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Folder tab on factsheet page.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/folder.action")
public class FolderActionBean extends AbstractActionBean implements Runnable {

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

        initTabs();

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        Pair<FolderItemDTO, List<FolderItemDTO>> result = folderDAO.getFolderContents(uri);
        folder = result.getLeft();
        folderItems = result.getRight();

        aclPath = FolderUtil.extractProjectAclPath(uri);

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
     * @return
     * @throws DAOException
     */
    public Resolution rename() throws DAOException {
        if (!isUserLoggedIn()) {
            addSystemMessage("Only logged in users can rename files.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

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
                String newUri = uri + "/" + StringUtils.replace(item.getNewName(), " ", "%20");

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

        if (uriRenamings.size() > 0) {
            helperDAO.renameUserUploads(uriRenamings);
        }
        if (fileRenamings.size() > 0) {
            FileStore.getInstance(getUserName()).rename(fileRenamings);
            addSystemMessage("Files renamed successfully!");
        }
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    /**
     * Handles deletion event.
     *
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {
        if (!isUserLoggedIn()) {
            addSystemMessage("Only logged in users can delete files.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

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

        List<String> fileOrFolderUris = new ArrayList<String>();

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        FileStore fileStore = FileStore.getInstance(getUserName());

        // Delete folders
        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected() && FolderItemDTO.Type.FOLDER.equals(item.getType())) {

                boolean folderDeleted = fileStore.deleteFolder(FolderUtil.extractPathInFolder(item.getUri()));
                if (!folderDeleted) {
                    logger.warn("Failed to delete folder from filestore for uri: " + item.getUri());
                }
            }
            if (item.isSelected()
                    && (FolderItemDTO.Type.FILE.equals(item.getType()) || FolderItemDTO.Type.FOLDER.equals(item.getType()))) {
                fileOrFolderUris.add(item.getUri());
            }
        }

        // Delete files
        for (RenameFolderItemDTO item : selectedItems) {
            if (item.isSelected() && FolderItemDTO.Type.FILE.equals(item.getType())) {
                String filePath = FolderUtil.extractPathInUserHome(item.getUri());
                fileStore.delete(StringUtils.replace(filePath, "%20", " "));
            }
        }

        if (fileOrFolderUris.size() > 0) {
            folderDAO.deleteFileOrFolderUris(uri, fileOrFolderUris);
            DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(fileOrFolderUris);
        }

        addSystemMessage("Files/folders deleted successfully.");
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    /**
     * Create new folder.
     *
     * @return
     * @throws DAOException
     */
    public Resolution createFolder() throws DAOException {
        if (!isUserLoggedIn()) {
            addSystemMessage("Only logged in users can create folders.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        if (StringUtils.isEmpty(title)) {
            addCautionMessage("Folder name must be valued.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        if (folderDAO.fileOrFolderExists(uri, title)) {
            addCautionMessage("File or folder with the same name already exists.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
        }

        String context = getUser().getHomeUri();
        String appHome = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        if (uri.startsWith(appHome + "/project")) {
            context = appHome + "/project";
        }

        folderDAO.createFolder(uri, title, label, context);

        try {
            if (uri.startsWith(appHome + "/project")) {
                String path = FolderUtil.extractPathInProjectFolder(uri + "/" + title);
                if (!StringUtils.isBlank(path)) {
                    String[] tokens = path.split("/");
                    if (tokens != null && tokens.length == 1) {
                        String aclPath = "/project/" + tokens[0];
                        HashMap acls = AccessController.getAcls();
                        if (!acls.containsKey(aclPath)) {
                            AccessController.addAcl(aclPath, getUserName(), "");
                        }
                    }
                }
            }
        } catch (SignOnException e) {
            e.printStackTrace();
        }

        addSystemMessage("Folder created successfully.");
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", uri);
    }

    /**
     * Displays the upload form.
     *
     * @return
     * @throws DAOException
     */
    public Resolution uploadForm() throws DAOException {
        initTabs();
        return new ForwardResolution("/pages/folder/uploadFile.jsp");
    }

    public Resolution upload() throws DAOException, IOException {
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
        fileExists = DAOFactory.get().getDao(HelperDAO.class).isExistingSubject(fileUri);

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

        // prepare cr:hasFile predicate
        ObjectDTO objectDTO = new ObjectDTO(getUploadedFileSubjectUri(), false);
        objectDTO.setSourceUri(uri);
        SubjectDTO homeSubjectDTO = new SubjectDTO(uri, false);
        homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);

        // declare file subject DTO, set it to null for starters
        SubjectDTO fileSubjectDTO = null;

        // if title needs to be stored, add it to file subject DTO
        if (!fileExists || !StringUtils.isBlank(title)) {

            String titleToStore = title;
            if (StringUtils.isBlank(titleToStore)) {
                titleToStore = URIUtil.extractURILabel(getUploadedFileSubjectUri(), SubjectDTO.NO_LABEL);
                titleToStore = StringUtils.replace(titleToStore, "%20", " ");
            }

            objectDTO = new ObjectDTO(titleToStore, true);
            objectDTO.setSourceUri(uri);
            fileSubjectDTO = new SubjectDTO(getUploadedFileSubjectUri(), false);
            fileSubjectDTO.addObject(Predicates.RDFS_LABEL, objectDTO);
        }

        try {
            HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);

            // persist the prepared "userHome cr:hasFile fileSubject" triple
            logger.debug("Creating the cr:hasFile predicate");
            helperDao.addTriples(homeSubjectDTO);

            // store file subject DTO if it has been initialized
            if (fileSubjectDTO != null) {

                // delete previous value of dc:title if new one set
                if (fileExists && fileSubjectDTO.hasPredicate(Predicates.RDFS_LABEL)) {

                    List<String> subjectUris = Collections.singletonList(fileSubjectDTO.getUri());
                    List<String> predicateUris = Collections.singletonList(Predicates.RDFS_LABEL);
                    List<String> sourceUris = Collections.singletonList(uri);

                    helperDao.deleteSubjectPredicates(subjectUris, predicateUris, sourceUris);
                }
                helperDao.addTriples(fileSubjectDTO);
            }

            // since user's home URI was used above as triple source, add it to HARVEST_SOURCE too
            // (but set interval minutes to 0, to avoid it being background-harvested)
            DAOFactory.get().getDao(HarvestSourceDAO.class)
            .addSourceIgnoreDuplicate(HarvestSourceDTO.create(getUser().getHomeUri(), false, 0, getUserName()));

        } catch (DAOException e) {
            saveAndHarvestException = e;
            return;
        }

        // save the file's content into database
        try {
            saveContent();
            contentSaved = true;
        } catch (DAOException e) {
            saveAndHarvestException = e;
            return;
        } catch (IOException e) {
            saveAndHarvestException = e;
            return;
        }

        // attempt to harvest the uploaded file
        harvestUploadedFile(getUploadedFileSubjectUri(), uploadedFile, null, getUserName());
    }

    /**
     * Stores file data into filesystem and database.
     *
     * @throws DAOException
     * @throws IOException
     */
    private void saveContent() throws DAOException, IOException {

        logger.debug("Going to save the uploaded file's content into database");

        SpoBinaryDTO dto = new SpoBinaryDTO(Hashes.spoHash(getUploadedFileSubjectUri()));
        dto.setContentType(uploadedFile.getContentType());
        dto.setLanguage("");
        dto.setMustEmbed(false);

        InputStream contentStream = null;
        try {
            DAOFactory.get().getDao(SpoBinaryDAO.class).add(dto);
            contentStream = uploadedFile.getInputStream();
            String filePath = FolderUtil.extractPathInUserHome(uri);
            if (StringUtils.isNotEmpty(filePath)) {
                filePath += "/" + uploadedFile.getFileName();
            } else {
                filePath = uploadedFile.getFileName();
            }
            FileStore.getInstance(getUserName()).add(filePath, replaceExisting, contentStream);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
    }

    /**
     * Harvests file.
     *
     * @param sourceUrl
     * @param uploadedFile
     * @param dcTitle
     */
    protected void harvestUploadedFile(String sourceUrl, FileBean uploadedFile, String dcTitle, String userName) {

        // create and store harvest source for the above source url,
        // don't throw exceptions, as an uploaded file does not have to be
        // harvestable
        HarvestSourceDTO harvestSourceDTO = null;
        try {
            logger.debug("Creating and storing harvest source");
            HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(sourceUrl);
            source.setIntervalMinutes(0);

            dao.addSourceIgnoreDuplicate(source);
            harvestSourceDTO = dao.getHarvestSourceByUrl(sourceUrl);
        } catch (DAOException e) {
            logger.info("Exception when trying to create" + "harvest source for the uploaded file content", e);
        }

        // perform harvest,
        // don't throw exceptions, as an uploaded file does not HAVE to be
        // harvestable
        try {
            if (harvestSourceDTO != null) {
                UploadHarvest uploadHarvest = new UploadHarvest(harvestSourceDTO, uploadedFile, dcTitle, userName);
                CurrentHarvests.addOnDemandHarvest(harvestSourceDTO.getUrl(), userName);
                try {
                    uploadHarvest.execute();
                } finally {
                    CurrentHarvests.removeOnDemandHarvest(harvestSourceDTO.getUrl());
                }
            } else {
                logger.debug("Harvest source was not created, so skipping harvest");
            }
        } catch (HarvestException e) {
            logger.info("Exception when trying to harvest uploaded file content", e);
        }
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
     * True, if the folder uri is user's home folder.
     *
     * @return
     */
    public boolean isHomeFolder() {
        String appHome = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        if (!isProjectFolder() && uri.startsWith(appHome + "/home") && StringUtils.isEmpty(FolderUtil.extractPathInUserHome(uri))) {
            return true;
        }
        return false;
    }

    /**
     * True, if the folder uri is project root folder.
     *
     * @return
     */
    public boolean isProjectFolder() {
        String appHome = GeneralConfig.getRequiredProperty(GeneralConfig.APPLICATION_HOME_URL);
        if (uri.equals(appHome + "/project")) {
            return true;
        }
        return false;
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

}
