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

package eionet.cr.staging.imp;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.imp.msaccess.MSAccessImporter;
import eionet.cr.util.LogUtil;

/**
 * A runnable that creates a given staging database and populates it from a given DB file.
 *
 * @author jaanus
 */
public final class ImportRunner extends Thread {

    /** A hash-map of current runs of {@link ImportRunner}. Keys match to db names, values match to threads. */
    private static final HashMap<String, ImportRunner> CURRENT_RUNS = new HashMap<String, ImportRunner>();

    /** Static logger for this class, entirely configured by Log4j properties. */
    private static final Logger LOGGER = Logger.getLogger(ImportRunner.class);

    /** Dynamic logger for the particular import run by this thread for the given database. */
    private Logger importLogger;

    /** Staging database DTO. */
    private StagingDatabaseDTO dbDTO;

    /** File from which the database is created. */
    private File dbFile;

    /** The {@link StagingDatabaseDAO} used by this thread to access the database. */
    private StagingDatabaseDAO dao;

    /**
     * Constructs a {@link ImportRunner} for the given database DTO and DB file.
     *
     * @param dbDTO The given database DTO.
     * @param dbFile The given file from where the database should be created.
     */
    private ImportRunner(StagingDatabaseDTO dbDTO, File dbFile) {

        this.dbDTO = dbDTO;
        this.dbFile = dbFile;
        importLogger = createLogger(dbDTO);
    }

    /**
     * Creates import logger for the given database-
     * @param dbDTO The given database, as DTO.
     * @return The created import logger.
     */
    private ImportLogger createLogger(StagingDatabaseDTO dbDTO) {

        String loggerName = dbDTO.getName() + "_" + ImportLogger.class.getSimpleName();
        ImportLogger logger = (ImportLogger) Logger.getLogger(loggerName, ImportLoggerFactory.INSTANCE);
        logger.setDbDTO(dbDTO);
        logger.setLevel(Level.TRACE);
        return logger;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        LogUtil.debug("Import for database " + dbDTO.getName() + " started from file " + dbFile.getName(), importLogger, LOGGER);

        try {
            execute(importLogger);
            updateImportStatus(ImportStatus.COMPLETED);
            LogUtil.debug("Import for database " + dbDTO.getName() + " finished from file " + dbFile.getName(), importLogger,
                    LOGGER);
        } catch (Exception e) {
            updateImportStatus(ImportStatus.ERROR);
            LogUtil.error("Staging database creation failed with error", e, importLogger, LOGGER);
        }
    }

    /**
     * The thread's execution body called by {@link #run()}.
     *
     * @param importLogger Logger that should be used by this database creation (i.e. import) thread.
     * @throws DAOException Thrown if database access error happens.
     * @throws ImportException If any other import error happens.
     */
    private void execute(Logger importLogger) throws DAOException, ImportException {

        updateImportStatus(ImportStatus.STARTED);

        // Create the database.
        DAOFactory.get().getDao(StagingDatabaseDAO.class).createDatabase(dbDTO.getName());

        // Populate the database from the given file.
        // TODO Use a factory mechanism to obtain a particular ImporterIF implementation.
        ImporterIF importer = new MSAccessImporter(importLogger);
        importer.doImport(dbFile, dbDTO.getName());
    }

    /**
     * Convenience method that creates an instance of {@link ImportRunner} for the given database from given file, and then starts
     * it.
     *
     * @param dbDTO Will be passed into the private constructor.
     * @param dbFile Will be passed into the private constructor
     * @return The created and started thread.
     */
    public static synchronized ImportRunner start(StagingDatabaseDTO dbDTO, File dbFile) {

        if (dbDTO == null || dbFile == null || !dbFile.exists() || !dbFile.isFile()) {
            throw new IllegalArgumentException("The database DTO and file must be given!");
        }

        String dbName = dbDTO.getName();
        ImportRunner currentRun = CURRENT_RUNS.get(dbName);
        if (currentRun != null) {
            if (currentRun.isAlive()) {
                throw new IllegalStateException("A creator is already running for this staging database: " + dbName);
            } else {
                CURRENT_RUNS.remove(dbName);
            }
        }

        ImportRunner creator = new ImportRunner(dbDTO, dbFile);
        creator.start();
        CURRENT_RUNS.put(dbName, creator);
        return creator;
    }

    /**
     * Updates this import's status in the database.
     *
     * @param importStatus The status to update to.
     */
    private void updateImportStatus(ImportStatus importStatus) {
        try {
            getDao().updateImportStatus(dbDTO.getId(), importStatus);
        } catch (DAOException e) {
            LOGGER.error("Failed to update import status of database " + dbDTO.getName(), e);
        }
    }

    /**
     * Lazy getter for the {@link StagingDatabaseDAO} that this thread should use.
     *
     * @return The DAO.
     */
    private StagingDatabaseDAO getDao() {
        if (dao == null) {
            dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        }
        return dao;
    }
}
