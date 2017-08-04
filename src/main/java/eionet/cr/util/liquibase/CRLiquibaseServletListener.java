package eionet.cr.util.liquibase;

import javax.servlet.ServletContextEvent;

import liquibase.database.DatabaseFactory;
import liquibase.integration.servlet.LiquibaseServletListener;
import eionet.liquibase.VirtuosoDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * An extension of {@link LiquibaseServletListener} to be instantiated from <listener> tag of web.xml.
 *
 * The idea is to register {@link VirtuosoDatabase} in Liquibase's {@link DatabaseFactory} before proceeding to
 * {@link LiquibaseServletListener#contextInitialized(ServletContextEvent)}.
 *
 * @author Jaanus
 */
public class CRLiquibaseServletListener extends LiquibaseServletListener {

    /** Static logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(CRLiquibaseServletListener.class);

    /*
     * (non-Javadoc)
     *
     * @see liquibase.integration.servlet.LiquibaseServletListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

        LOGGER.info("Initializing " + getClass().getSimpleName());

        DatabaseFactory.getInstance().register(new eionet.liquibase.VirtuosoDatabase());
        super.contextInitialized(servletContextEvent);
    }
}
