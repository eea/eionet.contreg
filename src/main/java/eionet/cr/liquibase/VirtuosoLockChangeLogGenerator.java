package eionet.cr.liquibase;

import eionet.cr.liquibase.VirtuosoDatabase;
import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.UpdateStatement;

import java.sql.Timestamp;

/**
 * Virtuoso-specific extension of the {@link LockDatabaseChangeLogGenerator}.
 * Necessary because in Virtuoso the data type of DATABASECHANGELOGLOCK.LOCKED needs to be int, not boolean.
 *
 * @author Jaanus
 */
public class VirtuosoLockChangeLogGenerator extends LockDatabaseChangeLogGenerator {

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)
     */
    @Override
    public boolean supports(LockDatabaseChangeLogStatement statement, Database database) {
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
     * @see
     * liquibase.sqlgenerator.core.LockDatabaseChangeLogGenerator#generateSql(liquibase.statement.core.LockDatabaseChangeLogStatement
     * , liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
     */
    @Override
    public Sql[] generateSql(LockDatabaseChangeLogStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {

        String liquibaseSchema = database.getLiquibaseSchemaName();
        String liquibaseCatalog = database.getLiquibaseCatalogName();

        UpdateStatement updateStatement =
                new UpdateStatement(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogLockTableName());
        updateStatement.addNewColumnValue("LOCKED", Integer.valueOf(1));
        updateStatement.addNewColumnValue("LOCKGRANTED", new Timestamp(new java.util.Date().getTime()));
        updateStatement.addNewColumnValue("LOCKEDBY", hostname + " (" + hostaddress + ")");
        updateStatement.setWhereClause(database.escapeColumnName(liquibaseCatalog, liquibaseSchema,
                database.getDatabaseChangeLogTableName(), "ID")
                + " = 1 AND "
                + database.escapeColumnName(liquibaseCatalog, liquibaseSchema, database.getDatabaseChangeLogTableName(), "LOCKED")
                + " = 0");

        return SqlGeneratorFactory.getInstance().generateSql(updateStatement, database);
    }
}
