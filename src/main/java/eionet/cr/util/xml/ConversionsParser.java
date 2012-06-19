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
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import eionet.cr.config.GeneralConfig;
import eionet.cr.util.URLUtil;
import eionet.cr.util.Util;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class ConversionsParser {

    /** */
    private String rdfConversionId = null;
    private String rdfConversionXslFileName = null;

    /**
     *
     * @param file
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
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

    /**
     *
     * @param inputStream
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void parse(InputStream inputStream) throws SAXException, IOException, ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document dom = db.parse(inputStream);

        Element rootElem = dom.getDocumentElement();
        if (rootElem != null) {
            NodeList nl = rootElem.getElementsByTagName("conversion");
            for (int i = 0; nl != null && i < nl.getLength(); i++) {
                readConversion((Element) nl.item(i));
            }
        }
    }

    /**
     *
     * @param conversionElement
     */
    private void readConversion(Element conversionElement) {

        if (conversionElement != null) {

            boolean isRDFConversion = false;
            NodeList nl = conversionElement.getElementsByTagName("result_type");
            if (nl != null && nl.getLength() > 0) {
                Element element = (Element) nl.item(0);
                Text text = (Text) element.getFirstChild();
                if (text != null) {
                    String resultType = text.getData();
                    if (resultType != null && resultType.equals("RDF"))
                        isRDFConversion = true;
                }
            }

            if (isRDFConversion && (rdfConversionId == null || rdfConversionId.length() == 0)) {

                nl = conversionElement.getElementsByTagName("convert_id");
                if (nl != null && nl.getLength() > 0) {
                    Element element = (Element) nl.item(0);
                    Text text = (Text) element.getFirstChild();
                    if (text != null) {
                        this.rdfConversionId = text.getData();
                    }
                }

                nl = conversionElement.getElementsByTagName("xsl");
                if (nl != null && nl.getLength() > 0) {
                    Element element = (Element) nl.item(0);
                    Text text = (Text) element.getFirstChild();
                    if (text != null) {
                        this.rdfConversionXslFileName = text.getData();
                    }
                }
            }
        }
    }

    /**
     *
     * @param schemaUri
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static ConversionsParser parseForSchema(String schemaUri) throws IOException, SAXException,
    ParserConfigurationException {

        String listConversionsUrl = GeneralConfig.getRequiredProperty(GeneralConfig.XMLCONV_LIST_CONVERSIONS_URL);
        listConversionsUrl = MessageFormat.format(listConversionsUrl, Util.toArray(URLEncoder.encode(schemaUri)));

        URL url = new URL(listConversionsUrl);
        URLConnection urlConnection = url.openConnection();
        InputStream inputStream = null;
        try {
            // avoid keep-alive by setting "Connection: close" header
            urlConnection.setRequestProperty("Connection", "close");
            inputStream = urlConnection.getInputStream();
            ConversionsParser conversionsParser = new ConversionsParser();
            conversionsParser.parse(inputStream);
            return conversionsParser;
        } finally {
            IOUtils.closeQuietly(inputStream);
            URLUtil.disconnect(urlConnection);
        }
    }

    /**
     *
     * @param schemaUri
     * @return
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static String getRdfConversionId(String schemaUri) throws IOException, SAXException, ParserConfigurationException {

        ConversionsParser convParser = ConversionsParser.parseForSchema(schemaUri);
        if (convParser != null) {
            return convParser.getRdfConversionId();
        } else {
            return null;
        }
    }

    /**
     * @return the rdfConversionId
     */
    public String getRdfConversionId() {
        return rdfConversionId;
    }

    /**
     * @return the rdfConversionXslFileName
     */
    public String getRdfConversionXslFileName() {
        return rdfConversionXslFileName;
    }
}
