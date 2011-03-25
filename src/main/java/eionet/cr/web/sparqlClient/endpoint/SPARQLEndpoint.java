package eionet.cr.web.sparqlClient.endpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang.StringUtils;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;
import eionet.cr.config.GeneralConfig;

@Path("/sparql")
public class SPARQLEndpoint {
    
    private static final String FORMAT_XML = "xml";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_HTML = "html";

    // This method is called if XML is request
    @GET
    @Produces( {"application/sparql-results+xml", MediaType.APPLICATION_XML, "application/rdf+xml"})
    public StreamingOutput queryXml(@QueryParam("query") String query) {
        final String q = query;
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    runQuery(q, FORMAT_XML, output);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
    
    // This method is called if XML is request
    @GET
    @Produces("application/sparql-results+json")
    public StreamingOutput queryJson(@QueryParam("query") String query) {
        final String q = query;
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    runQuery(q, FORMAT_JSON, output);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    // This method is called if HTML is request
    @GET
    @Produces(MediaType.TEXT_HTML)
    public StreamingOutput query(@QueryParam("query") String query, @QueryParam("format") String format) {
        
        final String q = query;
        final String f = format;
        
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    if(f != null && f.equals(FORMAT_XML))
                        runQuery(q, FORMAT_XML, output);
                    else if(f != null && f.equals(FORMAT_JSON))
                        runQuery(q, FORMAT_JSON, output);
                    else
                        runQuery(q, FORMAT_HTML, output);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
    
 // This method is called if is POST request
    @POST
    @Produces( {"application/sparql-results+xml", MediaType.APPLICATION_XML, "application/rdf+xml"})
    public StreamingOutput queryPost(@FormParam("query") String query) {
        final String q = query;
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    runQuery(q, FORMAT_XML, output);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
    
    private void runQuery(String query, String format, OutputStream out) {
        
        if (!StringUtils.isBlank(query)){
            String url = GeneralConfig.getProperty("virtuoso.db.url");
            String username = GeneralConfig.getProperty("virtuoso.db.usr");
            String password = GeneralConfig.getProperty("virtuoso.db.pwd");
            
            try{
                
                Repository myRepository = new VirtuosoRepository(url,username,password);
                myRepository.initialize();
                RepositoryConnection con = myRepository.getConnection();
                try{
                    TupleQuery resultsTable = con.prepareTupleQuery(QueryLanguage.SPARQL, query);
                    
                    if(format != null && format.equals(FORMAT_XML)){
                        SPARQLResultsXMLWriter sparqlWriter = new SPARQLResultsXMLWriter(out);
                        resultsTable.evaluate(sparqlWriter);
                    } else if(format != null && format.equals(FORMAT_JSON)){
                        SPARQLResultsJSONWriter sparqlWriter = new SPARQLResultsJSONWriter(out);
                        resultsTable.evaluate(sparqlWriter);
                    } else if(format != null && format.equals(FORMAT_HTML)){
                        TupleQueryResult bindings = resultsTable.evaluate();
                        if(bindings != null){
                            PrintWriter outWriter = new PrintWriter(out);
                            outWriter.println("<table><tr>");
                            List<String> names = bindings.getBindingNames();
                            for(String name : names){
                                outWriter.println("<th>");
                                outWriter.println(name);
                                outWriter.println("</th>");
                            }
                            outWriter.println("</tr>");
                            for (int row = 0; bindings.hasNext(); row++) {
                                outWriter.println("<tr>");
                                BindingSet pairs = bindings.next();
                                for (int i = 0; i < names.size(); i++) {
                                    String name = names.get(i);
                                    String val = "";
                                    if(pairs.getValue(name) != null)
                                        val = pairs.getValue(name).stringValue();
                                    outWriter.println("<td>");
                                    outWriter.println(val);
                                    outWriter.println("</td>");
                                }
                                outWriter.println("</tr>");
                            }
                            outWriter.println("</table>");
                            outWriter.flush();
                            outWriter.close();
                        }
                    }
                } finally {
                    con.close();
                }
            } catch (RepositoryException rex) {
                rex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
    
