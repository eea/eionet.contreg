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
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dto;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.openrdf.model.URI;

import eionet.cr.util.Hashes;
import eionet.cr.util.NamespaceUtil;
import eionet.cr.util.Util;
import eionet.cr.web.util.FactsheetObjectId;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 * 
 */
public class ObjectDTO implements Serializable {

    /**
     * serial.
     */
    private static final long serialVersionUID = 1L;

    /** */
    public enum Type {
        LITERAL, RESOURCE;
    }

    /** */
    private String value;
    private long hash;

    private boolean anonymous;
    private boolean literal;
    private String language;
    // Used for literals
    private URI datatype;

    private String derivSourceUri;
    private long derivSourceHash;
    private long derivSourceGenTime;

    private long sourceObjectHash;
    private String derviedLiteralValue;

    private String sourceUri;
    private long sourceHash;

    private ObjectDTO labelObject;

    /**
     * Repository-returned MD5 hash of the object. Used to indicate {@link #value} is different than the actual value in the
     * repository is different. For example {@link #value} might contain only a substring of what's really in the database.
     */
    private String objectMD5;

    /**
     * 
     * @param value
     * @param language
     * @param literal
     * @param anonymous
     * @param datatype
     */
    public ObjectDTO(String value, String language, boolean literal, boolean anonymous, URI datatype) {

        this.value = value;
        this.language = language;
        this.literal = literal;
        this.anonymous = anonymous;
        this.datatype = datatype;
        this.hash = Hashes.spoHash(value);
    }

    /**
     * 
     * @param value
     * @param language
     * @param literal
     * @param anonymous
     */
    public ObjectDTO(String value, String language, boolean literal, boolean anonymous) {
        this(value, language, literal, anonymous, null);
    }

    /**
     * 
     * @param value
     * @param literal
     */
    public ObjectDTO(String value, boolean literal) {
        this(value, null, literal, false, null);
    }

    /**
     * 
     * @param value
     * @param literal
     * @param datatype
     */
    public ObjectDTO(String value, boolean literal, URI datatype) {
        this(value, null, literal, false, datatype);
    }

    /**
     * 
     * @param hash
     * @param sourceHash
     * @param derivSourceHash
     * @param sourceObjectHash
     */
    private ObjectDTO(long hash, long sourceHash, long derivSourceHash, long sourceObjectHash) {

        this.hash = hash;
        this.sourceHash = sourceHash;
        this.derivSourceHash = derivSourceHash;
        this.sourceObjectHash = sourceObjectHash;
    }

    /**
     * 
     * @param hash
     * @param sourceHash
     * @param derivSourceHash
     * @param sourceObjectHash
     * @return ObjectDTO
     */
    public static ObjectDTO create(long hash, long sourceHash, long derivSourceHash, long sourceObjectHash) {

        return new ObjectDTO(hash, sourceHash, derivSourceHash, sourceObjectHash);
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @return the language
     */
    public String getLanguage() {

        if (isLiteral()) {
            return language;
        } else {
            return labelObject != null ? labelObject.getLanguage() : null;
        }
    }

    /**
     * @return the literal
     */
    public boolean isLiteral() {
        return literal;
    }

    /**
     * @return the anonymous
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object other) {

        if (this == other) {
            return true;
        }

        if (!(other instanceof ObjectDTO)) {
            return false;
        }

        String otherValue = ((ObjectDTO) other).getValue();
        return getValue() == null ? otherValue == null : getValue().equals(otherValue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return getValue() == null ? 0 : getValue().hashCode();
    }

    /**
     * 
     * @return long
     */
    public long getHash() {
        return hash;
    }

    /**
     * @return the derivSourceUri
     */
    public String getDerivSourceUri() {
        return derivSourceUri;
    }

    /**
     * @param derivSource the derivSourceUri to set
     */
    public void setDerivSourceUri(String derivSource) {
        this.derivSourceUri = derivSource;
    }

    /**
     * @return the sourceUri
     */
    public String getSourceUri() {
        return sourceUri;
    }

    /**
     * @param source the sourceUri to set
     */
    public void setSourceUri(String source) {
        this.sourceUri = source;
    }

    /**
     * 
     * @return String
     */
    public String getSourceSmart() {

        if (derivSourceUri != null && derivSourceUri.trim().length() > 0) {
            return derivSourceUri;
        } else if (sourceUri != null && sourceUri.trim().length() > 0) {
            return sourceUri;
        } else {
            return null;
        }
    }

    /**
     * @return the sourceObjectHash
     */
    public long getSourceObjectHash() {
        return sourceObjectHash;
    }

    /**
     * @param sourceObjectHash the sourceObjectHash to set
     */
    public void setSourceObjectHash(long sourceObjectHash) {
        this.sourceObjectHash = sourceObjectHash;
    }

    /**
     * 
     * @param hash
     */
    public void setHash(long hash) {
        this.hash = hash;
    }

    /**
     * @return the derivSourceGenTime
     */
    public long getDerivSourceGenTime() {
        return derivSourceGenTime;
    }

    /**
     * @param derivSourceGenTime the derivSourceGenTime to set
     */
    public void setDerivSourceGenTime(long derivSourceGenTime) {
        this.derivSourceGenTime = derivSourceGenTime;
    }

    /**
     * @return the derivSourceHash
     */
    public long getDerivSourceHash() {
        return derivSourceHash;
    }

    /**
     * @param derivSourceHash the derivSourceHash to set
     */
    public void setDerivSourceHash(long derivSourceHash) {
        this.derivSourceHash = derivSourceHash;
    }

    /**
     * @return the sourceHash
     */
    public long getSourceHash() {
        return sourceHash;
    }

    /**
     * 
     * @return long
     */
    public long getSourceHashSmart() {
        return derivSourceHash != 0 ? derivSourceHash : sourceHash;
    }

    /**
     * @param sourceHash the sourceHash to set
     */
    public void setSourceHash(long sourceHash) {
        this.sourceHash = sourceHash;
    }

    /**
     * 
     * @return String
     */
    public String getId() {

        return FactsheetObjectId.format(this);
    }

    /**
     * @return the derviedLiteralValue
     */
    public String getDerviedLiteralValue() {
        return labelObject == null ? derviedLiteralValue : labelObject.getValue();
    }

    /**
     * @param sourceObjectValue the derviedLiteralValue to set
     */
    public void setDerviedLiteralValue(String sourceObjectValue) {
        this.derviedLiteralValue = sourceObjectValue;
    }

    public URI getDatatype() {
        return datatype;
    }

    /**
     * Returns datatype label to display. If the namespace is known replaces it with the prefix defined in Namespace otherwise
     * returns URL with full namespace
     * 
     * @return String datatype label
     */
    public String getDataTypeLabel() {
        if (datatype == null) {
            return "Not specified";
        }
        // if datatype is from XSD schema, replace http://www.w3.org/2001/XMLSchema with xsd
        String ns = datatype.getNamespace();
        if (NamespaceUtil.getKnownNamespace(datatype.getNamespace()) != null) {
            ns = NamespaceUtil.getKnownNamespace(datatype.getNamespace()) + ":";
        }
        String local = datatype.getLocalName();
        return ns + local;
    }

    /**
     * 
     * @param datatype
     */
    public void setDatatype(URI datatype) {
        this.datatype = datatype;
    }

    /**
     * 
     * @param value
     * @return
     */
    public static ObjectDTO createLiteral(Object value) {
        return new ObjectDTO(value.toString(), true);
    }

    /**
     * 
     * @param value
     * @param datatype
     * @return
     */
    public static ObjectDTO createLiteral(Object value, URI datatype) {
        return new ObjectDTO(value.toString(), true, datatype);
    }

    /**
     * 
     * @param uri
     * @return
     */
    public static ObjectDTO createResource(String uri) {
        return new ObjectDTO(uri, false);
    }

    /**
     * @return
     */
    public Date getDateValue() {
        return Util.virtuosoStringToDate(getValue());
    }

    /**
     * 
     * @return
     */
    public String getDisplayValue() {

        if (isLiteral()) {
            return value;
        } else {
            String displayValue = getDerviedLiteralValue();
            return StringUtils.isBlank(displayValue) ? value : displayValue;
        }
    }

    /**
     * @param labelObject the labelObject to set
     */
    public void setLabelObject(ObjectDTO labelObject) {
        this.labelObject = labelObject;
    }

    /**
     * @return the objectMD5
     */
    public String getObjectMD5() {
        return objectMD5;
    }

    /**
     * @param objectMD5 the objectMD5 to set
     */
    public void setObjectMD5(String objectMD5) {
        this.objectMD5 = objectMD5;
    }
}
