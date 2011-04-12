package eionet.cr.web.action;

import java.io.IOException;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import virtuoso.sesame2.driver.VirtuosoRepository;

import eionet.cr.config.GeneralConfig;
import eionet.cr.web.sparqlClient.helpers.CRJsonWriter;
import eionet.cr.web.sparqlClient.helpers.CRXmlWriter;
import eionet.cr.web.sparqlClient.helpers.QueryResult;

/**
 * 
 * @author altnyris
 * 
 */
@UrlBinding("/sparql")
public class SPARQLEndpointActionBean extends AbstractActionBean {

    private static final String FORMAT_XML = "xml";
    private static final String FORMAT_JSON = "json";
    private static final String FORMAT_HTML = "html";

    /** */
    private static final String FORM_PAGE = "/pages/sparqlClient.jsp";

    private static List<String> xmlFormats = new ArrayList<String>();

    static {
        xmlFormats.add("application/sparql-results+xml");
        xmlFormats.add("application/rdf+xml");
        xmlFormats.add("application/xml");
        xmlFormats.add("text/xml");
        xmlFormats.add("application/x-binary-rdf-results-table");
    }

    /** */
    private String query;
    private String newQuery;
    private String format;
    private int nrOfHits;
    private long executionTime;

    private boolean useInferencing;

    /** */
    private QueryResult result;

    /**
     * 
     * @return Resolution
     * @throws OpenRDFException
     */
    @DefaultHandler
    public Resolution execute() throws OpenRDFException {

        String acceptHeader = getContext().getRequest().getHeader("accept");
        String[] accept = null;
        if (acceptHeader != null && acceptHeader.length() > 0) {
            accept = acceptHeader.split(",");
        }

        if (!StringUtils.isBlank(format)) {
            accept[0] = format;
        }

        if (nrOfHits == 0) {
            nrOfHits = 20;
        }
        
        //        
        // If user has marked CR Inferencing checkbob,
        // then add inferencing command to the query
 
        newQuery = StringEscapeUtils.unescapeHtml(query);
        if (useInferencing && !StringUtils.isBlank(query)) {
            String infCommand = "DEFINE input:inference '"
                    + GeneralConfig
                            .getProperty(GeneralConfig.VIRTUOSO_CR_RULESET_NAME)
                    + "'";
            newQuery = infCommand + "\n" + newQuery;
        }

        if (accept != null && xmlFormats.contains(accept[0])) {
            return new StreamingResolution("application/sparql-results+xml") {
                public void stream(HttpServletResponse response)
                        throws Exception {
                    runQuery(newQuery, FORMAT_XML, response.getOutputStream());
                }
            };
        } else if (accept != null
                && accept[0].equals("application/sparql-results+json")) {
            return new StreamingResolution("application/sparql-results+json") {
                public void stream(HttpServletResponse response)
                        throws Exception {
                    runQuery(newQuery, FORMAT_JSON, response.getOutputStream());
                }
            };
        } else {
            if (!StringUtils.isBlank(query)) {
                runQuery(newQuery, FORMAT_HTML, null);
            }
            return new ForwardResolution(FORM_PAGE);
        }
    }

    private void runQuery(String query, String format, OutputStream out) {

        if (!StringUtils.isBlank(query)) {
            String url = GeneralConfig
                    .getProperty(GeneralConfig.VIRTUOSO_DB_URL);
            String username = GeneralConfig
                    .getProperty(GeneralConfig.VIRTUOSO_DB_ROUSR);
            String password = GeneralConfig
                    .getProperty(GeneralConfig.VIRTUOSO_DB_ROPWD);

            try {

                Repository myRepository = new VirtuosoRepository(url, username,
                        password);
                myRepository.initialize();
                RepositoryConnection con = myRepository.getConnection();

                try {
                    TupleQuery resultsTable = con.prepareTupleQuery(
                            QueryLanguage.SPARQL, query);

                    if (format != null && format.equals(FORMAT_XML)) {
                        CRXmlWriter sparqlWriter = new CRXmlWriter(out);
                        resultsTable.evaluate(sparqlWriter);
                    } else if (format != null && format.equals(FORMAT_JSON)) {
                        CRJsonWriter sparqlWriter = new CRJsonWriter(out);
                        resultsTable.evaluate(sparqlWriter);
                    } else if (format != null && format.equals(FORMAT_HTML)) {
                        long startTime = System.currentTimeMillis();
                        TupleQueryResult bindings = resultsTable.evaluate();
                        executionTime = System.currentTimeMillis() - startTime;
                        if (bindings != null) {
                            result = new QueryResult(bindings);
                        }
                    }
                } finally {
                    con.close();
                }
            } catch (RepositoryException rex) {
                rex.printStackTrace();
                addWarningMessage("Repository exception: '" + rex.toString()
                        + "'");
//                throw new RuntimeException(rex.toString(), rex);
            } catch (Exception e) {
                e.printStackTrace();
                addWarningMessage("Error processing SPARQL: '" + e.toString()
                        + "'");
                // throw new RuntimeException(e.toString(), e);
            } finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query
     *            the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the result
     */
    public QueryResult getResult() {
        return result;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getNrOfHits() {
        return nrOfHits;
    }

    public void setNrOfHits(int nrOfHits) {
        this.nrOfHits = nrOfHits;
    }

    public boolean isUseInferencing() {
        return useInferencing;
    }

    public void setUseInferencing(boolean useInferencing) {
        this.useInferencing = useInferencing;
    }

}
