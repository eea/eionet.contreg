/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        jaanus
 */

package eionet.cr.staging.imp.msaccess;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

import eionet.cr.staging.imp.ImportException;
import eionet.cr.staging.imp.ImporterIF;
import eionet.cr.util.LogUtil;

// TODO: Auto-generated Javadoc
/**
 * An implementation of {@link ImporterIF} that is capable of importing MS Access files.
 *
 * @author jaanus
 */
public class MSAccessImporter implements ImporterIF {

    /** */
    private static final Logger LOGGER = Logger.getLogger(MSAccessImporter.class);

    /** For logging the import log messages. */
    private Logger importLogger;

    /** If true, only the staging database's structure should be imported and no data. */
    private boolean structOnly;

    /** If true, only the staging database's data should be imported, assuming the structure is already in place. */
    private boolean dataOnly;

    /**
     * Constructs a new instance with the given {@link ImportLoggerImpl}.
     *
     * @param importLogger the import logger
     */
    public MSAccessImporter(Logger importLogger) {

        this.importLogger = importLogger;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.staging.imp.ImporterIF#doImport(java.io.File, java.lang.String)
     */
    @Override
    public void doImport(File file, String dbName) throws ImportException {

        Database database = null;
        MSAccessImportHandlerIF handler = null;
        try {
            database = openDatabase(file);
            // TODO Use a factory mechanism to obtain a particular ImportHanderID implementation.
            handler = new VirtuosoHandler(dbName, importLogger);

            Set<String> tableNames = getTableNames(database);
            if (tableNames == null || tableNames.isEmpty()) {
                LogUtil.info("Found no tables in the database", LOGGER, importLogger);
                return;
            }

            for (String tableName : tableNames) {

                Table table = getTable(database, tableName);
                if (!dataOnly) {
                    handler.newTable(table);
                }

                if (!structOnly) {

                    LogUtil.info("Going to process the " + table.getRowCount() + " rows of table " + tableName, LOGGER, importLogger);

                    int rowNum = 0;
                    for (Map<String, Object> row : table) {
                        try {
                            rowNum++;
                            handler.processRow(table, row);
                        } catch (ImportException e) {
                            LogUtil.error(e.getClass().getSimpleName() + " at row #" + rowNum, LOGGER, importLogger);
                            throw e;
                        }
                    }
                }
            }

            handler.endOfFile();
        } finally {
            handler.close();
            close(database);
        }
    }

    /**
     * Gets the table.
     *
     * @param database the database
     * @param tableName the table name
     * @return the table
     * @throws ImportException the import exception
     */
    private Table getTable(Database database, String tableName) throws ImportException {
        try {
            return database.getTable(tableName);
        } catch (IOException e) {
            throw new ImportException("Failed to get table " + tableName, e);
        }
    }

    /**
     * Gets the table names.
     *
     * @param database the database
     * @return the table names
     * @throws ImportException the import exception
     */
    private Set<String> getTableNames(Database database) throws ImportException {

        try {
            return database.getTableNames();
        } catch (IOException e) {
            throw new ImportException("Failed to get the table names of the database", e);
        }
    }

    /**
     * Open database.
     *
     * @param file the file
     * @return the database
     * @throws ImportException the import exception
     */
    private Database openDatabase(File file) throws ImportException {
        try {
            Database database = Database.open(file);
            if (database == null) {
                throw new ImportException("Failed to open database at this file: " + file);
            }
            return database;
        } catch (IOException e) {
            throw new ImportException("Failed to open database at this file: " + file, e);
        }
    }

    /**
     * Close.
     *
     * @param database the database
     */
    private void close(Database database) {
        if (database != null) {
            try {
                database.close();
            } catch (IOException e) {
                // Deliberately ignore closing exceptions
            }
        }
    }
}
