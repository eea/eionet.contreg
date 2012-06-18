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
package eionet.cr.util.sql;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

/**
 * 
 * @author heinljab
 * 
 */
public class SQLValue {

    /** */
    private Object value;
    private int sqlType;

    /**
     * 
     * @param value
     * @param sqlType
     */
    public SQLValue(Object value, int sqlType) {
        this.value = value;
        this.sqlType = sqlType;
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>java.lang.String</code>.
     */
    public String getString() {

        return value == null ? null : value.toString();
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>java.sql.Date</code> or throws <code>SQLException</code>, if conversion
     * to date fails.
     */
    public Date getDate() throws SQLException {

        if (value == null)
            return null;

        switch (sqlType) {
            case Types.DATE:
                return (Date) value;
            case Types.TIME:
            case Types.TIMESTAMP:
                return new Date(((java.util.Date) value).getTime());
            default:
                throw new SQLException("Incompatible data type date: " + value);
        }
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>java.sql.Timestamp</code> or throws <code>SQLException</code>, if
     * conversion to timestamp fails.
     */
    public Timestamp getTimestamp() throws SQLException {
        if (value == null) {
            return null;
        }

        switch (sqlType) {
            case Types.TIMESTAMP:
                return (Timestamp) value;
            case Types.TIME:
            case Types.DATE:
                return new Timestamp(((java.util.Date) value).getTime());
            default:
                throw new SQLException("Incompatible data type for timestamp: " + value);
        }
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>int</code> or throws <code>SQLException</code>, if conversion to integer
     * fails.
     */
    public int getIntegerValue() throws SQLException {
        if (value == null) {
            return 0;
        }

        switch (sqlType) {
            case Types.INTEGER:
                return (int) ((Long) value).longValue();
            case Types.NUMERIC:
                return ((BigDecimal) value).intValue();
            case Types.VARCHAR:
            case Types.CHAR:
                // case Types.LONGNVARCHAR:
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException nfe) {
                    throw new SQLException("Incompatible data type for integer: " + value);
                }
            default:
                throw new SQLException("Incompatible data type for integer: " + value);
        }
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>java.lang.Integer</code> or throws <code>SQLException</code>, if
     * conversion to integer fails.
     */
    public Integer getInteger() throws SQLException {
        if (value == null) {
            return null;
        }

        return new Integer(getIntegerValue());
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>java.lang.Long</code> or throws <code>SQLException</code>, if conversion
     * to long integer fails.
     */
    public Long getLong() throws SQLException {
        if (value == null) {
            return null;
        }

        switch (sqlType) {
            case Types.INTEGER:
                return (Long) value;
            case Types.NUMERIC:
                return new Long(((BigDecimal) value).longValue());
            case Types.VARCHAR:
            case Types.CHAR:
                // case Types.LONGNVARCHAR:
                try {
                    return new Long((String) value);
                } catch (NumberFormatException nfe) {
                    throw new SQLException("Incompatible data type for long: " + value);
                }
            default:
                throw new SQLException("Incompatible data type for long: " + value);
        }
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>long</code> or throws <code>SQLException</code>, if conversion to long
     * integer fails.
     */
    public long getLongValue() throws SQLException {
        if (value == null) {
            return 0;
        }

        switch (sqlType) {
            case Types.INTEGER:
                return ((Long) value).longValue();
            case Types.NUMERIC:
                return ((BigDecimal) value).longValue();
            case Types.VARCHAR:
            case Types.CHAR:
                // case Types.LONGNVARCHAR:
                try {
                    return Long.parseLong((String) value);
                } catch (NumberFormatException nfe) {
                    throw new SQLException("Incompatible data type for long: " + value);
                }
            default:
                throw new SQLException("Incompatible data type for long: " + value);
        }
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>java.lang.Double</code> or throws <code>SQLException</code>, if
     * conversion to double fails.
     */
    public Double getDouble() throws SQLException {
        if (value == null) {
            return null;
        }

        return new Double(getDoubleValue());
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>double</code> or throws <code>SQLException</code>, if conversion to
     * double fails.
     */
    public double getDoubleValue() throws SQLException {
        if (value == null) {
            return 0.;
        }

        switch (sqlType) {
            case Types.NUMERIC:
                return ((BigDecimal) value).doubleValue();
            case Types.INTEGER:
                return ((Integer) value).doubleValue();
            case Types.VARCHAR:
            case Types.CHAR:
                // case Types.LONGNVARCHAR:
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException nfe) {
                    throw new SQLException("Incompatible data type for double: " + value);
                }
            default:
                throw new SQLException("Incompatible data type for double: " + value);
        }
    }

    /**
     * Returns value of this <code>SQLValue</code> as <code>byte[]</code> or throws <code>SQLException</code>, if conversion to byte
     * array fails.
     */
    public byte[] getBytes() throws SQLException {
        if (value == null) {
            return null;
        }

        switch (sqlType) {
            case Types.BINARY:
                return (byte[]) value;
            case Types.VARCHAR:
            case Types.CHAR:
                // case Types.LONGNVARCHAR:
                try {
                    return ((String) value).getBytes("UTF-8");
                } catch (UnsupportedEncodingException ue) {
                    throw new SQLException("Failed conversion to bytes: " + value);
                }
            default:
                throw new SQLException("Incompatible data type for byte[]: " + value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return getString();
    }

    /**
     * 
     * @return
     */
    public Object getObject() {
        return value;
    }

    /**
     * 
     * @return
     */
    public int getSQLType() {
        return sqlType;
    }
}
