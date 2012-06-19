package eionet.cr.util.export;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *
 * Utility class helping in the generation of the Excel files.
 *
 * @author Jaanus Heinlaid
 */
public final class XmlUtil {

    /** */
    public static final String INVALID_ELEMENT_NAME = "unknown";

    /**
     * Hide utility class constructor.
     */
    private XmlUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

    /**
     * write xml element start tag, data and end tag into XmlStreamWriter
     *
     * @param writer
     * @param element
     * @param value
     * @throws XMLStreamException
     */
    public static void writeSimpleDataElement(XMLStreamWriter writer, String element, String value) throws XMLStreamException {
        writer.writeStartElement(element);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    /**
     * Escape invalid characters that are not allowed in XML element names if the name is not still valid, then replace it with
     * INVALID_ELEMENT_NAME
     *
     */
    public static String getEscapedElementName(String elementName) {

        if (elementName == null || elementName.length() == 0)
            elementName = INVALID_ELEMENT_NAME;

        // replace whitespaces and other reserved characters with underscore
        elementName = elementName.replaceAll("[^A-Za-z0-9_-]", "_");

        // add leading unerscore if the name starts with invalid character or if it starts with xml (any case)
        if (!elementName.substring(0, 1).matches("[A-Z]|_|[a-z]") || elementName.toLowerCase().startsWith("xml")) {
            elementName = "_" + elementName;
        }

        return elementName;
    }
}
