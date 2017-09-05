package eionet.cr.web.listener;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.integration.spring.SpringLiquibase;
import liquibase.resource.ResourceAccessor;

import java.sql.Connection;

/**
 *
 * @author Thanos Tourikas
 *
 */
public class SpringLiquibaseVirtuoso extends SpringLiquibase {

    /**
     * Subclasses may override this method add change some database settings such as default schema before returning the
     * database object.
     *
     * @param c
     * @return a Database implementation retrieved from the {@link DatabaseFactory}.
     * @throws DatabaseException
     */
    @Override
//    protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {
    protected Database createDatabase(Connection c) throws DatabaseException {

        DatabaseFactory.getInstance().register(new eionet.liquibase.VirtuosoDatabase());
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        if (getDefaultSchema() != null) {
            database.setDefaultSchemaName(getDefaultSchema());
        }
        return database;
    }

}
