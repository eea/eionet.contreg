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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dataset.CreateDataset;
import eionet.cr.dto.DatasetDTO;
import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.URIUtil;
import eionet.cr.web.action.factsheet.CompiledDatasetActionBean;

// TODO: Auto-generated JavDoc for fields.
/**
 * Action bean for saving delivery files when merging them in CR triplestore.
 *
 * @author altnyris
 */
@UrlBinding("/saveFiles.action")
public class SaveFilesActionBean extends DisplaytagSearchActionBean {

    /** The selected deliveries. */
    private List<String> selectedDeliveries;

    /** The delivery files. */
    private List<DeliveryFilesDTO> deliveryFiles;

    /** The existing datasets. */
    private List<DatasetDTO> existingDatasets;

    /** The newest existing datasets. */
    private List<DatasetDTO> newestExistingDatasets;

    /** The folders. */
    private List<String> folders;

    /** The selected files. */
    private List<String> selectedFiles;

    /** The dataset id. */
    private String datasetId;

    /** The dataset title. */
    private String datasetTitle;

    /** The folder. */
    private String folder;

    /** The overwrite. */
    private boolean overwrite;

    /** The dataset. */
    private String dataset;

    /** Search criteria to be stored for compiled dataset. */
    private String searchCriteria;

    @DefaultHandler
    public Resolution getFiles() throws DAOException {
        logger.debug("Criteria: " + searchCriteria);
        if (getUser() == null) {
            addWarningMessage("You are not logged in!");
        } else if (getUser() != null && !getUser().hasPermission("/mergedeliveries", "v")) {
            addWarningMessage("You do not have enough privileges to view this page!");
        } else {
            init();
        }
        return new ForwardResolution("/pages/deliveryFiles.jsp");
    }

    private void init() throws DAOException {
        deliveryFiles = DAOFactory.get().getDao(CompiledDatasetDAO.class).getDeliveryFiles(selectedDeliveries);
        existingDatasets = DAOFactory.get().getDao(CompiledDatasetDAO.class).getCompiledDatasets(getUser().getHomeUri(), null);
        folders = FolderUtil.getUserAccessibleFolders(getUser());

        // Sort by modified
        Collections.sort(existingDatasets, new Comparator<DatasetDTO>() {
            @Override
            public int compare(DatasetDTO o1, DatasetDTO o2) {
                if (o1.getModified().equals(o2.getModified())) {
                    return 0;
                }
                if (o1.getModified().before(o2.getModified())) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });

        // Add the top N datasets to newsestExistingDatasets
        newestExistingDatasets = new ArrayList<DatasetDTO>();
        for (int i = 0; i < 3; i++) {
            if (existingDatasets.size() <= i) {
                break;
            }
            newestExistingDatasets.add(existingDatasets.get(i));
        }

        // Remove the added datsets
        for (DatasetDTO d : newestExistingDatasets) {
            existingDatasets.remove(d);
        }

        // Sort back alphabetically
        Collections.sort(existingDatasets, new Comparator<DatasetDTO>() {
            @Override
            public int compare(DatasetDTO o1, DatasetDTO o2) {
                return o1.getUri().compareTo(o2.getUri());
            }
        });
    }

    public Resolution save() throws DAOException {

        if (selectedFiles != null && selectedFiles.size() > 0) {
            try {
                // If dataset title is empty, then set it to dataset ID
                if (StringUtils.isBlank(datasetTitle) && dataset.equals("new_dataset")) {
                    datasetTitle = datasetId;
                }

                // Set folder to null if existing dataset selected
                if (!StringUtils.isBlank(dataset) && !dataset.equals("new_dataset")) {
                    folder = null;
                }

                // Construct new dataset uri
                if (!StringUtils.isBlank(dataset) && !StringUtils.isBlank(folder) && dataset.equals("new_dataset")) {
                    dataset = folder + "/" + StringUtils.replace(datasetId, " ", "%20");
                }

                CreateDataset cd = new CreateDataset(Predicates.CR_COMPILED_DATASET, getUser());
                cd.create(datasetTitle, dataset, folder, selectedFiles, overwrite, searchCriteria);

            } catch (Exception e) {
                throw new DAOException(e.getMessage(), e);
            }
        }

        return new RedirectResolution(CompiledDatasetActionBean.class).addParameter("uri", dataset);
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

        if (getUser() != null && !getUser().hasPermission("/mergedeliveries", "v")) {
            addGlobalValidationError("You do not have enough privileges to do this action!");
            return;
        }

        // Check if any files were selected
        if (selectedFiles == null || selectedFiles.size() == 0) {
            addGlobalValidationError("No files were selected!");
            return;
        }

        // no folder selected
        if (dataset != null && dataset.equals("new_dataset") && StringUtils.isBlank(folder)) {
            addGlobalValidationError("Folder not selected!");
            return;
        }

        // Check that dataset ID is not empty
        if (dataset != null && dataset.equals("new_dataset") && StringUtils.isBlank(datasetId)) {
            addGlobalValidationError("Dataset ID is mandatory!");
            return;
        }

        // Check that dataset ID does not contain slashes
        if (!StringUtils.isBlank(datasetId) && (!URIUtil.isURI("http://" + datasetId) || datasetId.contains("/"))) {
            addGlobalValidationError("Dataset ID contains invalid characters (':', '/', '?', '#', '[', ']', '@').");
            return;
        }

        // Check if file already exists
        if (!overwrite && dataset != null && dataset.equals("new_dataset") && !StringUtils.isBlank(datasetId)) {
            String datasetUri = folder + "/" + StringUtils.replace(datasetId, " ", "%20");
            boolean exists = DAOFactory.get().getDao(CompiledDatasetDAO.class).datasetExists(datasetUri);
            if (exists) {
                addGlobalValidationError("Dataset with ID \"" + datasetId + "\" already exists!");
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
                for (String file : existFiles) {
                    sb.append("<li>").append(file).append("</li>");
                }
                sb.append("</ul>");
                addGlobalValidationError(sb.toString());
            }
        }

        // Init lists
        init();

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {
            Resolution resolution = new ForwardResolution("/pages/deliveryFiles.jsp");
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     * Gets the selected files.
     *
     * @return the selected files
     */
    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * Sets the selected files.
     *
     * @param selectedFiles the new selected files
     */
    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

    /**
     * Checks if is overwrite.
     *
     * @return true, if is overwrite
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Sets the overwrite.
     *
     * @param overwrite the new overwrite
     */
    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    /**
     * Gets the selected deliveries.
     *
     * @return the selected deliveries
     */
    public List<String> getSelectedDeliveries() {
        return selectedDeliveries;
    }

    /**
     * Sets the selected deliveries.
     *
     * @param selectedDeliveries the new selected deliveries
     */
    public void setSelectedDeliveries(List<String> selectedDeliveries) {
        this.selectedDeliveries = selectedDeliveries;
    }

    /**
     * Gets the delivery files.
     *
     * @return the delivery files
     */
    public List<DeliveryFilesDTO> getDeliveryFiles() {
        return deliveryFiles;
    }

    /**
     * Gets the existing datasets.
     *
     * @return the existing datasets
     */
    public List<DatasetDTO> getExistingDatasets() {
        return existingDatasets;
    }

    /**
     * Sets the existing datasets.
     *
     * @param existingDatasets the new existing datasets
     */
    public void setExistingDatasets(List<DatasetDTO> existingDatasets) {
        this.existingDatasets = existingDatasets;
    }

    /**
     * Gets the dataset.
     *
     * @return the dataset
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Sets the dataset.
     *
     * @param dataset the new dataset
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    /**
     * Gets the folder.
     *
     * @return the folder
     */
    public String getFolder() {
        return folder;
    }

    /**
     * Sets the folder.
     *
     * @param folder the new folder
     */
    public void setFolder(String folder) {
        this.folder = folder;
    }

    /**
     * Gets the folders.
     *
     * @return the folders
     */
    public List<String> getFolders() {
        return folders;
    }

    /**
     * Gets the dataset title.
     *
     * @return the dataset title
     */
    public String getDatasetTitle() {
        return datasetTitle;
    }

    /**
     * Sets the dataset title.
     *
     * @param datasetTitle the new dataset title
     */
    public void setDatasetTitle(String datasetTitle) {
        this.datasetTitle = datasetTitle;
    }

    /**
     * Gets the dataset id.
     *
     * @return the dataset id
     */
    public String getDatasetId() {
        return datasetId;
    }

    /**
     * Sets the dataset id.
     *
     * @param datasetId the new dataset id
     */
    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    /**
     * Gets the newest existing datasets.
     *
     * @return the newest existing datasets
     */
    public List<DatasetDTO> getNewestExistingDatasets() {
        return newestExistingDatasets;
    }

    /**
     * Sets the newest existing datasets.
     *
     * @param newestExistingDatasets the new newest existing datasets
     */
    public void setNewestExistingDatasets(List<DatasetDTO> newestExistingDatasets) {
        this.newestExistingDatasets = newestExistingDatasets;
    }

    /**
     * @return the searchCriteria
     */
    public String getSearchCriteria() {
        return searchCriteria;
    }

    /**
     * @param searchCriteria the searchCriteria to set
     */
    public void setSearchCriteria(String searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

}
