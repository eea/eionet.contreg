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
import java.io.IOException;

import net.sourceforge.stripes.action.FileBean;

import org.apache.log4j.Logger;

import eionet.cr.common.TempFilePathGenerator;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.StagingDatabaseDAO;
import eionet.cr.dto.StagingDatabaseDTO;
import eionet.cr.staging.msaccess.ConversionException;
import eionet.cr.staging.msaccess.Converter;
import eionet.cr.staging.msaccess.VirtuosoCreator;
import eionet.cr.util.FileDeletionJob;

/**
 * A runnable that creates a given staging database and populates it from a given DB file.
 *
 * @author jaanus
 */
public class StagingDatabaseCreator extends Thread {

    /** */
    private static final Logger LOGGER = Logger.getLogger(StagingDatabaseCreator.class);

    /** */
    private StagingDatabaseDTO databaseDTO;
    private FileBean databaseFileBean;

    /**
     * Constructs a {@link StagingDatabaseCreator} for the given database DTO and DB file.
     *
     * @param databaseDTO
     * @param databaseFileBean
     */
    public StagingDatabaseCreator(StagingDatabaseDTO databaseDTO, FileBean databaseFileBean){

        this.databaseDTO = databaseDTO;
        this.databaseFileBean = databaseFileBean;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {

        LOGGER.debug("Staging DB creator started for " + databaseDTO + ", using file " + databaseFileBean.getFileName());

        try {
            execute();
            LOGGER.debug("Staging DB creator finished for " + databaseDTO + ", using file " + databaseFileBean.getFileName());
        } catch (Exception e) {
            LOGGER.error("Staging database creation failed with error", e);
        }
    }

    /**
     * @throws DAOException
     * @throws IOException
     * @throws ConversionException
     *
     */
    private void execute() throws DAOException, IOException, ConversionException {

        // Save FileBean to a "proper" temporary file location.
        File file = TempFilePathGenerator.generate();
        databaseFileBean.save(file);

        // Create the database.
        DAOFactory.get().getDao(StagingDatabaseDAO.class).createDatabase(null);

        // Populate the database from the given file.
        VirtuosoCreator virtuosoCreator = new VirtuosoCreator(databaseDTO.getName());
        try {
            Converter converter = new Converter(virtuosoCreator, false, false);
            converter.convert(file);
            LOGGER.debug("All done!");
        } finally {
            virtuosoCreator.close();
            FileDeletionJob.register(file);
        }
    }
}
