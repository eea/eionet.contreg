package eionet.cr.web.action.admin;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import net.sourceforge.stripes.controller.LifecycleStage;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.AbstractActionBean;

/**
 * Action bean for bulk add/delete harvest sources page.
 *
 * @author kaido
 */
@UrlBinding("/admin/bulkharvest")
public class HarvestSourceBulkActionBean extends AbstractActionBean {

    /** JSP page. */
    private static final String BULK_HARVEST_PAGE = "/pages/admin/bulkHarvest.jsp";

    /** harvest sources entered by user. */
    private String sourceUrlsString;

    /** is admin logged in. */
    private boolean adminLoggedIn = false;

    /** harvest source URLS. */
    private List<String> sourceUrls;

    /**
     * View sources bulk management page.
     *
     * @return Resolution
     */
    @DefaultHandler
    public Resolution view() {

        if (getUser() != null) {
            setAdminLoggedIn(true);
        }

        return new ForwardResolution(BULK_HARVEST_PAGE);
    }

    /**
     * Add parsed (see {@link #parseSourceUrlsString()}) sources to the database and schedule them for harvest.
     *
     * @return Resolution
     */
    public Resolution add() {

        if (getUser() != null) {

            bulkAddSources();
            setAdminLoggedIn(true);
        }

        return new ForwardResolution(BULK_HARVEST_PAGE);
    }

    /**
     * Delete parsed (see {@link #parseSourceUrlsString()}) sources from the database.
     *
     * @return Resolution
     */
    public Resolution delete() {

        if (getUser() != null) {
            bulkDeleteSources();
            setAdminLoggedIn(true);
        }

        return new ForwardResolution(BULK_HARVEST_PAGE);
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
     * True if the user is authenticated and is an administrator.
     *
     * @return boolean
     */
    public boolean isAdminLoggedIn() {
        return adminLoggedIn;
    }

    /**
     * @param adminLoggedIn boolean
     */
    public void setAdminLoggedIn(final boolean adminLoggedIn) {
        this.adminLoggedIn = adminLoggedIn;
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

        sourceUrls = new ArrayList<String>();

        // split on both new line and carriage return
        String[] urls = StringUtils.split(sourceUrlsString, "\r\n");
        for (String url : urls) {

            url = url.trim();
            if (URLUtil.isURL(url)) {
                sourceUrls.add(url);
            } else {
                addCautionMessage("Not a valid URL: " + url);
            }
        }
    }

    /**
     * Add parsed (see {@link #parseSourceUrlsString()}) sources to the database and schedule them for harvest.
     */
    private void bulkAddSources() {

        int counter = 0;
        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

        for (String sourceUrl : sourceUrls) {

            boolean success = true;
            try {
                dao.addSource(HarvestSourceDTO.create(URLUtil.escapeIRI(sourceUrl), false,
                        HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL, getUserName()));
            } catch (DAOException e) {
                success = false;
                addSystemMessage("Adding " + sourceUrl + " failed with " + e.toString());
            }

            try {
                UrgentHarvestQueue.addPullHarvest(sourceUrl);
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
    private void bulkDeleteSources() {

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

}
