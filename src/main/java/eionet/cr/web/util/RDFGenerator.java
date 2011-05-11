package eionet.cr.web.util;

import java.io.OutputStream;

import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.rdfxml.RDFXMLWriter;

import eionet.cr.common.CRException;
import eionet.cr.util.sesame.SesameUtil;

/**
 * Helper class for generating RDF from SPARQL query.
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
     * Generates RDF of given harvest source (graph) to the output stream.
     * @param source String harvest source
     * @param output OutputStream where the RDF is sent to
     * @throws CRException if generating fails
     */
    public static void generate(final String source, final OutputStream output) throws CRException {

        RepositoryConnection conn = null;
        String sparql = "CONSTRUCT { ?s ?p ?o } FROM <" + source + "> WHERE { ?s ?p ?o } ";
        RDFHandler rdfxmlWriter = new RDFXMLWriter(output);

        try {
            conn = SesameUtil.getRepositoryConnection();
            SesameUtil.exportGraphQuery(sparql, rdfxmlWriter, conn);
        } catch (Exception e) {
            throw new CRException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }
}
