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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Type definition ...
 *
 * @author jaanus
 */
public class ObjectType {

    /** */
    private String uri;

    /** */
    private String label;

    /** */
    private ArrayList<ObjectProperty> properties;

    /** */
    private String datasetColumn;

    /** */
    private String datasetNamespace;

    /** */
    private String idTemplate;

    /** */
    private String idNamespace;

    /** */
    private HashMap<String, ObjectProperty> columnToDefaultProperty;

    /** */
    private HashMap<String, ObjectProperty> predicateToProperty;

    /**
     * Class constructor.
     * @param uri
     * @param label
     */
    public ObjectType(String uri, String label) {
        super();
        this.uri = uri;
        this.label = label;
        properties = new ArrayList<ObjectProperty>();
        columnToDefaultProperty = new HashMap<String, ObjectProperty>();
        predicateToProperty = new HashMap<String, ObjectProperty>();
    }

    /**
     *
     * @param property
     */
    public void addProperty(ObjectProperty property) {
        properties.add(property);
        predicateToProperty.put(property.getPredicate(), property);
    }

    /**
     *
     * @param property
     * @param defaultForColumn
     */
    public void addProperty(ObjectProperty property, String defaultForColumn) {
        addProperty(property);
        setDefaultProperty(defaultForColumn, property);
    }

    /**
     *
     * @param column
     * @return
     */
    public ObjectProperty getDefaultProperty(String column) {
        return columnToDefaultProperty.get(column.toUpperCase());
    }

    /**
     *
     * @param column
     * @param property
     */
    public void setDefaultProperty(String column, ObjectProperty property) {
        columnToDefaultProperty.put(column.toUpperCase(), property);
    }

    /**
     *
     * @return
     */
    public List<ObjectProperty> getProperties() {
        return properties;
    }

    /**
     *
     * @param predicateUri
     * @return
     */
    public ObjectProperty getPropertyByPredicate(String predicateUri) {
        return predicateToProperty.get(predicateUri);
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
     * @return the idTemplate
     */
    public String getIdTemplate() {
        return idTemplate;
    }

    /**
     * @param idTemplate the idTemplate to set
     */
    public void setIdTemplate(String idTemplate) {
        this.idTemplate = idTemplate;
    }

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
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
     * @return the idNamespace
     */
    public String getIdNamespace() {
        return idNamespace;
    }

    /**
     * @param idNamespace the idNamespace to set
     */
    public void setIdNamespace(String idNamespace) {
        this.idNamespace = idNamespace;
    }
}
