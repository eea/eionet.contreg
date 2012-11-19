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
import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.helpers.AbstractSearchHelper;
import eionet.cr.util.Bindings;
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

    /**
     * SPARQL for home folders.
     */
    private static final String USER_HOMES_SPARQL =

        SPARQLQueryUtil.getSparqlQueryHeader(true)
        .append("SELECT ?parent ?subject bif:either( bif:isnull(?lbl) , ?subject, ?lbl) as ?label ?fileCount ?folderCount ")
        .append("WHERE {")
        .append("?subject a <").append(Subjects.CR_USER_FOLDER).append("> .")
        .append("?parent ?hasPredicate ?subject . ")
        .append(" FILTER (?hasPredicate IN (<").append(Predicates.CR_HAS_FILE).append(">, <").append(Predicates.CR_HAS_FOLDER).append(">)) .")
        .append("  FILTER(?parent= ?parentFolder)")

        .append("  { SELECT ?subject (count(?file) AS ?fileCount)")
        .append("  WHERE { ?subject <").append(Predicates.CR_HAS_FILE).append("> ?file")
        .append("    } GROUP BY ?subject}")

        .append("  { SELECT ?subject (count(?folder) AS ?folderCount)")
        .append("  WHERE { ?subject <").append(Predicates.CR_HAS_FOLDER).append("> ?folder ")
        .append("    } GROUP BY ?subject}")

        .append("OPTIONAL {  {")
        .append("SELECT ?subject ?lbl ")
        .append("WHERE { ?subject rdfs:label ?lbl }  }  }")
        .append("} GROUP BY ?subject ").toString();


    //private String parentFolderUri;
    private Bindings bindings;

    public VirtuosoUserFolderSearchHelper(String parentFolderUri, PagingRequest pagingRequest, SortingRequest sortingRequest) {
        super(pagingRequest, sortingRequest);
        // check the validity of filters
        if (parentFolderUri == null || !URLUtil.isURL(parentFolderUri)) {
            throw new CRRuntimeException("Parent folder has to be defined!");
        }
        //this.parentFolderUri = parentFolderUri;
        bindings = new Bindings();
        bindings.setURI("parentFolder", parentFolderUri);

    }

    @Override
    protected String getOrderedQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder(USER_HOMES_SPARQL);
        strBuilder.append(" ORDER BY ");

        if (sortOrder != null) {
            strBuilder.append(sortOrder);
        }
        strBuilder.append("(bif:either( bif:isnull(?label) , (bif:lcase(bif:subseq (bif:replace (?subject, '/', '#'), bif:strrchr (bif:replace (?subject, '/', '#'), '#')+1))) , bif:lcase(?label)))");

        return strBuilder.toString();
    }

    @Override
    public String getUnorderedQuery(List<Object> inParams) {

        return USER_HOMES_SPARQL;
    }

    @Override
    public String getCountQuery(List<Object> inParams) {
        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("SELECT count(distinct(?s)) WHERE {?s ?p ?o . ?s a <").append(Subjects.CR_USER_FOLDER).append(">}");

        return strBuilder.toString();
    }


    @Override
    public Bindings getQueryBindings() {
        return bindings;
    }

}
