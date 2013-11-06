package eionet.cr.util.liquibase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.InitializeDatabaseChangeLogLockTableGenerator;
import liquibase.statement.core.DeleteStatement;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.InsertStatement;

/**
 * Virtuoso-specific extension of the {@link InitializeDatabaseChangeLogLockTableGenerator}.
 * Necessary because in Virtuoso the data type of DATABASECHANGELOGLOCK.LOCKED needs to be int, not boolean.
 *
 * @author Jaanus
 */
public class VirtuosoInitChangeLogLockTableGenerator extends InitializeDatabaseChangeLogLockTableGenerator {

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)
     */
    @Override
    public boolean supports(InitializeDatabaseChangeLogLockTableStatement statement, Database database) {
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
     * @see liquibase.sqlgenerator.core.InitializeDatabaseChangeLogLockTableGenerator#generateSql(liquibase.statement.core.
     * InitializeDatabaseChangeLogLockTableStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
     */
    @Override
    public Sql[] generateSql(InitializeDatabaseChangeLogLockTableStatement statement, Database database,
            SqlGeneratorChain sqlGeneratorChain) {

        DeleteStatement deleteStatement =
                new DeleteStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                        database.getDatabaseChangeLogLockTableName());
        InsertStatement insertStatement =
                new InsertStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                        database.getDatabaseChangeLogLockTableName()).addColumnValue("ID", 1).addColumnValue("LOCKED", 0);

        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(deleteStatement, database)));
        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(insertStatement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
