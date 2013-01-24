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
 *        jaanus
 */

package eionet.cr.staging.exp;

import java.util.HashMap;

/**
 * Type definition ...
 *
 * @author jaanus
 */
public class QueryConfiguration {

    /**  */
    private String query;
    /**  */
    private String objectsClass;
    /**  */
    private String objectIdTemplate;
    /**  */
    private HashMap<String, PropertyConfiguration> fieldMappings = new HashMap<String, PropertyConfiguration>();
    /**  */
    private String targetGraph;

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
     * @return the objectsClass
     */
    public String getObjectsClass() {
        return objectsClass;
    }

    /**
     * @param objectsClass the objectsClass to set
     */
    public void setObjectsClass(String objectsClass) {
        this.objectsClass = objectsClass;
    }

    /**
     * @return the objectIdTemplate
     */
    public String getObjectIdTemplate() {
        return objectIdTemplate;
    }

    /**
     * @param objectIdTemplate the objectIdTemplate to set
     */
    public void setObjectIdTemplate(String objectIdTemplate) {
        this.objectIdTemplate = objectIdTemplate;
    }

    /**
     * @return the fieldMappings
     */
    public HashMap<String, PropertyConfiguration> getFieldMappings() {
        return fieldMappings;
    }

    /**
     * @param fieldMappings the fieldMappings to set
     */
    public void setFieldMappings(HashMap<String, PropertyConfiguration> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    /**
     * @return the targetGraph
     */
    public String getTargetGraph() {
        return targetGraph;
    }

    /**
     * @param targetGraph the targetGraph to set
     */
    public void setTargetGraph(String targetGraph) {
        this.targetGraph = targetGraph;
    }
}
