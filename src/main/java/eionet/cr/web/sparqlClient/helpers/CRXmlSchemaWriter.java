package eionet.cr.web.sparqlClient.helpers;

import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

import eionet.cr.util.export.SchemaHelper;
import eionet.cr.util.export.XmlElementMetadata;

/**
 * A {@link TupleQueryResultWriter} that writes tuple query results in the XML with Schema (Microsoft Office format).
 *
 * @author Juhan Voolaid
 */
public class CRXmlSchemaWriter implements TupleQueryResultWriter {

    protected static final String SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";
    protected static final String SCHEMA_NS_PREFIX = "xsd";

    private static final String ENCODING = "UTF-8";
    private static final String ROOT_ELEMENT = "root";
    protected static final String DATA_ROOT_ELEMENT = "dataroot";
    protected static final String ROW_ELEMENT = "resources";

    private Map<String, XmlElementMetadata> elements = null;

    /**
     * XMLWriter to write XML to.
     */
    private XMLStreamWriter writer = null;

    public CRXmlSchemaWriter(OutputStream out) throws XMLStreamException, FactoryConfigurationError {
        writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out, ENCODING);
        elements = new LinkedHashMap<String, XmlElementMetadata>();
    }

    public final TupleQueryResultFormat getTupleQueryResultFormat() {
        return TupleQueryResultFormat.SPARQL;
    }

    public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
        try {
            for (String bindingName : bindingNames) {
                elements.put(bindingName, new XmlElementMetadata(bindingName));
            }

            writer.writeStartDocument(ENCODING, "1.0");
            writer.writeStartElement(ROOT_ELEMENT);
            writer.writeNamespace(SCHEMA_NS_PREFIX, SCHEMA_NS_URI);

            writer.writeStartElement(DATA_ROOT_ELEMENT);
        } catch (Exception e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    public void endQueryResult() throws TupleQueryResultHandlerException {
        try {
            writer.writeEndElement();
            // write XML schema
            new SchemaHelper(writer, elements).writeXmlSchema();
            writer.writeEndDocument();
        } catch (Exception e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        try {
            writer.writeStartElement(ROW_ELEMENT);

            for (Binding binding : bindingSet) {
                if (binding.getValue() != null) {
                    XmlElementMetadata meta = elements.get(binding.getName());
                    meta.setType(binding.getValue().stringValue());
                    meta.setMaxLength(binding.getValue().stringValue().length());

                    writer.writeStartElement(binding.getName());
                    writer.writeCharacters(binding.getValue().stringValue());
                    writer.writeEndElement();
                }
            }

            writer.writeEndElement();
        } catch (Exception e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

}
