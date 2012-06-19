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
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Enriko Käsper, Tieto Estonia
 */
package eionet.cr.util.export;

/**
 * @author <a href="mailto:enriko.kasper@tieto.com">Enriko Käsper</a>, Tieto Estonia
 */

public class XmlElementMetadata {

    public enum Type {
        STRING, DOUBLE;
    }

    private String name;
    private int maxLength = -1;
    private Type type = null;

    public XmlElementMetadata(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Apply new length only if it is bigger than exisiting
     *
     * @param length
     */
    public void setMaxLength(int length) {
        if (this.maxLength < length) {
            this.maxLength = length;
        }
    }

    /**
     * Get type value. If no value defined, then it is a STRING
     *
     * @return
     */
    public Type getType() {
        return (this.type == null) ? Type.STRING : this.type;
    }

    /**
     * apply new type only if the type is not a string already or the new type is string It is impossible to change the type
     * numeric, if it is STRING already
     *
     * @param type
     */
    public void setType(Type type) {
        if (this.type == null || type == Type.STRING) {
            this.type = type;
        }
    }

    /**
     * Try to guess the element data type from the given value
     *
     * @param value
     */
    public void setType(String stringValue) {
        try {
            new Double(stringValue);
            setType(Type.DOUBLE);
        } catch (Exception ignored) {
            setType(Type.STRING);
        }
    }

    @Override
    public String toString() {
        return "XmlElementMetadata [name=" + name + ", maxLength=" + maxLength + ", type=" + type + "]";
    }
}
