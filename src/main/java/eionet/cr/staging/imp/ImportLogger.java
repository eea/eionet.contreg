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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.util.Util;

/**
 * An extension of {@link Logger} that logs messages of a staging database import process into the relevant table in the database.
 *
 * @author jaanus
 */
public class ImportLogger extends Logger {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ImportLogger.class);

    /** Database whose import is being logged. */
    private StagingDatabaseDTO dbDTO;

    /** The DAO used for storinbg the log messages. */
    private StagingDatabaseDAO dao;

    /**
     * Just call the parent constructor.
     *
     * @param name the logger's name
     */
    public ImportLogger(String name) {
        super(name);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.apache.log4j.Category#forcedLog(java.lang.String, org.apache.log4j.Priority, java.lang.Object, java.lang.Throwable)
     */
    @Override
    protected void forcedLog(String fqcn, Priority level, Object messageObj, Throwable throwable) {

        String message = messageObj == null ? "" : messageObj.toString();
        if (!message.isEmpty() || throwable != null) {

            StringBuilder sb = new StringBuilder();
            sb.append(toString(level)).append(": ").append(message).append("\n");
            if (throwable != null) {
                sb.append(Util.getStackTrace(throwable)).append("\n");
            }

            try {
                getDao().appendToImportLog(dbDTO.getId(), sb.toString());
            } catch (DAOException e) {
                LOGGER.warn("Failed to log an event for database with id = " + dbDTO.getId());
            }
        }
    }

    /**
     * Converts the given {@link Priority} to its suitable string representation for import log.
     * @param priority The priority to convert.
     * @return The string representation.
     */
    private String toString(Priority priority) {

        if (priority.isGreaterOrEqual(Level.ERROR)) {
            return "ERROR";
        } else if (priority.isGreaterOrEqual(Level.WARN)) {
            return "WARNING";
        } else if (priority.isGreaterOrEqual(Level.TRACE)) {
            return "INFO";
        } else {
            return "INFO";
        }
    }

    /**
     * Lazy getter for the {@link #dao}.
     *
     * @return the DAO
     */
    private StagingDatabaseDAO getDao() {
        if (dao == null) {
            dao = DAOFactory.get().getDao(StagingDatabaseDAO.class);
        }
        return dao;
    }

    /**
     * Sets the database DTO.
     *
     * @param dbDTO the new db dto
     */
    public void setDbDTO(StagingDatabaseDTO dbDTO) {
        this.dbDTO = dbDTO;
    }
}
