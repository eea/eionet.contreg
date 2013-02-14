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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.validation.SimpleError;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.action.HarvestSourceActionBean;
import eionet.cr.web.action.HarvestSourcesActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.SourceTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Edit source tab.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/sourceEdit.action")
public class EditSourceActionBean extends AbstractActionBean {

    /** URI of the source. */
    private String uri;

    /** Tabs. */
    private List<TabElement> tabs;

    /** Harvest source. */
    private HarvestSourceDTO harvestSource;

    /** Is schema source. */
    private boolean schemaSource;

    /** Username when changing owner. */
    private String ownerName;

    /** Interval multiplyers. */
    private int intervalMultiplier;
    private static LinkedHashMap<Integer, String> intervalMultipliers;

    static {
        intervalMultipliers = new LinkedHashMap<Integer, String>();
        intervalMultipliers.put(Integer.valueOf(1), "minutes");
        intervalMultipliers.put(Integer.valueOf(60), "hours");
        intervalMultipliers.put(Integer.valueOf(1440), "days");
        intervalMultipliers.put(Integer.valueOf(10080), "weeks");
    }

    /**
     * Action event for displaying the source edit form.
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            schemaSource = factory.getDao(HarvestSourceDAO.class).isSourceInInferenceRule(uri);
            harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);

            SourceTabMenuHelper helper = new SourceTabMenuHelper(uri, isUserOwner(harvestSource));
            tabs = helper.getTabs(SourceTabMenuHelper.TabTitle.EDIT);

            if (!isUserOwner(harvestSource)) {
                addCautionMessage("Only owner can modify this source.");
                return new RedirectResolution(ViewSourceActionBean.class).addParameter("uri", harvestSource.getUrl());
            }
        }

        return new ForwardResolution("/pages/source/sourceEdit.jsp");
    }

    /**
     * Action event for saving the source.
     *
     * @return
     * @throws DAOException
     */
    public Resolution save() throws DAOException {

        if (!isUserLoggedIn()) {
            addWarningMessage(getBundle().getString("not.logged.in"));
            return view();
        }

        if (!validateEditSource()) {
            return view();
        }

        // All Schema sources are also Priority sources.
        if (schemaSource) {
            harvestSource.setPrioritySource(true);
        }
        // Add/remove source into/from inferencing ruleset.
        manageRuleset(harvestSource.getUrl());
        factory.getDao(HarvestSourceDAO.class).editSource(harvestSource);
        addSystemMessage(getBundle().getString("update.success"));

        return new RedirectResolution(ViewSourceActionBean.class).addParameter("uri", harvestSource.getUrl());
    }

    /**
     * Action for deleting the source.
     *
     * @return
     * @throws DAOException
     */
    public Resolution delete() throws DAOException {
        if (!isUserLoggedIn()) {
            addWarningMessage(getBundle().getString("not.logged.in"));
            return view();
        }

        harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);

        if (harvestSource.isPrioritySource()) {
            addWarningMessage("Priority sources cannot be deleted.");
            return view();
        }

        if (!isUserOwner(harvestSource)) {
            addWarningMessage("Only owner can delete this source.");
            return view();
        }

        if (CurrentHarvests.contains(uri)) {
            addWarningMessage("Cannot delete the source because it is currently being harvested.");
            return view();
        }

        List<String> urls = new ArrayList<String>();
        urls.add(uri);
        factory.getDao(HarvestSourceDAO.class).removeHarvestSources(urls);

        addSystemMessage("The source successfully deleted.");

        return new RedirectResolution(HarvestSourcesActionBean.class);
    }

    /**
     * Action for changing the owner.
     *
     * @return
     * @throws DAOException
     */
    public Resolution changeOwner() throws DAOException {

        if (!isUserLoggedIn()) {
            addWarningMessage(getBundle().getString("not.logged.in"));
            return view();
        }

        harvestSource = factory.getDao(HarvestSourceDAO.class).getHarvestSourceByUrl(uri);

        if (!isUserOwner(harvestSource)) {
            addWarningMessage("Only owner can delete this source.");
            return view();
        }

        // change owner
        harvestSource.setOwner(ownerName);
        factory.getDao(HarvestSourceDAO.class).editSource(harvestSource);
        addSystemMessage("Owner successfully changed.");

        return new RedirectResolution(EditSourceActionBean.class).addParameter("uri", harvestSource.getUrl());
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
     * Manages ruleset.
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
     * Validates source for editing.
     *
     * @return
     */
    private boolean validateEditSource() {

        String urlString = harvestSource.getUrl();

        if (!isUserOwner(harvestSource)) {
            addGlobalValidationError("Only owner can edit this source.");
        }

        if (StringUtils.isBlank(urlString)) {
            addGlobalValidationError(new SimpleError("URL missing!"));
        } else {
            try {
                URL url = new URL(urlString);
                if (url.getRef() != null) {
                    addGlobalValidationError(new SimpleError("URL with a fragment part not allowed!"));
                }

                if (!StringUtils.equals(uri, urlString) && URLUtil.isNotExisting(urlString, harvestSource.isSparqlEndpoint())) {
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

        return getContext().getValidationErrors() == null || getContext().getValidationErrors().isEmpty();
    }

    /**
     * Chekcs if user is owner of the harvest source.
     *
     * @return
     */
    public boolean isUserOwner() {
        return isUserOwner(harvestSource);
    }

    /**
     *
     * @return int
     */
    public int getSelectedIntervalMultiplier() {
        return getIntervalMultipliers().keySet().iterator().next().intValue();
    }

    /**
     * Returns all the valid media types.
     *
     * @return
     */
    public List<String> getMediaTypes() {
        return HarvestSourceActionBean.MEDIA_TYPES;
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
     * @return the harvestSource
     */
    public HarvestSourceDTO getHarvestSource() {
        return harvestSource;
    }

    /**
     * @param harvestSource the harvestSource to set
     */
    public void setHarvestSource(HarvestSourceDTO harvestSource) {
        this.harvestSource = harvestSource;
    }

    /**
     * @return the schemaSource
     */
    public boolean isSchemaSource() {
        return schemaSource;
    }

    /**
     * @param schemaSource the schemaSource to set
     */
    public void setSchemaSource(boolean schemaSource) {
        this.schemaSource = schemaSource;
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the intervalMultiplier
     */
    public int getIntervalMultiplier() {
        return intervalMultiplier;
    }

    /**
     * @param intervalMultiplier the intervalMultiplier to set
     */
    public void setIntervalMultiplier(int intervalMultiplier) {
        this.intervalMultiplier = intervalMultiplier;
    }

    /**
     * @return the intervalMultipliers
     */
    public LinkedHashMap<Integer, String> getIntervalMultipliers() {
        return intervalMultipliers;
    }

    /**
     * @return the ownerName
     */
    public String getOwnerName() {
        return ownerName;
    }

    /**
     * @param ownerName the ownerName to set
     */
    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

}
