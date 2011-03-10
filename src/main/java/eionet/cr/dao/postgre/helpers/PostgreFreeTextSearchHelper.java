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
package eionet.cr.dao.postgre.helpers;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.dao.helpers.FreeTextSearchHelper;
import eionet.cr.dao.util.SearchExpression;
import eionet.cr.util.Hashes;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.PostgreSQLFullTextQuery;
import eionet.cr.web.util.columns.SubjectLastModifiedColumn;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreFreeTextSearchHelper extends FreeTextSearchHelper{

    /** */
//	public enum FilterType { ANY_OBJECT, ANY_FILE, TEXTS, DATASETS, IMAGES, EXACT_MATCH };

    /** */
    private SearchExpression expression;
    private PostgreSQLFullTextQuery pgExpression;
//	private FilterType filter = FilterType.ANY_OBJECT;

    /**
     *
     * @param expression
     * @param pagingRequest
     * @param sortingRequest
     */
    public PostgreFreeTextSearchHelper(SearchExpression expression,
            PostgreSQLFullTextQuery pgExpression,
            PagingRequest pagingRequest,
            SortingRequest sortingRequest){

        super(pagingRequest, sortingRequest);
        this.expression = expression;
        this.pgExpression = pgExpression;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getUnorderedQuery(java.util.List)
     */
    public String getUnorderedQuery(List<Object> inParams){

        StringBuffer buf = new StringBuffer().
        append("select distinct F.SUBJECT as ").append(PairReader.LEFTCOL).append(", ").
        append("(CASE WHEN F.OBJ_DERIV_SOURCE<>0 THEN F.OBJ_DERIV_SOURCE ELSE F.SOURCE END)").
        append(" as ").append(PairReader.RIGHTCOL).append(" from SPO as F ");


        buf.append(addFilterParams());


        buf.append(getFreetextQueryCondition(inParams));

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getOrderedQuery(java.util.List)
     */

    private String addFilterParams(){

        StringBuffer buf = new StringBuffer();

        if (filter != FilterType.ANY_OBJECT){
            buf.append(" join SPO as Ty on F.SUBJECT=Ty.SUBJECT ");
            buf.append(" AND Ty.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).append(" ");

            long objectHash = 0;

            if (filter == FilterType.ANY_FILE){
                objectHash = Hashes.spoHash(Subjects.CR_FILE);
            } else if (filter == FilterType.DATASETS){
                objectHash = Hashes.spoHash(Predicates.DC_MITYPE_DATASET);
            } else if (filter == FilterType.IMAGES){
                objectHash = Hashes.spoHash(Predicates.DC_MITYPE_IMAGE);
            } else if (filter == FilterType.TEXTS){
                objectHash = Hashes.spoHash(Predicates.DC_MITYPE_TEXT);
            }

            buf.append(" AND Ty.object_hash =").append(objectHash).append(" ");
        }
        return buf.toString();
    }

    protected String getOrderedQuery(List<Object> inParams){

        StringBuffer subSelect = new StringBuffer().
        append("select distinct on (").append(PairReader.LEFTCOL).append(")").
        append(" F.SUBJECT as ").append(PairReader.LEFTCOL).append(", ").
        append("(CASE WHEN F.OBJ_DERIV_SOURCE<>0 THEN F.OBJ_DERIV_SOURCE ELSE F.SOURCE END)").
        append(" as ").append(PairReader.RIGHTCOL).append(", ");

        if (sortPredicate.equals(SubjectLastModifiedColumn.class.getSimpleName())){
            subSelect.append(" RESOURCE.LASTMODIFIED_TIME as OBJECT_ORDERED_BY from SPO as F").
            append(" left join RESOURCE on (F.SUBJECT=RESOURCE.URI_HASH)");
        }
        else{
            subSelect.append(" ORDERING.OBJECT as OBJECT_ORDERED_BY from SPO as F").
            append(" left join SPO as ORDERING on").
            append(" (F.SUBJECT=ORDERING.SUBJECT and ORDERING.PREDICATE=").
            append(Hashes.spoHash(sortPredicate)).append(")");
        }
        subSelect.append(addFilterParams());
        subSelect.append(getFreetextQueryCondition(inParams));

        StringBuffer buf = new StringBuffer().
        append("select * from (").append(subSelect).append(") as FOO order by OBJECT_ORDERED_BY");

        return buf.toString();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.postgre.helpers.AbstractSearchHelper#getCountQuery(java.util.List)
     */
    public String getCountQuery(List<Object> inParams){

        StringBuffer buf = new StringBuffer("select count(distinct F.SUBJECT) from SPO as F ");
        buf.append(addFilterParams());
        buf.append(getFreetextQueryCondition(inParams));

        return buf.toString();
    }

    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        StringBuffer buf = new StringBuffer("select min(F.SUBJECT) as LCOL, max(F.SUBJECT) as RCOL from SPO as F");
        buf.append(addFilterParams());
        buf.append(getFreetextQueryCondition(inParams));

        return buf.toString();
    }

    private String getFreetextQueryCondition(List<Object> inParams) {

        StringBuffer buf = new StringBuffer();

        if (expression.isUri() || expression.isHash()){
            buf.append(" where F.OBJECT_HASH=?");
            inParams.add(expression.isHash() ?
                    Long.valueOf(expression.toString()) : Long.valueOf(Hashes.spoHash(expression.toString())));
        }
        else{
            buf.
            append(" where to_tsvector('simple', F.OBJECT) @@ to_tsquery('simple', ?)").
            append(" and F.LIT_OBJ='Y'");
            inParams.add(pgExpression.getParsedQuery());

            HashSet<String> phrases = pgExpression.getPhrases();
            for (String phrase : phrases){
                if (!StringUtils.isBlank(phrase)){
                    buf.append(" and F.OBJECT like ?");
                    inParams.add("%" + phrase + "%");
                }
            }
        }
        return buf.toString();
    }

//	public FilterType getFilter() {
//		return filter;
//	}
//
//	public void setFilter(FilterType filter) {
//		this.filter = filter;
//	}
}
