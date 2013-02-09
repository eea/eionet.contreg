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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// TODO: Auto-generated Javadoc
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
    private ArrayList<ObjectProperty> properties = new ArrayList<ObjectProperty>();

    /** */
    private String objectIdTemplate;

    /** */
    private String objectIdNamespace;

    /** */
    private String datasetIdTemplate;

    /** */
    private String datasetIdNamespace;

    /** */
    private HashMap<String, ObjectProperty> columnToDefaultProperty = new HashMap<String, ObjectProperty>();

    /** */
    private HashMap<String, ObjectProperty> predicateToProperty = new HashMap<String, ObjectProperty>();

    /** */
    private HashSet<ObjectHiddenProperty> hiddenProperties = new HashSet<ObjectHiddenProperty>();

    /**
     * Gets the hidden properties.
     *
     * @return the hiddenProperties
     */
    public Set<ObjectHiddenProperty> getHiddenProperties() {
        return hiddenProperties;
    }

    /**
     * Class constructor.
     *
     * @param uri the uri
     * @param label the label
     */
    public ObjectType(String uri, String label) {

        super();
        this.uri = uri;
        this.label = label;
    }

    /**
     * Adds the property.
     *
     * @param property the property
     */
    public void addProperty(ObjectProperty property) {
        properties.add(property);
        predicateToProperty.put(property.getPredicate(), property);
    }

    /**
     * Adds the property.
     *
     * @param property the property
     * @param defaultForColumn the default for column
     */
    public void addProperty(ObjectProperty property, String... defaultForColumn) {
        addProperty(property);

        if (defaultForColumn != null && defaultForColumn.length > 0) {
            for (String column : defaultForColumn) {
                columnToDefaultProperty.put(column.toUpperCase(), property);
            }
        }
    }

    /**
     * Adds the hidden property.
     *
     * @param property the property
     */
    public void addHiddenProperty(ObjectHiddenProperty property) {
        hiddenProperties.add(property);
    }

    /**
     * Gets the default property.
     *
     * @param column the column
     * @return the default property
     */
    public ObjectProperty getDefaultProperty(String column) {
        return columnToDefaultProperty.get(column.toUpperCase());
    }

    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public List<ObjectProperty> getProperties() {
        return properties;
    }

    /**
     * Gets the property by predicate.
     *
     * @param predicateUri the predicate uri
     * @return the property by predicate
     */
    public ObjectProperty getPropertyByPredicate(String predicateUri) {
        return predicateToProperty.get(predicateUri);
    }

    /**
     * Gets the object id template.
     *
     * @return the objectIdTemplate
     */
    public String getObjectIdTemplate() {
        return objectIdTemplate;
    }

    /**
     * Sets the object id template.
     *
     * @param objectIdTemplate the objectIdTemplate to set
     */
    public void setObjectIdTemplate(String objectIdTemplate) {
        this.objectIdTemplate = objectIdTemplate;
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets the label.
     *
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the object id namespace.
     *
     * @return the objectIdNamespace
     */
    public String getObjectIdNamespace() {
        return objectIdNamespace;
    }

    /**
     * Sets the object id namespace.
     *
     * @param objectIdNamespace the new object id namespace
     */
    public void setObjectIdNamespace(String objectIdNamespace) {
        this.objectIdNamespace = objectIdNamespace;
    }

    /**
     * Gets the dataset id template.
     *
     * @return the datasetIdTemplate
     */
    public String getDatasetIdTemplate() {
        return datasetIdTemplate;
    }

    /**
     * Sets the dataset id template.
     *
     * @param datasetIdTemplate the datasetIdTemplate to set
     */
    public void setDatasetIdTemplate(String datasetIdTemplate) {
        this.datasetIdTemplate = datasetIdTemplate;
    }

    /**
     * Gets the dataset id namespace.
     *
     * @return the datasetIdNamespace
     */
    public String getDatasetIdNamespace() {
        return datasetIdNamespace;
    }

    /**
     * Sets the dataset id namespace.
     *
     * @param datasetIdNamespace the datasetIdNamespace to set
     */
    public void setDatasetIdNamespace(String datasetIdNamespace) {
        this.datasetIdNamespace = datasetIdNamespace;
    }
}
