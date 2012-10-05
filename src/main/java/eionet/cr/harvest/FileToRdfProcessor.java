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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.harvest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URLEncoder;
import java.nio.channels.FileLockInterruptionException;
import java.text.MessageFormat;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import javax.imageio.IIOException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.xml.sax.SAXException;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.util.FileRdfFormatDetector;
import eionet.cr.util.FileDeletionJob;
import eionet.cr.util.FileUtil;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 *
 * @author Jaanus Heinlaid
 */
public class FileToRdfProcessor {

    /** */
    private static final Logger LOGGER = Logger.getLogger(FileToRdfProcessor.class);

    /** */
    private File file;

    /** */
    private String contextUrl;

    /**
     * source content type.
     */
    private RDFFormat rdfFormat;

    /**
     *
     * @param file
     * @param contextUrl
     */
    public FileToRdfProcessor(File file, String contextUrl) {

        if (file == null || contextUrl == null) {
            throw new IllegalArgumentException("File and context URL must not be null!");
        }

        this.file = file;
        this.contextUrl = contextUrl;
    }

    /**
     *
     * @param messageObject
     * @return
     */
    private String loggerMsg(Object messageObject) {
        return messageObject + " [" + contextUrl + "]";
    }

    /**
     *
     * @return file if type was known and detected otherwise null
     * @throws IOException if error at I/O level
     * @throws SAXException if error in SAX parser when analyzing XML
     * @throws RDFParseException if RDF parser fails
     * @throws RDFHandlerException if exception occurs in RDF parser
     */
    public File process() throws IOException, SAXException, RDFHandlerException, RDFParseException {

        // try unzipping (if the file is not zipped, reference to the same file is returned,
        // otherwise reference to the newly created unzipped file is returned)
        File unzippedFile = tryUnzip(file);
        if (unzippedFile != file) {
            LOGGER.debug(loggerMsg("File was zipped!"));
        }

        // initialize result to null
        File resultFile = null;
        try {
            // See if the unzipped (*if* it was zipped) file is of any RDF format.
            // If not, see if it is XML that can be converted into RDF.

            FileRdfFormatDetector rdfFormatDetector = new FileRdfFormatDetector();
            rdfFormat = rdfFormatDetector.detect(unzippedFile, contextUrl);
            if (rdfFormat == null){

                // File was not of any RDF format, but log any parsing errors encountered in the process.
                for (Entry<RDFFormat, Exception> entry : rdfFormatDetector.getParsingExceptions().entrySet()) {
                    String formatName = entry.getKey().getName();
                    Exception formatParsingException = entry.getValue();
                    LOGGER.debug(loggerMsg("Probably not a (valid) " + formatName + " file: " + formatParsingException));
                }

                // So the file was not of any RDF format, but see if it's an XML that can be converted into RDF.
                XmlAnalysis xmlAnalysis = getXmlAnalysis(unzippedFile);
                if (xmlAnalysis != null) {

                    // Get the file's start element: if it is RDF, the result will be the file as it is.
                    String startElemUri = xmlAnalysis.getStartElemUri();
                    if (startElemUri != null && startElemUri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")) {
                        // Unlikely to reach this block, as we already detected above that the file was not of any RDF format.
                        LOGGER.debug(loggerMsg("Seems to be XML file with rdf:RDF start element"));
                        resultFile = unzippedFile;
                        rdfFormat = RDFFormat.RDFXML;
                    } else {
                        // The file's start element was not RDF, so try to convert it to RDF.
                        LOGGER.debug(loggerMsg("Seems to be XML file, attempting RDF conversion"));
                        String conversionSchema = xmlAnalysis.getConversionSchema();
                        resultFile = attemptRdfConversion(unzippedFile, conversionSchema, contextUrl);
                        if (resultFile != null){
                            rdfFormat = RDFFormat.RDFXML;
                        }
                    }
                }
            }

            // Return the final resulting file.
            // May be null if the file was not of any RDF format, or not an XML that could be converted into RDF.
            // Shortly: may be null if no RDF format was possible to process out of this file.
            return resultFile;

        } finally {
            // if the result that's going to be returned is not the unzipped file,
            // then delete the latter, as it won't be used outside of this method any more
            // (this includes the case where the result is null)
            if (resultFile != unzippedFile) {
                FileDeletionJob.register(unzippedFile);
            }
        }
    }

    /**
     *
     * @param file
     * @return
     */
    private File tryUnzip(File file) {

        GZIPInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new GZIPInputStream(new FileInputStream(file));
            File unzippedFile = new File(file.getAbsolutePath() + ".unzipped");
            outputStream = new FileOutputStream(unzippedFile);
            IOUtils.copy(inputStream, outputStream);
            return unzippedFile;

        } catch (IOException e) {
            return file;
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(outputStream);
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    private XmlAnalysis getXmlAnalysis(File file) throws IOException {

        XmlAnalysis xmlAnalysis = new XmlAnalysis();
        try {
            xmlAnalysis.parse(file);
            return xmlAnalysis;
        } catch (ParserConfigurationException e) {
            throw new CRRuntimeException("SAX parser configuration error", e);
        } catch (SAXException e) {
            LOGGER.debug(loggerMsg("Probably not a (valid) XML file: " + e));
        } catch (IOException e) {

            if (e instanceof FileNotFoundException) {
                throw e;
            } else if (e instanceof FileLockInterruptionException) {
                throw e;
            } else if (e instanceof IIOException) {
                throw e;
            } else if (e instanceof InterruptedIOException) {
                throw e;
            } else {
                LOGGER.debug(loggerMsg("Probably not a (valid) XML file: " + e));
            }
        }

        return null;
    }

    /**
     *
     * @param conversionSchema
     * @return
     * @throws SAXException
     * @throws IOException
     */
    private File attemptRdfConversion(File file, String conversionSchema, String contextUrl) throws IOException, SAXException {

        // First, attempt to convert by the file's URL. If no conversions found
        // by that, try converting by the given conversion schema.

        // So here we look for conversion by the file's URL.
        String conversionId = null;
        try {
            conversionId = ConversionsParser.parseForSchema(contextUrl).getRdfConversionId();
        } catch (ParserConfigurationException e) {
            throw new CRRuntimeException("SAX parser configuration error");
        }

        // If no conversion was found by the file's URL, look by the given conversion schema
        if (StringUtils.isBlank(conversionId)) {
            try {
                conversionId = ConversionsParser.parseForSchema(conversionSchema).getRdfConversionId();
            } catch (ParserConfigurationException e) {
                throw new CRRuntimeException("SAX parser configuration error");
            }
        } else {
            LOGGER.trace(loggerMsg("Found conversion by the file's URL"));
        }

        // if conversion to RDF has been found, run it and return the converted file
        if (!StringUtils.isBlank(conversionId)) {

            LOGGER.debug(loggerMsg("Found RDF conversion for this schema, going to run it"));

            // prepare conversion URL
            String convertUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
            Object[] args = new String[2];
            args[0] = URLEncoder.encode(conversionId, "UTF-8");
            args[1] = URLEncoder.encode(contextUrl, "UTF-8");
            convertUrl = MessageFormat.format(convertUrl, args);

            // run conversion and save the response to a new file
            File convertedFile = new File(file.getAbsolutePath() + ".converted");
            FileUtil.downloadUrlToFile(convertUrl, convertedFile);

            // return converted file
            return convertedFile;
        } else {
            LOGGER.debug(loggerMsg("No RDF conversion found for this schema or DTD"));
        }

        return null;
    }

    /**
     * Returns the {@link RDFFormat} of the analysed and/or converted file.
     *
     * @return RDF
     */
    public RDFFormat getRdfFormat() {
        return rdfFormat;
    }


    // /**
    // *
    // * @param file
    // * @param contextUrl
    // * @return
    // * @throws IOException

    // */
    // public File process(File file, String contextUrl) throws IOException {
    //
    // if (file == null) {
    // throw new IllegalArgumentException("File must not be null!");
    // }
    //
    // File unzippedFile = null;
    // File processedFile = null;
    // try {
    // // try unzipping (if not zipped, reference to the same file is returned,
    // // otherwise reference to the unzipped file is returned)
    // unzippedFile = tryUnzip(file);
    //
    // // try processing as XML (if it turns out to be RDF, reference to the same file
    // // is returned; otherwise it is attempted to convert the file into RDF, and if
    // // it indeed succeeds, reference to the converted file is returned)
    // processedFile = processAsXml(unzippedFile, contextUrl);
    // return processedFile;
    //
    // } catch (ParserConfigurationException e) {
    // throw new CRRuntimeException("SAX parser configuration error", e);
    // } catch (IOException e) {
    // if (throwThisIOException(e)) {
    // throw e;
    // } else {
    // LOGGER.info("Swallowing this file processing exception: " + e.toString());
    // return null;
    // }
    // } catch (SAXException e) {
    // throw new CRRuntimeException("Expected handler to swallow this SAX parsing error", e);
    // } finally {
    // // if the processed file is not the unzipped file, then it means the processed file is
    // // a new file, in which case we must delete the unzipped file, which is not used any more
    // if (unzippedFile != null && unzippedFile != processedFile) {
    // FileDeletionJob.register(unzippedFile);
    // }
    // }
    // }

    // /**
    // *
    // * @param file
    // * @param contextUrl
    // * @return
    // * @throws ParserConfigurationException
    // * @throws SAXException
    // * @throws IOException
    // */
    // private File processAsXml(File file, String contextUrl) throws ParserConfigurationException, SAXException, IOException {
    //
    // // run XML analysis
    // // (will detect if the file is XML, and has a schema or DTD specified; will not throw XML validation errors)
    // XmlAnalysis xmlAnalysis = new XmlAnalysis();
    // xmlAnalysis.parse(file);
    //
    // // get schema uri, if it's not found then fall back to dtd
    // String schemaOrDtd = xmlAnalysis.getSchemaLocation();
    // if (StringUtils.isBlank(schemaOrDtd)) {
    // schemaOrDtd = xmlAnalysis.getSystemDtd();
    // if (StringUtils.isBlank(schemaOrDtd)) {
    // schemaOrDtd = xmlAnalysis.getPublicDtd();
    // }
    // }
    //
    // // if no schema or DTD still found,
    // // assume the URI of the starting element to be the schema by which conversions should be looked for
    // if (StringUtils.isBlank(schemaOrDtd)) {
    //
    // schemaOrDtd = xmlAnalysis.getStartElemUri();
    // if (schemaOrDtd.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")) {
    //
    // // file seems to be actually RDF, so return its reference back
    // return file;
    // }
    // }
    //
    // // if schema or DTD found, then get its RDF conversion ID
    // String conversionId = null;
    // if (!StringUtils.isBlank(schemaOrDtd)) {
    //
    // ConversionsParser convParser = ConversionsParser.parseForSchema(schemaOrDtd);
    // if (convParser != null) {
    // conversionId = convParser.getRdfConversionId();
    // }
    // }
    //
    // // if conversion to RDF has been found, run it and return the converted file
    // if (!StringUtils.isBlank(conversionId)) {
    //
    // // prepare conversion URL
    // String convertUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_CONVERT_URL);
    // Object[] args = new String[2];
    // args[0] = URLEncoder.encode(conversionId, "UTF-8");
    // args[1] = URLEncoder.encode(contextUrl, "UTF-8");
    // convertUrl = MessageFormat.format(convertUrl, args);
    //
    // // run conversion and save the response to a new file
    // File convertedFile = new File(file.getAbsolutePath() + ".converted");
    // FileUtil.downloadUrlToFile(convertUrl, convertedFile);
    //
    // // return converted file
    // return convertedFile;
    // } else {
    // return null;
    // }
    // }

}
