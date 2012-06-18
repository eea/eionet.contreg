/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.web.action;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.virtuoso.helpers.QueryExecutor;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.sparqlClient.helpers.SPARQLEndpoints;

/**
 * Action bean for other SPARQL systems. Currently provides SPARQL client functionality.
 * 
 * @author Juhan Voolaid
 */
@UrlBinding("/sparqlclient")
public class SPARQLSystemsAction extends AbstractActionBean {

    /** SPARQL client form page. */
    private static final String FORM_PAGE = "/pages/sparqlSystemsClient.jsp";

    /** Endpint. */
    private String endpoint;

    /** Query. */
    private String query;

    /** Explore. */
    private String explore;

    /** Result. */
    private QueryResult result;

    /**
     * Submits SPARQL query.
     * 
     * @return Resolution
     */
    @DefaultHandler
    public Resolution execute() {

        if (!StringUtils.isBlank(endpoint)) {

            if (!StringUtils.isBlank(explore)) {
                QueryExecutor queryExecutor = new QueryExecutor();
                query = queryExecutor.executeExploreQuery(endpoint, explore);
                result = queryExecutor.getResults();
            } else if (!StringUtils.isBlank(query)) {
                QueryExecutor queryExecutor = new QueryExecutor();
                queryExecutor.executeQuery(endpoint, query);
                result = queryExecutor.getResults();
            }
        }

        return new ForwardResolution(FORM_PAGE);
    }

    /**
     * 
     * @return List<String>
     */
    public List<String> getEndpoints() {

        return SPARQLEndpoints.getInstance();
    }

    /**
     * @return the endpoint
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * @param endpoint the endpoint to set
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query the query to set
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

    /**
     * @param explore the explore to set
     */
    public void setExplore(String explore) {
        this.explore = explore;
    }

    /**
     * @return the explore
     */
    public String getExplore() {
        return explore;
    }
}
