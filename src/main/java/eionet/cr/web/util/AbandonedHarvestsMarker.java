package eionet.cr.web.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A servlet context listener that is executed on context startup and "mark" the status of all abandoned harvests
 * (i.e. terminated by Tomcat restart, for example)..
 *
 * @author Jaanus
 */
public class AbandonedHarvestsMarker implements ServletContextListener {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbandonedHarvestsMarker.class);

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent context) {

        LOGGER.debug("Abandoned harvests marker starting ...");

        try {
            int count = DAOFactory.get().getDao(HarvestDAO.class).markAbandonedHarvests();
            LOGGER.debug(count + " harvests marked as abandoned!");
        } catch (DAOException e) {
            LOGGER.error("Failed to mark abandoned harvests!", e);
        } finally {
            LOGGER.debug("Abandoned harvests marker finished!");
        }
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent context) {
        // Do nothing here.
    }
}
