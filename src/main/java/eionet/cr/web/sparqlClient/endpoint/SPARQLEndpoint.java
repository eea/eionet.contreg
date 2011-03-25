package eionet.cr.web.sparqlClient.endpoint;

import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.output.ByteArrayOutputStream;
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
    public String queryXml(@QueryParam("query") String query) {
        return runQuery(query, FORMAT_XML);
    }
    
    // This method is called if XML is request
    @GET
    @Produces("application/sparql-results+json")
    public String queryJson(@QueryParam("query") String query) {
        return runQuery(query, FORMAT_JSON);
    }

    // This method is called if HTML is request
    @GET
    @Produces(MediaType.TEXT_HTML)
    public String query(@QueryParam("query") String query, @QueryParam("format") String format) {
        if(format != null && format.equals(FORMAT_XML))
            return runQuery(query, FORMAT_XML);
        else if(format != null && format.equals(FORMAT_JSON))
            return runQuery(query, FORMAT_JSON);
        else
            return "<html><title>" + "Query result" + "</title><body>" + runQuery(query, FORMAT_HTML) + "</body></html> ";
    }
    
 // This method is called if is POST request
    @POST
    @Produces( {"application/sparql-results+xml", MediaType.APPLICATION_XML, "application/rdf+xml"})
    public String queryPost(@FormParam("query") String query) {
        return runQuery(query, FORMAT_XML);
    }
    
    private String runQuery(String query, String format) {
        
        String ret = "";
        ByteArrayOutputStream out = new ByteArrayOutputStream();

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
                        ret = new String(out.toByteArray());
                    } else if(format != null && format.equals(FORMAT_JSON)){
                        SPARQLResultsJSONWriter sparqlWriter = new SPARQLResultsJSONWriter(out);
                        resultsTable.evaluate(sparqlWriter);
                        ret = new String(out.toByteArray());
                    } else if(format != null && format.equals(FORMAT_HTML)){
                        StringBuffer sb = new StringBuffer();
                        
                        TupleQueryResult bindings = resultsTable.evaluate();
                        if(bindings != null){
                            sb.append("<table><tr>");
                            List<String> names = bindings.getBindingNames();
                            for(String name : names){
                                sb.append("<th>").append(name).append("</th>");
                            }
                            sb.append("</tr>");
                            for (int row = 0; bindings.hasNext(); row++) {
                                sb.append("<tr>");
                                BindingSet pairs = bindings.next();
                                for (int i = 0; i < names.size(); i++) {
                                    String name = names.get(i);
                                    String val = "";
                                    if(pairs.getValue(name) != null)
                                        val = pairs.getValue(name).stringValue();
                                    sb.append("<td>").append(val).append("</td>");
                                }
                                sb.append("</tr>");
                            }
                            sb.append("</table>");
                        }
                        ret = sb.toString();
                    }
                    
                } finally {
                    con.close();
                }
            } catch (RepositoryException rex) {
                rex.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return "Query is empty!";
        }
        
        return ret;
    }


}
    
