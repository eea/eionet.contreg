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

package eionet.cr.staging.exp;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.util.Util;

// TODO: Auto-generated Javadoc
/**
 * An extension of {@link Logger} that logs messages of a staging database RDF export process into the relevant table in the
 * database.
 *
 * @author jaanus
 */
public class ExportLogger extends Logger {

    /** */
    private static final Logger LOGGER = Logger.getLogger(ExportLogger.class);

    /** The ID of the RDF export being logged. */
    private int exportId;

    /** The DAO used for storing the log messages. */
    private StagingDatabaseDAO dao;

    /**
     * Just call the parent constructor.
     *
     * @param name the logger's name
     */
    public ExportLogger(String name) {
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
                getDao().appendToExportLog(exportId, sb.toString());
            } catch (DAOException e) {
                LOGGER.warn("Failed to log an event for the RDF export with id = " + exportId);
            }
        }
    }

    /**
     * To string.
     *
     * @param level the level
     * @return the string
     */
    private String toString(Priority level) {

        if (level.isGreaterOrEqual(Level.ERROR)) {
            return "ERROR";
        } else if (level.isGreaterOrEqual(Level.WARN)) {
            return "WARNING";
        } else if (level.isGreaterOrEqual(Level.TRACE)) {
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
     * Sets the export id.
     *
     * @param exportId the exportId to set
     */
    public void setExportId(int exportId) {
        this.exportId = exportId;
    }
}
