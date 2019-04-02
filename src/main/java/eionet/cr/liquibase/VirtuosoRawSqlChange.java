package eionet.cr.liquibase;

import eionet.cr.liquibase.VirtuosoDatabase;
import liquibase.change.AbstractSQLChange;
import liquibase.change.DatabaseChange;
import liquibase.change.core.RawSQLChange;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.util.StringUtils;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Virtuoso-specific extension of the {@link RawSQLChange}.
 * Necessary because {@link RawSQLChange} extends {@link AbstractSQLChange} which in turn calls {@link Connection#nativeSQL(String)}
 * prior to finalizing the change SQL that is to be executed. But since Virtuoso's JDBC driver always returns an empty string for
 * that method, we needed to avoid this method being called. This is done in {@link #generateStatements(Database)}.
 *
 * @author Jaanus
 */
@DatabaseChange(
        name = "sql",
        description = "The 'sql' tag allows you to specify whatever sql you want. It is useful for complex changes that aren't supported through Liquibase's automated refactoring tags and to work around bugs and limitations of Liquibase. The SQL contained in the sql tag can be multi-line.\n"
                + "\n"
                + "The createProcedure refactoring is the best way to create stored procedures.\n"
                + "\n"
                + "The 'sql' tag can also support multiline statements in the same file. Statements can either be split using a ; at the end of the last line of the SQL or a go on its own on the line between the statements can be used.Multiline SQL statements are also supported and only a ; or go statement will finish a statement, a new line is not enough. Files containing a single statement do not need to use a ; or go.\n"
                + "\n"
                + "The sql change can also contain comments of either of the following formats:\n"
                + "\n"
                + "A multiline comment that starts with /* and ends with */.\n"
                + "A single line comment starting with <space>--<space> and finishing at the end of the line\n"
                + "Note: By default it will attempt to split statements on a ';' or 'go' at the end of lines. Because of this, if you have a comment or some other non-statement ending ';' or 'go', don't have it at the end of a line or you will get invalid SQL.",
        priority = VirtuosoDatabase.PRIORITY)
public class VirtuosoRawSqlChange extends RawSQLChange {

    /**
     *
     * Class constructor.
     */
    public VirtuosoRawSqlChange() {
        super();
    }

    /**
     *
     * Class constructor.
     *
     * @param sql
     */
    public VirtuosoRawSqlChange(String sql) {
        super(sql);
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.change.AbstractSQLChange#generateStatements(liquibase.database.Database)
     */
    @Override
    public SqlStatement[] generateStatements(Database database) {

        List<SqlStatement> returnStatements = new ArrayList<SqlStatement>();

        if (StringUtils.trimToNull(getSql()) == null) {
            return new SqlStatement[0];
        }

        String processedSQL = normalizeLineEndings(super.getSql());
        String[] statements =
                StringUtils.processMutliLineSQL(processedSQL, isStripComments(), isSplitStatements(), getEndDelimiter());
        for (String statement : statements) {
            returnStatements.add(new RawSqlStatement(statement, getEndDelimiter()));
        }

        return returnStatements.toArray(new SqlStatement[returnStatements.size()]);
    }

    /*
     * (non-Javadoc)
     *
     * @see liquibase.change.core.RawSQLChange#getConfirmationMessage()
     */
    @Override
    public String getConfirmationMessage() {
        return "SQL executed successfully";
    }
}
