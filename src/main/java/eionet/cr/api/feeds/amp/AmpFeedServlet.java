/*
* The contents of this file are subject to the Mozilla Public
*
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
*
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.api.feeds.amp;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.api.feeds.SubjectsRDFWriter;
import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.common.SubjectProcessor;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.SearchDAO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SesameUtil;

/**
 * The AmpFeedServlet searches for objects of rdf:type
 * "http://rdfdata.eionet.europa.eu/amp/ontology/Output". It then outputs the
 * predicates for which there are XML namespace declarations.
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class AmpFeedServlet extends HttpServlet {

    /** */
    private static final Logger logger = Logger.getLogger(AmpFeedServlet.class);

    /** */
    public static final String sparqlQuery =
        "DEFINE input:inference 'CRInferenceRule'" +
    	" select ?s ?p ?o where {?s ?p ?o." +
    	" { select distinct ?s where {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://rdfdata.eionet.europa.eu/amp/ontology/Output> }}}" +
    	" order by ?s ?p ?o";

    /*
     * (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String methodName = new StringBuffer(AmpFeedServlet.class.getSimpleName()).append(".doGet()").toString();
        logger.debug("Entered " + methodName);
        response.setContentType("text/xml");

        RepositoryConnection conn = null;
        OutputStream outputStream = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            outputStream = response.getOutputStream();
            
            AmpFeedWriter feedWriter = new AmpFeedWriter(outputStream);
            feedWriter.addNamespace(Namespace.CR);
            feedWriter.addNamespace(Namespace.DC);
            feedWriter.addNamespace(Namespace.IMS);
            feedWriter.addNamespace(Namespace.AMP);
            feedWriter.addNamespace(Namespace.OWL);
            
            SesameUtil.executeQuery(AmpFeedServlet.sparqlQuery, feedWriter, conn);
            
            int rowCount = feedWriter.getRowCount();
            if (rowCount==0){
                feedWriter.writeEmptyHeader();
            }
            else{
                feedWriter.closeRDF();
            }
            
            // do a final flush for just in case
            outputStream.flush();

            logger.debug("Number of rows that the query returned: " + rowCount);
            logger.debug("Written triples count: " + feedWriter.getWrittenTriplesCount());
            logger.debug("Written subjects count: " + feedWriter.getWrittenSubjectsCount());
            
        } catch (Exception e) {
            logger.error("Error in " + methodName, e);
            if (!response.isCommitted()) {
                response.sendError(500);
            }
        }

        response.getOutputStream().flush();
    }
}
