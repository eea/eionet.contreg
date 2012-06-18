package eionet.cr.web.util;

import java.io.OutputStream;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import eionet.cr.common.CRException;
import eionet.cr.util.Bindings;
import eionet.cr.util.sesame.SesameUtil;

/**
 * Helper class for generating RDF from SPARQL query.
 * 
 * @author kaido
 * 
 */
public final class RDFGenerator {

    /**
     * to prevent public initializing.
     */
    private RDFGenerator() {

    }

    /**
     * SPARQL for exporting a graph.
     */
    private static final String EXPORT_GRAPH_SPARQL = "CONSTRUCT { ?s ?p ?o } FROM ?source WHERE { ?s ?p ?o } ";

    /**
     * Generates RDF of given harvest source (graph) to the output stream.
     * 
     * @param source String harvest source
     * @param output OutputStream where the RDF is sent to
     * @throws CRException if generating fails
     */
    public static void generate(final String source, final OutputStream output) throws CRException {

        RepositoryConnection conn = null;
        RDFHandler rdfxmlWriter = new RDFXMLWriter(output);

        try {
            conn = SesameUtil.getRepositoryConnection();
            Bindings bindings = new Bindings();
            bindings.setURI("source", source);
            SesameUtil.exportGraphQuery(EXPORT_GRAPH_SPARQL, rdfxmlWriter, conn, bindings);
        } catch (Exception e) {
            throw new CRException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * SPARQL for exporting a graph.
     */
    private static final String EXPORT_PROPERTIES_SPARQL = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o } ";

    /**
     * Generates RDF of given uri proerties to the output stream.
     * 
     * @param uri source uri
     * @param output OutputStream where the RDF is sent to
     * @throws CRException if generating fails
     */
    public static void generateProperties(final String uri, final OutputStream output) throws CRException {

        RepositoryConnection conn = null;
        RDFHandler rdfxmlWriter = new RDFXMLWriter(output);

        try {
            conn = SesameUtil.getRepositoryConnection();
            Bindings bindings = new Bindings();
            bindings.setURI("s", uri);
            SesameUtil.exportGraphQuery(EXPORT_PROPERTIES_SPARQL, rdfxmlWriter, conn, bindings);
        } catch (Exception e) {
            throw new CRException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }
}
