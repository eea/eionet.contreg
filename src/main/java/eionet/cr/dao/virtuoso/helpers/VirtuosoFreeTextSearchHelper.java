package eionet.cr.dao.virtuoso.helpers;

import java.util.List;

import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.VirtuosoFullTextQuery;

/**
 *
 * @author risto
 *
 */
public class VirtuosoFreeTextSearchHelper extends FreeTextSearchHelper{

    /** */
    private SearchExpression expression;
    private VirtuosoFullTextQuery virtExpression;
    private boolean exactMatch;

    /**
     *
     * @param expression
     * @param virtExpression
     * @param exactMatch
     * @param pagingRequest
     * @param sortingRequest
     */
    public VirtuosoFreeTextSearchHelper(SearchExpression expression,
            VirtuosoFullTextQuery virtExpression, boolean exactMatch, PagingRequest pagingRequest, SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);
        this.expression = expression;
        this.virtExpression = virtExpression;
        this.exactMatch = exactMatch;
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select distinct ?s ?g where { graph ?g {");
        if(exactMatch){
            strBuilder.append("?s ?p ?o .").append(virtExpression.getType())
                    .append(" FILTER (?o = '").append(expression.toString()).append("').");
        } else {
            strBuilder.append(virtExpression.getParsedQuery());
        }
        strBuilder.append("}}");

        return strBuilder.toString();
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    @Override
    public String getCountQuery(List<Object> inParams) {

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("select count(distinct ?s) where { graph ?g {");
        if (exactMatch){
            strBuilder.append("?s ?p ?o .").append(virtExpression.getType())
                    .append(" FILTER (?o = '").append(expression.toString()).append("').");
        } else {
            strBuilder.append(virtExpression.getParsedQuery());
        }
        strBuilder.append("}}");

        return strBuilder.toString();
    }
    
    /* (non-Javadoc)
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getMinMaxHashQuery(java.util.List)
     */
    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        throw new UnsupportedOperationException("Method not implemented");
    }

}
