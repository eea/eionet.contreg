package eionet.cr.util.liquibase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import liquibase.database.Database;
import liquibase.datatype.DataTypeFactory;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.core.CreateDatabaseChangeLogLockTableGenerator;
import liquibase.statement.NotNullConstraint;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateTableStatement;

/**
 * Virtuoso-specific extension of the {@link CreateDatabaseChangeLogLockTableGenerator}.
 * Necessary because in Virtuoso the data type of DATABASECHANGELOGLOCK.LOCKED needs to be int, not boolean.
 *
 * @author Jaanus
 */
public class VirtuosoCreateChangeLogLockTableGenerator extends CreateDatabaseChangeLogLockTableGenerator {

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)
     */
    @Override
    public boolean supports(CreateDatabaseChangeLogLockTableStatement statement, Database database) {
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
     * @see liquibase.sqlgenerator.core.CreateDatabaseChangeLogLockTableGenerator#generateSql(liquibase.statement.core.
     * CreateDatabaseChangeLogLockTableStatement, liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
     */
    @Override
    public Sql[] generateSql(CreateDatabaseChangeLogLockTableStatement statement, Database database,
            SqlGeneratorChain sqlGeneratorChain) {

        CreateTableStatement createTableStatement =
                new CreateTableStatement(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(),
                        database.getDatabaseChangeLogLockTableName())
                        .setTablespace(database.getLiquibaseTablespaceName())
                        .addPrimaryKeyColumn("ID", DataTypeFactory.getInstance().fromDescription("INT"), null, null, null,
                                new NotNullConstraint())
                        .addColumn("LOCKED", DataTypeFactory.getInstance().fromDescription("INT"), null, new NotNullConstraint())
                        .addColumn("LOCKGRANTED", DataTypeFactory.getInstance().fromDescription("DATETIME"))
                        .addColumn("LOCKEDBY", DataTypeFactory.getInstance().fromDescription("VARCHAR(255)"));
        List<Sql> sql = new ArrayList<Sql>();

        sql.addAll(Arrays.asList(SqlGeneratorFactory.getInstance().generateSql(createTableStatement, database)));

        return sql.toArray(new Sql[sql.size()]);
    }
}
