package eionet.cr.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.Query;

import eionet.cr.common.CRRuntimeException;
import eionet.cr.dao.virtuoso.VirtuosoBaseDAO;
import eionet.cr.util.sesame.SPARQLQueryUtil;

/**
 *
 * @author jaanus
 *
 */
public class Bindings {

    /** */
    private HashMap<String, Object> bindings = new LinkedHashMap<String, Object>();

    /** */
    private static DatatypeFactory datatypeFactory;
    static {
        try {
            datatypeFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new CRRuntimeException(e);
        }
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setByte(String name, byte value) {
        bindings.put(name, Byte.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setShort(String name, short value) {
        bindings.put(name, Short.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setInt(String name, int value) {
        bindings.put(name, Integer.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setLong(String name, long value) {
        bindings.put(name, Long.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setFloat(String name, float value) {
        bindings.put(name, Float.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setDouble(String name, double value) {
        bindings.put(name, Double.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setBoolean(String name, boolean value) {
        bindings.put(name, Boolean.valueOf(value));
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setDate(String name, Date value) {
        bindings.put(name, value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setString(String name, String value) {
        bindings.put(name, value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setURI(String name, URI value) {
        bindings.put(name, value);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setURI(String name, String value) {

        if (!value.startsWith(VirtuosoBaseDAO.BNODE_URI_PREFIX)) {
            try {
                bindings.put(name, new URI(value));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
            // Blank nods are URI's as well
        } else {
            bindings.put(name, new BlankNode(value));
        }
    }

    /**
     *
     * @param query
     * @param valueFactory
     */
    public void applyTo(Query query, ValueFactory valueFactory) {

        if (bindings.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> binding : bindings.entrySet()) {

            String name = binding.getKey();
            Object value = binding.getValue();

            if (value == null) {
                continue;
            } else if (value instanceof Byte) {
                query.setBinding(name, valueFactory.createLiteral(((Byte) value).byteValue()));
            } else if (value instanceof Short) {
                query.setBinding(name, valueFactory.createLiteral(((Short) value).shortValue()));
            } else if (value instanceof Integer) {
                query.setBinding(name, valueFactory.createLiteral(((Integer) value).intValue()));
            } else if (value instanceof Long) {
                query.setBinding(name, valueFactory.createLiteral(((Long) value).longValue()));
            } else if (value instanceof Float) {
                query.setBinding(name, valueFactory.createLiteral(((Float) value).floatValue()));
            } else if (value instanceof Double) {
                query.setBinding(name, valueFactory.createLiteral(((Double) value).doubleValue()));
            } else if (value instanceof Boolean) {
                query.setBinding(name, valueFactory.createLiteral(((Boolean) value).booleanValue()));
            } else if (value instanceof String) {
                query.setBinding(name, valueFactory.createLiteral(value.toString()));
            } else if (value instanceof URI) {
                try {
                    query.setBinding(name, valueFactory.createURI(value.toString()));
                } catch (IllegalArgumentException e) {
                    query.setBinding(name, valueFactory.createBNode(value.toString()));
                }
            } else if (value instanceof Date) {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime((Date) value);
                query.setBinding(name, valueFactory.createLiteral(datatypeFactory.newXMLGregorianCalendar(calendar)));
            } else if (value instanceof BlankNode) {
                query.setBinding(name, valueFactory.createBNode(((BlankNode) value).getId()));
            } else {
                throw new IllegalArgumentException("Unsupported type is bound to name " + name);
            }
        }
    }

    /**
     * Textual representation of predicates and values.
     *
     * @return key value (Object.toString()) pairs separated by equal sign.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Iterator<String> i = this.bindings.keySet().iterator(); i.hasNext();) {
            String k = i.next();
            Object o = this.bindings.get(k);
            s.append(k).append("=").append(o).append("\r\n");
        }

        return s.toString();
    }

    /**
     * Private wrapper for blanknode binding.
     */
    private static class BlankNode {
        /** node id. */
        String id;

        /**
         * initializes blank node.
         *
         * @param id Bode ID (with blank node prefix)
         */
        BlankNode(String id) {
            this.id = id.substring(VirtuosoBaseDAO.BNODE_URI_PREFIX.length());
        }

        public String getId() {
            return id;
        }
    }

    /**
     * Common method to set IRI that is suppposed to be URI. As IRI and URI have different standards if the URI is invalid URI the
     * query has to use IRI() function and the parameter is given as string not URI.
     *
     * @param name Param Name
     * @param value Param Value
     */
    public void setIRI(String name, String value) {

        if (SPARQLQueryUtil.isIRI(value)) {
            setURI(name, value);
        } else {
            setString(name, value);
        }
    }

    /**
     * Returns bindings.
     *
     * @return
     */
    public HashMap<String, Object> getBindings() {
        return bindings;
    }

}
