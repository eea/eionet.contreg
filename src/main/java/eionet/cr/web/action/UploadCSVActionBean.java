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
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;

import au.com.bytecode.opencsv.CSVReader;
import eionet.cr.common.Predicates;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.web.util.CharsetToolkit;

/**
 * 
 * @author Risto Alt
 * 
 */
@UrlBinding("/uploadCSV.action")
public class UploadCSVActionBean extends AbstractActionBean {

    private FileBean file;
    private String type;
    private String[] columns;
    private String filePath;
    private String fileName;

    private String objectType;
    private int labelColumn;
    private List<Integer> uniqueColumns;
    private boolean fileUploaded = false;

    private String appUrl = GeneralConfig.getProperty(GeneralConfig.APPLICATION_HOME_URL);

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 
     * @return ForwardResolution
     */
    @DefaultHandler
    public Resolution init() {
        return new ForwardResolution("/pages/home/uploadCSV.jsp");
    }

    /**
     * Upload CSV/TSV file to /username/files folder.
     * 
     * @return ForwardResolution
     */
    public Resolution upload() {

        try {
            if (file != null) {
                char delimeter = ',';
                if (type != null && type.equals("tsv")) {
                    delimeter = '\t';
                }
                CSVReader reader = new CSVReader(file.getReader(), delimeter);
                columns = reader.readNext();

                // Save file under user/files folder
                fileName = file.getFileName();
                File storedFile = FileStore.getInstance(getUserName()).add(file.getFileName(), true, file.getInputStream());
                filePath = storedFile.getAbsolutePath();

                String subjectUri = appUrl + "/home/" + getUserName() + "/" + StringUtils.replace(fileName, " ", "%20");

                // Store file as new source, but don't harvest it
                addSource(subjectUri);

                // Add metadata about user folder update
                addMetadata(subjectUri);

                fileUploaded = true;
            }

        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
            addWarningMessage(e.getMessage());
        }

        return new ForwardResolution("/pages/home/uploadCSV.jsp");
    }

    /**
     * Insert content found from CSV/TSV file into Virtuoso triplestore.
     * 
     * @return ForwardResolution
     */
    public Resolution insert() {

        Resolution resolution = new ForwardResolution("/pages/home/uploadCSV.jsp");

        try {
            char delimeter = ',';
            if (type != null && type.equals("tsv")) {
                delimeter = '\t';
            }

            File file = new File(filePath);
            Charset guessedCharset = CharsetToolkit.guessEncoding(file, 4096, Charset.forName("UTF-8"));

            String graphName = appUrl + "/home/" + getUserName() + "/" + StringUtils.replace(fileName, " ", "%20");
            String type = graphName + "/" + objectType;

            CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(file), guessedCharset), delimeter);
            // First line contains column names
            String[] nextLine = reader.readNext();
            while ((nextLine = reader.readNext()) != null) {
                String id = "";
                // Resolve resource ID
                if (uniqueColumns != null) {
                    for (int colIndex : uniqueColumns) {
                        if (id.length() > 0) {
                            id = id + "_";
                        }
                        if (nextLine.length > colIndex) {
                            id = id + nextLine[colIndex];
                        }
                    }
                }

                String subjectUri = graphName + "/" + id;

                SubjectDTO subject = new SubjectDTO(subjectUri, false);

                // Add rdf:type
                ObjectDTO typeObject = new ObjectDTO(type, false);
                typeObject.setSourceUri(graphName);
                subject.addObject(Predicates.RDF_TYPE, typeObject);

                // Add rdfs:label
                if (labelColumn != -1) {
                    String label = nextLine[labelColumn];
                    ObjectDTO labelObject = new ObjectDTO(label, true);
                    labelObject.setSourceUri(graphName);
                    subject.addObject(Predicates.RDFS_LABEL, labelObject);
                }

                // Add all other values found from file
                int idx = 0;
                for (String col : columns) {
                    col = col.replace(" ", "_");
                    if (idx != labelColumn && nextLine.length > idx) {
                        String value = nextLine[idx];
                        ObjectDTO obj = null;
                        if (col.equalsIgnoreCase("url") || col.equalsIgnoreCase("uri")) {
                            obj = new ObjectDTO(value, false);
                        } else {
                            if (col.equalsIgnoreCase("date")) {
                                obj = new ObjectDTO(value, true, XMLSchema.DATE);
                            }
                            if (col.equalsIgnoreCase("datetime")) {
                                obj = new ObjectDTO(value, true, XMLSchema.DATETIME);
                            } else if (col.equalsIgnoreCase("boolean")) {
                                obj = new ObjectDTO(value.equalsIgnoreCase("true") ? "true" : "false", true, XMLSchema.BOOLEAN);
                            } else if (col.equalsIgnoreCase("number")) {
                                try {
                                    Integer.parseInt(value);
                                    obj = new ObjectDTO(value, true, XMLSchema.INTEGER);
                                } catch (NumberFormatException nfe1) {
                                    try {
                                        Long.parseLong(value);
                                        obj = new ObjectDTO(value, true, XMLSchema.LONG);
                                    } catch (NumberFormatException nfe2) {
                                        try {
                                            Double.parseDouble(value);
                                            obj = new ObjectDTO(value, true, XMLSchema.DOUBLE);
                                        } catch (NumberFormatException nfe3) {
                                            // Don't do anything
                                        }
                                    }
                                }
                            } else {
                                obj = new ObjectDTO(value, true);
                            }
                        }
                        obj.setSourceUri(graphName);
                        subject.addObject(graphName + "#" + col, obj);
                    }
                    idx++;
                }
                DAOFactory.get().getDao(HelperDAO.class).addTriples(subject);
            }
            // addSystemMessage("File successfully imported and stored under your <a href=\"" + getUser().getHomeUri()
            // + "\">home folder</a>!");

        } catch (Exception e) {
            System.out.println("Exception while reading csv file: " + e);
            addWarningMessage(e.toString());
            return resolution;
        }

        // If everything went successfully then redirect to the uploads list
        resolution = new RedirectResolution("/home/" + getUserName() + "/uploads");

        return resolution;
    }

    /**
     * @throws DAOException
     */
    @ValidationMethod(on = {"upload", "insert"})
    public void validatePostEvent() throws DAOException {

        // the below validation is relevant only when the event is requested through POST method
        if (!isPostRequest()) {
            return;
        }

        // for all the above POST events, user must be authorized
        if (getUser() == null) {
            addGlobalValidationError("User not logged in!");
            return;
        }

        // if upload event, make sure the file bean is not null
        String eventName = getContext().getEventName();
        if (eventName.equals("upload")) {
            if (file == null) {
                addGlobalValidationError("No file specified!");
            }
        }

        // if insert event, make sure unique columns and object type are not null
        if (eventName.equals("insert")) {
            if (uniqueColumns == null || uniqueColumns.size() == 0) {
                addGlobalValidationError("No unique column selected!");
            }
            if (StringUtils.isBlank(objectType)) {
                addGlobalValidationError("No object type specified!");
            }
            fileUploaded = true;
        }

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {
            Resolution resolution = new ForwardResolution("/pages/home/uploadCSV.jsp");
            getContext().setSourcePageResolution(resolution);
        }
    }

    private void addMetadata(String subjectUri) {

        // prepare cr:hasFile predicate
        ObjectDTO objectDTO = new ObjectDTO(subjectUri, false);
        objectDTO.setSourceUri(getUser().getHomeUri());
        SubjectDTO homeSubjectDTO = new SubjectDTO(getUser().getHomeUri(), false);
        homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);

        logger.debug("Creating the cr:hasFile predicate");
        try {
            // persist the prepared cr:hasFile and dc:title predicates
            DAOFactory.get().getDao(HelperDAO.class).addTriples(homeSubjectDTO);

            // since user's home URI was used above as triple source, add it to HARVEST_SOURCE too
            // (but set interval minutes to 0, to avoid it being background-harvested)
            DAOFactory.get().getDao(HarvestSourceDAO.class)
            .addSourceIgnoreDuplicate(HarvestSourceDTO.create(getUser().getHomeUri(), false, 0, getUserName()));

        } catch (DAOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void addSource(String subjectUri) throws Exception {

        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(subjectUri, false, 0, getUserName()));

        DAOFactory
        .get()
        .getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(subjectUri, Predicates.CR_BYTE_SIZE,
                new ObjectDTO(String.valueOf(file.getSize()), true));

        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(subjectUri, Predicates.CR_MEDIA_TYPE, new ObjectDTO(String.valueOf(type), true));

        DAOFactory
        .get()
        .getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(subjectUri, Predicates.CR_LAST_MODIFIED,
                new ObjectDTO(dateFormat.format(new Date()), true));

    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public int getLabelColumn() {
        return labelColumn;
    }

    public void setLabelColumn(int labelColumn) {
        this.labelColumn = labelColumn;
    }

    public List<Integer> getUniqueColumns() {
        return uniqueColumns;
    }

    public void setUniqueColumns(List<Integer> uniqueColumns) {
        this.uniqueColumns = uniqueColumns;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isFileUploaded() {
        return fileUploaded;
    }

    public void setFileUploaded(boolean fileUploaded) {
        this.fileUploaded = fileUploaded;
    }

}
