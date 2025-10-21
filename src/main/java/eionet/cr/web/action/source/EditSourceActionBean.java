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
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.SimpleError;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    /** Indicates the harvest source interval value as submitted from the form. */
    private int intervalValue;

    /** Indicates the harvest source interval unit as submitted from the form. */
    private HarvestIntervalUnit intervalUnit;

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
            intervalValue = harvestSource.getIntervalPair().getLeft();
            intervalUnit = harvestSource.getIntervalPair().getRight();

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

        return new RedirectResolution(ViewSourceActionBean.class).addParameter("uri", harvestSource.getUrl());
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
     *
     * @Deprecated Inferencing is removed from CR
     */
    @Deprecated
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

                if (!StringUtils.equals(uri, urlString) && !URLUtil.resourceExists(urlString, harvestSource.isSparqlEndpoint())) {
                    addGlobalValidationError(new SimpleError("There is no resource existing behind this URL!"));
                }

                if (intervalValue < 0 || intervalUnit == null) {
                    addGlobalValidationError(new SimpleError("No harvest interval specified!"));
                } else {
                    harvestSource.setIntervalMinutes(Integer.valueOf(intervalValue * intervalUnit.getMinutes()));
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
     * @return
     */
    public HarvestIntervalUnit[] getIntervalUnits() {
        return HarvestIntervalUnit.values();
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

    public int getIntervalValue() {
        return intervalValue;
    }

    public void setIntervalValue(int intervalValue) {
        this.intervalValue = intervalValue;
    }

    public HarvestIntervalUnit getIntervalUnit() {
        return intervalUnit;
    }

    public void setIntervalUnit(HarvestIntervalUnit intervalUnit) {
        this.intervalUnit = intervalUnit;
    }
}
