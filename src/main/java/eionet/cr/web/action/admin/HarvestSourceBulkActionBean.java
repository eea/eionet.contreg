package eionet.cr.web.action.admin;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.UpToDateChecker;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.AbstractActionBean;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.ValidationMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Action bean for harvest source bulk actions: add, delete, check.
 *
 * @author kaido
 * @author jaanus
 */
@UrlBinding("/admin/sourceBulkActions")
public class HarvestSourceBulkActionBean extends AbstractActionBean {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(HarvestSourceBulkActionBean.class);

    /** JSP page. */
    private static final String BULK_ACTIONS_PAGE = "/pages/admin/harvestSourceBulkActions.jsp";

    /** harvest sources entered by user. */
    private String sourceUrlsString;

    /** harvest source URLS. */
    private List<String> sourceUrls = new ArrayList<String>();

    /** Resolutions of all checked URLs. */
    private Map<String, eionet.cr.harvest.UpToDateChecker.Resolution> checkResolutions;

    /** URL checking resolutions that should be specifically remarked to the user. */
    private Map<String, eionet.cr.harvest.UpToDateChecker.Resolution> checkRemarks;

    /**
     * View sources bulk management page.
     *
     * @return Resolution to go to.
     */
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution(BULK_ACTIONS_PAGE);
    }

    /**
     * Add parsed (see {@link #parseSourceUrlsString()}) sources to the database and schedule them for harvest.
     *
     * @return Resolution to go to.
     */
    public Resolution add() {

        // Security checks done by dedicated Stripes validation method.

        if (!sourceUrls.isEmpty()) {
            bulkAddSources(getUserName());
        } else {
            addWarningMessage("No URLs provided!");
        }
        return new ForwardResolution(BULK_ACTIONS_PAGE);
    }

    /**
     * Delete parsed (see {@link #parseSourceUrlsString()}) sources from the database.
     *
     * @return Resolution to go to.
     */
    public Resolution delete() {

        // Security checks done by dedicated Stripes validation method.

        if (!sourceUrls.isEmpty()) {
            bulkDeleteSources();
        } else {
            addWarningMessage("No URLs provided!");
        }
        return new ForwardResolution(BULK_ACTIONS_PAGE);
    }

    /**
     * Delete parsed (see {@link #parseSourceUrlsString()}) sources to see if they need harvesting.
     *
     * @return Resolution to go to.
     *
     * @throws ParserConfigurationException See {@link UpToDateChecker#check(String...)}.
     * @throws SAXException See {@link UpToDateChecker#check(String...)}.
     * @throws IOException See {@link UpToDateChecker#check(String...)}.
     * @throws DAOException See {@link UpToDateChecker#check(String...)}.
     */
    public Resolution check() throws DAOException, IOException, SAXException, ParserConfigurationException {

        // Security checks done by dedicated Stripes validation method.

        if (!sourceUrls.isEmpty()) {
            bulkCheckSources();
        } else {
            addWarningMessage("No URLs provided!");
        }
        return new ForwardResolution(BULK_ACTIONS_PAGE);
    }

    /**
     *
     * @return The string containing source URLs in this request.
     */
    public String getSourceUrlsString() {
        return sourceUrlsString;
    }

    /**
     *
     * @param harvestSources The string containing source URLs in this request.
     */
    public void setSourceUrlsString(final String harvestSources) {
        this.sourceUrlsString = harvestSources;
    }

    /**
     * Parses new-line-separated source URLs into a list that will be used by {@link #add()} and {@link #delete()}.
     *
     * @param strSources
     */
    @Before(stages = {LifecycleStage.EventHandling})
    public void parseSourceUrlsString() {

        if (StringUtils.isBlank(sourceUrlsString)) {
            return;
        }

        // split on both new line and carriage return
        String[] urls = StringUtils.split(sourceUrlsString, "\r\n");
        for (String url : urls) {

            url = url.trim();
            if (URLUtil.isURL(url)) {
                sourceUrls.add(url);
            } else {
                addWarningMessage("Not a valid URL: " + url);
            }
        }
    }

    /**
     * Validates that the user is authorized for any operations on this action bean. If user not authorized, redirects to the
     * {@link AdminWelcomeActionBean} which displays a proper error message. Will be run on any events.
     */
    @ValidationMethod(priority = 1)
    public void validateUserAuthorised() {

        if (getUser() == null || !getUser().isAdministrator()) {
            addGlobalValidationError("You are not authorized for this operation!");
            getContext().setSourcePageResolution(new RedirectResolution(AdminWelcomeActionBean.class));
        }
    }

    /**
     * @return the checkResolutions
     */
    public Map<String, eionet.cr.harvest.UpToDateChecker.Resolution> getCheckResolutions() {
        return checkResolutions;
    }

    /**
     * Returns the entries of the check resolutions map.
     *
     * @return the entries
     */
    public Set<Entry<String, eionet.cr.harvest.UpToDateChecker.Resolution>> getCheckResolutionsEntries() {
        return checkResolutions == null ? null : checkResolutions.entrySet();
    }

    /**
     * @return the checkRemarks
     */
    public Map<String, eionet.cr.harvest.UpToDateChecker.Resolution> getCheckRemarks() {
        return checkRemarks;
    }

    /**
     * Add parsed (see {@link #parseSourceUrlsString()}) sources to the database and schedule them for harvest.
     *
     * @param userName The user who is adding.
     */
    private void bulkAddSources(String userName) {

        int counter = 0;
        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

        for (String sourceUrl : sourceUrls) {

            boolean success = true;
            try {
                HarvestSourceDTO sourceDTO = HarvestSourceDTO.create(URLUtil.escapeIRI(sourceUrl), false,
                        HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL, getUserName());
                dao.addSource(sourceDTO);
            } catch (DAOException e) {
                success = false;
                addSystemMessage("Adding " + sourceUrl + " failed with " + e.toString());
            }

            try {
                UrgentHarvestQueue.addPullHarvest(sourceUrl, userName);
            } catch (HarvestException e) {
                success = false;
                addSystemMessage("Queueing " + sourceUrl + " for harvest failed with " + e.toString());
            }

            if (success) {
                counter++;
            }
        }

        if (counter > 0) {
            addSystemMessage(0, counter + " source(s) successfully added and scheduled for harvest.");
        }
    }

    /**
     * Delete parsed (see {@link #parseSourceUrlsString()}) sources from the database.
     */
    void bulkDeleteSources() {

        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        LinkedHashSet<String> sourcesToDelete = new LinkedHashSet<String>();
        LinkedHashSet<String> sourcesCurrentlyHarvested = new LinkedHashSet<String>();

        for (String url : sourceUrls) {

            if (CurrentHarvests.contains(url)) {
                sourcesCurrentlyHarvested.add(url);
            } else {
                sourcesToDelete.add(url);
            }
        }

        try {
            dao.removeHarvestSources(sourcesToDelete);
            addSystemMessage(sourcesToDelete.size() + " sources were successfully removed from the system.");
        } catch (DAOException e) {
            addSystemMessage("Deletion failed with " + e.toString());
        }

        if (!sourcesCurrentlyHarvested.isEmpty()) {

            addCautionMessage("The following sources were not deleted, because they are curently harvested:");
            for (String url : sourcesCurrentlyHarvested) {
                addCautionMessage(url);
            }
        }
    }

    /**
     * Worker method for the {@link #check()} event.
     *
     * @throws ParserConfigurationException See {@link UpToDateChecker#check(String...)}.
     * @throws SAXException See {@link UpToDateChecker#check(String...)}.
     * @throws IOException See {@link UpToDateChecker#check(String...)}.
     * @throws DAOException See {@link UpToDateChecker#check(String...)}.
     */
    void bulkCheckSources() throws DAOException, IOException, SAXException, ParserConfigurationException {

        String[] sourcesUrlsArray = sourceUrls.toArray(new String[sourceUrls.size()]);

        UpToDateChecker checker = new UpToDateChecker();
        checkResolutions = checker.check(sourcesUrlsArray);
        checkRemarks = new HashMap<String, eionet.cr.harvest.UpToDateChecker.Resolution>();

        LOGGER.trace("Harvest sources bulk check resolutions:\n" + checkResolutions);

        StringBuilder outOfDateOrNewUrls = new StringBuilder();
        for (int i = 0; i < sourcesUrlsArray.length; i++) {

            String sourceUrl = sourcesUrlsArray[i];
            eionet.cr.harvest.UpToDateChecker.Resolution resolution = checkResolutions.get(sourceUrl);
            if (eionet.cr.harvest.UpToDateChecker.Resolution.OUT_OF_DATE.equals(resolution)
                    || eionet.cr.harvest.UpToDateChecker.Resolution.NOT_HARVEST_SOURCE.equals(resolution)
                    || eionet.cr.harvest.UpToDateChecker.Resolution.CONVERSION_MODIFIED.equals(resolution)
                    || eionet.cr.harvest.UpToDateChecker.Resolution.SCRIPTS_MODIFIED.equals(resolution)) {

                outOfDateOrNewUrls.append(sourceUrl).append("\n");

                if (!eionet.cr.harvest.UpToDateChecker.Resolution.OUT_OF_DATE.equals(resolution)) {
                    checkRemarks.put(sourceUrl, resolution);
                }
            }
        }

        sourceUrlsString = outOfDateOrNewUrls.toString();

        if (StringUtils.isNotBlank(sourceUrlsString)) {
            addSystemMessage("The following sources were found to be out of date or not harvest sources yet!");
            if (!checkRemarks.isEmpty()) {
                addSystemMessage("Please also see specific remarks below.");
            }
        } else {
            addSystemMessage("All checked sources were found to be up to date!");
        }
    }
}
