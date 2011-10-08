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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.web.action;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.openrdf.rio.RDFFormat;

import eionet.cr.common.Predicates;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;

/**
 *
 * @author altnyris
 *
 */
@UrlBinding("/saveFiles.action")
public class SaveFilesActionBean extends DisplaytagSearchActionBean {

    private List<String> selectedDeliveries;
    private List<DeliveryFilesDTO> deliveryFiles;
    private List<String> existingDatasets;

    private List<String> selectedFiles;
    private String fileName;
    private boolean overwrite;
    private String dataset;

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @DefaultHandler
    public Resolution getFiles() throws DAOException {
        if (getUser() == null) {
            addWarningMessage("You are not logged in!");
        } else {
            init();
        }
        return new ForwardResolution("/pages/deliveryFiles.jsp");
    }

    private void init() throws DAOException {
        deliveryFiles = DAOFactory.get().getDao(CompiledDatasetDAO.class).getDeliveryFiles(selectedDeliveries);
        existingDatasets = DAOFactory.get().getDao(CompiledDatasetDAO.class).getCompiledDatasets(getUser().getHomeUri());
    }

    public Resolution save() throws DAOException {
        if (selectedFiles != null && selectedFiles.size() > 0) {
            try {
                // If files are to be stored under existing dataset, add dataset files to selected files and set overwrite to true
                if (dataset != null && !dataset.equals("new_dataset")) {
                    List<String> datasetFiles = DAOFactory.get().getDao(CompiledDatasetDAO.class).getDatasetFiles(dataset);
                    if (datasetFiles != null) {
                        selectedFiles.addAll(datasetFiles);
                    }
                    overwrite = true;
                    // Get fileName from existing dataset uri
                    if (dataset.contains("/")){
                        int idx = dataset.lastIndexOf("/");
                        if (dataset.length() > idx) {
                            fileName = dataset.substring(idx + 1);
                        }
                    }
                }

                // Save compiled dataset
                File file = DAOFactory.get().getDao(CompiledDatasetDAO.class).saveConstructedDataset(
                        selectedFiles, fileName, getUserName(), overwrite);

                if (file != null) {
                    String subjectUri = getUser().getHomeUri() + "/" + StringUtils.replace(fileName, " ", "%20");

                    //Harvest the file
                    DAOFactory.get().getDao(HarvestSourceDAO.class).loadIntoRepository(
                            new FileInputStream(file), RDFFormat.RDFXML, subjectUri, true);

                    // Store file as new source, but don't harvest it
                    addSource(subjectUri, file.length());

                    // Add metadata
                    addMetadata(subjectUri);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new DAOException(e.getMessage(), e);
            }
        }
        init();
        addSystemMessage("Successfully saved under \"Compiled datasets\" in your " +
                "<a href=\"" + getUser().getHomeUri() + "/compiledDatasets\">home-folder</a>");
        return new ForwardResolution("/pages/deliveryFiles.jsp");
    }

    private void addMetadata(String subjectUri) {

        try {
            // store rdf:type predicate
            ObjectDTO typeObjectDTO = new ObjectDTO(Predicates.CR_COMPILED_DATASET, false);
            typeObjectDTO.setSourceUri(getUser().getHomeUri());
            SubjectDTO typeSubjectDTO = new SubjectDTO(subjectUri, false);
            typeSubjectDTO.addObject(Predicates.RDF_TYPE, typeObjectDTO);
            DAOFactory.get().getDao(HelperDAO.class).addTriples(typeSubjectDTO);

            // store cr:generatedFrom predicates
            for (String file : selectedFiles) {
                ObjectDTO genFromObjectDTO = new ObjectDTO(file, false);
                genFromObjectDTO.setSourceUri(getUser().getHomeUri());
                SubjectDTO genFromSubjectDTO = new SubjectDTO(subjectUri, false);
                genFromSubjectDTO.addObject(Predicates.CR_GENERATED_FROM, genFromObjectDTO);
                DAOFactory.get().getDao(HelperDAO.class).addTriples(genFromSubjectDTO);
            }

            // since user's home URI was used above as triple source, add it to HARVEST_SOURCE too
            // (but set interval minutes to 0, to avoid it being background-harvested)
            DAOFactory.get().getDao(HarvestSourceDAO.class)
            .addSourceIgnoreDuplicate(HarvestSourceDTO.create(getUser().getHomeUri(), false, 0, getUserName()));

        } catch (DAOException e) {
            e.printStackTrace();
            return;
        }
    }

    private void addSource(String subjectUri, long fileSize) throws Exception {

        DAOFactory.get().getDao(HarvestSourceDAO.class)
        .addSourceIgnoreDuplicate(HarvestSourceDTO.create(subjectUri, false, 0, getUserName()));

        DAOFactory
        .get()
        .getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(subjectUri, Predicates.CR_BYTE_SIZE,
                new ObjectDTO(String.valueOf(fileSize), true));

        DAOFactory
        .get()
        .getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(subjectUri, Predicates.CR_LAST_MODIFIED,
                new ObjectDTO(dateFormat.format(new Date()), true));

    }

    /**
     * @throws DAOException
     */
    @ValidationMethod(on = {"save"})
    public void validateSaveEvent() throws DAOException {

        // the below validation is relevant only when the event is requested through POST method
        if (!isPostRequest()) {
            return;
        }

        // for all the above POST events, user must be authorized
        if (getUser() == null) {
            addGlobalValidationError("User not logged in!");
            return;
        }

        // Check if any files were selected
        if (selectedFiles == null || selectedFiles.size() == 0) {
            addGlobalValidationError("No files were selected!");
        }

        // Check if file name is not empty
        if (dataset != null && dataset.equals("new_dataset") && StringUtils.isBlank(fileName)) {
            addGlobalValidationError("File name is mandatory!");
        }

        // Check if file already exists
        if (!overwrite && dataset != null && dataset.equals("new_dataset") && !StringUtils.isBlank(fileName)) {
            File f = FileStore.getInstance(getUserName()).get(fileName);
            if ( f!= null) {
                addGlobalValidationError("File already exists: " + fileName);
            }
        }

        // Check if dataset already contains some of the selected files
        if (dataset != null && !dataset.equals("new_dataset")) {
            List<String> datasetFiles = DAOFactory.get().getDao(CompiledDatasetDAO.class).getDatasetFiles(dataset);
            List<String> existFiles = new ArrayList<String>();
            if (datasetFiles != null && selectedFiles != null) {
                for (String file : selectedFiles) {
                    if (datasetFiles.contains(file)) {
                        existFiles.add(file);
                    }
                }
            }
            if (existFiles.size() > 0) {
                StringBuffer sb = new StringBuffer();
                sb.append("The dataset already contains some of the selected files:");
                sb.append("<ul>");
                for(String file : existFiles) {
                    sb.append("<li>").append(file).append("</li>");
                }
                sb.append("</ul>");
                addGlobalValidationError(sb.toString());
            }
        }

        //Init lists
        init();

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {
            Resolution resolution = new ForwardResolution("/pages/deliveryFiles.jsp");
            getContext().setSourcePageResolution(resolution);
        }
    }


    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public List<String> getSelectedDeliveries() {
        return selectedDeliveries;
    }

    public void setSelectedDeliveries(List<String> selectedDeliveries) {
        this.selectedDeliveries = selectedDeliveries;
    }

    public List<DeliveryFilesDTO> getDeliveryFiles() {
        return deliveryFiles;
    }

    public List<String> getExistingDatasets() {
        return existingDatasets;
    }

    public void setExistingDatasets(List<String> existingDatasets) {
        this.existingDatasets = existingDatasets;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

}
