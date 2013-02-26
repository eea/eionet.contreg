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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;
import net.sourceforge.stripes.validation.ValidationMethod;

import org.apache.commons.lang.StringUtils;
import org.quartz.SchedulerException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.FolderDAO;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dataset.CreateDataset;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.harvest.util.RDFMediaTypes;
import eionet.cr.util.FolderUtil;
import eionet.cr.util.Pair;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.factsheet.FactsheetActionBean;
import eionet.cr.web.action.source.ViewSourceActionBean;
import eionet.cr.web.util.RDFGenerator;

/**
 * Controller for adding new harvest source and for exporting source triples.
 *
 * @author altnyris
 */
@UrlBinding("/source.action")
public class HarvestSourceActionBean extends AbstractActionBean {

    /** */
    public static final List<String> MEDIA_TYPES;

    /** */
    private HarvestSourceDTO harvestSource;
    private List<HarvestDTO> harvests;

    /** */
    private List<TripleDTO> sampleTriples;

    /** */
    private int intervalMultiplier;
    private static LinkedHashMap<Integer, String> intervalMultipliers;

    /** */
    private static final List<Pair<String, String>> tabs;
    private String selectedTab = "view";

    /** */
    private String urlBefore;

    /** */
    private enum ExportType {
        FILE, HOMESPACE
    };

    /** */
    private List<String> folders;
    private String exportType;
    private String datasetName;
    private String folder;
    private boolean overwriteDataset;

    private boolean schemaSource;

    /** */
    static {
        tabs = new LinkedList<Pair<String, String>>();
        tabs.add(new Pair<String, String>("view", "View"));
        tabs.add(new Pair<String, String>("sampleTriples", "Sample triples"));
        tabs.add(new Pair<String, String>("history", "History"));

        intervalMultipliers = new LinkedHashMap<Integer, String>();
        intervalMultipliers.put(new Integer(1), "minutes");
        intervalMultipliers.put(new Integer(60), "hours");
        intervalMultipliers.put(new Integer(1440), "days");
        intervalMultipliers.put(new Integer(10080), "weeks");

        MEDIA_TYPES = new ArrayList<String>();
        MEDIA_TYPES.add(null);
        MEDIA_TYPES.addAll(RDFMediaTypes.collection());
        MEDIA_TYPES.add("application/rss+xml");
        MEDIA_TYPES.add("application/atom+xml");
        MEDIA_TYPES.add("application/octet-stream");
        MEDIA_TYPES.add("text/plain");
    }

    /**
     *
     * @return HarvestSourceDTO
     */
    public HarvestSourceDTO getHarvestSource() {
        return harvestSource;
    }

    /**
     *
     * @param harvestSource
     */
    public void setHarvestSource(HarvestSourceDTO harvestSource) {
        this.harvestSource = harvestSource;
    }

    /**
     *
     * @return List<HarvestDTO>
     */
    public List<HarvestDTO> getHarvests() {
        return harvests;
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     */
    @DefaultHandler
    @HandlesEvent("view")
    public Resolution view() throws DAOException {
        return new RedirectResolution(ViewSourceActionBean.class).addParameter("uri", harvestSource.getUrl());
    }

    /**
     *
     * @return Resolution
     * @throws DAOException
     * @throws SchedulerException
     * @throws HarvestException
     * @throws MalformedURLException
     */
    public Resolution add() throws DAOException, SchedulerException, HarvestException, MalformedURLException {

        Resolution resolution = new ForwardResolution("/pages/addsource.jsp");
        if (isUserLoggedIn()) {
            if (isPostRequest()) {

                if (validateAddEdit()) {

                    // create new harvest source
                    boolean isSparqlEndpoint = false;
                    HarvestSourceDTO hSourceDTO = getHarvestSource();
                    if (hSourceDTO != null) {

                        isSparqlEndpoint = hSourceDTO.isSparqlEndpoint();

                        // escape spaces in URLs
                        if (hSourceDTO.getUrl() != null) {
                            hSourceDTO.setUrl(URLUtil.escapeIRI(hSourceDTO.getUrl()));
                        }

                        hSourceDTO.setOwner(getUserName());
                        // All Schema sources are also Priority sources
                        if (schemaSource) {
                            hSourceDTO.setPrioritySource(true);
                        }
                        // Add/remove source into/from inferencing ruleset
                        manageRuleset(hSourceDTO.getUrl());
                    }
                    factory.getDao(HarvestSourceDAO.class).addSource(hSourceDTO);

                    // set up the resolution
                    resolution = new ForwardResolution(HarvestSourcesActionBean.class);

                    // schedule urgent harvest, unless explicitly requested not to
                    if (!isSparqlEndpoint && getContext().getRequestParameter("dontHarvest") == null) {

                        UrgentHarvestQueue.addPullHarvest(getHarvestSource().getUrl());
                        addSystemMessage("Harvest source successfully created and scheduled for urgent harvest!");
                    } else {
                        addSystemMessage("Harvest source successfully created!");
                    }
                }
            }
        } else {
            addWarningMessage(getBundle().getString("not.logged.in"));
        }

        return resolution;
    }

    /**
     *
     * @param url
     * @throws DAOException
     */
    private void manageRuleset(String url) throws DAOException {

        if (GeneralConfig.isUseInferencing()) {
            boolean isAlreadyInRuleset = factory.getDao(HarvestSourceDAO.class).isSourceInInferenceRule(url);
            if (schemaSource && !isAlreadyInRuleset) {
                factory.getDao(HarvestSourceDAO.class).addSourceIntoInferenceRule(url);
            } else if (!schemaSource && isAlreadyInRuleset) {
                factory.getDao(HarvestSourceDAO.class).removeSourceFromInferenceRule(url);
            }
        }
    }

    /**
     *
     * @return Resolution
     */
    public Resolution export() throws DAOException {

        Resolution resolution = new ForwardResolution("/pages/export.jsp");

        // process further only if exportType has been specified
        if (!StringUtils.isBlank(exportType)) {

            // process further only if source URL has been specified
            if (harvestSource != null && !StringUtils.isBlank(harvestSource.getUrl())) {

                // if exporting to file, generate and stream RDF into servlet
                // response
                if (ExportType.FILE.toString().equals(exportType)) {

                    resolution = (new StreamingResolution("application/rdf+xml") {

                        @Override
                        public void stream(HttpServletResponse response) throws Exception {
                            RDFGenerator.generate(harvestSource.getUrl(), response.getOutputStream());
                        }
                    }).setFilename("rdf.xml");
                } else if (ExportType.HOMESPACE.toString().equals(exportType)) {
                    try {
                        // If datasetName not provided, then extract it from source url
                        if (StringUtils.isBlank(datasetName)) {
                            datasetName = StringUtils.substringAfterLast(harvestSource.getUrl(), "/");
                        }
                        // Construct dataset uri
                        if (!StringUtils.isBlank(datasetName) && !StringUtils.isBlank(folder)) {
                            String dataset = folder + "/" + StringUtils.replace(datasetName, " ", "%20");

                            List<String> selectedFiles = new ArrayList<String>();
                            selectedFiles.add(harvestSource.getUrl());

                            CreateDataset cd = new CreateDataset(null, getUser());
                            cd.create(datasetName, dataset, folder, selectedFiles, overwriteDataset, null);

                            return new RedirectResolution(FactsheetActionBean.class).addParameter("uri", dataset);
                        }
                    } catch (Exception e) {
                        throw new DAOException(e.getMessage(), e);
                    }
                } else {
                    throw new IllegalArgumentException("Unknown export type: " + exportType);
                }
            }
        } else if (getUser() != null) {
            folders = FolderUtil.getUserAccessibleFolders(getUser());
        }

        return resolution;
    }

    /**
     * @throws DAOException
     */
    @ValidationMethod(on = {"export"})
    public void validateSaveEvent() throws DAOException {

        // the below validation is relevant only when exported to HOMESPACE
        if (exportType == null || ExportType.FILE.toString().equals(exportType)) {
            return;
        }

        // for all the above POST events, user must be authorized
        if (getUser() == null) {
            addGlobalValidationError("User not logged in!");
            return;
        }

        // no folder selected
        if (StringUtils.isBlank(folder)) {
            addGlobalValidationError("Folder not selected!");
            return;
        }

        // Check if file already exists
        // If datasetName not provided, then extract it from source url
        if (StringUtils.isBlank(datasetName)) {
            datasetName = StringUtils.substringAfterLast(harvestSource.getUrl(), "/");
        }
        if (!overwriteDataset && !StringUtils.isBlank(datasetName) && !StringUtils.isBlank(folder)) {
            String datasetUri = folder + "/" + StringUtils.replace(datasetName, " ", "%20");
            boolean exists = DAOFactory.get().getDao(FolderDAO.class).fileOrFolderExists(datasetUri);
            if (exists) {
                addGlobalValidationError("File named \"" + datasetName + "\" already exists in folder \"" + folder + "\"!");
            }
        }

        folders = FolderUtil.getUserAccessibleFolders(getUser());

        // if any validation errors were set above, make sure the right resolution is returned
        if (hasValidationErrors()) {
            Resolution resolution = new ForwardResolution("/pages/export.jsp");
            getContext().setSourcePageResolution(resolution);
        }
    }

    /**
     *
     * @return boolean
     */
    public boolean validateAddEdit() {

        if (isPostRequest()) {

            String urlString = harvestSource.getUrl();
            if (StringUtils.isBlank(urlString)) {
                addGlobalValidationError(new SimpleError("URL missing!"));
            } else {
                try {
                    URL url = new URL(urlString);
                    if (url.getRef() != null) {
                        addGlobalValidationError(new SimpleError("URL with a fragment part not allowed!"));
                    }

                    if (!StringUtils.equals(urlBefore, urlString)
                            && URLUtil.isNotExisting(urlString, harvestSource.isSparqlEndpoint())) {
                        addGlobalValidationError(new SimpleError("There is no resource existing behind this URL!"));
                    }

                    Integer intervalMinutes = harvestSource.getIntervalMinutes();
                    if (intervalMinutes != null) {
                        if (intervalMinutes.intValue() < 0 || intervalMultiplier < 0) {
                            addGlobalValidationError(new SimpleError("Harvest interval must be >= 0"));
                        } else {
                            harvestSource.setIntervalMinutes(Integer.valueOf(intervalMinutes.intValue() * intervalMultiplier));
                        }
                    } else {
                        harvestSource.setIntervalMinutes(Integer.valueOf(0));
                    }
                } catch (MalformedURLException e) {
                    addGlobalValidationError(new SimpleError("Invalid URL!"));
                }
            }
        }

        return getContext().getValidationErrors() == null || getContext().getValidationErrors().isEmpty();
    }

    /**
     * @return the sampleTriples
     */
    public List<TripleDTO> getSampleTriples() {
        return sampleTriples;
    }

    /**
     * @param intervalMultiplier the intervalMultiplier to set
     */
    public void setIntervalMultiplier(int intervalMultiplier) {
        this.intervalMultiplier = intervalMultiplier;
    }

    /**
     *
     * @return Map<Integer,String>
     */
    public Map<Integer, String> getIntervalMultipliers() {

        return intervalMultipliers;
    }

    /**
     *
     * @return int
     */
    public int getSelectedIntervalMultiplier() {
        return getIntervalMultipliers().keySet().iterator().next().intValue();
    }

    /**
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
     *
     * @param minutes
     * @return
     */
    private static String getMinutesDisplay(int minutes) {

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
     * Returns all the valid media types.
     *
     * @return
     */
    public List<String> getMediaTypes() {
        return MEDIA_TYPES;
    }

    /**
     * @return the tabs
     */
    public List<Pair<String, String>> getTabs() {
        return HarvestSourceActionBean.tabs;
    }

    /**
     * @return the selectedTab
     */
    public String getSelectedTab() {
        return selectedTab;
    }

    /**
     * @param selectedTab the selectedTab to set
     */
    public void setSelectedTab(String selectedTab) {
        this.selectedTab = selectedTab;
    }

    /**
     * @return the exportType
     */
    public String getExportType() {
        return exportType;
    }

    /**
     * @param exportType the exportType to set
     */
    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public boolean isSchemaSource() {
        return schemaSource;
    }

    public void setSchemaSource(boolean schemaSource) {
        this.schemaSource = schemaSource;
    }

    /**
     *
     * @return boolean
     */
    public boolean isCurrentlyHarvested() {
        String uri = harvestSource == null ? null : harvestSource.getUrl();
        return uri == null ? false : (CurrentHarvests.contains(uri) || UrgentHarvestQueue.isInQueue(uri));
    }

    /**
     * @param urlBefore the urlBefore to set
     */
    public void setUrlBefore(String urlBefore) {
        this.urlBefore = urlBefore;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public boolean getOverwriteDataset() {
        return overwriteDataset;
    }

    public void setOverwriteDataset(boolean overwriteDataset) {
        this.overwriteDataset = overwriteDataset;
    }

    public List<String> getFolders() {
        return folders;
    }

    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }
}
