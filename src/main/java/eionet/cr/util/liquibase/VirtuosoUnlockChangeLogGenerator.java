package eionet.cr.util.liquibase;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.UnlockDatabaseChangeLogGenerator;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;

/**
 * Virtuoso-specific extension of the {@link UnlockDatabaseChangeLogGenerator}.
 * Necessary because in Virtuoso the data type of DATABASECHANGELOGLOCK.LOCKED needs to be int, not boolean.
 *
 * @author Jaanus
 */
public class VirtuosoUnlockChangeLogGenerator extends UnlockDatabaseChangeLogGenerator {

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)
     */
    @Override
    public boolean supports(UnlockDatabaseChangeLogStatement statement, Database database) {
        return database instanceof VirtuosoDatabase;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#getPriority()
     */
    @Override
    public int getPriority() {
        return VirtuosoDatabase.PRIORITY;
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.UnlockDatabaseChangeLogGenerator#generateSql(liquibase.statement.core.
     * UnlockDatabaseChangeLogStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
     */
    @Override
    public Sql[] generateSql(UnlockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        String liquibaseSchema = database.getLiquibaseSchemaName();

        UpdateStatement releaseStatement =
                new UpdateStatement(database.getLiquibaseCatalogName(), liquibaseSchema,
                        database.getDatabaseChangeLogLockTableName());
        releaseStatement.addNewColumnValue("LOCKED", Integer.valueOf(0));
        releaseStatement.addNewColumnValue("LOCKGRANTED", null);
        releaseStatement.addNewColumnValue("LOCKEDBY", null);
        releaseStatement.setWhereClause(database.escapeColumnName(database.getLiquibaseCatalogName(), liquibaseSchema,
                database.getDatabaseChangeLogTableName(), "ID")
                + " = 1");

        return SqlGeneratorFactory.getInstance().generateSql(releaseStatement, database);
    }
}
