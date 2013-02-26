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

package eionet.cr.web.action.source;

import java.util.Arrays;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.PostHarvestScriptDAO;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.PostHarvestScriptDTO;
import eionet.cr.dto.PostHarvestScriptDTO.TargetType;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.harvest.util.CsvImportUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.admin.endpointquery.EndpointQueriesActionBean;
import eionet.cr.web.action.admin.postHarvest.PostHarvestScriptActionBean;
import eionet.cr.web.action.admin.postHarvest.PostHarvestScriptsActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.SourceTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * View source tab.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/sourceView.action")
public class ViewSourceActionBean extends AbstractActionBean {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ViewSourceActionBean.class);

    /** URI of the source. */
    private String uri;

    /** Tabs. */
    private List<TabElement> tabs;

    /** Harvest source. */
    private HarvestSourceDTO harvestSource;

    /** Harvest history. */
    private List<HarvestDTO> harvests;

    /** Number of post-harvest scripts available for this source. */
    private int noOfPostHarvestScripts;

    /** Is schema source. */
    private boolean schemaSource;

    /**
     * Action event for displaying the source data and history.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);
            noOfPostHarvestScripts =
                    factory.getDao(PostHarvestScriptDAO.class).list(PostHarvestScriptDTO.TargetType.SOURCE, uri).size();
            if (harvestSource != null) {
                schemaSource = factory.getDao(HarvestSourceDAO.class).isSourceInInferenceRule(uri);
                harvests = factory.getDao(HarvestDAO.class).getHarvestsBySourceId(harvestSource.getSourceId());
            }

            boolean isUserOwner = harvestSource == null ? false : isUserOwner(harvestSource);
            SourceTabMenuHelper helper = new SourceTabMenuHelper(uri, isUserOwner);
            tabs = helper.getTabs(SourceTabMenuHelper.TabTitle.VIEW);
        }

        return new ForwardResolution("/pages/source/sourceView.jsp");
    }

    /**
     * Action event for scheduling urgent harvest.
     *
     * @return Resolution
     * @throws DAOException
     * @throws HarvestException
     */
    public Resolution scheduleUrgentHarvest() throws DAOException, HarvestException {

        // Populate the bean's harvest source.
        harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);

        // Get the subject data of this harvest source. It might be null!
        SubjectDTO subject = DAOFactory.get().getDao(HelperDAO.class).getSubject(uri);

        // Set some flags indicating the subject's type.
        boolean isTableFile = subject != null && CsvImportUtil.isSourceTableFile(subject);
        boolean isFolder =
                subject != null
                && Arrays.asList(Subjects.CR_FOLDER, Subjects.CR_USER_FOLDER).contains(
                        subject.getObjectValue(Predicates.RDF_TYPE));

        // Different action depending on the subject's type.
        if (isTableFile) {
            try {
                List<String> warnings = CsvImportUtil.harvestTableFile(subject, uri, getUserName());
                for (String msg : warnings) {
                    addWarningMessage(msg);
                }
            } catch (Exception e) {
                String msg = "Harvesting CSV/TSV file failed";
                LOGGER.error(msg, e);
                addWarningMessage(msg + ": " + e);
            }
        } else if (isFolder) {
            addWarningMessage("A folder cannot be harvested!");
        } else {
            UrgentHarvestQueue.addPullHarvest(getHarvestSource().getUrl());
            addSystemMessage("Successfully scheduled for urgent harvest!");
        }

        // Redirect back to view message.
        return new RedirectResolution(ViewSourceActionBean.class).addParameter("uri", uri);
    }

    /**
     * Returns source's interval minutes.
     *
     * @return String
     */
    public String getIntervalMinutesDisplay() {

        String result = "";
        if (harvestSource != null && harvestSource.getIntervalMinutes() != null) {
            result = getMinutesDisplay(harvestSource.getIntervalMinutes().intValue());
        }

        return result;
    }

    /**
     * Returns formated time of minutes.
     *
     * @param minutes
     * @return
     */
    private String getMinutesDisplay(int minutes) {

        int days = minutes / 1440;
        minutes = minutes - (days * 1440);
        int hours = minutes / 60;
        minutes = minutes - (hours * 60);

        StringBuffer buf = new StringBuffer();
        if (days > 0) {
            buf.append(days).append(days == 1 ? " day" : " days");
        }
        if (hours > 0) {
            buf.append(buf.length() > 0 ? ", " : "").append(hours).append(hours == 1 ? " hour" : " hours");
        }
        if (minutes > 0) {
            buf.append(buf.length() > 0 ? ", " : "").append(minutes).append(minutes == 1 ? " minute" : " minutes");
        }

        return buf.toString();
    }

    /**
     * Chekcs if user is owner of the harvest source.
     *
     * @param harvestSourceDTO
     * @return
     */
    private boolean isUserOwner(HarvestSourceDTO harvestSourceDTO) {

        boolean result = false;

        if (harvestSourceDTO != null) {

            String sourceOwner = harvestSourceDTO.getOwner();
            CRUser user = getUser();

            if (user != null && (user.isAdministrator() || user.getUserName().equals(sourceOwner))) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Target type for adding source-specific post-harvest script.
     *
     * @return the targetType
     */
    public TargetType getTargetType() {
        return TargetType.SOURCE;
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
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the harvestSource
     */
    public HarvestSourceDTO getHarvestSource() {
        return harvestSource;
    }

    /**
     * @return the schemaSource
     */
    public boolean isSchemaSource() {
        return schemaSource;
    }

    /**
     * @return the harvests
     */
    public List<HarvestDTO> getHarvests() {
        return harvests;
    }

    /**
     * Returns the Java class object of the post-harvest script action bean class. This method is used in JSP for building a
     * refactoring-safe link to adding a post-harvest script for this source.
     *
     * @return The class in question.
     */
    public Class getPostHarvestScriptActionBeanClass() {
        return PostHarvestScriptActionBean.class;
    }

    /**
     * Returns the Java class object of the post-harvest scripts action bean class. This method is used in JSP for building a
     * refactoring-safe link to listing the source's post-harvest scripts.
     *
     * @return
     */
    public Class getPostHarvestScriptsActionBeanClass() {
        return PostHarvestScriptsActionBean.class;
    }

    /**
     * Returns the Java class object of the endpoint harvest queries action bean class. This method is used in JSP for building
     * refactoring-safe links.
     *
     * @return
     */
    public Class getEndpointQueriesActionBeanClass() {
        return EndpointQueriesActionBean.class;
    }

    /**
     * @return the noOfPostHarvestScripts
     */
    public int getNoOfPostHarvestScripts() {
        return noOfPostHarvestScripts;
    }
}
