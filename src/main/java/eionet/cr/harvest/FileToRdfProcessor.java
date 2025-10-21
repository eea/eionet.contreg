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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URLEncoder;
import java.nio.channels.FileLockInterruptionException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import javax.imageio.IIOException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.lang3.StringUtils;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.config.GeneralConfig;
import eionet.cr.harvest.util.FileRdfFormatDetector;
import eionet.cr.util.CompressUtil;
import eionet.cr.util.FileDeletionJob;
import eionet.cr.util.FileUtil;
import eionet.cr.util.xml.ConversionSchema;
import eionet.cr.util.xml.ConversionsParser;
import eionet.cr.util.xml.XmlAnalysis;

/**
 *
 * @author Jaanus Heinlaid
 */
public class FileToRdfProcessor {

    /** Static Log4j logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileToRdfProcessor.class);

    /** The file to process. */
    private File file;

    /** The context URL where this file was downloaded from (i.e. the harvest source). */
    private String contextUrl;

    /** Source content type. */
    private RDFFormat rdfFormat;

    /** The source file's conversion schema. */
    private ConversionSchema conversionSchema;

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
     * @throws IOException
     *             if error at I/O level
     * @throws SAXException
     *             if error in SAX parser when analyzing XML
     * @throws RDFParseException
     *             if RDF parser fails
     * @throws RDFHandlerException
     *             if exception occurs in RDF parser
     */
    public File process() throws IOException, SAXException, RDFHandlerException, RDFParseException {

        // Try unpacking (if the file is not zipped/archived, reference to the same file is returned,
        // otherwise reference to the newly created unpacked file is returned)
        File unpackedFile = tryUnpack(file);
        if (unpackedFile != file) {
            LOGGER.debug(loggerMsg("File was zipped!"));
        }

        // initialize result to null
        File resultFile = null;
        try {
            // See if the unzipped (if it was zipped) file is an XML that can be processed into RDF.
            XmlAnalysis xmlAnalysis = createXmlAnalysis(unpackedFile);
            if (xmlAnalysis != null) {

                // File seems to be XML.
                // Get the file's start element: if it is RDF, the result will be the file as it is.
                String startElemUri = xmlAnalysis.getStartElemUri();
                if (startElemUri != null && startElemUri.startsWith("http://www.w3.org/1999/02/22-rdf-syntax-ns#RDF")) {
                    // Unlikely to reach this block, as we already detected above that the file was not of any RDF format.
                    LOGGER.debug(loggerMsg("Seems to be XML file with rdf:RDF start element"));
                    resultFile = unpackedFile;
                    rdfFormat = RDFFormat.RDFXML;
                } else {
                    // The file's start element was not RDF, so try to convert it to RDF.
                    LOGGER.debug(loggerMsg("Seems to be XML file, attempting RDF conversion"));

                    conversionSchema = xmlAnalysis.getConversionSchema();
                    resultFile = attemptRdfConversion(unpackedFile, conversionSchema.getStringValue(), contextUrl);
                    if (resultFile != null) {
                        rdfFormat = RDFFormat.RDFXML;
                    }
                }
            } else {
                // The file wasn't XML, so see if it is any of the supported RDF formats.
                FileRdfFormatDetector rdfFormatDetector = new FileRdfFormatDetector();
                rdfFormat = rdfFormatDetector.detect(unpackedFile, contextUrl);
                if (rdfFormat != null) {
                    // File was one of RDF formats, so assign to result file.
                    resultFile = unpackedFile;
                } else {
                    // File was not of any RDF format, but log any parsing errors encountered in the process.
                    for (Entry<RDFFormat, Exception> entry : rdfFormatDetector.getParsingExceptions().entrySet()) {
                        String formatName = entry.getKey().getName();
                        Exception formatParsingException = entry.getValue();
                        LOGGER.debug(loggerMsg("Probably not a (valid) " + formatName + " file: " + formatParsingException));
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

            // if file = unzippedFile (in case it could not be unzipped) it is deleted in the calling method
            // will be excluded here to prevent local non-RDF (binary) files to be deleted
            if (resultFile != unpackedFile && file != unpackedFile) {
                FileDeletionJob.register(unpackedFile);
            }
        }
    }

    /**
     * Attempts to unpack the given file.
     *
     * First the method checks if this is a compressed file (e.g. gzipped). If yes, then the file is uncompressed and it is checked
     * if the latter is an archive file (e.g. TAR, ZIP) in which case the archive's first entry is what is finally returned by the
     * method. If the uncompressed file is not an archive file, then the method returns the uncompressed file.
     *
     * However, if the original file was not a compressed file and actually happens to be an archive file, then again the archive's
     * first entry is what is returned by the method.
     *
     * In all other cases the original file is returned as it is.
     *
     * @param file The file to process.
     * @return The resulting file (may be the input file itself, it wasn't a packed file at all).
     */
    private File tryUnpack(File file) {

        File uncompressedFile = new File(file.getAbsolutePath() + ".uncmopressed");
        try {
            CompressUtil.uncompress(file, uncompressedFile);
        } catch (IOException e) {
            // Decompression failed, but check if this is an archive file.
            List<ArchiveEntry> archiveEntries = CompressUtil.getArchiveEntries(file);
            if (CollectionUtils.isNotEmpty(archiveEntries)) {
                // Looks like archived file, so lets extract the first file.
                String firstEntryName = archiveEntries.iterator().next().getName();
                String extractedFileName = file.getName() + ".extracted";
                try {
                    CompressUtil.extract(file, Collections.singletonMap(firstEntryName, extractedFileName));
                    return new File(file.getParent(), extractedFileName);
                } catch (IOException ee) {
                    // Extraction failed, yet at this point it *must* be an archived file. So lets just return the original file.
                    return file;
                }
            } else {
                // Not an archive file either, so return the original file as it is.
                return file;
            }
        }

        if (uncompressedFile.exists() && uncompressedFile.isFile()) {

            // Check if the uncompressed file is an archive file.
            List<ArchiveEntry> archiveEntries = CompressUtil.getArchiveEntries(uncompressedFile);
            if (CollectionUtils.isNotEmpty(archiveEntries)) {

                // Looks like archived file, so lets extract the first file.
                String firstEntryName = archiveEntries.iterator().next().getName();
                String extractedFileName = file.getName() + ".extracted";
                try {
                    CompressUtil.extract(uncompressedFile, Collections.singletonMap(firstEntryName, extractedFileName));
                    // Ensure the uncompressed file gets deleted.
                    FileDeletionJob.register(uncompressedFile);
                    // Return final file, i.e. the extracted one.
                    return new File(file.getParent(), extractedFileName);
                } catch (IOException e) {
                    // Extraction failed, yet at this point it *must* be an archived file. So lets just return the original file.
                    return file;
                }
            } else {
                // The uncompressed file is not an archive file, so return it as it is.
                return uncompressedFile;
            }
        } else {
            // no uncompressed file actually existing
            return file;
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     */
    private XmlAnalysis createXmlAnalysis(File file) throws IOException {

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
     * Returns the {@link RDFFormat} of the analyzed and/or converted file.
     *
     * @return RDF
     */
    public RDFFormat getRdfFormat() {
        return rdfFormat;
    }

    /**
     * @return the conversionSchemaString
     */
    public ConversionSchema getConversionSchema() {
        return conversionSchema;
    }
}
