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
 * The Original Code is Content Registry 3.0
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s): Enriko Käsper
 */
package eionet.cr.dao.virtuoso.helpers;

import java.util.List;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.common.Namespace;
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.SortingRequest;
import eionet.cr.util.URLUtil;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sesame.SPARQLQueryUtil;

/**
 *
 * @author Enriko Käsper
 *
 */
public class VirtuosoUserFolderSearchHelper extends AbstractSearchHelper {

    private String parentFolderUri;

    public VirtuosoUserFolderSearchHelper(String parentFolderUri, PagingRequest pagingRequest,
            SortingRequest sortingRequest) {
        super(pagingRequest, sortingRequest);
        // check the validity of filters
        if (parentFolderUri == null || !URLUtil.isURL(parentFolderUri)) {
            throw new CRRuntimeException(
                    "Parent folder has to be defined!");
        }
        this.parentFolderUri = parentFolderUri;
    }

    @Override
    protected String getOrderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = SPARQLQueryUtil.getSparqlQueryHeader(true);
        strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("} ORDER BY ");
        if (sortOrder != null) {
            strBuilder.append(sortOrder);
        }
        if (Predicates.RDFS_LABEL.equals(sortPredicate)) {
            strBuilder
                    .append("(bif:either( bif:isnull(?oorderby) , (bif:lcase(bif:subseq (bif:replace (?s, '/', '#'), bif:strrchr (bif:replace (?s, '/', '#'), '#')+1))) , bif:lcase(?oorderby)))");
        } else {
            strBuilder.append("(?s)");
        }

        return strBuilder.toString();
    }

    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        StringBuilder strBuilder = SPARQLQueryUtil.getSparqlQueryHeader(true, Namespace.CR, Namespace.RDF);
        strBuilder.append("select distinct ?s where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("}");

        return strBuilder.toString();
    }

    @Override
    public String getCountQuery(List<Object> inParams) {
        StringBuilder strBuilder = SPARQLQueryUtil.getSparqlQueryHeader(true);
        strBuilder.append("select count(distinct ?s) where { ?s ?p ?o ");
        strBuilder.append(getQueryParameters(inParams));
        strBuilder.append("}");

        return strBuilder.toString();
    }

    @Override
    public String getMinMaxHashQuery(List<Object> inParams) {
        // TODO PostgreSQL specific function - requires interface cleanup
        throw new UnsupportedOperationException("Method not implemented");
    }

    public String getQueryParameters(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();

        strBuilder.append(" . ?s <").append(Predicates.RDF_TYPE).append("> ?type . FILTER (?type  IN (")
                .append("<").append(Subjects.CR_FILE).append(">, <").append(Subjects.CR_USER_FOLDER).append(">)) . <")
                .append(parentFolderUri).
                append("> ?hasPredicate ?s . FILTER (?hasPredicate IN (<").append(Predicates.CR_HAS_FILE).append(">, <")
                .append(Predicates.CR_HAS_FOLDER).append(">))");
        if (sortPredicate != null) {
            strBuilder.append(" . OPTIONAL {?s <").append(sortPredicate)
                    .append("> ?oorderby }");
        }
        return strBuilder.toString();

    }

}
