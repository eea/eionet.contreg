package eionet.cr.web.action.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.util.URLUtil;
import eionet.cr.web.action.AbstractActionBean;
/**
 * Action bean for bulk add/delete harvest sources page.
 * @author kaido
 *
 */
@UrlBinding("/admin/bulkharvest")
public class HarvestSourceBulkActionBean extends AbstractActionBean {

    /** harvest sources entered by user. */
    private String strHarvestSources;

    /** is admin logged in.*/
    private boolean adminLoggedIn = false;

    /** parsed sources from user input. */
    private List<HarvestSourceDTO> harvestSources;

    /** harvest source URLS. */
    private List<String> sourceUrls;

    /** local holder of warnings. */
    private StringBuilder warnings = new StringBuilder();

    /** JSP name . */
    private static final String BULK_HARVEST_PAGE = "/pages/admin/bulkHarvest.jsp";

    /**
     * View sources bulk management page.
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
     * Parse input string and adds sources to the table.
     * Sources are entered one per line
     * @return Resolution
     */
    public Resolution add() {
        logger.debug("HarvestSourceBulkActionBean.add() ");
        strHarvestSources = StringEscapeUtils.escapeHtml(strHarvestSources);
        if (getUser() != null) {

            parseHarvestSources(getStrHarvestSources());
            bulkAddSources();

            setAdminLoggedIn(true);
        }
        return new ForwardResolution(BULK_HARVEST_PAGE);
    }

    /**
     * Deletes sources added by the user.
     * Harvest sources are separated by carriage return in the textarea
     * @return Resolution
     */
    public Resolution delete() {
        logger.debug("HarvestSourceBulkActionBean.delete() ");
        strHarvestSources = StringEscapeUtils.escapeHtml(strHarvestSources);
        if (getUser() != null) {
            parseHarvestSources(getStrHarvestSources());
            bulkDeleteSources();
            setAdminLoggedIn(true);
        }

        return new ForwardResolution(BULK_HARVEST_PAGE);
    }

    /** Sources : user entered input text.
     * @return String
     */
    public String getStrHarvestSources() {
        return strHarvestSources;
    }

    /** omits value to input String.
     * @param harvestSources String
     */
    public void setStrHarvestSources(final String harvestSources) {
        this.strHarvestSources = harvestSources;
    }
    /** True if the user is authenticated.
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
     * parses input String to harvest source DTO objects.
     * @param strSources user input in UI
     */
    private void parseHarvestSources(final String strSources) {
        harvestSources = new ArrayList<HarvestSourceDTO>();
        sourceUrls = new ArrayList<String>();

        String trimmedStrSources = strSources.trim();

        StringTokenizer urls = new StringTokenizer(trimmedStrSources);
//        List<HarvestSourceDTO> sources = new ArrayList<HarvestSourceDTO>();
        while (urls.hasMoreElements()) {
            String url = urls.nextToken();
            if (URLUtil.isURL(url)) {
                harvestSources.add(HarvestSourceDTO.create(url, false, HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL, getUserName()));
                sourceUrls.add(url);
            } else {
                warnings.append("Entered URL \"").append(url).append("\" is not a valid URL.<BR/>");
            }

        }
        if (StringUtils.isNotEmpty(warnings.toString())) {
            addCautionMessage(warnings.toString());
        }
    }

    /**
     * Adds sources to DB and urgent harvesting.
     */
    private void bulkAddSources() {
        HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
        int counter = 0;
        for (HarvestSourceDTO source : harvestSources) {
            try {
                //checking of duplicate sources is made by the unique index in the DB
                dao.addSource(source);
                UrgentHarvestQueue.addPullHarvest(source.getUrl());
                counter++;
            } catch (DAOException e) {
                //if adding fails, proceed with adding the other sources and show error message
                warnings.append("Adding source \"").append(source.getUrl()).append("\" failed, reason: ").
                    append(e.toString()).append("<BR/>");
            } catch (HarvestException he) {
                warnings.append("Adding source \"").append(source.getUrl()).append("\" to the harvest queue failed, reason: ").
                append(he.toString()).append("<BR/>");
            }
        }
        addSystemMessage("Adding sources finished. Successfully added " + counter + " sources for urgent harvesting.");
        if (StringUtils.isNotEmpty(warnings.toString())) {
            addSystemMessage(warnings.toString());
        }
    }

    /**
     * Adds sources to removal queue.
     * Those ones that already are in the delete queue are not added
     */
    private void bulkDeleteSources() {
            try {
                HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);
                List<String> sourcesInDeleteQue = dao.getScheduledForDeletion();
                List<String> sourcesForRemoval = new ArrayList<String>();

                for (String url : sourceUrls) {
                    //if the source is already in the delete queue, do not try to add it twice
                    if (!sourcesInDeleteQue.contains(url)) {
                        sourcesForRemoval.add(url);
                    }
                }
                dao.queueSourcesForDeletion(sourcesForRemoval);
                addSystemMessage(sourcesForRemoval.size() + " source(s) were scheduled for removal.");
                if (sourceUrls.size() - sourcesForRemoval.size() > 0) {
                    addSystemMessage((sourceUrls.size() - sourcesForRemoval.size())
                            + " source(s) were already in the delete queue.");
                }
            } catch (DAOException e) {
                warnings.append("Adding sources to delete queue failed, reason: ").
                    append(e.toString()).append("<BR/>");
            }
        if (StringUtils.isNotEmpty(warnings.toString())) {
            addSystemMessage(warnings.toString());
        }
    }

}
