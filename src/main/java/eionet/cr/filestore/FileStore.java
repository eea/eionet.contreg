package eionet.cr.filestore;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.security.CRUser;
import net.sourceforge.stripes.action.FileBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URLDecoder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * A utility class representing CR local file store.
 *
 * @author jaanus
 *
 */
public final class FileStore {

    /** Static logger for this class */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStore.class);

    /** File store path in file-system. */
    public static final String PATH = GeneralConfig.getRequiredProperty(GeneralConfig.FILESTORE_PATH);

    /** Current user home folder. */
    private final File userDir;

    /**
     * Instantiates a new file store.
     *
     * @param userName the user name
     */
    private FileStore(String userName) {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("userName must not be null or blank");
        }

        userDir = new File(FileStore.PATH, userName);
    }

    /**
     *
     * @param userName
     * @return FileStore
     */
    public static FileStore getInstance(String userName) {
        return new FileStore(userName);
    }

    /**
     *
     * @param filePath
     *            file folder path with file name
     * @param overwrite
     * @param inputStream
     * @return File
     * @throws IOException
     */
    public File add(String filePath, boolean overwrite, InputStream inputStream) throws IOException {

        File path = prepareFileWrite(filePath, overwrite);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(path);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return path;
    }

    /**
     *
     * @param filePath
     * @param overwrite
     * @param fileBean
     * @return
     * @throws IOException
     */
    public File addByMoving(String filePath, boolean overwrite, FileBean fileBean) throws IOException {

        File path = prepareFileWrite(filePath, overwrite);
        fileBean.save(path);
        return path;
    }

    /**
     *
     * @param fileName
     * @param overwrite
     * @param reader
     * @return File
     * @throws IOException
     */
    public File add(String fileName, boolean overwrite, Reader reader) throws IOException {

        File filePath = prepareFileWrite(fileName, overwrite);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            IOUtils.copy(reader, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return null;
    }

    /**
     * Returns file object of the uploaded file in the file system.
     *
     * @param filePath
     *            file folder path with file name
     * @param overwrite
     * @throws FileAlreadyExistsException
     */
    protected File prepareFileWrite(String filePath, boolean overwrite) throws FileAlreadyExistsException {

        String folderStr;
        String fileName;

        if (StringUtils.contains(filePath, "/")) {
            String fileFolder = StringUtils.substringBeforeLast(filePath, "/");
            folderStr = userDir.getAbsolutePath() + "/" + fileFolder;
            fileName = StringUtils.substringAfterLast(filePath, "/");
        } else {
            folderStr = userDir.getAbsolutePath();
            fileName = filePath;
        }

        File dir = new File(folderStr);

        if (!dir.exists() || !dir.isDirectory()) {
            // creates the directory, including any necessary but nonexistent parent directories
            try {
                FileUtils.forceMkdir(dir);
            } catch (IOException e) {
                LOGGER.error("Failed to create folder: " + dir.getAbsolutePath(), e);
            }
        }

        File path = new File(dir, fileName);
        if (path.exists() && path.isFile()) {

            if (overwrite == false) {
                throw new FileAlreadyExistsException("File already exists: " + path);
            } else {
                path.delete();
            }
        }

        return path;
    }

    /**
     *
     * @param filePath
     */
    public void delete(String filePath) {

        File file = new File(userDir, filePath);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    /**
     * True, if folder is deleted.
     *
     * @param folderPath
     * @return
     */
    public boolean deleteFolder(String folderPath) {
        return deleteFolder(folderPath, false);
    }

    /**
     * True, if folder is deleted.
     *
     * @param folderPath
     * @param cleanFolderFirst
     *            - removes all files from folder before deleting it
     * @return boolean
     */
    public boolean deleteFolder(String folderPath, boolean cleanFolderFirst) {
        File file = new File(userDir, folderPath);
        if (file.isDirectory()) {
            if (cleanFolderFirst) {
                try {
                    FileUtils.cleanDirectory(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
            return file.delete();
        } else {
            return true;
        }
    }

    /**
     *
     * @param renamings
     */
    public void rename(Map<String, String> renamings) {

        int renamedCount = 0;
        if (renamings != null) {

            for (Map.Entry<String, String> entry : renamings.entrySet()) {

                String oldName = entry.getKey();
                String newName = entry.getValue();

                rename(oldName, newName);
                renamedCount++;
            }
        }

        LOGGER.debug("Total of " + renamedCount + " files renamed in the file store");
    }

    /**
     * Rename single file
     *
     * @param oldName
     * @param newName
     */
    public void rename(String oldName, String newName) {

        File file = new File(userDir, oldName);
        if (file.exists() && file.isFile()) {
            file.renameTo(new File(userDir, newName));
        }
    }

    /**
     * Gets file by the relative path given. Relative means relative to the user's file-store directory.
     *
     * If no such file found, return null. Otherwise returns {@link File} reference to the found file.
     *
     * If the given relative path is blank, return null.
     *
     * @param relativePath
     * @return File
     */
    public File getFile(String relativePath) {

        if (StringUtils.isBlank(relativePath)) {
            return null;
        }

        File file = new File(userDir, relativePath);
        if (!file.exists() || !file.isFile()) {
            LOGGER.debug("File not found: " + file.getAbsolutePath());
            return null;
        } else {
            return file;
        }
    }

    /**
     * Returns true if this file store has a file by the given relative path.
     *
     * @param relativePath The file's relative path.
     * @return Boolean in indicating the file's existence.
     */
    public boolean fileExists(String relativePath) {

        if (StringUtils.isBlank(relativePath)) {
            return false;
        }

        File file = new File(userDir, relativePath);
        return file.exists() && file.isFile();
    }

    /**
     * Gets file by its URI.
     *
     * @param uriString the uri string
     * @return File
     */
    public static File getByUri(String uriString) {

        if (CRUser.isHomeUri(uriString)) {

            String userName = CRUser.getUserNameFromUri(uriString);
            if (userName != null && userName.trim().length() > 0) {

                String fileName = FolderUtil.extractPathInUserHome(uriString);
                try {
                    fileName = URLDecoder.decode(fileName, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    throw new CRRuntimeException(e);
                }

                return FileStore.getInstance(userName).getFile(fileName);
            } else {
                LOGGER.info("Could not extract user name from this URI: " + uriString);
            }
        } else if (FolderUtil.isProjectFolder(uriString)) {
            String fileName = FolderUtil.extractPathInSpecialFolder(uriString, "project");
            try {
                fileName = URLDecoder.decode(fileName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new CRRuntimeException(e);
            }

            return StringUtils.isBlank(fileName) ? null : FileStore.getInstance("project").getFile(fileName);
        } else {
            LOGGER.info("Not a home URI: " + uriString);
        }

        return null;
    }

    /**
     * Checks if the resource is in local filestore.
     *
     * @param uriString
     *            given URI
     * @return true if a filestore uri. Does not check if file really exists
     */
    public static boolean isFileStoreUri(String uriString) {
        return CRUser.isHomeUri(uriString) || FolderUtil.isProjectFolder(uriString);
    }

    /**
     * Checks if the resource is in local filestore and is folder.
     *
     * @param uriString
     *            given URI
     * @return true if a filestore folder uri. Checks if file really exists
     */
    public static boolean isFolderUri(String uriString) {

        try {
            if (isFileStoreUri(uriString)) {
                File f = getByUri(uriString);
                return f.isDirectory();
            }
        } catch (Exception e) {
            return false;
        }

        return false;

    }

    /**
     * Changes the encoding of a local file.
     *
     * Original file is removed and the new file is saved with the original name.
     *
     * @param relativePath
     * @param currentEncoding
     * @param targetEncoding
     * @throws IOException
     */
    public void changeFileEncoding(String relativePath, Charset currentEncoding, Charset targetEncoding) throws IOException {

        if (!StringUtils.isBlank(relativePath)) {
            File file = new File(userDir, relativePath);

            String tempFileName = relativePath + ".encoding.temp";
            File tempFile = new File(userDir, tempFileName);

            if (tempFile.exists() && tempFile.isFile()) {
                tempFile.delete();
            }

            if (file.exists() && file.isFile()) {

                CharBuffer buffer = CharBuffer.allocate(1024);
                int bytesRead;

                FileInputStream fis = new FileInputStream(file.getAbsolutePath());
                InputStreamReader isr = new InputStreamReader(fis, currentEncoding);

                FileOutputStream fos = new FileOutputStream(tempFile.getAbsolutePath());
                Writer out = new OutputStreamWriter(fos, targetEncoding);

                boolean encodingSuccessful = false;

                try {
                    while ((bytesRead = isr.read(buffer)) != -1) {
                        out.write(buffer.array(), 0, bytesRead);
                        buffer.clear();
                    }
                } finally {
                    fis.close();
                    isr.close();
                    out.close();
                    fos.close();
                    encodingSuccessful = true;
                }

                if (encodingSuccessful) {
                    delete(relativePath);
                    rename(tempFileName, relativePath);
                }

            }
        }
    }
}
