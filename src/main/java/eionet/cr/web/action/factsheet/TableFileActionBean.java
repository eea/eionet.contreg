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

package eionet.cr.web.action.factsheet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.lang.StringUtils;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.sesame.SesameConnectionProvider;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.sparqlClient.helpers.QueryResult;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 * Factsheet page for cr:TableFile type.
 *
 * @author Juhan Voolaid
 */
@UrlBinding("/tableFile.action")
public class TableFileActionBean extends AbstractActionBean {

    /** The current object URI. */
    private String uri;

    /** Associated SPARQL query. */
    private String spqrqlQuery;

    private QueryResult queryResult;

    /** Factsheet page tabs. */
    private List<TabElement> tabs;

    /**
     * View action.
     *
     * @return
     * @throws DAOException
     * @throws RepositoryException
     * @throws MalformedQueryException
     * @throws QueryEvaluationException
     */
    @DefaultHandler
    public Resolution view() throws DAOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
        initTabs();

        // Get query result - CSV/TSV contents
        TupleQueryResult result = null;
        RepositoryConnection con = null;
        if (StringUtils.isNotEmpty(spqrqlQuery)) {
            try {
                con = SesameConnectionProvider.getRepositoryConnection();
                Query queryObject = con.prepareQuery(QueryLanguage.SPARQL, spqrqlQuery);
                result = ((TupleQuery) queryObject).evaluate();

                if (result != null) {
                    queryResult = new QueryResult(result, true);
                }
            } finally {
                SesameUtil.close(result);
                SesameUtil.close(con);
            }
        }

        return new ForwardResolution("/pages/factsheet/tableFile.jsp");
    }

    /**
     * Initializes tabs.
     *
     * @throws DAOException
     */
    private void initTabs() throws DAOException {
        if (StringUtils.isEmpty(uri)) {
            addCautionMessage("No request criteria specified!");
        } else {
            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);

            Map<String, Integer> predicatePageNumbers = new HashMap<String, Integer>();
            predicatePageNumbers.put(Predicates.CR_SPARQL_QUERY, 1);

            SubjectDTO subject = helperDAO.getFactsheet(uri, null, predicatePageNumbers);
            spqrqlQuery = subject.getObjectValue(Predicates.CR_SPARQL_QUERY);

            FactsheetTabMenuHelper helper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
            tabs = helper.getTabs(FactsheetTabMenuHelper.TabTitle.TABLE_FILE_CONTENTS);
        }
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @param uri
     *            the uri to set
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     * @return the tabs
     */
    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * @return the spqrqlQuery
     */
    public String getSpqrqlQuery() {
        return spqrqlQuery;
    }

    /**
     * @return the queryResult
     */
    public QueryResult getQueryResult() {
        return queryResult;
    }

}
