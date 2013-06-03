package eionet.cr.web.util;

import info.aduna.text.StringUtil;

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
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

/**
 * CR specific implementation to TSV output. To get MS Excel to open TSV files correctly they have to have UTF-16LE encoding
 *
 * @author kaido
 */
public class CRSPARQLTSVWriter implements TupleQueryResultWriter {
    /** internal writer. */
    private Writer writer;

    /** local binding names. */
    private List<String> bindingNames;

    /**
     * Creates writer.
     *
     * @param out
     *            outputStream
     */
    public CRSPARQLTSVWriter(OutputStream out) {
        Writer w = new OutputStreamWriter(out, Charset.forName("UTF-16LE"));
        writer = new BufferedWriter(w, 1024);
    }

    @Override
    public void startQueryResult(List<String> bindingNames) throws TupleQueryResultHandlerException {
        this.bindingNames = bindingNames;

        try {
            for (int i = 0; i < bindingNames.size(); i++) {
                writer.write("?"); // mandatory prefix in TSV
                writer.write(bindingNames.get(i));
                if (i < bindingNames.size() - 1) {
                    writer.write("\t");
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
                    writer.write("\t");
                }
            }
            writer.write("\r\n");
        } catch (IOException e) {
            throw new TupleQueryResultHandlerException(e);
        }
    }

    @Override
    public TupleQueryResultFormat getTupleQueryResultFormat() {
        return new TupleQueryResultFormat("SPARQL/TSV", "text/tab-separated-values", Charset.forName("UTF-16LE"), "tsv");
    }

    /**
     * Writes value from the result.
     * @param val value
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
     * writes resource object.
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
     * writes URI object.
     * @param uri UrI
     * @throws IOException if writing fails
     */
    protected void writeURI(URI uri) throws IOException {
        String uriString = uri.toString();
        writer.write("<" + uriString + ">");
    }

    /**
     * writes blank node.
     * @param bNode Blank node
     * @throws IOException if writing fails
     */
    protected void writeBNode(BNode bNode) throws IOException {
        writer.write("_:");
        writer.write(bNode.getID());
    }

    /**
     * writes literal object.
     * @param lit Literal object
     * @throws IOException if writing fails
     */
    private void writeLiteral(Literal lit) throws IOException {
        String label = lit.getLabel();

        boolean quoted = false;
        if (label.contains(",") || label.contains("\r") || label.contains("\n") || label.contains("\"")) {
            quoted = true;
            writer.write("\"");
        }

        writer.write(encodeString(label));

        if (quoted) {
            writer.write("\"");
        }

    }

    /**
     * encodes value string.
     * removes carriage returns but preserves linefeeds to make linefeeds to be handled correctly in TSV
     * @param s String to be encoded
     * @return encoded String value
     */
    private static String encodeString(String s) {
        s = StringUtil.gsub("\\", "\\\\", s);
        s = StringUtil.gsub("\t", "\\t", s);
        s = StringUtil.gsub("\r", "", s);
        s = StringUtil.gsub("\"", "\"\"", s);
        return s;
    }
}
