package eionet.cr.util.cleanup;

import eionet.cr.config.GeneralConfig;
import eionet.cr.filestore.FileStore;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class FoldersAndFilesRestorer {

    private static final File FOLDERS_ROOT_PATH = new File(FileStore.PATH, "project");
    private static final String FOLDERS_ROOT_URI = GeneralConfig.getRequiredProperty(
            GeneralConfig.APPLICATION_HOME_URL) + "/project";

    private List<File> foldersAndFiles = new ArrayList<>();

    public void restore() {

        File foldersRootPath = new File(FileStore.PATH, "project");
        traverse(foldersRootPath, true);

        for (File folderOrFile : foldersAndFiles) {

            String uri = getFolderOrFileUri(folderOrFile);
            String parentUri = getFolderOrFileUri(folderOrFile.getParentFile());

            if (folderOrFile.isDirectory()) {
                System.out.println(String.format("%s hasFolder %s", parentUri, uri));
            } else {
                System.out.println(String.format("%s hasFile %s", parentUri, uri));
            }
        }
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
        return FOLDERS_ROOT_URI + getPathRelativeToFoldersRoot(folderOrFile);
    }
}
