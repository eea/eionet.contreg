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
 *        jaanus
 */

package eionet.cr.web.action.admin.staging;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterConfig;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.controller.StripesFilter;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import eionet.cr.staging.AvailableFile;
import eionet.cr.staging.FileDownloader;
import eionet.cr.util.FileDeletionJob;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.AdminWelcomeActionBean;

/**
 * Action bean that lists files available for the creation of staging databases, and provides other actions on these files as well.
 *
 * @author jaanus
 */
@UrlBinding("/admin/availFiles.action")
public class AvailableFilesActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(AvailableFilesActionBean.class);

    /** */
    private static final String LIST_JSP = "/pages/admin/staging/availableFiles.jsp";

    /** */
    private ArrayList<AvailableFile> availableFiles = new ArrayList<AvailableFile>();

    /** */
    private FileBean uploadFile;

    /** */
    private String downloadUrl;

    /** */
    private String newFileName;

    /** */
    private int maxFilePostSize;

    /** */
    private List<String> fileNames;

    /**
     * List all available files.
     *
     * @return the resolution
     */
    @DefaultHandler
    public Resolution list() {

        File[] files = FileDownloader.FILES_DIR.listFiles();
        Arrays.sort(files);
        for (int i = 0; i < files.length; i++) {
            availableFiles.add(AvailableFile.create(files[i]));
        }

        return new ForwardResolution(LIST_JSP);
    }

    /**
     * Upload.
     *
     * @return the resolution
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public Resolution upload() throws IOException {

        String renameTo = StringUtils.isBlank(newFileName) ? uploadFile.getFileName() : newFileName;
        uploadFile.save(new File(FileDownloader.FILES_DIR, FileDownloader.ensureUniqueFileName(renameTo)));

        addSystemMessage("File successfully uploaded!");
        return new RedirectResolution(this.getClass());
    }

    /**
     * Download.
     *
     * @return the resolution
     */
    public Resolution download() {

        new FileDownloader(downloadUrl, newFileName).start();

        addSystemMessage("File download started! Refresh the page to see progress.");
        return new RedirectResolution(this.getClass());
    }

    /**
     * Delete.
     *
     * @return the resolution
     */
    public Resolution delete() {

        if (fileNames != null && !fileNames.isEmpty()) {

            int noOfFilesBefore = FileDownloader.FILES_DIR.listFiles().length;
            for (String fileName : fileNames) {
                File file = new File(FileDownloader.FILES_DIR, fileName);
                boolean success = file.delete();
                if (success == false) {
                    FileDeletionJob.register(file);
                }
            }

            if (FileDownloader.FILES_DIR.listFiles().length != noOfFilesBefore - fileNames.size()) {
                addWarningMessage("Failed to delete some of the files!");
            } else {
                addSystemMessage("Files successfully deleted!");
            }
        } else {
            addCautionMessage("No files selected!");
        }

        return new RedirectResolution(this.getClass());
    }

    /**
     * Validate "upload" event.
     */
    @ValidationMethod(on = {"upload"})
    public void validateUpload() {

        // Check the user-supplied new file name is valid.
        if (StringUtils.isNotBlank(newFileName)) {
            String s = newFileName.replaceAll("[^a-zA-Z0-9-._]+", "");
            if (!s.equals(newFileName)) {
                addGlobalValidationError("Only digits, latin letters, underscores, dots and minuses allowed in file name!");
            }
        }

        getContext().setSourcePageResolution(list());
    }

    /**
     * Validate "download" event.
     */
    @ValidationMethod(on = {"download"})
    public void validateDownload() {

        // Check the URL is valid.
        if (!URLUtil.isURL(downloadUrl)) {
            addGlobalValidationError("You supplied an invalid URL!");
        }

        // Check the user-supplied new file name is valid.
        if (StringUtils.isNotBlank(newFileName)) {
            String s = newFileName.replaceAll("[^a-zA-Z0-9-._]+", "");
            if (!s.equals(newFileName)) {
                addGlobalValidationError("Only digits, latin letters, underscores, dots and minuses allowed in file name!");
            }
        }

        getContext().setSourcePageResolution(list());
    }

    /**
     * Returns java.io.File referring to the staging files directory in file system.
     *
     * @return As indicated above.
     */
    public File getStagingFilesDir() {
        return FileDownloader.FILES_DIR;
    }

    /**
     * Suffix to display in UI at file names that are currently being downloaded.
     *
     * @return As indicated above.
     */
    public String getDownloadingSuffix() {
        return FileDownloader.FILE_SUFFIX;
    }

    /**
     * Gets the staging database action bean class.
     *
     * @return the staging database action bean class
     */
    public Class getStagingDatabaseActionBeanClass() {
        return StagingDatabaseActionBean.class;
    }

    /**
     * Gets the staging databases action bean class.
     *
     * @return the staging databases action bean class
     */
    public Class getStagingDatabasesActionBeanClass() {
        return StagingDatabasesActionBean.class;
    }

    /**
     * Sets the upload file.
     *
     * @param uploadFile the uploadFile to set
     */
    public void setUploadFile(FileBean uploadFile) {
        this.uploadFile = uploadFile;
    }

    /**
     * Sets the download url.
     *
     * @param downloadUrl the downloadUrl to set
     */
    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    /**
     * Sets the new file name.
     *
     * @param newFileName the newFileName to set
     */
    public void setNewFileName(String newFileName) {
        this.newFileName = newFileName;
    }

    /**
     * Gets the available files.
     *
     * @return the availableFiles
     */
    public ArrayList<AvailableFile> getAvailableFiles() {
        return availableFiles;
    }

    /**
     * Validates the the user is authorised for any operations on this action bean. If user not authorised, redirects to the
     * {@link AdminWelcomeActionBean} which displays a proper error message. Will be run on any events, since no specific events
     * specified in the {@link ValidationMethod} annotation.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * Initializations and other pre-actions that should be invoked prior to {@link LifecycleStage.BindingAndValidation}.
     */
    @Before(stages = {LifecycleStage.BindingAndValidation})
    public void beforeBindingAndValidation() {

        FilterConfig stripesFilterConfig = StripesFilter.getConfiguration().getBootstrapPropertyResolver().getFilterConfig();
        maxFilePostSize = NumberUtils.toInt(stripesFilterConfig.getInitParameter("FileUpload.MaximumPostSize"));
    }

    /**
     * Gets the max file post size.
     *
     * @return the maxFilePostSize
     */
    public int getMaxFilePostSize() {
        return maxFilePostSize;
    }

    /**
     * Sets the file names.
     *
     * @param fileNames the fileNames to set
     */
    public void setFileNames(List<String> fileNames) {
        this.fileNames = fileNames;
    }
}
