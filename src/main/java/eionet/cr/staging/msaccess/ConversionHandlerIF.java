package eionet.cr.staging.msaccess;

import java.util.Map;

import com.healthmarketscience.jackcess.Table;

/**
 *
 * @author jaanus
 *
 */
public interface ConversionHandlerIF {

    /**
     *
     * @param table
     * @throws ConversionException
     */
    void newTable(Table table) throws ConversionException;

    /**
     *
     * @param table
     * @param row
     * @throws ConversionException
     */
    void processRow(Table table, Map<String,Object> row) throws ConversionException;

    /**
     * @throws ConversionException
     *
     */
    void endOfFile() throws ConversionException;

    /**
     *
     */
    void close();
}
