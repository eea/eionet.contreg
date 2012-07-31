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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.util.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.sourceforge.stripes.action.FileBean;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import eionet.cr.common.CRException;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class XmlAnalysis {

    /** */
    private static Log logger = LogFactory.getLog(XmlAnalysis.class);

    /** */
    private Handler handler = new Handler();
    private SAXDoctypeReader doctypeReader = new SAXDoctypeReader();

    /**
     *
     * @param file
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws CRException
     */
    public void parse(File file) throws ParserConfigurationException, SAXException, IOException {

        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            parse(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    public void parse(FileBean fileBean) throws ParserConfigurationException, SAXException, IOException {

        InputStream inputStream = null;
        try {
            inputStream = fileBean.getInputStream();
            parse(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     *
     * @param inputStream
     * @return
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws GDEMException
     * @throws SAXException
     * @throws IOException
     */
    public void parse(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {

        // set up the parser and reader
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        SAXParser parser = parserFactory.newSAXParser();
        XMLReader reader = parser.getXMLReader();

        // turn off validation against schema or dtd (we only need the document to be well-formed XML)
        parserFactory.setValidating(false);
        reader.setFeature("http://xml.org/sax/features/validation", false);
        reader.setFeature("http://apache.org/xml/features/validation/schema", false);
        reader.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        reader.setFeature("http://xml.org/sax/features/namespaces", true);

        // turn on dtd handling
        doctypeReader = new SAXDoctypeReader();
        try {
            parser.setProperty("http://xml.org/sax/properties/lexical-handler", doctypeReader);
        } catch (SAXNotRecognizedException e) {
            logger.warn("Installed XML parser does not provide lexical events", e);
        } catch (SAXNotSupportedException e) {
            logger.warn("Cannot turn on comment processing here", e);
        }

        // set the handler and do the parsing
        handler = new Handler();
        reader.setContentHandler(handler);
        try {
            reader.parse(new InputSource(inputStream));
        } catch (SAXException e) {
            Exception ee = e.getException();
            if (ee == null || !(ee instanceof CRException))
                throw e;
        }
    }

    /**
     * Method returns an URI by which conversions for the analyzed file should be looked for. If the file has a declared schema, it
     * is returned. If not, then the method falls back to system DTD. If that one is not found either, the method falls back to
     * public DTD. And should that one be missing too, the method returns fully qualified URI of the file's start element.
     *
     * @return the result
     */
    public String getConversionSchema() {

        // get schema URI, if it's not found then fall back to system DTD,
        // if it's not found then fall back to public DTD, and if still not
        // found, fall back to the start element's URI

        String result = getSchemaLocation();
        if (StringUtils.isBlank(result)) {
            result = getSystemDtd();
            if (StringUtils.isBlank(result)) {
                result = getPublicDtd();
                if (StringUtils.isBlank(result)) {
                    result = getStartElemUri();
                }
            }
        }

        return result;
    }

    /**
     *
     * @return
     */
    public String getStartElemLocalName() {
        return handler.getStartElemLocalName();
    }

    /**
     *
     * @return
     */
    public String getStartElemNamespace() {
        return handler.getStartElemNamespace();
    }

    /**
     *
     * @return
     */
    public String getSchemaLocation() {
        return handler.getSchemaLocation();
    }

    /**
     *
     * @return
     */
    public String getSchemaNamespace() {
        return handler.getSchemaNamespace();
    }

    /**
     *
     * @return
     */
    public String getStartElemUri() {

        String startElemNamespace = handler.getStartElemNamespace();
        if (startElemNamespace == null) {
            startElemNamespace = "";
        }

        String startElemLocalName = handler.getStartElemLocalName();
        if (startElemLocalName == null) {
            startElemLocalName = "";
        }

        return startElemNamespace + startElemLocalName;
    }

    /**
     *
     * @return
     */
    public String getSystemDtd() {
        return doctypeReader.getDtdSystemId();
    }

    /**
     *
     * @return
     */
    public String getPublicDtd() {
        return doctypeReader.getDtdPublicId();
    }

    /**
     *
     * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
     *
     */
    private class Handler extends DefaultHandler {

        /** */
        private String startElemLocalName = null;
        private String startElemNamespace = null;
        private String schemaLocation = null;
        private String schemaNamespace = null;
        private HashMap<String, String> usedNamespaces = new HashMap<String, String>();

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {

            this.startElemNamespace = uri;
            this.startElemLocalName = localName.length() > 0 ? localName : qName;

            int attrCount = attrs != null ? attrs.getLength() : 0;
            for (int i = 0; i < attrCount; i++) {

                String attrName = attrs.getLocalName(i);
                if (attrName.equalsIgnoreCase("noNamespaceSchemaLocation"))
                    this.schemaLocation = attrs.getValue(i);
                else if (attrName.equalsIgnoreCase("schemaLocation")) {

                    String attrValue = attrs.getValue(i);
                    if (attrValue != null && attrValue.length() > 0) {

                        StringTokenizer tokens = new StringTokenizer(attrValue);
                        if (tokens.countTokens() >= 2) {
                            this.schemaNamespace = tokens.nextToken();
                            this.schemaLocation = tokens.nextToken();
                        } else
                            this.schemaLocation = attrValue;
                    }
                }

                String attrQName = attrs.getLocalName(i);
                if (attrQName.startsWith("xmlns:")) {
                    usedNamespaces.put(attrName, attrs.getValue(i));
                }
            }

            // quit parsing now, since we need info only about the very first element,
            // meaning we need only one call to this method
            throw new SAXException(new CRException());
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#error(org.xml.sax.SAXParseException)
         */
        public void error(SAXParseException e) {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#fatalError(org.xml.sax.SAXParseException)
         */
        public void fatalError(SAXParseException e) {
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.helpers.DefaultHandler#warning(org.xml.sax.SAXParseException)
         */
        public void warning(SAXParseException e) throws SAXException {
        }

        /**
         * @return the startElemLocalName
         */
        public String getStartElemLocalName() {
            return startElemLocalName;
        }

        /**
         * @return the startElemNamespace
         */
        public String getStartElemNamespace() {
            return startElemNamespace;
        }

        /**
         * @return the schemaLocation
         */
        public String getSchemaLocation() {
            return schemaLocation;
        }

        /**
         * @return the schemaNamespace
         */
        public String getSchemaNamespace() {
            return schemaNamespace;
        }
    }

//    public static void main(String[] args) {
//
//        XmlAnalysis info = new XmlAnalysis();
//        try {
//            info.parse(new File("D:/temp/kala.xml"));
//            System.out.println(info.getStartElemLocalName());
//            System.out.println(info.getStartElemNamespace());
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }
}
