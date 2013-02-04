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

package eionet.cr.staging;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.imp.ImportException;
import eionet.cr.staging.imp.ImportLoggerImpl;
import eionet.cr.staging.imp.ImportStatus;
import eionet.cr.staging.imp.ImporterIF;
import eionet.cr.staging.imp.msaccess.MSAccessImporter;

/**
 * A runnable that creates a given staging database and populates it from a given DB file.
 *
 * @author jaanus
 */
public final class StagingDatabaseCreator extends Thread {

    /** */
    private static final HashMap<String, StagingDatabaseCreator> CURRENT_RUNS = new HashMap<String, StagingDatabaseCreator>();

    /** */
    private static final Logger LOGGER = Logger.getLogger(StagingDatabaseCreator.class);

    /** */
    private StagingDatabaseDTO dbDTO;

    /** */
    private File dbFile;

    /** */
    private StagingDatabaseDAO dao;

    /**
     * Constructs a {@link StagingDatabaseCreator} for the given database DTO and DB file.
     *
     * @param dbDTO The given database DTO.
     * @param dbFile The given file from where the database should be cerated.
     */
    private StagingDatabaseCreator(StagingDatabaseDTO dbDTO, File dbFile) {

        this.dbDTO = dbDTO;
        this.dbFile = dbFile;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        LOGGER.debug("Staging DB creator started for " + dbDTO + ", using file " + dbFile.getName());

        ImportLoggerImpl importLogger = new ImportLoggerImpl(dbDTO.getId());
        try {
            execute(importLogger);
            LOGGER.debug("Staging DB creator finished for " + dbDTO + ", using file " + dbFile.getName());
        } catch (Exception e) {
            updateImportStatus(ImportStatus.ERROR);
            String message = "Staging database creation failed with error";
            importLogger.error(message, e);
            LOGGER.error(message, e);
        }
    }

    /**
     * The thread's execution body called by {@link #run()}.
     *
     * @param importLogger Logger that should be used by this database creation (i.e. import) thread.
     * @throws DAOException Thrown if database access error happens.
     * @throws ImportException If any other import error happens.
     */
    private void execute(ImportLoggerImpl importLogger) throws DAOException, ImportException {

        updateImportStatus(ImportStatus.STARTED);

        // Create the database.
        DAOFactory.get().getDao(StagingDatabaseDAO.class).createDatabase(dbDTO.getName());

        // Populate the database from the given file.
        // TODO Use a factory mechanism to obtain a particular ImporterIF implementation.
        ImporterIF importer = new MSAccessImporter(importLogger);
        importer.doImport(dbFile, dbDTO.getName());

        updateImportStatus(ImportStatus.COMPLETED);
        String message = "All done!";
        importLogger.info(message);
        LOGGER.debug(message);
    }

    /**
     * Convenience method that creates an instance of {@link StagingDatabaseCreator} for the given database from given file, and
     * then starts it.
     * @param dbDTO Will be passed into the private constructor.
     * @param dbFile Will be passed into the private constructor
     * @return The created and started thread.
     */
    public static synchronized StagingDatabaseCreator start(StagingDatabaseDTO dbDTO, File dbFile) {

        String dbName = dbDTO.getName();
        StagingDatabaseCreator currentRun = CURRENT_RUNS.get(dbName);
        if (currentRun != null) {
            if (currentRun.isAlive()) {
                throw new IllegalStateException("A creator is already running for this staging database: " + dbName);
            } else {
                CURRENT_RUNS.remove(dbName);
            }
        }

        StagingDatabaseCreator creator = new StagingDatabaseCreator(dbDTO, dbFile);
        creator.start();
        CURRENT_RUNS.put(dbName, creator);
        return creator;
    }

    /**
     * Updates this import's status in the database.
     * @param importStatus The status to update to.
     * @throws DAOException If a database access error happens.
     *
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
     * @return The DAO.
     */
    private StagingDatabaseDAO getDao() {
        if (dao == null) {
            dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        }
        return dao;
    }
}
