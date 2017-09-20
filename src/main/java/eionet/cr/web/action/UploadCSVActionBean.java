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
 * Risto Alt
 */
package eionet.cr.web.action;

import au.com.bytecode.opencsv.CSVReader;
import eionet.acl.AccessController;
import eionet.acl.SignOnException;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.helpers.CsvImportHelper;
import eionet.cr.dto.ScriptTemplateDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.filestore.ScriptTemplateDaoImpl;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.action.factsheet.FolderActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.FileUploadEncoding;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV upload action bean.
 *
 * @author Jaanus Heinlaid
 * @author George Sofianos
 */
@UrlBinding("/uploadCSV.action")
public class UploadCSVActionBean extends AbstractActionBean {

    /** Enum for uploaded files' types. */
    public enum FileType {

        /** The CSV file type. */
        CSV,
        /** The TSV file type. */
        TSV;
    }

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(UploadCSVActionBean.class);

    /** Default JSP to forward to. */
    private static final String JSP_PAGE = "/pages/home/uploadCSV.jsp";

    /** The Constant UPLOAD_EVENT. */
    private static final String UPLOAD_EVENT = "upload";

    /** The Constant SAVE_EVENT. */
    private static final String SAVE_EVENT = "save";

    /** The Constant PARAM_DISPLAY_WIZARD. */
    private static final String PARAM_DISPLAY_WIZARD = "displayWizard";

    /** The Constant PARAM_FINAL_ENCODING. */
    private static final String PARAM_FINAL_ENCODING = "finalEncoding";

    /** URI of the folder where the file will be uploaded. */
    private String folderUri;

    /**
     * Online source of CSV/TSV
     */
    private String fileURL;

    private String onlineFileName;

    /** Uploaded file's bean object. */
    private FileBean fileBean;

    /** Uploaded file's type. */
    private FileType fileType;

    /** Uploaded file's name. */
    private String fileName;

    /** The URI that will be assigned to the resource representing the file. */
    private String fileUri;

    /** User can specify rdf:label for the file. */
    private String fileLabel;

    /** Stored file's relative path in the user's file-store. */
    private String relativeFilePath;

    /** The type of objects contained in the file (user-given free text). */
    private String objectsType;

    /** The column (i.e. column title) representing the contained objects' labels. */
    // private String labelColumn;

    /** The columns (i.e. column titles) forming the contained objects' unique identifiers. */
    private List<String> uniqueColumns;

    /** True, when upload is meant for overwriting existing file. */
    private boolean overwrite;

    /** Publisher of uploaded material. */
    private String publisher;

    /** License of uploaded material. */
    private String license;

    /** Attribution. */
    private String attribution;

    /** Source of the uploaded material. */
    private String source;

    /** Form parameter, when true, data linking scripts are added. */
    private boolean addDataLinkingScripts;

    /** Selected scripts/columns data. */
    private List<DataLinkingScript> dataLinkingScripts;

    /** Available scripts. */
    private List<ScriptTemplateDTO> scriptTemplates;

    /** Column labels detected in the uploaded file (titles without type and language code). */
    private List<String> columnLabels;

    /** Encoding of the uploadable file */
    private String fileEncoding;

    /**
     * Re-harvest interval
     */
    private int interval;

    /**
     * Is a CSV/TSV that has bean fetch from an online source instead of uploading it?
     */
    private boolean isOnlineCsvTsv = false;

    /** Encoding used to parse the file */
    private String finalEncoding;

    private static final String ENCODING_AUTODETECT_ID = "AUTODETECT";

    /** Upload file encoding values */
    private static Map<String, String> fileEncodings;

    /**
     * Static initialization block.
     */
    static {
        fileEncodings = new LinkedHashMap<String, String>();
        fileEncodings.put(ENCODING_AUTODETECT_ID, "Auto detect");
        fileEncodings.putAll(FileUploadEncoding.getInstance());
    }

    /**
     * Default event.
     *
     * @return Resolution to got to.
     */
    @DefaultHandler
    public Resolution init() {
        if (folderUri == null) {
            addSystemMessage("You need to have a folder selected before uploading a file.");
            return new ErrorResolution(HttpServletResponse.SC_NOT_FOUND);
        }
        if (!uploadAllowed()) {
            addSystemMessage("No permission to upload CSV/TSV file.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", folderUri);
        }

        return new ForwardResolution(JSP_PAGE);
    }

    /**
     *
     * @return
     * @throws DAOException
     * @throws SignOnException if adding ACL fails
     */
    public Resolution upload() throws DAOException, SignOnException {

        if (fileBean == null) {
            try {
                isOnlineCsvTsv = true;
                URL website = new URL(fileURL);
                if(onlineFileName != null && !onlineFileName.isEmpty()) {
                    fileName = onlineFileName + "." + fileType.toString().toLowerCase();
                }
                else {
                    fileName = website.getFile().split("/")[website.getFile().split("/").length - 1];
                }
                fileUri = folderUri + "/" + StringUtils.replace(fileName, " ", "%20");
                //TODO improve temp directories functionality
                String tempFilePath = GeneralConfig.getProperty("app.home") + "/tmp/" + FolderUtil.extractPathInUserHome(folderUri + "/" + fileName);
                File tempFile = new File(tempFilePath);
                tempFile.getParentFile().mkdirs();
                tempFile.createNewFile();

                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

                fileBean = new FileBean(tempFile, "text/plain", fileName);
            } catch (MalformedURLException e) {
                LOGGER.error("Malformed URL", e);
                addWarningMessage(e.toString());
                return new ForwardResolution(JSP_PAGE);
            } catch (IOException e) {
                LOGGER.error("IOException when creating local file from online source.", e);
                addWarningMessage(e.toString());
                return new ForwardResolution(JSP_PAGE);
            }
        }


        if (!uploadAllowed()) {
            addSystemMessage("No permission to upload CSV/TSV file.");
            return new RedirectResolution(FolderActionBean.class).addParameter("uri", folderUri);
        }

        // Prepare various stuff
        ForwardResolution resolution = new ForwardResolution(JSP_PAGE);
        fileName = fileBean.getFileName();
        relativeFilePath = FolderUtil.extractPathInUserHome(folderUri + "/" + fileName);
        FileStore fileStore = FileStore.getInstance(FolderUtil.getUserDir(folderUri, getUserName()));
        CsvImportHelper helper =
                new CsvImportHelper(uniqueColumns, fileUri, fileLabel, fileType, objectsType, publisher, license, attribution,
                        source);

        // Get already existing data-linking scripts for this file URI, as we need to remember them before overwrite.
        if (fileStore.fileExists(relativeFilePath)) {
            dataLinkingScripts = helper.getExistingDataLinkingScripts(getColumnLabels());
        }
        if (CollectionUtils.isNotEmpty(dataLinkingScripts)) {
            addDataLinkingScripts = true;
        }

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        if (overwrite) {

            // If doing overwrite, load wizard inputs from previous upload
            loadWizardInputsFromPreviousUpload();

            if (folderDAO.fileOrFolderExists(folderUri, StringUtils.replace(fileName, " ", "%20"))) {
                String oldFileUri = folderUri + "/" + StringUtils.replace(fileName, " ", "%20");
                // Delete existing data
                folderDAO.deleteFileOrFolderUris(folderUri, Collections.singletonList(oldFileUri));
                DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(Collections.singletonList(oldFileUri));
                fileStore.delete(FolderUtil.extractPathInUserHome(folderUri + "/" + fileName));
            }
        } else {
            if (folderDAO.fileOrFolderExists(folderUri, StringUtils.replace(fileName, " ", "%20"))) {
                addCautionMessage("File or folder with the same name already exists.");
                return new RedirectResolution(UploadCSVActionBean.class).addParameter("folderUri", folderUri).addParameter(
                        "fileEncoding", fileEncoding);
            }
        }

        try {
            // Save the file into user's file-store.
            long fileSize = fileBean.getSize();
            fileStore.addByMoving(relativeFilePath, true, fileBean);

            // Detect charset and convert the file to UTF-8
            if (StringUtils.isNotBlank(fileEncoding)) {
                if (fileEncoding.equals(ENCODING_AUTODETECT_ID)) {
                    Charset detectedCharset = helper.detectCSVencoding(folderUri, relativeFilePath, getUserName());
                    if (detectedCharset == null) {
                        addCautionMessage("The charset of the uploaded file could not be detected automatically. Please select the files charset from the list.");
                        fileStore.delete(relativeFilePath);
                        return new RedirectResolution(UploadCSVActionBean.class).addParameter("folderUri", folderUri)
                                .addParameter("fileEncoding", fileEncoding);
                    } else if (!detectedCharset.toString().startsWith("UTF")) {
                        fileStore.changeFileEncoding(relativeFilePath, detectedCharset, Charset.forName("UTF-8"));
                    }
                    resolution.addParameter(PARAM_FINAL_ENCODING, detectedCharset.name());
                } else {
                    fileStore.changeFileEncoding(relativeFilePath, Charset.forName(fileEncoding), Charset.forName("UTF-8"));
                    resolution.addParameter(PARAM_FINAL_ENCODING, "UTF-8");
                }
            }

            // Store file as new source, but don't harvest it
            helper.insertFileMetadataAndSource(fileSize, getUserName(), isOnlineCsvTsv, interval, fileURL);

            // Add metadata about user folder update
            helper.linkFileToFolder(folderUri, getUserName());

            // Prepare data linkins scripts dropdown
            if (CollectionUtils.isEmpty(dataLinkingScripts)) {
                dataLinkingScripts = new ArrayList<DataLinkingScript>();
                dataLinkingScripts.add(new DataLinkingScript());
            }

            // If not given, the file's label equals the file's name
            if (StringUtils.isEmpty(fileLabel)) {
                fileLabel = fileName;
            }

            // Tell the JSP page that it should display the wizard.
            resolution.addParameter(PARAM_DISPLAY_WIZARD, "");

            // Add ACL, unless overwriting an already existing one.
            if (!overwrite) {
                AccessController.addAcl(FolderUtil.extractAclPath(folderUri) + "/" + fileName, getUserName(), "");
            }

        } catch (Exception e) {
            LOGGER.error("Error while reading the file: ", e);
            addWarningMessage(e.getMessage());
        }

        return resolution;
    }

    /**
     *
     * @return
     */
    public Resolution save() {

        CSVReader csvReader = null;
        CsvImportHelper helper =
                new CsvImportHelper(uniqueColumns, fileUri, fileLabel, fileType, objectsType, publisher, license, attribution,
                        source);
        try {

            // The file was encoded to UTF-8 or UTF-* after upload
            csvReader = helper.createCSVReader(folderUri, relativeFilePath, getUserName(), Charset.forName(finalEncoding));
            if (csvReader == null) {
                throw new IllegalStateException("No CSV reader successfully created!");
            }

            helper.extractObjects(csvReader);
            helper.saveWizardInputs();

            // Save data-linking scripts, if any added.
            if (addDataLinkingScripts) {
                try {
                    LOGGER.debug("Saving data-linking scripts for " + fileUri);
                    helper.saveDataLinkingScripts(dataLinkingScripts);
                } catch (DAOException e) {
                    LOGGER.error("Failed to add data linking script", e);
                    addWarningMessage("Failed to add data linking script: " + e.getMessage());
                }
            }

            // Run all post-harvest scripts specific to this source (i.e. to this uploaded file).
            // This will run both the data data-linking scripts saved in the previous block, plus any that existed already.
            try {
                LOGGER.debug("Running all source-specific post-harvest scripts of " + fileUri);
                List<String> warnings = helper.runScripts();
                if (warnings.size() > 0) {
                    for (String w : warnings) {
                        addWarningMessage(w);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Failed to run data linking scripts", e);
                addWarningMessage("Failed to run data linking scripts: " + e.getMessage());
            }

            // Finally, make sure that the file has the correct number of harvested statements in its predicates.
            DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatementsTriple(fileUri);

        } catch (Exception e) {
            LOGGER.error("Exception while processing the uploaded file:", e);
            addWarningMessage(e.toString());
            return new ForwardResolution(JSP_PAGE);
        } finally {
            try {
                helper.generateAndStoreTableFileQuery();
            } catch (Exception e2) {
                LOGGER.error("Failed to generate SPARQL query", e2);
            }
            CsvImportHelper.close(csvReader);
        }

        // If everything went successfully then redirect to the folder items list
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", folderUri);
    }

    /**
     * Form action, that adds aditional input for data linking scripts.
     *
     * @return
     */
    public Resolution addScript() {
        dataLinkingScripts.add(new DataLinkingScript());

        ForwardResolution resolution = new ForwardResolution(JSP_PAGE);
        resolution.addParameter(PARAM_DISPLAY_WIZARD, "");
        return resolution;
    }

    /**
     * Form action, that removes the last input for data linking scripts.
     *
     * @return
     */
    public Resolution removeScript() {
        dataLinkingScripts.remove(dataLinkingScripts.size() - 1);
        ForwardResolution resolution = new ForwardResolution(JSP_PAGE);
        resolution.addParameter(PARAM_DISPLAY_WIZARD, "");
        return resolution;
    }

    /**
     * Actions to be performed before starting any event handling.
     */
    @Before(stages = LifecycleStage.EventHandling)
    public void beforeEventHandling() {

        if (fileName == null && fileBean != null) {
            fileName = fileBean.getFileName();
        }
        fileUri = folderUri + "/" + StringUtils.replace(fileName, " ", "%20");
    }

    /**
     *-
     * @throws DAOException
     */
    @ValidationMethod(on = {UPLOAD_EVENT, SAVE_EVENT})
    public void validatePostEvent() throws DAOException {

        // the below validation is relevant only when the event is requested through POST method
        if (!isPostRequest()) {
            return;
        }

        // for all the above POST events, user must be authorized
        String aclPath = FolderUtil.extractAclPath(folderUri);
        boolean actionAllowed = CRUser.hasPermission(aclPath, getUser(), CRUser.INSERT_PERMISSION, false);

        if (!actionAllowed) {
            addGlobalValidationError("You are not authorised for this operation!");
            return;
        }

        // if upload event, make sure the file bean is not null
        String eventName = getContext().getEventName();
        if (eventName.equals(UPLOAD_EVENT) && fileBean == null && fileURL == null) {
            addGlobalValidationError("You either need to upload a file or provide a url!");
        }

        // if insert event, make sure unique columns and object type are not null
        if (eventName.equals(SAVE_EVENT)) {

            if (StringUtils.isBlank(relativeFilePath)) {
                addGlobalValidationError("No file specified!");
            }

            if (StringUtils.isBlank(fileName)) {
                addGlobalValidationError("No file name specified!");
            }

            // File file = FileStore.getInstance(getUserName()).getFile(relativeFilePath);
            File file = FileStore.getInstance(FolderUtil.getUserDir(folderUri, getUserName())).getFile(relativeFilePath);
            if (file == null || !file.exists()) {
                addGlobalValidationError("Could not find stored file!");
            }

            if (StringUtils.isBlank(objectsType)) {
                addGlobalValidationError("No object type specified!");
            }

            if (StringUtils.isBlank(publisher)) {
                addGlobalValidationError("No original publisher specified!");
            }

            if (StringUtils.isBlank(attribution)) {
                addGlobalValidationError("No copyright attribution specified!");
            }

            if (StringUtils.isBlank(source)) {
                addGlobalValidationError("No source specified!");
            }
        }

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {
            ForwardResolution resolution = new ForwardResolution(JSP_PAGE);
            if (eventName.equals(SAVE_EVENT)) {
                resolution.addParameter(PARAM_DISPLAY_WIZARD, "");
            }
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     * True, if there is more than one script bean available.
     *
     * @return
     */
    public boolean isRemoveScriptsAvailable() {
        return dataLinkingScripts.size() > 1;
    }

    /**
     *
     * @throws DAOException
     */
    private void loadWizardInputsFromPreviousUpload() throws DAOException {

        // If, for some reason, all inputs already have a value, do nothing and return
        if (!StringUtils.isBlank(objectsType) && !uniqueColumns.isEmpty()) {
            return;
        }

        SubjectDTO fileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(fileUri);
        if (fileSubject != null) {

            if (StringUtils.isBlank(fileLabel)) {
                fileLabel = fileSubject.getObjectValue(Predicates.RDFS_LABEL);
            }

            if (StringUtils.isBlank(objectsType)) {
                objectsType = fileSubject.getObjectValue(Predicates.CR_OBJECTS_TYPE);
            }

            if (uniqueColumns == null || uniqueColumns.isEmpty()) {
                Collection<String> coll = fileSubject.getObjectValues(Predicates.CR_OBJECTS_UNIQUE_COLUMN);
                if (coll != null && !coll.isEmpty()) {
                    uniqueColumns = new ArrayList<String>();
                    uniqueColumns.addAll(coll);
                }
            }

            if (StringUtils.isBlank(publisher)) {
                publisher = fileSubject.getObjectValue(Predicates.DCTERMS_PUBLISHER);
            }

            if (StringUtils.isBlank(license)) {
                license = fileSubject.getObjectValue(Predicates.DCTERMS_LICENSE);
                if (StringUtils.isBlank(license)) {
                    license = fileSubject.getObjectValue(Predicates.DCTERMS_RIGHTS);
                }
            }

            if (StringUtils.isBlank(attribution)) {
                attribution = fileSubject.getObjectValue(Predicates.DCTERMS_BIBLIOGRAPHIC_CITATION);
            }

            if (StringUtils.isBlank(source)) {
                source = fileSubject.getObjectValue(Predicates.DCTERMS_SOURCE);
            }
        }
    }

    /**
     * Singleton getter for column labels.
     *
     * @return
     */
    public List<String> getColumnLabels() {
        if (columnLabels == null) {
            try {
                columnLabels = CsvImportHelper.extractColumnLabels(folderUri, relativeFilePath, getUserName(), fileType);
            } catch (Exception e) {
                LOGGER.error("Exception while reading uploaded file:", e);
                addWarningMessage(e.toString());
                return new ArrayList<String>();
            }
        }

        return columnLabels;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

    public String getOnlineFileName() {
        return onlineFileName;
    }

    public void setOnlineFileName(String onlineFileName) {
        this.onlineFileName = onlineFileName;
    }

    /**
     * @param file
     */
    public void setFileBean(FileBean file) {
        this.fileBean = file;
    }

    /**
     * @return
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * @param type
     */
    public void setFileType(FileType type) {
        this.fileType = type;
    }

    /**
     * @return
     */
    public String getObjectsType() {
        return objectsType;
    }

    /**
     * @param objectType
     */
    public void setObjectsType(String objectType) {
        this.objectsType = objectType;
    }

    /**
     * @return
     */
    public List<String> getUniqueColumns() {
        return uniqueColumns;
    }

    /**
     * @param uniqueColumns
     */
    public void setUniqueColumns(List<String> uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    /**
     * @return
     */
    public String getRelativeFilePath() {
        return relativeFilePath;
    }

    /**
     * @param filePath
     */
    public void setRelativeFilePath(String filePath) {
        this.relativeFilePath = filePath;
    }

    /**
     * @return the uri
     */
    public String getFolderUri() {
        return folderUri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setFolderUri(String uri) {
        this.folderUri = uri;
    }

    /**
     * @return
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public String getFileLabel() {
        return fileLabel;
    }

    public void setFileLabel(String fileLabel) {
        this.fileLabel = fileLabel;
    }

    /**
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * @param publisher
     *            the publisher to set
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * @return the license
     */
    public String getLicense() {
        return license;
    }

    /**
     * @param license
     *            the license to set
     */
    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * @return the attribution
     */
    public String getAttribution() {
        return attribution;
    }

    /**
     * @param attribution
     *            the attribution to set
     */
    public void setAttribution(String attribution) {
        this.attribution = attribution;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source
     *            the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    public boolean isAddDataLinkingScripts() {
        return addDataLinkingScripts;
    }

    public void setAddDataLinkingScripts(boolean addDataLinkingScripts) {
        this.addDataLinkingScripts = addDataLinkingScripts;
    }

    /**
     * @return the dataLinkingScripts
     */
    public List<DataLinkingScript> getDataLinkingScripts() {
        return dataLinkingScripts;
    }

    /**
     * @param dataLinkingScripts
     *            the dataLinkingScripts to set
     */
    public void setDataLinkingScripts(List<DataLinkingScript> dataLinkingScripts) {
        this.dataLinkingScripts = dataLinkingScripts;
    }

    /**
     * @return the scriptTemplates
     */
    public List<ScriptTemplateDTO> getScriptTemplates() {
        if (scriptTemplates == null) {
            scriptTemplates = new ScriptTemplateDaoImpl().getScriptTemplates();
        }
        return scriptTemplates;
    }

    /**
     * @param scriptTemplates
     *            the scriptTemplates to set
     */
    public void setScriptTemplates(List<ScriptTemplateDTO> scriptTemplates) {
        this.scriptTemplates = scriptTemplates;
    }

    public Map<String, String> getFileEncodings() {
        return fileEncodings;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public String getFileEncoding() {
        return fileEncoding;
    }

    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    public String getFinalEncoding() {
        return finalEncoding;
    }

    public void setFinalEncoding(String finalEncoding) {
        this.finalEncoding = finalEncoding;
    }

    /**
     * True if user can upload the file.
     *
     * @return boolean
     */
    protected boolean uploadAllowed() {
        String aclPath = FolderUtil.extractAclPath(folderUri);
        return CRUser.hasPermission(aclPath, getUser(), overwrite ? CRUser.UPDATE_PERMISSION : CRUser.INSERT_PERMISSION, false);
    }
}
