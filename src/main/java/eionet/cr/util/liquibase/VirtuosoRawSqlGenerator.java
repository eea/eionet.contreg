package eionet.cr.util.liquibase;

import liquibase.database.Database;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.RawSqlGenerator;
import liquibase.statement.core.RawSqlStatement;

/**
 * Virtuoso-specific extension of {@link RawSqlGenerator}.
 * Right now it just forwards everything to the super-class, but that might have to change in the future.
 *
 * @author Jaanus
 */
public class VirtuosoRawSqlGenerator extends RawSqlGenerator {

    /*
     * (non-Javadoc)
     *
     * @see liquibase.sqlgenerator.core.AbstractSqlGenerator#supports(liquibase.statement.SqlStatement, liquibase.database.Database)
     */
    @Override
    public boolean supports(RawSqlStatement statement, Database database) {
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
     * @see liquibase.sqlgenerator.core.RawSqlGenerator#generateSql(liquibase.statement.core.RawSqlStatement,
     * liquibase.database.Database, liquibase.sqlgenerator.SqlGeneratorChain)
     */
    @Override
    public Sql[] generateSql(RawSqlStatement statement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return super.generateSql(statement, database, sqlGeneratorChain);
    }
}
