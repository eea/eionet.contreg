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
import java.util.Map;
import java.util.Map.Entry;

import eionet.cr.util.LinkedCaseInsensitiveMap;

/**
 * A bean that represents an RDF export query's configuration (in the context of staging databases).
 *
 * @author jaanus
 */
public class QueryConfiguration implements Serializable {

    /** */
    private static final String LINE_BREAK = "\n";

    /** The query. */
    private String query;

    /** The object type uri. */
    private String objectTypeUri;

    /** The column mappings. */
    private LinkedCaseInsensitiveMap<ObjectProperty> columnMappings = new LinkedCaseInsensitiveMap<ObjectProperty>();

    /** The object id template. */
    private String objectIdTemplate;

    /** The object id namespace. */
    private String objectIdNamespace;

    /** Template for the dataset ID. */
    private String datasetIdTemplate;

    /** The dataset namespace. */
    private String datasetIdNamespace;

    /** The indicator URI. */
    private String indicator;

    /** The dataset URI. */
    private String dataset;

    /**
     * @return the datasetIdTemplate
     */
    public String getDatasetIdTemplate() {
        return datasetIdTemplate;
    }

    /**
     * @param datasetIdTemplate the datasetIdTemplate to set
     */
    public void setDatasetIdTemplate(String datasetIdTemplate) {
        this.datasetIdTemplate = datasetIdTemplate;
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
     * @return the datasetIdNamespace
     */
    public String getDatasetIdNamespace() {
        return datasetIdNamespace;
    }

    /**
     * @param datasetIdNamespace the datasetIdNamespace to set
     */
    public void setDatasetIdNamespace(String datasetIdNamespace) {
        this.datasetIdNamespace = datasetIdNamespace;
    }

    //    /**
    //     * Sets the defaults.
    //     */
    //    public void setDefaults() {
    //
    //        if (objectTypeUri == null || objectTypeUri.isEmpty()) {
    //            return;
    //        }
    //
    //        ObjectType objectType = ObjectTypes.getByUri(objectTypeUri);
    //        if (objectType != null) {
    //
    //            for (Entry<String, ObjectProperty> entry : columnMappings.entrySet()) {
    //                String column = entry.getKey();
    //                ObjectProperty defaultProperty = objectType.getDefaultProperty(column);
    //                entry.setValue(defaultProperty);
    //            }
    //
    //            objectIdTemplate = objectType.getObjectIdTemplate();
    //            datasetIdTemplate = objectType.getDatasetIdTemplate();
    //
    //            objectIdNamespace = objectType.getObjectIdNamespace();
    //            datasetIdNamespace = objectType.getDatasetIdNamespace();
    //        }
    //    }

    /**
     * Returns a string "dump" of this {@link QueryConfiguration} that is suitable for storage into the RDF export table in the
     * database.
     * @return The string "dump".
     */
    public String toLongString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Query]").append(LINE_BREAK);
        sb.append(query).append(LINE_BREAK);
        sb.append(LINE_BREAK);
        sb.append("[Column mappings]").append(LINE_BREAK);
        for (Entry<String, ObjectProperty> entry : columnMappings.entrySet()) {
            sb.append(entry.getKey()).append(" = ").append(entry.getValue().getLabel()).append(LINE_BREAK);
        }
        sb.append(LINE_BREAK);
        sb.append("[Other settings]").append(LINE_BREAK);
        //        sb.append("Dataset identifier template: ").append(datasetIdTemplate).append(LINE_BREAK);
        //        sb.append("Objects identifier template: ").append(objectIdTemplate);
        sb.append("Indicator: ").append(indicator).append(LINE_BREAK);
        sb.append("Dataset: ").append(dataset);
        sb.append(LINE_BREAK);

        return sb.toString();
    }

    /**
     * @param columnMappings the columnMappings to set
     */
    public void setColumnMappings(LinkedCaseInsensitiveMap<ObjectProperty> columnMappings) {
        this.columnMappings = columnMappings;
    }

    /**
     * @return the indicator
     */
    public String getIndicator() {
        return indicator;
    }

    /**
     * @param indicator the indicator to set
     */
    public void setIndicator(String indicator) {
        this.indicator = indicator;
    }

    /**
     * @return the dataset
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * @param dataset the dataset to set
     */
    public void setDataset(String dataset) {
        this.dataset = dataset;
    }
}
