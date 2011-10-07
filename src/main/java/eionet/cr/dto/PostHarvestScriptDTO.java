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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dto;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Jaanus Heinlaid
 */
public abstract class PostHarvestScriptDTO {

    /** */
    public enum Type {
        HARVEST_SOURCE, RDF_TYPE
    }

    /** */
    private String uri;

    /** */
    private List<String> queries = new ArrayList<String>();

    /**
     *
     * @param uri
     */
    public PostHarvestScriptDTO(String uri){

        if (StringUtils.isBlank(uri)){
            throw new IllegalArgumentException("URI must not be blank!");
        }
        this.uri = uri;
    }

    /**
     *
     * @param query
     */
    public void addQuery(String query){

        if (StringUtils.isBlank(query)){
            throw new IllegalArgumentException("The query must not be blank!");
        }
        queries.add(query);
    }

    /**
     *
     * @param queries
     */
    public void addQueries(List<String> queries){

        if (queries!=null && !queries.isEmpty()){
            this.queries.addAll(queries);
        }
    }


    /**
     * @return the queries
     */
    public List<String> getQueries() {
        return queries;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     *
     * @return
     */
    public int getNumberOfQueries(){
        return queries.size();
    }

    /**
     *
     * @param type
     * @param uri
     * @return
     */
    public static PostHarvestScriptDTO create(Type type, String uri){

        return type.equals(Type.HARVEST_SOURCE) ? new SourcePostHarvestScriptDTO(uri) : new TypePostHarvestScriptDTO(uri);
    }
}