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

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.util.Util;

/**
 * A class that logs a staging database's import log messages into the database.
 *
 * @author jaanus
 */
public class ImportLoggerImpl {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = Logger.getLogger(ImportLoggerImpl.class);

    /** The id of the staging database that is being imported. */
    private int databaseId;

    /** The DAO that serves functions for logging the messages into the database.*/
    private StagingDatabaseDAO dao;

    /**
     * Constructs a new logger for the given database.
     *
     * @param databaseId The id of the staging database that is being imported
     */
    public ImportLoggerImpl(int databaseId) {
        this.databaseId = databaseId;
    }

    /**
     * Logs the given message on "info" level.
     *
     * @param message the message
     */
    public void info(Object message) {
        log(message, ImportLogLevel.INFO);
    }

    /**
     * Logs the given message and throwable on "info" level.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void info(Object message, Throwable throwable) {
        log(message, throwable, ImportLogLevel.INFO);
    }

    /**
     * Logs the given message on "warn" level.
     *
     * @param message the message
     */
    public void warn(Object message) {
        log(message, ImportLogLevel.WARNING);
    }

    /**
     * Logs the given message and throwable on "warn" level.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void warn(Object message, Throwable throwable) {
        log(message, throwable, ImportLogLevel.WARNING);
    }

    /**
     * Logs the given message on "error" level.
     *
     * @param message the message
     */
    public void error(Object message) {
        log(message, ImportLogLevel.ERROR);
    }

    /**
     * Logs the given message and throwable on "error" level.
     *
     * @param message the message
     * @param throwable the throwable
     */
    public void error(Object message, Throwable throwable) {
        log(message, throwable, ImportLogLevel.ERROR);
    }

    /**
     * Convenience method that marks the given log message with the given log level, and forwards it to the DAO.
     *
     * @param message the message
     * @param level the level
     */
    private void log(Object message, ImportLogLevel level) {
        try {
            getDao().addImportLogMessage(databaseId, level.name() + ": " + message + "\n");
        } catch (DAOException e) {
            LOGGER.error("Failed to log database import message", e);
        }
    }

    /**
     * Convenience method that marks the given log message with the given log level, appends with the the given throwable's stack
     * trace and forwards the result to the DAO.
     *
     * @param message the message
     * @param throwable the throwable
     * @param level the level
     */
    private void log(Object message, Throwable throwable, ImportLogLevel level) {
        try {
            getDao().addImportLogMessage(databaseId, level.name() + ": " + message + "\n" + Util.getStackTrace(throwable) + "\n");
        } catch (DAOException e) {
            LOGGER.error("Failed to log database import message", e);
        }
    }

    /**
     * Lazy getter for the {@link #dao}.
     * @return
     */
    private StagingDatabaseDAO getDao() {
        if (dao == null) {
            dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        }
        return dao;
    }
}
