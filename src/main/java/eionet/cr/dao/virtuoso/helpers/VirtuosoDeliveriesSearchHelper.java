package eionet.cr.dao.virtuoso.helpers;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;

/**
 *
 * @author altnyris
 *
 */
public class VirtuosoDeliveriesSearchHelper extends AbstractSearchHelper {

    private String obligation;
    private String locality;
    private String year;

    /**
     * @param obligation
     * @param locality
     * @param year
     * @param pagingRequest
     * @param sortingRequest
     */
    public VirtuosoDeliveriesSearchHelper(String obligation, String locality, String year, PagingRequest pagingRequest,
            SortingRequest sortingRequest) {

        super(pagingRequest, sortingRequest);
        this.obligation = obligation;
        this.locality = locality;
        this.year = year;
    }

    public String getValuesQuery(List<String> subjectUris) {

        StringBuffer sparql = new StringBuffer();
        if (subjectUris != null && subjectUris.size() > 0) {
            String commaSeparatedSubjects = SPARQLQueryUtil.urisToCSV(subjectUris);
            sparql.append("select distinct ?s ?p ?o ?cname where { ");
            sparql.append("?s ?p ?o .");
            sparql.append("filter (?s IN (").append(commaSeparatedSubjects).append(")) ");
            sparql.append("OPTIONAL { ");
            sparql.append("?s <").append(Predicates.ROD_LOCALITY_PROPERTY).append("> ?loc . ");
            sparql.append("?loc <").append(Predicates.RDFS_LABEL).append("> ?cname ");
            sparql.append("}}");
        }

        return sparql.toString();
    }

    @Override
    public String getUnorderedQuery(List<Object> inParams) {
        StringBuffer sparql = getQuery(false);
        sparql.append("}");
        return sparql.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */
    @Override
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuffer sparql = getQuery(false);
        sparql.append("optional {?s <").append(sortPredicate).append("> ?ord}");
        sparql.append("} ORDER BY ");

        if (sortOrder != null) {
            sparql.append(sortOrder);
        }

        if (sortPredicate != null && sortPredicate.equals(Predicates.RDFS_LABEL)) {
            // If Label is not null then use Label. Otherwise use subject where we replace all / with #
            // and then get the string after last #.
            sparql.append("(bif:lcase(bif:either(bif:isnull(?ord), "
                    + "(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1)), ?ord)))");
        } else {
            sparql.append("(?ord)");
        }
        return sparql.toString();
    }

    /**
     * SPARQL for getting count of subjects in the source.
     *
     * @param inParams
     * @return
     */
    @Override
    public String getCountQuery(List<Object> inParams) {
        StringBuffer sparql = getQuery(true);
        sparql.append("}");
        return sparql.toString();
    }

    private StringBuffer getQuery(boolean isCount) {
        StringBuffer sparql = new StringBuffer();
        sparql.append("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ");
        if (isCount) {
            sparql.append("select count(distinct ?s) where {?s ?p ?o . ");
        } else {
            sparql.append("select distinct ?s where {?s ?p ?o . ");
        }
        sparql.append("?s <").append(Predicates.RDF_TYPE).append("> <").append(Subjects.ROD_DELIVERY_CLASS).append("> .");
        if (!StringUtils.isBlank(obligation)) {
            sparql.append("?s <").append(Predicates.ROD_OBLIGATION_PROPERTY).append("> <").append(obligation).append("> .");
        }
        if (!StringUtils.isBlank(locality)) {
            sparql.append("?s <").append(Predicates.ROD_LOCALITY_PROPERTY).append("> <").append(locality).append("> .");
        }
        if (!StringUtils.isBlank(year)) {
            sparql.append("?s <").append(Predicates.ROD_START_OF_PERIOD).append("> ?startOfPeriod . ");
            sparql.append("?s <").append(Predicates.ROD_END_OF_PERIOD).append("> ?endOfPeriod . ");
            sparql.append("filter (");
            sparql.append("(xsd:int(bif:substring(STR(?startOfPeriod), 1 , 4)) <= ").append(year);
            sparql.append(" AND xsd:int(bif:substring(STR(?endOfPeriod), 1 , 4)) >= ").append(year).append("))");
        }
        return sparql;
    }

}
