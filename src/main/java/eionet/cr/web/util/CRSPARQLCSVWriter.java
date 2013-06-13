package eionet.cr.web.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.List;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * CR specific CSV writer.
 *
 * @author kaido
 */
public class CRSPARQLCSVWriter implements TupleQueryResultWriter {

    /** internal writer object. */
    private Writer writer;

    /**
     * internal data separator variable.
     */
    private char separator;

    /** */
    private List<String> bindingNames;

    /**
     * Creates Writer.
     * @param out outputstream where to write to
     * @param separator data separator for the CSV file
     */
    public CRSPARQLCSVWriter(OutputStream out, char separator) {
        Writer w = new OutputStreamWriter(out, Charset.forName("UTF-8"));
        writer = new BufferedWriter(w, 1024);
        this.separator = separator;
    }

    @Override
    public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
        this.bindingNames = bindingNames;

        try {
            for (int i = 0; i < bindingNames.size(); i++) {
                writer.write(bindingNames.get(i));
                if (i < bindingNames.size() - 1) {
                    writer.write(separator);
                }
            }
            writer.write("\r\n");
        } catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    @Override
    public void endQueryResult() throws TupleQueryResultHandlerException {
        try {
            writer.flush();
        } catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }

    }

    @Override
    public void handleSolution(BindingSet bindingSet) throws TupleQueryResultHandlerException {
        try {
            for (int i = 0; i < bindingNames.size(); i++) {
                String name = bindingNames.get(i);
                Value value = bindingSet.getValue(name);
                if (value != null) {
                    writeValue(value);
                }

                if (i < bindingNames.size() - 1) {
                    writer.write(separator);
                }
            }
            writer.write("\r\n");
        } catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    @Override
    public TupleQueryResultFormat getTupleQueryResultFormat() {
        return TupleQueryResultFormat.CSV;
    }

    /**
     * Writes value to the stream.
     * @param val Value
     * @throws IOException if writing fails
     */
    protected void writeValue(Value val) throws IOException {
        if (val instanceof Resource) {
            writeResource((Resource) val);
        } else {
            writeLiteral((Literal) val);
        }
    }

    /**
     * Writes Resource to the stream.
     * @param res Resource
     * @throws IOException if writing fails
     */

    protected void writeResource(Resource res) throws IOException {
        if (res instanceof URI) {
            writeURI((URI) res);
        } else {
            writeBNode((BNode) res);
        }
    }

    /**
     * Writes uri to the stream.
     * @param uri URI
     * @throws IOException if writing fails
     */
    protected void writeURI(URI uri) throws IOException {
        String uriString = uri.toString();
        writer.write(uriString);
    }

    /**
     * Writes blank node to the stream.
     * @param bNode Blank node
     * @throws IOException if writing fails
     */
    protected void writeBNode(BNode bNode) throws IOException {

        // Note that "_:" is the standard N3 namespace prefix for blank nodes.
        writer.write("_:");
        writer.write(bNode.getID());
    }

    /**
     * Writes literal to the stream.
     * @param literal Literal
     * @throws IOException if writing fails
     */

    private void writeLiteral(Literal literal) throws IOException {
        String label = literal.getLabel();
        URI datatype = literal.getDatatype();
        String language = literal.getLanguage();

        boolean quoted = false;

        if (datatype != null
                && (XMLDatatypeUtil.isIntegerDatatype(datatype) || XMLDatatypeUtil.isDecimalDatatype(datatype) || XMLSchema.DOUBLE
                        .equals(datatype))) {
            try {
                String normalized = XMLDatatypeUtil.normalize(label, datatype);
                writer.write(normalized);
                return; // done
            } catch (IllegalArgumentException e) {
                // not a valid numeric datatyped literal. ignore error and write as
                // (optionally quoted) string instead.
            }
        }

        if (label.contains(Character.toString(separator)) || label.contains(",") || label.contains("\r")
                || label.contains("\n") || label.contains("\"")) {
            quoted = true;

            // escape quotes inside the string
            label = label.replaceAll("\"", "\"\"");

            // add quotes around the string (escaped with a second quote for the
            // CSV parser)
            // label = "\"\"" + label + "\"\"";
        }

        if (quoted) {
            // write opening quote for entire value
            writer.write("\"");
        }

        writer.write(label);

        if (quoted) {
            // write closing quote for entire value
            writer.write("\"");
        }

    }
}
