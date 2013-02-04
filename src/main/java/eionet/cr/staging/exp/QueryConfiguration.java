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

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A bean that represents an RDF export query's configuration (in the context of staging databases).
 *
 * @author jaanus
 */
public class QueryConfiguration implements Serializable {

    /** The query. */
    private String query;

    /** The object type uri. */
    private String objectTypeUri;

    /** The column mappings. */
    private LinkedHashMap<String, ObjectProperty> columnMappings = new LinkedHashMap<String, ObjectProperty>();

    /** The object id template. */
    private String objectIdTemplate;

    /** The object id namespace. */
    private String objectIdNamespace;

    /** The dataset column. */
    private String datasetColumn;

    /** The dataset namespace. */
    private String datasetNamespace;

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
     * @return the objectTypeUri
     */
    public String getObjectTypeUri() {
        return objectTypeUri;
    }

    /**
     * @param objectTypeUri the objectTypeUri to set
     */
    public void setObjectTypeUri(String objectTypeUri) {
        this.objectTypeUri = objectTypeUri;
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
     * @return the columnMappings
     */
    public Map<String, ObjectProperty> getColumnMappings() {
        return columnMappings;
    }

    /**
     * Put column mapping.
     *
     * @param columnName the column name
     * @param propertyConf the property conf
     */
    public void putColumnMapping(String columnName, ObjectProperty propertyConf) {
        columnMappings.put(columnName, propertyConf);
    }

    /**
     * Put column names.
     *
     * @param columnNames the column names
     */
    public void putColumnNames(Iterable<String> columnNames) {

        for (String colName : columnNames) {
            columnMappings.put(colName, null);
        }
    }

    /**
     * @return the datasetColumn
     */
    public String getDatasetColumn() {
        return datasetColumn;
    }

    /**
     * @param datasetColumn the datasetColumn to set
     */
    public void setDatasetColumn(String datasetColumn) {
        this.datasetColumn = datasetColumn;
    }

    /**
     * Clear column mappings.
     */
    public void clearColumnMappings() {
        this.columnMappings.clear();
    }

    /**
     * @return the objectIdNamespace
     */
    public String getObjectIdNamespace() {
        return objectIdNamespace;
    }

    /**
     * @param objectIdNamespace the objectIdNamespace to set
     */
    public void setObjectIdNamespace(String objectIdNamespace) {
        this.objectIdNamespace = objectIdNamespace;
    }

    /**
     * @return the datasetNamespace
     */
    public String getDatasetNamespace() {
        return datasetNamespace;
    }

    /**
     * @param datasetNamespace the datasetNamespace to set
     */
    public void setDatasetNamespace(String datasetNamespace) {
        this.datasetNamespace = datasetNamespace;
    }

    /**
     * Sets the defaults.
     */
    public void setDefaults() {

        if (objectTypeUri == null || objectTypeUri.isEmpty()) {
            return;
        }

        ObjectType objectType = ObjectTypes.getByUri(objectTypeUri);
        if (objectType != null) {

            for (Entry<String, ObjectProperty> entry : columnMappings.entrySet()) {
                String column = entry.getKey();
                ObjectProperty defaultProperty = objectType.getDefaultProperty(column);
                entry.setValue(defaultProperty);
            }

            objectIdTemplate = objectType.getIdTemplate();
            datasetColumn = objectType.getDatasetColumn();

            objectIdNamespace = objectType.getIdNamespace();
            datasetNamespace = objectType.getDatasetNamespace();
        }
    }
}
