package eionet.cr.web.action.home;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.UploadHarvest;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.context.CRActionBeanContext;
import eionet.cr.web.util.columns.SearchResultColumn;
import eionet.cr.web.util.columns.SubjectPredicateColumn;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

public abstract class AbstractHomeActionBean extends AbstractActionBean {

    protected static final String TYPE_BOOKMARK = "bookmark";
    protected static final String TYPE_HISTORY = "history";
    protected static final String TYPE_UPLOADS = "uploads";
    protected static final String TYPE_REGISTRATIONS = "registrations";
    protected static final String TYPE_REVIEWS = "reviews";

    protected static final String SHOWPUBLIC_YES = "Y";
    protected static final String SHOWPUBLIC_NO = "N";

    /** */
    protected static List<Map<String, String>> tabs;
    private static final Map<String, List<SearchResultColumn>> typesColumns;

    // Note: attemptedUserName might be used in some situations where showPublic
    // = true and content of that user is visible to everyone.
    private String attemptedUserName;

    private String section;
    private String baseHomeUrl;

    private boolean userAuthorized;
    private String authenticationMessage;
    /** */
    private String tabType;

    private String authenticatedUserName;

    private boolean showPublic;

    static {
        tabs = new ArrayList<Map<String, String>>();

        Map<String, String> tabType;

        tabType = new HashMap<String, String>();
        tabType.put("title", "Uploads");
        tabType.put("tabType", "uploads");
        tabType.put("showPublic", SHOWPUBLIC_YES);
        tabs.add(tabType);

        tabType = new HashMap<String, String>();
        tabType.put("title", "Bookmarks");
        tabType.put("tabType", "bookmark");
        tabType.put("showPublic", SHOWPUBLIC_YES);
        tabs.add(tabType);

        tabType = new HashMap<String, String>();
        tabType.put("title", "Registrations");
        tabType.put("tabType", "registrations");
        tabType.put("showPublic", SHOWPUBLIC_YES);
        tabs.add(tabType);

        tabType = new HashMap<String, String>();
        tabType.put("title", "History");
        tabType.put("tabType", "history");
        tabType.put("showPublic", SHOWPUBLIC_YES);
        tabs.add(tabType);

        tabType = new HashMap<String, String>();
        tabType.put("title", "Reviews");
        tabType.put("tabType", "reviews");
        tabType.put("showPublic", SHOWPUBLIC_YES);
        tabs.add(tabType);

        typesColumns = new HashMap<String, List<SearchResultColumn>>();

        /* columns for bookmarks */
        List<SearchResultColumn> list = new ArrayList<SearchResultColumn>();
        list.add(new SubjectPredicateColumn("Bookmark", false, Predicates.RDFS_LABEL));
        typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);

        /* columns for registrations */
        list = new ArrayList<SearchResultColumn>();
        list.add(new SubjectPredicateColumn("Subject", false, Predicates.RDFS_LABEL));
        list.add(new SubjectPredicateColumn("Predicate", false, Predicates.RDFS_LABEL));
        list.add(new SubjectPredicateColumn("Object", false, Predicates.RDFS_LABEL));
        typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);

        /* columns for history */
        list = new ArrayList<SearchResultColumn>();
        list.add(new SubjectPredicateColumn("URL", false, Predicates.RDFS_LABEL));
        list.add(new SubjectPredicateColumn("Last Update", false, Predicates.RDFS_LABEL));
        typesColumns.put(Subjects.DUBLIN_CORE_SOURCE_URL, list);

    }

    public AbstractHomeActionBean() {
        setHomeContext(true);
        // setUrlParams();
    }

    protected void setEnvironmentParams(CRActionBeanContext context, String activeSection, boolean showPublic) {

        setShowPublic(showPublic);

        attemptedUserName = context.getRequest().getParameter("username");
        section = activeSection;
        setDefaultSection();
        if (isUserLoggedIn()) {

            // be case-sensitive about user name in session and user name in URL
            String loggedInUserName = getUserName();
            if (attemptedUserName.toLowerCase().equals(loggedInUserName.toLowerCase())) {
                userAuthorized = true;
                authenticatedUserName = attemptedUserName;
            } else {
                userAuthorized = false;
                authenticationMessage = "Logged in username and home url don't match.";
            }
        } else {
            userAuthorized = false;
            authenticationMessage = "You must be logged in to access your home";
        }

        String s = context.getRequest().getRequestURI().split(attemptedUserName)[0];
        baseHomeUrl = StringUtils.substringAfter(s, context.getRequest().getContextPath());
    }

    private void setDefaultSection() {
        if (section == null
                || (!section.equals(TYPE_BOOKMARK) && !section.equals(TYPE_UPLOADS) && !section.equals(TYPE_HISTORY)
                        && !section.equals(TYPE_REGISTRATIONS) && !section.equals(TYPE_REVIEWS))) {
            section = TYPE_UPLOADS;
        }
    }

    /**
     * @return the tabType
     */
    public String getTabType() {

        if (tabType == null) {
            tabType = TYPE_UPLOADS;
        }

        return tabType;
    }

    /**
     * @return
     */
    public String getAttemptedUserName() {
        return attemptedUserName;
    }

    /**
     * @param attemptedUserName
     */
    public void setAttemptedUserName(String attemptedUserName) {
        this.attemptedUserName = attemptedUserName;
    }

    /**
     * @return
     */
    public String getSection() {
        return section;
    }

    /**
     * @param section
     */
    public void setSection(String section) {
        this.section = section;
    }

    /**
     * @return
     */
    public boolean isUserAuthorized() {
        return userAuthorized;
    }

    /**
     * @return
     */
    public String getAuthenticationMessage() {
        return authenticationMessage;
    }

    /**
     * @param authenticationMessage
     */
    public void setAuthenticationMessage(String authenticationMessage) {
        this.authenticationMessage = authenticationMessage;
    }

    /**
     * @param tabType
     */
    public void setTabType(String tabType) {
        this.tabType = tabType;
    }

    /**
     * @return
     */
    public List<Map<String, String>> getTabs() {
        return tabs;
    }

    /**
     * @param tabs
     */
    public void setTabs(List<Map<String, String>> tabs) {
        AbstractHomeActionBean.tabs = tabs;
    }

    /**
     * @return
     */
    public Map<String, List<SearchResultColumn>> getTypescolumns() {
        return typesColumns;
    }

    /**
     * @return
     */
    public String getBaseHomeUrl() {
        return baseHomeUrl;
    }

    /**
     * @param baseHomeUrl
     */
    public void setBaseHomeUrl(String baseHomeUrl) {
        this.baseHomeUrl = baseHomeUrl;
    }

    /**
     * @return
     */
    public String getAuthenticatedUserName() {
        return authenticatedUserName;
    }

    /**
     * @param authenticatedUserName
     */
    public void setAuthenticatedUserName(String authenticatedUserName) {
        this.authenticatedUserName = authenticatedUserName;
    }

    /**
     * @return
     */
    public boolean isShowPublic() {
        return showPublic;
    }

    /**
     * @param showPublic
     */
    public void setShowPublic(boolean showPublic) {
        this.showPublic = showPublic;
    }

    /**
     * @return
     */
    public String getShowpublicYes() {
        return SHOWPUBLIC_YES;
    }

    /**
     * @return
     */
    public String getShowpublicNo() {
        return SHOWPUBLIC_NO;
    }

    /**
     *
     * @param sourceUrl
     * @param uploadedFile
     * @param dcTitle
     */
    protected void harvestUploadedFile(String sourceUrl, FileBean uploadedFile, String dcTitle, String userName) {

        // create and store harvest source for the above source url,
        // don't throw exceptions, as an uploaded file does not have to be
        // harvestable
        HarvestSourceDTO harvestSourceDTO = null;
        try {
            logger.debug("Creating and storing harvest source");
            HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(sourceUrl);
            source.setIntervalMinutes(0);

            dao.addSourceIgnoreDuplicate(source);
            harvestSourceDTO = dao.getHarvestSourceByUrl(sourceUrl);
        } catch (DAOException e) {
            logger.info("Exception when trying to create" + "harvest source for the uploaded file content", e);
        }

        // perform harvest,
        // don't throw exceptions, as an uploaded file does not HAVE to be
        // harvestable
        try {
            if (harvestSourceDTO != null) {
                UploadHarvest uploadHarvest = new UploadHarvest(harvestSourceDTO, uploadedFile, dcTitle, userName);
                CurrentHarvests.addOnDemandHarvest(harvestSourceDTO.getUrl(), userName);
                try{
                    uploadHarvest.execute();
                }
                finally{
                    CurrentHarvests.removeOnDemandHarvest(harvestSourceDTO.getUrl());
                }
            } else {
                logger.debug("Harvest source was not created, so skipping harvest");
            }
        } catch (HarvestException e) {
            logger.info("Exception when trying to harvest uploaded file content", e);
        }
    }

    /**
     *
     * @param uploadedFile
     */
    protected void deleteUploadedFile(FileBean uploadedFile) {

        if (uploadedFile != null) {
            try {
                uploadedFile.delete();
            } catch (IOException e) {
                logger.error("Failed to delete uploaded file [" + uploadedFile + "]", e);
            }
        }
    }

    /**
     * Returns URI of the page where username is replaced with real username. to be used in sorting. Assumed that section is lsat
     * part of urlbinding.
     *
     * @return correct URL for sorting.
     */
    public String getParsedUrlBinding() {
        return "/home/" + attemptedUserName + "/" + section;
    }
}
