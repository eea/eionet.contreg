package eionet.cr.filestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.Hashes;

/**
 * 
 * @author jaanus
 * 
 */
public class FileStore {

    /** */
    public static final String PATH = GeneralConfig.getRequiredProperty(GeneralConfig.FILESTORE_PATH);

    /** */
    private String userName;

    /**
     * 
     * @param userName
     */
    private FileStore(String userName) {

        if (StringUtils.isBlank(userName)) {
            throw new IllegalArgumentException("userName must not be null or blank");
        }

        this.userName = userName;
    }
    
    /**
     * 
     * @param userName
     * @return
     */
    public static FileStore getInstance(String userName){
        return new FileStore(userName);
    }

    /**
     * 
     * @param subjectUri
     * @param overwrite
     * @param inputStream
     * @return
     * @throws IOException
     */
    public File add(String subjectUri, boolean overwrite, InputStream inputStream) throws IOException {

        File filePath = prepareFileWrite(subjectUri, overwrite);
        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(filePath);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }

        return null;
    }

    /**
     * 
     * @param subjectUri
     * @param overwrite
     * @param reader
     * @return
     * @throws IOException
     */
    public File add(String subjectUri, boolean overwrite, Reader reader) throws IOException {

        File filePath = prepareFileWrite(subjectUri, overwrite);
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
     * 
     * @param subjectUri
     * @param overwrite
     * @throws FileAlreadyExistsException
     */
    protected File prepareFileWrite(String subjectUri, boolean overwrite) throws FileAlreadyExistsException {

        String fileName = String.valueOf(Hashes.spoHash(subjectUri));
        File userDir = new File(FileStore.PATH, userName);
        if (!userDir.exists() || !userDir.isDirectory()) {
            // creates the directory, including any necessary but nonexistent parent directories
            userDir.mkdirs();
        }

        File filePath = new File(userDir, fileName);
        if (filePath.exists() && filePath.isFile()) {

            if (overwrite == false) {
                throw new FileAlreadyExistsException("File already exists: " + fileName);
            } else {
                filePath.delete();
            }
        }

        return filePath;
    }

//    /**
//     * 
//     * @param fileSubjectUri
//     * @param fileName
//     * @return
//     */
//    private static final String generateFileName(String fileSubjectUri, String fileName) {
//
//        StringBuilder result = new StringBuilder();
//        if (!StringUtils.isBlank(fileSubjectUri)) {
//            result.append(fileSubjectUri);
//        }
//
//        if (result.length() > 0) {
//            result.append(".");
//        }
//
//        if (!StringUtils.isBlank(fileName)) {
//            result.append(fileName);
//        }
//
//        if (result.length() == 0) {
//            result.append(System.currentTimeMillis());
//        }
//
//        return result.toString();
//    }
}
