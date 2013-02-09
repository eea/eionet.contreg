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

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;

import eionet.cr.staging.exp.ObjectProperty.Range;

// TODO: Auto-generated Javadoc
/**
 * Metadata bean for an RDF property that is hidden and fixed, i.e. it will always be added to every exported object.
 *
 * @author jaanus
 */
public class ObjectHiddenProperty {

    /** */
    private String predicate;

    /** */
    private Range range;

    /** */
    private String dataType;

    /** */
    private String value;

    /** */
    private URI predicateURI;

    /** */
    private Value valueValue;

    /**
     * Class constructor.
     *
     * @param predicate the predicate
     * @param range the range
     */
    public ObjectHiddenProperty(String predicate, Range range) {

        this.predicate = predicate;
        this.range = range;
    }

    /**
     * Gets the data type.
     *
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * Sets the data type.
     *
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * Gets the predicate.
     *
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * Gets the range.
     *
     * @return the range
     */
    public Range getRange() {
        return range;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value.
     *
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Returns true if the property's value is a resource.
     *
     * @return As said above.
     */
    public boolean isResourceRange() {
        return Range.RESOURCE.equals(range);
    }

    /**
     * Returns true if the property's value is a literal.
     *
     * @return As said above.
     */
    public boolean isLiteralRange() {
        return Range.LITERAL.equals(range);
    }

    /**
     * Sets {@link URI} and {@link Value} for this property's {@link #predicate} and {@link #value}, using the given.
     *
     * @param vf The value factory. {@link ValueFactory}.
     */
    protected void setValues(ValueFactory vf) {

        predicateURI = vf.createURI(predicate);
        if (isLiteralRange()) {
            if (dataType != null && !dataType.isEmpty()) {
                valueValue = vf.createLiteral(value, vf.createURI(dataType));
            } else {
                valueValue = vf.createLiteral(value);
            }
        } else {
            valueValue = vf.createURI(value);
        }
    }

    /**
     * Gets the predicate uri.
     *
     * @return the predicateURI
     */
    public URI getPredicateURI() {
        return predicateURI;
    }

    /**
     * Gets the value value.
     *
     * @return the valueValue
     */
    public Value getValueValue() {
        return valueValue;
    }
}
