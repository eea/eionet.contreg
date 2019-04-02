package eionet.cr.liquibase;

import eionet.cr.liquibase.VirtuosoDatabase;
import liquibase.database.Database;
import liquibase.exception.CustomPreconditionErrorException;
import liquibase.exception.CustomPreconditionFailedException;
import liquibase.precondition.CustomPrecondition;

/**
 * Custom Precondition for checking if index is NOT present in Virtuoso.
 * @author Kaido
 */
public class VirtuosoIndexNotExists implements CustomPrecondition {

    private String indexName;
    
    private String tableName;
    
    @Override
    public void check(Database database) throws CustomPreconditionFailedException, CustomPreconditionErrorException {
        VirtuosoDatabase virtuosoDatabase = (VirtuosoDatabase)database;
        try {
            if (virtuosoDatabase.indexExists(tableName, indexName)) {
                //according to API CustomPreconditionFailedException must be thrown if condition is FALSE
                throw new CustomPreconditionFailedException("Index exists");
            }
        } catch (CustomPreconditionFailedException cpfe) {
            throw cpfe;
        } catch (Exception e) {
            throw new CustomPreconditionErrorException("Checking precondition failed " + e.getMessage());
        }

    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
}
