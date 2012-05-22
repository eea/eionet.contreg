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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryException;

import au.com.bytecode.opencsv.CSVReader;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Util;
import eionet.cr.web.action.factsheet.FolderActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.CharsetToolkit;

/**
 *
 * @author Jaanus Heinlaid
 *
 */
@UrlBinding("/uploadCSV.action")
public class UploadCSVActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(UploadCSVActionBean.class);

    /** */
    private static final String JSP_PAGE = "/pages/home/uploadCSV.jsp";

    /** */
    private static final String UPLOAD_EVENT = "upload";
    private static final String SAVE_EVENT = "save";

    /** */
    private static final String PARAM_DISPLAY_WIZARD = "displayWizard";

    /** Enum for uploaded files' types. */
    public enum FileType {
        CSV, TSV;
    }

    /** URI of the folder where the file will be uploaded. */
    private String folderUri;

    /** Uploaded file's bean object. */
    private FileBean fileBean;

    /** Uploaded file's type. */
    private FileType fileType;

    /** Uploaded file's name. */
    private String fileName;

    /** The URI that will be assigned to the resource representing the file. */
    private String fileUri;

    /** Stored file's relative path in the user's file-store. */
    private String relativeFilePath;

    /** Columns detected in the uploaded file (it's the titles of the columns). */
    private List<String> columns;

    /** Column labels detected in the uploaded file (titles without type and language code). */
    private List<String> columnLabels;

    /** The type of objects contained in the file (user-given free text). */
    private String objectsType;

    /** The column (i.e. column title) representing the contained objects' labels. */
    private String labelColumn;

    /** The columns (i.e. column titles) forming the contained objects' unique identifiers. */
    private List<String> uniqueColumns;

    /** True, when upload is meant for overwriting existing file. */
    private boolean overwrite;

    /**
     * @return
     */
    @DefaultHandler
    public Resolution init() {
        return new ForwardResolution(JSP_PAGE);
    }

    /**
     *
     * @return
     * @throws DAOException
     */
    public Resolution upload() throws DAOException {

        // Prepare resolution.
        ForwardResolution resolution = new ForwardResolution(JSP_PAGE);

        fileName = fileBean.getFileName();

        FolderDAO folderDAO = DAOFactory.get().getDao(FolderDAO.class);
        if (overwrite) {
            if (folderDAO.fileOrFolderExists(folderUri, StringUtils.replace(fileName, " ", "%20"))) {
                String oldFileUri = folderUri + "/" + StringUtils.replace(fileName, " ", "%20");
                // Delete existing data
                FileStore fileStore = FileStore.getInstance(FolderUtil.getUserDir(folderUri, getUserName()));
                folderDAO.deleteFileOrFolderUris(folderUri, Collections.singletonList(oldFileUri));
                DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(Collections.singletonList(oldFileUri));
                fileStore.delete(FolderUtil.extractPathInUserHome(folderUri + "/" + fileName));
            }
        } else {
            if (folderDAO.fileOrFolderExists(folderUri, StringUtils.replace(fileName, " ", "%20"))) {
                addCautionMessage("File or folder with the same name already exists.");
                return new RedirectResolution(UploadCSVActionBean.class).addParameter("folderUri", folderUri);
            }
        }

        try {
            // Save the file into user's file-store.
            long fileSize = fileBean.getSize();
            relativeFilePath = FolderUtil.extractPathInUserHome(folderUri + "/" + fileName);
            //FileStore fileStore = FileStore.getInstance(getUserName());
            FileStore fileStore = FileStore.getInstance(FolderUtil.getUserDir(folderUri, getUserName()));
            fileStore.addByMoving(relativeFilePath, true, fileBean);

            // Store file as new source, but don't harvest it
            createFileMetadataAndSource(fileSize);

            // Add metadata about user folder update
            linkFileToFolder();

            // Pre-load wizard input values if this has been uploaded already before (so a re-upload now)
            preloadWizardInputs();

            // Tell the JSP page that it should display the wizard.
            resolution.addParameter(PARAM_DISPLAY_WIZARD, "");

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
        try {
            csvReader = createCSVReader(true);
            extractObjects(csvReader);
            saveWizardInputs();

        } catch (Exception e) {
            LOGGER.error("Exception while reading uploaded file:", e);
            addWarningMessage(e.toString());
            return new ForwardResolution(JSP_PAGE);
        } finally {
            close(csvReader);
        }

        // If everything went successfully then redirect to the folder items list
        return new RedirectResolution(FolderActionBean.class).addParameter("uri", folderUri);
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
     *
     * @throws DAOException
     */
    @ValidationMethod(on = { UPLOAD_EVENT, SAVE_EVENT })
    public void validatePostEvent() throws DAOException {

        // the below validation is relevant only when the event is requested through POST method
        if (!isPostRequest()) {
            return;
        }


        // for all the above POST events, user must be authorized
        String aclPath = FolderUtil.extractAclPath(folderUri);
        boolean actionAllowed = CRUser.hasPermission(aclPath, getUser(), "i", false);

        if (!actionAllowed) {
            addGlobalValidationError("You are not authorised for this operation!");
            return;
        }

        // if upload event, make sure the file bean is not null
        String eventName = getContext().getEventName();
        if (eventName.equals(UPLOAD_EVENT) && fileBean == null) {
            addGlobalValidationError("No file specified!");
        }

        // if insert event, make sure unique columns and object type are not null
        if (eventName.equals(SAVE_EVENT)) {

            if (StringUtils.isBlank(relativeFilePath)) {
                addGlobalValidationError("No file specified!");
            }

            if (StringUtils.isBlank(fileName)) {
                addGlobalValidationError("No file name specified!");
            }

            //File file = FileStore.getInstance(getUserName()).getFile(relativeFilePath);
            File file  =  FileStore.getInstance(FolderUtil.getUserDir(folderUri, getUserName())).getFile(relativeFilePath);
            if (file == null || !file.exists()) {
                addGlobalValidationError("Could not find stored file!");
            }

            if (uniqueColumns == null || uniqueColumns.size() == 0) {
                addGlobalValidationError("No unique column selected!");
            }

            if (StringUtils.isBlank(objectsType)) {
                addGlobalValidationError("No object type specified!");
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
     *
     * @param csvReader
     * @throws IOException
     * @throws DAOException
     */
    public void extractObjects(CSVReader csvReader) throws IOException, DAOException {

        // Set columns and columnLabels by reading the first line.
        String[] columnsArray = csvReader.readNext();
        if (columnsArray == null || columnsArray.length == 0) {
            columns = new ArrayList<String>();
            columnLabels = new ArrayList<String>();
        } else {
            // We do trimming, because CSV Reader doesn't do it
            columns = Arrays.asList(trimAll(columnsArray));
            if (columns != null && columns.size() > 0) {
                columnLabels = new ArrayList<String>();
                for (String col : columns) {
                    col = StringUtils.substringBefore(col, ":");
                    col = StringUtils.substringBefore(col, "@");
                    columnLabels.add(col.trim());
                }
            }
        }

        // Read the contained objects by reading the rest of lines.
        String[] line = null;
        String objectsTypeUri = fileUri + "/" + objectsType;
        HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);
        while ((line = csvReader.readNext()) != null) {

            SubjectDTO subject = extractObject(line, objectsTypeUri);
            helperDao.addTriples(subject);
        }

        // Finally, make sure that the file has the correct number of harvested statements in its predicates.
        DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatementsTriple(fileUri);
    }

    /**
     * @param line
     * @param objectsTypeUri
     * @return
     */
    private SubjectDTO extractObject(String[] line, String objectsTypeUri) {

        // Construct subject URI and DTO object.
        String subjectUri = fileUri + "/" + extractObjectId(line);
        SubjectDTO subject = new SubjectDTO(subjectUri, false);

        // Add rdf:type to DTO.
        ObjectDTO typeObject = new ObjectDTO(objectsTypeUri, false);
        typeObject.setSourceUri(fileUri);
        subject.addObject(Predicates.RDF_TYPE, typeObject);

        // Add all other values.
        for (int i = 0; i < columns.size(); i++) {

            // If current columns index out of bounds for some reason, then break.
            if (i >= line.length) {
                break;
            }

            // Get column title, skip this column if it's the label column, otherwise replace spaces.
            String column = columns.get(i);

            // Extract column type and language code
            String type = StringUtils.substringAfter(column, ":");
            if (type != null && type.length() == 0) {
                type = null;
            }
            String lang = StringUtils.substringAfter(column, "@");
            if (lang != null && lang.length() == 0) {
                lang = null;
            }

            // Get column label
            column = columnLabels.get(i);
            column = column.replace(" ", "_");

            // Create ObjectDTO representing the given column's value on this line
            ObjectDTO objectDTO = createValueObject(column, line[i], type, lang);
            objectDTO.setSourceUri(fileUri);

            // Add ObjectDTO to the subject.
            String predicateUri = fileUri + "#" + column;
            if (column.equals(labelColumn)) {
                predicateUri = Predicates.RDFS_LABEL;
            }
            subject.addObject(predicateUri, objectDTO);
        }

        return subject;
    }

    /**
     * @param column
     * @param value
     * @return
     */
    private ObjectDTO createValueObject(String column, String value, String type, String lang) {

        HashMap<String, URI> types = new HashMap<String, URI>();
        types.put("url", null);
        types.put("uri", null);
        types.put("date", XMLSchema.DATE);
        types.put("datetime", XMLSchema.DATETIME);
        types.put("boolean", XMLSchema.BOOLEAN);
        types.put("integer", XMLSchema.INTEGER);
        types.put("int", XMLSchema.INT);
        types.put("long", XMLSchema.LONG);
        types.put("double", XMLSchema.DOUBLE);
        types.put("decimal", XMLSchema.DECIMAL);
        types.put("float", XMLSchema.FLOAT);

        // If type is not defined, but column name matches one of the types, then use column name as datatype
        if (type == null) {
            if (types.keySet().contains(column.toLowerCase())) {
                type = column.toLowerCase();
            }
        }

        ObjectDTO objectDTO = null;
        if (!StringUtils.isBlank(type)) {
            if (type.equalsIgnoreCase("url") || type.equalsIgnoreCase("uri")) {
                objectDTO = new ObjectDTO(value, lang, false, false, null);
            } else if (types.keySet().contains(type.toLowerCase())) {
                if (type.equalsIgnoreCase("boolean")) {
                    value = value.equalsIgnoreCase("true") ? "true" : "false";
                }
                URI datatype = types.get(type.toLowerCase());
                objectDTO = new ObjectDTO(value, lang, true, false, datatype);
            } else if (type.equalsIgnoreCase("number")) {
                try {
                    Integer.parseInt(value);
                    objectDTO = new ObjectDTO(value, lang, true, false, XMLSchema.INTEGER);
                } catch (NumberFormatException nfe1) {
                    try {
                        Long.parseLong(value);
                        objectDTO = new ObjectDTO(value, lang, true, false, XMLSchema.LONG);
                    } catch (NumberFormatException nfe2) {
                        try {
                            Double.parseDouble(value);
                            objectDTO = new ObjectDTO(value, lang, true, false, XMLSchema.DOUBLE);
                        } catch (NumberFormatException nfe3) {
                            // ignore deliberately
                        }
                    }
                }
            }
        }

        return objectDTO == null ? new ObjectDTO(value, lang, true, false, null) : objectDTO;
    }

    /**
     *
     * @param line
     * @return
     */
    public String extractObjectId(String[] line) {

        StringBuilder buf = new StringBuilder();
        if (uniqueColumns != null && !uniqueColumns.isEmpty()) {

            for (String uniqueCol : uniqueColumns) {
                int colIndex = columnLabels.indexOf(uniqueCol);
                if (colIndex >= 0 && colIndex < line.length && !StringUtils.isBlank(line[colIndex])) {
                    if (buf.length() > 0) {
                        buf.append("_");
                    }
                    buf.append(line[colIndex]);
                }
            }
        }

        return buf.length() == 0 ? UUID.randomUUID().toString() : buf.toString();
    }

    /**
     *
     * @throws DAOException
     */
    private void preloadWizardInputs() throws DAOException {

        // If, for some reason, all inputs already have a value, do nothing and return
        if (!StringUtils.isBlank(labelColumn) && !StringUtils.isBlank(objectsType) && !uniqueColumns.isEmpty()) {
            return;
        }

        SubjectDTO fileSubject = DAOFactory.get().getDao(HelperDAO.class).getSubject(fileUri);
        if (fileSubject != null) {

            if (StringUtils.isBlank(labelColumn)) {
                labelColumn = fileSubject.getObjectValue(Predicates.CR_OBJECTS_LABEL_COLUMN);
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
        }
    }

    /**
     * @throws IOException
     * @throws RepositoryException
     * @throws DAOException
     *
     */
    private void saveWizardInputs() throws DAOException, RepositoryException, IOException {

        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_OBJECTS_TYPE, ObjectDTO.createLiteral(objectsType));
        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_OBJECTS_LABEL_COLUMN, ObjectDTO.createLiteral(labelColumn));

        ObjectDTO[] uniqueColTitles = new ObjectDTO[uniqueColumns.size()];
        for (int i = 0; i < uniqueColumns.size(); i++) {
            uniqueColTitles[i] = ObjectDTO.createLiteral(uniqueColumns.get(i));
        }

        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_OBJECTS_UNIQUE_COLUMN, uniqueColTitles);
    }

    /**
     *
     * @param csvReader
     */
    private void close(CSVReader csvReader) {
        if (csvReader != null) {
            try {
                csvReader.close();
            } catch (Exception e) {
                // deliberately ignoring
            }
        }
    }

    /**
     *
     * @throws DAOException
     */
    private void linkFileToFolder() throws DAOException {

        // prepare "folder hasFile file" statement
        ObjectDTO fileObject = ObjectDTO.createResource(fileUri);
        //        fileObject.setSourceUri(folderUri);
        String folderContext = FolderUtil.folderContext(folderUri);
        fileObject.setSourceUri(folderContext);
        SubjectDTO folderSubject = new SubjectDTO(folderUri, false);
        folderSubject.addObject(Predicates.CR_HAS_FILE, fileObject);

        logger.debug("Creating the cr:hasFile predicate");

        // persist the prepared "folder hasFile file" statement
        DAOFactory.get().getDao(HelperDAO.class).addTriples(folderSubject);

        // since folder URI was used above as triple source, add it to HARVEST_SOURCE too
        // (but set interval minutes to 0, to avoid it being background-harvested)
        //        HarvestSourceDTO folderHarvestSource = HarvestSourceDTO.create(folderUri, false, 0, getUserName());
        HarvestSourceDTO folderHarvestSource =
                HarvestSourceDTO.create(folderContext, false, 0, (getUser() != null ? getUserName() : null));
        DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIgnoreDuplicate(folderHarvestSource);
    }

    /**
     *
     * @param fileSize
     * @throws Exception
     */
    private void createFileMetadataAndSource(long fileSize) throws Exception {

        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        dao.addSourceIgnoreDuplicate(HarvestSourceDTO.create(fileUri, false, 0, getUserName()));

        String mediaType = fileType.toString();
        String lastModified = Util.virtuosoDateToString(new Date());

        dao.insertUpdateSourceMetadata(fileUri, Predicates.RDFS_LABEL, ObjectDTO.createLiteral(fileName));
        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_BYTE_SIZE, ObjectDTO.createLiteral(fileSize));
        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_MEDIA_TYPE, ObjectDTO.createLiteral(mediaType));
        dao.insertUpdateSourceMetadata(fileUri, Predicates.CR_LAST_MODIFIED, ObjectDTO.createLiteral(lastModified));
    }

    /**
     * @param guessEncoding
     * @return
     * @throws IOException
     */
    private CSVReader createCSVReader(boolean guessEncoding) throws IOException {

        CSVReader result = null;
        //File file = FileStore.getInstance(getUserName()).getFile(relativeFilePath);
        File file = FileStore.getInstance(FolderUtil.getUserDir(folderUri, getUserName())).getFile(relativeFilePath);
        if (file != null && file.exists()) {
            if (guessEncoding) {
                Charset charset = CharsetToolkit.guessEncoding(file, 4096, Charset.forName("UTF-8"));
                result = new CSVReader(new InputStreamReader(new FileInputStream(file), charset), getDelimiter());
            } else {
                result = new CSVReader(new FileReader(file), getDelimiter());
            }
        }

        return result;
    }

    /**
     *
     * @param strings
     */
    private static String[] trimAll(String[] strings) {

        if (strings != null) {
            for (int i = 0; i < strings.length; i++) {
                strings[i] = strings[i].trim();
            }
        }

        return strings;
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
     * @throws IOException
     */
    public List<String> getColumns() throws IOException {

        if (columns == null) {

            CSVReader csvReader = null;
            try {
                csvReader = createCSVReader(false);
                String[] columnsArray = csvReader.readNext();
                columns = new ArrayList<String>();
                if (columnsArray != null && columnsArray.length > 0) {
                    for (String col : columnsArray) {
                        String colLabel = StringUtils.substringBefore(col, ":").trim();
                        colLabel = StringUtils.substringBefore(colLabel, "@").trim();
                        columns.add(colLabel);
                    }
                    // columns = Arrays.asList(trimAll(columnsArray));
                }
            } finally {
                close(csvReader);
            }
        }

        return columns;
    }

    /**
     * @return
     */
    public String getLabelColumn() {
        return labelColumn;
    }

    /**
     * @param labelColumn
     */
    public void setLabelColumn(String labelColumn) {
        this.labelColumn = labelColumn;
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
     *
     * @return
     */
    public char getDelimiter() {
        return fileType != null && fileType.equals(FileType.TSV) ? '\t' : ',';
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
}
