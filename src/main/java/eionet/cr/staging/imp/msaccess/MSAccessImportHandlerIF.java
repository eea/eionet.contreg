package eionet.cr.staging.imp.msaccess;

import java.util.Map;

import com.healthmarketscience.jackcess.Table;

import eionet.cr.staging.imp.ImportException;
import eionet.cr.staging.imp.ImportHandlerIF;

/**
 * An interface that provides methods for handling the tables and rows found in a staging database file.
 *
 * @author jaanus
 */
public interface MSAccessImportHandlerIF extends ImportHandlerIF {

    /**
     * Handle start of new table.
     *
     * @param table the table
     * @throws ImportException the import exception
     */
    void newTable(Table table) throws ImportException;

    /**
     * Handle new row from the given table.
     *
     * @param table the table
     * @param row the row
     * @throws ImportException the import exception
     */
    void processRow(Table table, Map<String, Object> row) throws ImportException;
}
