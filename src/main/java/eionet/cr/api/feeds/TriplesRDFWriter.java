package eionet.cr.api.feeds;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import eionet.cr.dto.PredicateDTO;

/**
 * 
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 * 
 */
public class TriplesRDFWriter extends SubjectsRDFWriter {

    public void writeHeader(List<PredicateDTO> distinctPredicates, OutputStream out) throws IOException {

    }

    public void closeRDF(OutputStream out) throws IOException {
        out.write("</rdf:RDF>\n".getBytes());
    }

}
