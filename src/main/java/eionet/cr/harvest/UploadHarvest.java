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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.xml.sax.SAXException;

import eionet.cr.common.Predicates;
import eionet.cr.common.TempFilePathGenerator;
import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.harvest.util.RDFMediaTypes;
import eionet.cr.util.FileDeletionJob;
import eionet.cr.util.URIUtil;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadHarvest extends BaseHarvest {

    /** */
    private static final Logger LOGGER = Logger.getLogger(UploadHarvest.class);

    /** */
    private FileBean fileBean;

    /**
     *
     * @param contextSourceDTO
     * @param fileBean
     * @param fileTitle
     * @param userName
     */
    public UploadHarvest(HarvestSourceDTO contextSourceDTO, FileBean fileBean, String fileTitle, String userName) {

        // call super-class constructor
        super(contextSourceDTO);

        // make sure file bean is not null
        if (fileBean == null) {
            throw new IllegalArgumentException("File bean must not be null");
        }

        // assign fields
        this.fileBean = fileBean;

        // set source metadata already detectable right now
        addSourceMetadata(Predicates.CR_BYTE_SIZE, ObjectDTO.createLiteral(String.valueOf(fileBean.getSize())));
        addSourceMetadata(Predicates.CR_LAST_MODIFIED, ObjectDTO.createLiteral(formatDate(new Date()), XMLSchema.DATETIME));
        String contentType = fileBean.getContentType();
        if (!StringUtils.isBlank(contentType)) {
            addSourceMetadata(Predicates.CR_MEDIA_TYPE, ObjectDTO.createLiteral(contentType));
        }
        if (!StringUtils.isBlank(fileTitle)) {
            addSourceMetadata(Predicates.DC_TITLE, ObjectDTO.createLiteral(fileTitle));
        }

        // upload harvest should remove the source's previous metadata
        setCleanAllPreviousSourceMetadata(true);
    }

    /**
     * @see eionet.cr.harvest.temp.BaseHarvest#doHarvest()
     */
    @Override
    protected void doHarvest() throws HarvestException {

        int noOfTriples = 0;
        File convertedFile = null;
        InputStream inputStream = null;
        try {
            LOGGER.debug("Pre-parsing the file");

            XmlAnalysis xmlAnalysis = parse();

            // if file is XML
            if (xmlAnalysis != null) {

                LOGGER.debug("File is XML, trying conversion");

                // convert file to RDF, if success then construct input stream from converted file,
                // otherwise construct it from original file

                convertedFile = convert(xmlAnalysis, fileBean);
                if (convertedFile != null && convertedFile.exists()) {

                    LOGGER.debug("Converted file will be harvested");
                    inputStream = new FileInputStream(convertedFile);
                } else {
                    LOGGER.debug("No conversion made, will harvest the file as it is");
                    inputStream = fileBean.getInputStream();
                }
            } else {
                LOGGER.debug("File does not seem to be XML");
            }

            HarvestSourceDAO harvestSourceDAO = DAOFactory.get().getDao(HarvestSourceDAO.class);

            if (inputStream != null) {

                // try to load the stream
                LOGGER.debug("Loading the file contents into the triple store");
                RDFFormat rdfFormat = RDFMediaTypes.toRdfFormat(fileBean.getContentType());
                noOfTriples = harvestSourceDAO.loadIntoRepository(inputStream, rdfFormat, getContextUrl(), true);
                setStoredTriplesCount(noOfTriples);
                LOGGER.debug(noOfTriples + " triples stored by the triple store");
            }

            finishWithOK(noOfTriples);

        } catch (ParserConfigurationException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (IOException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (SAXException e) {
            finishWithError(e, noOfTriples);
            throw new HarvestException(e.toString(), e);
        } catch (OpenRDFException e) {

            finishWithError(e, noOfTriples);
            // when harvesting an uploaded-file, RDF parse exceptions must be ignored,
            // because its only something that CR attempts to do, but doesn't have to be successful at
            if (e instanceof RDFParseException) {
                LOGGER.info("Following exception happened when parsing uploaded file as RDF", e);
            } else {
                throw new HarvestException(e.toString(), e);
            }
        } finally {

            IOUtils.closeQuietly(inputStream);
            FileDeletionJob.register(convertedFile);
        }
    }

    /**
     *
     * @param noOfTriples
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
     *
     * @param error
     * @param noOfTriples
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
     *
     * @return
     * @throws ParserConfigurationException
     */
    private XmlAnalysis parse() throws ParserConfigurationException {

        Exception parsingException = null;
        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        try {
            xmlAnalysis.parse(fileBean);
        } catch (SAXException e) {
            parsingException = e;
        } catch (IOException e) {
            parsingException = e;
        }

        if (parsingException != null) {

            LOGGER.debug("Error when parsing as XML: " + parsingException.toString());
            return null;
        }

        return xmlAnalysis;
    }

    /**
     *
     * @param xmlAnalysis
     * @param fileBean
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private File convert(XmlAnalysis xmlAnalysis, FileBean fileBean) throws IOException, SAXException,
            ParserConfigurationException {

        // detect conversion id, if it's blank then return null, as no point in going further
        String conversionId = getConversionId(xmlAnalysis);
        if (StringUtils.isBlank(conversionId)) {
            return null;
        }

        LOGGER.debug("Found conversion with ID = " + conversionId);

        // because Stripes' FileBean has no API for getting the location of the file
        // on the file system, we use its API to rename it to a location we want to
        // (FileBean.save(File toFile) does a simple rename if toFile is on the same file system)
        File renamedFile = TempFilePathGenerator.generate();
        fileBean.save(renamedFile);

        LOGGER.debug("File bean renamed to " + renamedFile + ", calling conversion service");

        // do the conversion using the conversion service's POST request API

        MultipartEntity multipartEntity = new MultipartEntity();
        multipartEntity.addPart("convert_id", new StringBody(conversionId));
        multipartEntity.addPart("convert_file", new FileBody(renamedFile));

        HttpPost httpPost = new HttpPost(GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_PUSH_URL));
        httpPost.setEntity(multipartEntity);

        HttpResponse response = new DefaultHttpClient().execute(httpPost);

        LOGGER.debug("Conversion service response code: " + response.getStatusLine());

        InputStream inputStream = null;
        OutputStream outputStream = null;
        File convertedFile = new File(renamedFile.getAbsolutePath() + ".converted");
        try {
            LOGGER.debug("Storing conversion response to " + convertedFile);

            inputStream = response.getEntity().getContent();
            outputStream = new FileOutputStream(convertedFile);
            IOUtils.copy(inputStream, outputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }

        return convertedFile;
    }

    /**
     *
     * @param xmlAnalysis
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private String getConversionId(XmlAnalysis xmlAnalysis) throws IOException, SAXException, ParserConfigurationException {

        String result = null;

        // Get schema uri, if it's not found then fall back to DTD.
        String schemaOrDtd = xmlAnalysis.getSchemaLocation();
        if (schemaOrDtd == null || schemaOrDtd.length() == 0) {
            schemaOrDtd = xmlAnalysis.getSystemDtd();
            if (schemaOrDtd == null || !URLUtil.isURL(schemaOrDtd)) {
                schemaOrDtd = xmlAnalysis.getPublicDtd();
            }
        }

        // If no schema or DTD still found, assume the URI of the starting element
        // to be the schema by which conversions should be looked for.
        if (schemaOrDtd == null || schemaOrDtd.length() == 0) {
            schemaOrDtd = xmlAnalysis.getStartElemUri();
        }

        // If schema or DTD found, and it's not rdf:DRF, then get its RDF conversion ID,
        // otherwise assume the file is RDF and return.
        if (schemaOrDtd != null && schemaOrDtd.length() > 0
                && !schemaOrDtd.equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")) {

            boolean isURI = URIUtil.isSchemedURI(schemaOrDtd);
            ObjectDTO objectDTO = isURI ? new ObjectDTO(schemaOrDtd, false) : new ObjectDTO(schemaOrDtd, true);
            addSourceMetadata(Predicates.CR_SCHEMA, objectDTO);
            result = ConversionsParser.getRdfConversionId(schemaOrDtd);
        }

        return result;
    }

    /**
     * @see eionet.cr.harvest.BaseHarvest#getHarvestType()
     */
    protected String getHarvestType() {

        return HarvestConstants.TYPE_PUSH;
    }
}
