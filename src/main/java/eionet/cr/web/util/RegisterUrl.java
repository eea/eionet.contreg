package eionet.cr.web.util;

import org.apache.commons.lang.StringUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.scheduled.UrgentHarvestQueue;
import eionet.cr.web.security.CRUser;

/**
 * Utility class for registering URLs.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 */
public final class RegisterUrl {

    /**
     * Hide utility class constructor.
     */
    private RegisterUrl() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * Register a URL.
     *
     * @param url
     *            - URL to register
     * @param user
     *            - Authentication object
     * @param saveToBookmarks
     *            - a flag to say whether to also create a bookmark in user's bookmarks
     * @param label
     *            - bookmark label
     * @throws DAOException
     * @throws HarvestException
     */
    public static void register(String url, CRUser user, boolean saveToBookmarks, String label) throws DAOException,
            HarvestException {
        // register URL
        DAOFactory.get().getDao(HelperDAO.class).registerUserUrl(user, url, saveToBookmarks, label);

        // add the URL into HARVEST_SOURCE
        // (the dao is responsible for handling if HARVEST_SOURCE already has such a URL)

        String urlWithoutFragment = StringUtils.substringBefore(url, "#");
        Integer intervalMinutes = GeneralConfig.getDefaultHarvestIntervalMinutes();

        HarvestSourceDTO source = new HarvestSourceDTO();
        source.setUrl(urlWithoutFragment);
        source.setIntervalMinutes(intervalMinutes);
        source.setPrioritySource(false);
        // FIXME: setOwner missing

        DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIgnoreDuplicate(source);
        // schedule urgent harvest of this URL
        UrgentHarvestQueue.addPullHarvest(urlWithoutFragment, user.getUserName());
    }

    /**
     * Test if the url is already registered
     *
     * @param subject
     * @return
     * @throws DAOException
     */
    public static boolean isSubjectRegistered(String subject) throws DAOException {
        return DAOFactory.get().getDao(HelperDAO.class).isExistingSubject(subject);
    }

    /**
     * Test if the provided subject is already bookmarked by the user
     *
     * @param subject
     * @param user
     * @return
     * @throws DAOException
     */
    public static boolean isSubjectBookmarkedByUser(String subject, CRUser user) throws DAOException {
        return DAOFactory.get().getDao(HelperDAO.class).isSubjectUserBookmark(user, subject);
    }

    public static void saveSubjectUserBookmark(String subject, CRUser user, String label) throws DAOException {
        DAOFactory.get().getDao(HelperDAO.class).addUserBookmark(user, subject, label);
    }
}
