package eionet.cr.util.xml;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility POJO for metadata about an XML file's conversion schema or DTD.
 *
 * @author Jaanus
 */
public class ConversionSchema {

    /**
     * Different types of conversion schema.
     */
    public enum Type {

        /** Classical XML schema (i.e. provided via xsi:schemaLocation). */
        XML_SCHEMA,
        /** System DTD identifier. */
        SYSTEM_DTD,
        /** Public DTD identifier. */
        PUBLIC_DTD,
        /** The root XML element. */
        ROOT_ELEM
    };

    /** The schema's string value (depends on {@link #type}) . */
    private String stringValue;

    /** The schema's type as enumerated in {@link Type}. */
    private Type type;

    /**
     * Default constructor.
     *
     * @param stringValue the stringValue
     * @param type the type
     */
    public ConversionSchema(String stringValue, Type type) {

        if (StringUtils.isBlank(stringValue) || type == null) {
            throw new IllegalArgumentException("String value and type must not be null or blank!");
        }

        this.stringValue = stringValue;
        this.type = type;
    }

    /**
     * @return the stringValue
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }
}
