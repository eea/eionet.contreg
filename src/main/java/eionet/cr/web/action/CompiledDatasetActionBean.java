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
 *        Juhan Voolaid
 */

package eionet.cr.web.action;

import java.io.IOException;
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
import org.apache.log4j.Logger;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.RepositoryException;
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
import eionet.cr.dataset.CurrentLoadedDatasets;
import eionet.cr.dataset.LoadTriplesJob;
import eionet.cr.dataset.LoadTriplesJobListener;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Compiled dataset tab controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/compiledDataset.action")
public class CompiledDatasetActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(CompiledDatasetActionBean.class);

    /**  */
    private static final String COMPILED_DATASET_JSP = "/pages/compiledDataset.jsp";

    /** URI by which the factsheet has been requested. */
    private String uri;

    /** Files to be selected for removal. */
    private List<String> selectedFiles;

    /** Compiled dataset sources. */
    private List<SubjectDTO> sources;

    /** */
    private List<TabElement> tabs;

    /** */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /** */
    private Boolean isUserDataset = null;

    /**
     * Action event for displaying dataset sources.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
            SubjectDTO subject = helperDAO.getFactsheet(uri, null, null);
            sources = factory.getDao(CompiledDatasetDAO.class).getDetailedDatasetFiles(uri);

            FactsheetTabMenuHelper helper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
            tabs = helper.getTabs(FactsheetTabMenuHelper.TabTitle.COMPILED_DATASET);
        }

        return new ForwardResolution(COMPILED_DATASET_JSP);
    }

    /**
     * Action event for reloading dataset.
     *
     * @return
     * @throws DAOException
     */
    public Resolution reload() throws DAOException {

        boolean success = false;
        try {
            // Raise the flag that dataset is being reloaded
            CurrentLoadedDatasets.addLoadedDataset(uri, getUserName());

            // Start dataset reload job
            SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
            Scheduler sched = schedFact.getScheduler();
            sched.start();

            JobDetail jobDetail = new JobDetail("LoadTriplesJob", null, LoadTriplesJob.class);
            jobDetail.getJobDataMap().put("datasetUri", uri);
            jobDetail.getJobDataMap().put("overwrite", true);
            List<String> datasetFiles = DAOFactory.get().getDao(CompiledDatasetDAO.class).getDatasetFiles(uri);
            jobDetail.getJobDataMap().put("selectedFiles", datasetFiles);

            LoadTriplesJobListener listener = new LoadTriplesJobListener();
            jobDetail.addJobListener(listener.getName());
            sched.addJobListener(listener);

            SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName(), null, new Date(), null, 0, 0L);
            sched.scheduleJob(jobDetail, trigger);

            // Update source last modified date
            DAOFactory
            .get()
            .getDao(HarvestSourceDAO.class)
            .insertUpdateSourceMetadata(uri, Predicates.CR_LAST_MODIFIED,
                    ObjectDTO.createLiteral(dateFormat.format(new Date()), XMLSchema.DATETIME));

            success = true;
        } catch (Exception e) {

            LOGGER.error("Dataset reload failed with the following exception", e);
            addCautionMessage("Error occured while executing compiled dataset reload process!");

            // Remove the flag that dataset is being reloaded
            CurrentLoadedDatasets.removeLoadedDataset(uri);
        }

        if (success) {
            if (!CurrentLoadedDatasets.contains(uri)) {
                addSystemMessage("Reloaded successfully");
            } else {
                addSystemMessage("Reload started in the background.");
            }
        }

        return new RedirectResolution(this.getClass()).addParameter("uri", uri);
    }

    /**
     *
     */
    @ValidationMethod(on = {"reload"})
    public void validateReload() {

        if (getUser() == null) {
            addGlobalValidationError("You are not authorized for this operation!");
        } else {
            if (StringUtils.isBlank(uri)) {
                addGlobalValidationError("Dataset URI missing!");
            } else if (getIsUserDataset() == false) {
                addGlobalValidationError("You are not the owner of this dataset!");
            }
        }

        if (!getContext().getValidationErrors().isEmpty()) {
            getContext().setSourcePageResolution(new ForwardResolution(COMPILED_DATASET_JSP));
        }
    }

    /**
     * Removes files from compiled dataset.
     *
     * @return
     * @throws DAOException
     * @throws IOException
     * @throws RepositoryException
     */
    public Resolution removeFiles() throws DAOException, RepositoryException, IOException {
        if (selectedFiles == null || selectedFiles.size() == 0) {
            return new RedirectResolution(CompiledDatasetActionBean.class).addParameter("uri", uri);
        }

        // Remove triles
        CompiledDatasetDAO compiledDatasetDao = DAOFactory.get().getDao(CompiledDatasetDAO.class);
        compiledDatasetDao.removeFiles(uri, selectedFiles);

        // Remove generatedFrom properties
        HelperDAO helperDao = DAOFactory.get().getDao(HelperDAO.class);
        List<TripleDTO> triples = new ArrayList<TripleDTO>();
        for (String file : selectedFiles) {
            TripleDTO t = new TripleDTO(uri, Predicates.CR_GENERATED_FROM, file);
            t.setSourceUri(getUser().getHomeUri());
            triples.add(t);
        }
        helperDao.deleteTriples(triples);

        // Update source last modified date
        DAOFactory
        .get()
        .getDao(HarvestSourceDAO.class)
        .insertUpdateSourceMetadata(uri, Predicates.CR_LAST_MODIFIED,
                ObjectDTO.createLiteral(dateFormat.format(new Date()), XMLSchema.DATETIME));

        // Update HARVESTED STATEMENTS
        DAOFactory.get().getDao(HarvestSourceDAO.class).updateHarvestedStatements(uri);

        addSystemMessage("Selected files are removed from the dataset.");
        if (compiledDatasetDao.hasCompiledDatasetExpiredData(uri, selectedFiles)) {
            addCautionMessage("Some of the data from the deleted files may still exist. Reloding the dataset is recommended.");
        }

        return new RedirectResolution(CompiledDatasetActionBean.class).addParameter("uri", uri);
    }

    /**
     * Returns true if there is an authenticated user, and the dataset URI is not blank and it starts with the authenticated user's
     * home-URI.
     *
     * @return See method description.
     */
    public boolean getIsUserDataset() {

        if (isUserDataset == null) {
            isUserDataset = getUser() != null && !StringUtils.isBlank(uri) && uri.startsWith(getUser().getHomeUri());
        }

        return isUserDataset;
    }

    /**
     *
     * @return boolean
     */
    public boolean isCurrentlyReloaded() {

        return uri == null ? false : CurrentLoadedDatasets.contains(uri);
    }

    /**
     *
     * @return
     */
    public boolean isSourcesEmpty() {
        return sources.isEmpty();
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the sources
     */
    public List<SubjectDTO> getSources() {
        return sources;
    }

    /**
     * @return the selectedFiles
     */
    public List<String> getSelectedFiles() {
        return selectedFiles;
    }

    /**
     * @param selectedFiles the selectedFiles to set
     */
    public void setSelectedFiles(List<String> selectedFiles) {
        this.selectedFiles = selectedFiles;
    }

}
