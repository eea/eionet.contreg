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
import eionet.cr.dao.FolderDAO;
import eionet.cr.dataset.CreateDataset;
import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.PairDTO;
import eionet.cr.web.action.factsheet.FactsheetActionBean;

/**
 *
 * @author altnyris
 *
 */
@UrlBinding("/saveFiles.action")
public class SaveFilesActionBean extends DisplaytagSearchActionBean {

    private List<String> selectedDeliveries;
    private List<DeliveryFilesDTO> deliveryFiles;
    private List<PairDTO> existingDatasets;
    private List<String> folders;

    private List<String> selectedFiles;
    private String datasetId;
    private String datasetTitle;
    private String folder;
    private boolean overwrite;
    private String dataset;

    @DefaultHandler
    public Resolution getFiles() throws DAOException {
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
        folders = factory.getDao(FolderDAO.class).getUserFolders(getUser().getHomeUri());
    }

    public Resolution save() throws DAOException {

        if (selectedFiles != null && selectedFiles.size() > 0) {
            try {
                // If dataset title is empty, then set it to dataset ID
                if (StringUtils.isBlank(datasetTitle) && dataset.equals("new_dataset")) {
                    datasetTitle = datasetId;
                }

                //Set folder to null if existing dataset selected
                if (!StringUtils.isBlank(dataset) && !dataset.equals("new_dataset")) {
                    folder = null;
                }

                // Construct new dataset uri
                if (!StringUtils.isBlank(dataset) && !StringUtils.isBlank(folder) && dataset.equals("new_dataset")) {
                    dataset = folder + "/" + StringUtils.replace(datasetId, " ", "%20");
                }

                CreateDataset cd = new CreateDataset(Predicates.CR_COMPILED_DATASET, getUser());
                cd.create(datasetTitle, dataset, folder, selectedFiles, overwrite);

            } catch (Exception e) {
                throw new DAOException(e.getMessage(), e);
            }
        }

        return new RedirectResolution(FactsheetActionBean.class).addParameter("uri", dataset);
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
        if (!StringUtils.isBlank(datasetId) && datasetId.contains("/")) {
            addGlobalValidationError("Dataset ID may not contain \"/\" character!");
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

    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
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

    public List<PairDTO> getExistingDatasets() {
        return existingDatasets;
    }

    public void setExistingDatasets(List<PairDTO> existingDatasets) {
        this.existingDatasets = existingDatasets;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public List<String> getFolders() {
        return folders;
    }

    public String getDatasetTitle() {
        return datasetTitle;
    }

    public void setDatasetTitle(String datasetTitle) {
        this.datasetTitle = datasetTitle;
    }

    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

}
