package eionet.cr.util.cleanup;

import eionet.acl.AccessController;
import eionet.acl.SignOnException;
import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.filestore.FileStore;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Pair;
import eionet.cr.web.security.CRUser;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FoldersAndFilesRestorer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FoldersAndFilesRestorer.class);

    private static final File FOLDERS_ROOT_PATH = new File(FileStore.PATH, "project");
    private static final String FOLDERS_ROOT_URI = GeneralConfig.getRequiredProperty(
            GeneralConfig.APPLICATION_HOME_URL) + "/project";

    private static final String SAFE_SPACE = "*SPACE*";
    private static final String SAFE_SLASH = "*SLASH*";

    private List<File> foldersAndFiles = new ArrayList<>();
    private CRUser crUser;
    private List<Pair<String, File>> filesToHarvest = new ArrayList<>();

    private int createdFoldersCount;
    private int createdFilesCount;

    public FoldersAndFilesRestorer(CRUser crUser) {

        if (crUser == null) {
            throw new IllegalArgumentException("User object must not be null!");
        }

        this.crUser = crUser;
    }

    public void restore() throws DAOException, SignOnException {

        File foldersRootPath = new File(FileStore.PATH, "project");
        traverse(foldersRootPath, true);

        for (File folderOrFile : foldersAndFiles) {
            createFolderOrFile(folderOrFile);
        }

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        for (Pair<String, File> filePair : filesToHarvest) {

            String fileUri = filePair.getLeft();
            File file = filePair.getRight();
            LOGGER.debug("Scheduling file for harvest: " + fileUri);
            folderDAO.harvestUploadedFile(fileUri, file, file.getName(), crUser.getUserName(), null);
        }
    }

    private void createFolderOrFile(File folderOrFile) throws DAOException, SignOnException {

        String uri = getFolderOrFileUri(folderOrFile);
        String parentUri = getFolderOrFileUri(folderOrFile.getParentFile());

        if (folderOrFile.isDirectory()) {
            createFolder(folderOrFile, uri, parentUri);
        } else {
            createFile(folderOrFile, uri, parentUri);
        }
    }

    private void createFolder(File folder, String folderUri, String parentUri) throws DAOException, SignOnException {

        String folderName = folder.getName();
        String uriLegalFolderName = fileOrFolderNameToURILegal(folderName);

        LOGGER.debug("Checking folder \"{}\" in folder {}", folderName, parentUri);

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        if (folderDAO.fileOrFolderExists(parentUri, uriLegalFolderName)) {
            LOGGER.debug("Folder \"{}\" already exists in folder {}", folderName, parentUri);
            return;
        }

        String context = FolderUtil.folderContext(parentUri);
        folderDAO.createFolder(parentUri, uriLegalFolderName, "", context);

        String aclPath = FolderUtil.extractAclPath(parentUri);
        String aclName = aclPath + "/" + folderName;

        if (!AccessController.aclExists(aclName)) {
            AccessController.addAcl(aclName, crUser.getUserName(), folderName, true);
        }

        if (FolderUtil.isProjectRootFolder(parentUri)) {
            aclName = aclPath + "/" + folderName + "/bookmarks";
            if (!AccessController.aclExists(aclName)) {
                AccessController.addAcl(aclName, crUser.getUserName(), "Bookmarks for "
                        + folderName, true);
            }
        }

        createdFoldersCount++;
        LOGGER.debug("Folder \"{}\" successfully created in folder {}", folderName, parentUri);
    }

    private void createFile(File file, String fileUri, String parentUri) throws DAOException, SignOnException {

        String fileName = file.getName();

        LOGGER.debug("Checking file \"{}\" in folder {}", fileName, parentUri);

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        if (folderDAO.fileOrFolderExists(fileUri)) {
            LOGGER.debug("File \"{}\" already exists in folder {}", fileName, parentUri);
            return;
        }

        folderDAO.createFileSubject(parentUri, fileUri, fileName, crUser.getUserName(), false);
        filesToHarvest.add(new Pair<>(fileUri, file));

        String aclPath = FolderUtil.extractAclPath(parentUri);
        String aclName = aclPath + "/" + fileName;
        if (!AccessController.aclExists(aclName)) {
            AccessController.addAcl(aclName, crUser.getUserName(), "");
        }

        createdFilesCount++;
        LOGGER.debug("File \"{}\" successfully created in folder {}", fileName, parentUri);
    }

    private void traverse(File item, boolean isFoldersRoot) {

        if (item.isDirectory()) {

            if (!isFoldersRoot) {
                foldersAndFiles.add(item);
            }

            for (File child : item.listFiles()) {
                traverse(child, false);
            }
        } else {
            foldersAndFiles.add(item);
        }
    }

    private String getPathRelativeToFoldersRoot(File file) {
        return StringUtils.substringAfter(file.getAbsolutePath(), FOLDERS_ROOT_PATH.getAbsolutePath()).replace('\\', '/');
    }

    private String getFolderOrFileUri(File folderOrFile) {

        String relativePath = getPathRelativeToFoldersRoot(folderOrFile);
        return FOLDERS_ROOT_URI + pathToURILegal(relativePath);
    }

    private String pathToURILegal(String path) {
        try {
            String safeStr = path.replace(" ", SAFE_SPACE).replace("/", SAFE_SLASH);
            String encodedStr = URLEncoder.encode(safeStr, "UTF-8");
            String finalStr = encodedStr.replace(SAFE_SLASH, "/").replace(SAFE_SPACE, "%20");
            return finalStr;
        } catch (UnsupportedEncodingException e) {
            throw new CRRuntimeException("Unexpected " + UnsupportedEncodingException.class.getSimpleName(), e);
        }
    }

    private String fileOrFolderNameToURILegal(String fileOrFolderName) {
        try {
            String safeStr = fileOrFolderName.replace(" ", SAFE_SPACE);
            String encodedStr = URLEncoder.encode(safeStr, "UTF-8");
            String finalStr = encodedStr.replace(SAFE_SPACE, "%20");
            return finalStr;
        } catch (UnsupportedEncodingException e) {
            throw new CRRuntimeException("Unexpected " + UnsupportedEncodingException.class.getSimpleName(), e);
        }
    }

    public int getCreatedFoldersCount() {
        return createdFoldersCount;
    }

    public int getCreatedFilesCount() {
        return createdFilesCount;
    }
}
