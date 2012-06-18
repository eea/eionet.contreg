/**
 *
 */
package eionet.cr.test.helpers.dbunit;

import java.sql.Types;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
import org.dbunit.dataset.datatype.StringDataType;

/**
 * @author Risto Alt
 * 
 */
public class DbUnitVirtuosoDataTypeFactory extends DefaultDataTypeFactory {

    static final DataType DATETIME = new StringDataType("DATETIME", Types.TIMESTAMP);

    public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
        if (sqlType == 11) {
            if (sqlTypeName.equalsIgnoreCase("DATETIME")) {
                return DATETIME;
            }
        }
        return super.createDataType(sqlType, sqlTypeName);
    }
}
