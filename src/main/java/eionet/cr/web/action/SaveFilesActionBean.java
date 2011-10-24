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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.vocabulary.XMLSchema;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;

import eionet.cr.common.Predicates;
import eionet.cr.dao.CompiledDatasetDAO;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dataset.CompileDatasetJob;
import eionet.cr.dataset.CompileDatasetJobListener;
import eionet.cr.dataset.CurrentCompiledDatasets;
import eionet.cr.dto.DeliveryFilesDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;

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

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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
                // Get existing dataset uri
                if (!StringUtils.isBlank(dataset) && dataset.equals("new_dataset")) {
                    dataset = getUser().getHomeUri() + "/" + StringUtils.replace(fileName, " ", "%20");
                }

                // Store file as new source, but don't harvest it
                addSource(dataset);

                // Add metadata
                addMetadata(dataset);

                // Raise the flag that dataset is being compiled
                CurrentCompiledDatasets.addCompiledDataset(dataset, getUserName());

                // Start dataset compiling job
                SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
                Scheduler sched = schedFact.getScheduler();
                sched.start();

                JobDetail jobDetail = new JobDetail("CompileDatasetJob", null, CompileDatasetJob.class);
                jobDetail.getJobDataMap().put("selectedFiles", selectedFiles);
                jobDetail.getJobDataMap().put("datasetUri", dataset);
                jobDetail.getJobDataMap().put("overwrite", overwrite);

                CompileDatasetJobListener listener = new CompileDatasetJobListener();
                jobDetail.addJobListener(listener.getName());
                sched.addJobListener(listener);

                SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName(), null, new Date(), null, 0, 0L);
                sched.scheduleJob(jobDetail, trigger);

            } catch (Exception e) {
                e.printStackTrace();

                // Remove the flag that dataset is being compiled
                CurrentCompiledDatasets.removeCompiledDataset(dataset);

                throw new DAOException(e.getMessage(), e);
            }
        }

        return new RedirectResolution(FactsheetActionBean.class).addParameter("uri", dataset);
    }

    private void addMetadata(String subjectUri) {

        try {
            // prepare cr:hasFile predicate
            ObjectDTO objectDTO = new ObjectDTO(subjectUri, false);
            objectDTO.setSourceUri(getUser().getHomeUri());
            SubjectDTO homeSubjectDTO = new SubjectDTO(getUser().getHomeUri(), false);
            homeSubjectDTO.addObject(Predicates.CR_HAS_FILE, objectDTO);
            DAOFactory.get().getDao(HelperDAO.class).addTriples(homeSubjectDTO);

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

    private void addSource(String subjectUri) throws Exception {

        DAOFactory.get().getDao(HarvestSourceDAO.class)
                .addSourceIgnoreDuplicate(HarvestSourceDTO.create(subjectUri, false, 0, getUserName()));

        DAOFactory
                .get()
                .getDao(HarvestSourceDAO.class)
                .insertUpdateSourceMetadata(subjectUri, Predicates.CR_LAST_MODIFIED,
                        ObjectDTO.createLiteral(dateFormat.format(new Date()), XMLSchema.DATETIME));
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
            return;
        }

        // Check if file name is not empty
        if (dataset != null && dataset.equals("new_dataset") && StringUtils.isBlank(fileName)) {
            addGlobalValidationError("File name is mandatory!");
            return;
        }

        // Check if file already exists
        if (!overwrite && dataset != null && dataset.equals("new_dataset") && !StringUtils.isBlank(fileName)) {
            String datasetUri = getUser().getHomeUri() + "/" + StringUtils.replace(fileName, " ", "%20");
            boolean exists = DAOFactory.get().getDao(CompiledDatasetDAO.class).datasetExists(datasetUri);
            if (exists) {
                addGlobalValidationError("Dataset named \"" + fileName + "\" already exists!");
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
