/*
 * The contents of this file are subject to the Mozilla Public
 *
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency. All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.harvest;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.Util;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadHarvest extends BaseHarvest {

    /** */
    private static final Logger LOGGER = Logger.getLogger(UploadHarvest.class);

    /** File stored in local file system.*/
    private File file;

    /** Uploaded file content type. */
    private String contentType;

    public UploadHarvest(HarvestSourceDTO contextSourceDTO, File file, String fileTitle, String contentType) {

        // call super-class constructor
        super(contextSourceDTO);

        // make sure file bean is not null
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("File must not be null and has to exist in the file system.");
        }

        // assign fields
        this.file = file;

        // set source metadata already detectable right now
        addSourceMetadata(Predicates.CR_BYTE_SIZE, ObjectDTO.createLiteral(String.valueOf(file.length())));
        addSourceMetadata(Predicates.CR_LAST_MODIFIED, ObjectDTO.createLiteral(formatDate(new Date()), XMLSchema.DATETIME));
        if (!StringUtils.isBlank(contentType)) {
            addSourceMetadata(Predicates.CR_MEDIA_TYPE, ObjectDTO.createLiteral(contentType));
        }
        if (!StringUtils.isBlank(fileTitle)) {
            addSourceMetadata(Predicates.DC_TITLE, ObjectDTO.createLiteral(fileTitle));
        }

        // upload harvest should remove the source's previous metadata
        setCleanAllPreviousSourceMetadata(true);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.BaseHarvest#doHarvest()
     */
    @Override
    protected void doHarvest() throws HarvestException {

        int noOfTriples = 0;
        try {

            if (file != null) {
                LOGGER.debug("Loading the file contents into the triple store");
                noOfTriples = processLocalContent(file, contentType);
                setStoredTriplesCount(noOfTriples);
                LOGGER.debug(noOfTriples + " triples stored by the triple store");
            }

            finishWithOK(noOfTriples);

        } catch (IOException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (SAXException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (DAOException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (RDFHandlerException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (RDFParseException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        }
    }

    /**
     * Harvest successfully finished and store required metadata.
     * @param noOfTriples nuber of triples.
     */
    private void finishWithOK(int noOfTriples) {

        // update context source DTO with the results of this harvest
        getContextSourceDTO().setStatements(noOfTriples);
        getContextSourceDTO().setLastHarvest(new Date());
        getContextSourceDTO().setLastHarvestFailed(false);

        // add source metadata resulting from this harvest
        String firstSeen = formatDate(getContextSourceDTO().getTimeCreated());
        String lastRefreshed = formatDate(new Date());

        addSourceMetadata(Predicates.CR_FIRST_SEEN, ObjectDTO.createLiteral(firstSeen, XMLSchema.DATETIME));
        addSourceMetadata(Predicates.CR_LAST_REFRESHED, ObjectDTO.createLiteral(lastRefreshed, XMLSchema.DATETIME));
    }

    /**
     * Harvest finished with errors, store relevant metadata for harves source.
     * @param error Error message.
     * @param noOfTriples number of triples.
     */
    private void finishWithError(Throwable error, int noOfTriples) {

        // update context source DTO with the results of this harvest
        getContextSourceDTO().setStatements(noOfTriples);
        getContextSourceDTO().setLastHarvest(new Date());
        getContextSourceDTO().setLastHarvestFailed(error != null);

        // add harvest message about the given error if it's not null
        if (error != null) {
            String message = error.getMessage() == null ? error.toString() : error.getMessage();
            String stackTrace = Util.getStackTrace(error);
            stackTrace = StringUtils.replace(stackTrace, "\r", "");
            addHarvestMessage(message, HarvestMessageType.ERROR, stackTrace);
        }

        // add source metadata resulting from this harvest
        String firstSeen = formatDate(getContextSourceDTO().getTimeCreated());
        String lastRefreshed = formatDate(new Date());

        addSourceMetadata(Predicates.CR_FIRST_SEEN, ObjectDTO.createLiteral(firstSeen, XMLSchema.DATETIME));
        addSourceMetadata(Predicates.CR_LAST_REFRESHED, ObjectDTO.createLiteral(lastRefreshed, XMLSchema.DATETIME));

        if (error != null) {
            addSourceMetadata(Predicates.CR_ERROR_MESSAGE, ObjectDTO.createLiteral(error.toString()));
        }
    }

    /**
     * @see eionet.cr.harvest.BaseHarvest#getHarvestType()
     */
    @Override
    protected String getHarvestType() {

        return HarvestConstants.TYPE_PUSH;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.harvest.BaseHarvest#afterFinishActions()
     */
    @Override
    protected void afterFinish() {
    }
}
