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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

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
import eionet.cr.dataset.CurrentCompiledDatasets;
import eionet.cr.dataset.ReloadDatasetJob;
import eionet.cr.dataset.ReloadDatasetJobListener;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Compiled dataset tab controller.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/compiledDataset.action")
public class CompiledDatasetActionBean extends AbstractActionBean {

    /** URI by which the factsheet has been requested. */
    private String uri;

    /** Compiled dataset sources. */
    private List<SubjectDTO> sources;

    private List<TabElement> tabs;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

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

        return new ForwardResolution("/pages/compiledDataset.jsp");
    }

    /**
     * Action event for reloading dataset.
     *
     * @return
     * @throws DAOException
     */
    public Resolution reload() throws DAOException {
        boolean success = false;
        if (getUser() != null) {
            if (isUsersDataset()) {
                if (!StringUtils.isBlank(uri)) {
                    try {
                        // Raise the flag that dataset is being reloaded
                        CurrentCompiledDatasets.addCompiledDataset(uri, getUserName());

                        // Start dataset reload job
                        SchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory();
                        Scheduler sched = schedFact.getScheduler();
                        sched.start();

                        JobDetail jobDetail = new JobDetail("ReloadDatasetJob", null, ReloadDatasetJob.class);
                        jobDetail.getJobDataMap().put("datasetUri", uri);

                        ReloadDatasetJobListener listener = new ReloadDatasetJobListener();
                        jobDetail.addJobListener(listener.getName());
                        sched.addJobListener(listener);

                        SimpleTrigger trigger = new SimpleTrigger(jobDetail.getName(), null, new Date(), null, 0, 0L);
                        sched.scheduleJob(jobDetail, trigger);

                        // Update source last modified date
                        DAOFactory.get().getDao(HarvestSourceDAO.class).insertUpdateSourceMetadata(uri, Predicates.CR_LAST_MODIFIED,
                                ObjectDTO.createLiteral(dateFormat.format(new Date()), XMLSchema.DATETIME));

                        success = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        addCautionMessage("Error occured while executing compiled dataset reload process!");

                        // Remove the flag that dataset is being reloaded
                        CurrentCompiledDatasets.removeCompiledDataset(uri);
                    }
                }
            } else {
                addCautionMessage("User must be the owner of the compiled dataset!");
            }
        } else {
            addCautionMessage("User must be logged in!");
        }

        if (success) {
            if (!CurrentCompiledDatasets.contains(uri)) {
                addSystemMessage("Reloaded successfully");
            } else {
                addSystemMessage("Reload started in the background.");
            }
        }
        return view();
    }

    /**
     * True, if the dataset with given uri belongs to the currently logged in user.
     *
     * @return
     */
    public boolean isUsersDataset() {
        boolean ret = false;
        try {
            if (getUser() != null) {
                ret = factory.getDao(CompiledDatasetDAO.class).isUsersDataset(uri, getUser().getHomeUri());
            }
        } catch (DAOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    /**
     *
     * @return boolean
     */
    public boolean isCurrentlyReloaded() {

        return uri == null ? false : CurrentCompiledDatasets.contains(uri);
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
     * @param uri
     *            the uri to set
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

}
