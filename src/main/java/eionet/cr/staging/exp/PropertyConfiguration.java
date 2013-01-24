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

/**
 * Type definition ...
 *
 * @author jaanus
 */
public class PropertyConfiguration {

    /**  */
    private String predicate;
    /**  */
    private ValueType valueType;
    /**  */
    private String valueTemplate;
    /**  */
    private String dataType;

    /**
     * @return the predicate
     */
    public String getPredicate() {
        return predicate;
    }

    /**
     * @param predicate the predicate to set
     */
    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    /**
     * @return the valueType
     */
    public ValueType getValueType() {
        return valueType;
    }

    /**
     * @param valueType the valueType to set
     */
    public void setValueType(ValueType valueType) {
        this.valueType = valueType;
    }

    /**
     * @return the valueTemplate
     */
    public String getValueTemplate() {
        return valueTemplate;
    }

    /**
     * @param valueTemplate the valueTemplate to set
     */
    public void setValueTemplate(String valueTemplate) {
        this.valueTemplate = valueTemplate;
    }

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     *
     * @return
     */
    public boolean isResourceValueType() {
        return ValueType.RESOURCE.equals(valueType);
    }

    /**
     *
     * @return
     */
    public boolean isLiteralValueType() {
        return ValueType.LITERAL.equals(valueType);
    }

    /**
     * @author jaanus
     */
    public static enum ValueType {

        /** */
        RESOURCE("Resource"), LITERAL("Literal");

        /** */
        private String friendlyName;

        /**
         * @param friendlyName
         */
        private ValueType(String friendlyName) {
            this.friendlyName = friendlyName;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return friendlyName;
        }
    }
}
