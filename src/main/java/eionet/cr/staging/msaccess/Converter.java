package eionet.cr.staging.msaccess;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

/**
 *
 * @author jaanus
 *
 */
public class Converter {

    /** */
    private static final Logger LOGGER = Logger.getLogger(Converter.class);

    /** */
    private ConversionHandlerIF convHandler;

    /** */
    private boolean             dataOnly;

    /** */
    private boolean             structOnly;

    /**
     *
     * @param convHandler
     */
    public Converter(ConversionHandlerIF convHandler) {

        if (convHandler == null) {
            throw new IllegalArgumentException("Conversion handler must not be null!");
        }
        this.convHandler = convHandler;
    }

    /**
     *
     * @param convHandler
     */
    public Converter(ConversionHandlerIF convHandler, boolean structOnly, boolean dataOnly) {

        this(convHandler);
        this.structOnly = structOnly;
        this.dataOnly = dataOnly;
    }

    /**
     *
     * @param filePath
     * @throws ConversionException
     */
    public void convert(String filePath) throws ConversionException {
        convert(new File(filePath));
    }

    /**
     *
     * @param file
     * @throws ConversionException
     */
    public void convert(File file) throws ConversionException {

        Database database = null;
        try {
            database = openDatabase(file);
            Set<String> tableNames = getTableNames(database);
            if (tableNames == null || tableNames.isEmpty()) {
                LOGGER.info("Found no tables in the database");
                return;
            }

            for (String tableName : tableNames) {

                Table table = getTable(database, tableName);
                if (!dataOnly) {
                    convHandler.newTable(table);
                }

                if (!structOnly) {
                    LOGGER.info("Going to process the "  + table.getRowCount() + " rows of table " + tableName);
                    int rowNum = 0;
                    for (Map<String, Object> row : table) {
                        try {
                            rowNum++;
                            convHandler.processRow(table, row);
                        } catch (ConversionException e) {
                            LOGGER.error(e.getClass().getSimpleName() + " at row #" + rowNum);
                            throw e;
                        }
                    }
                }
            }
            convHandler.endOfFile();
        } finally {
            close(database);
        }
    }

    /**
     *
     * @param database
     * @param tableName
     * @return
     * @throws ConversionException
     */
    private Table getTable(Database database, String tableName) throws ConversionException {
        try {
            return database.getTable(tableName);
        } catch (IOException e) {
            throw new ConversionException("Failed to get table " + tableName, e);
        }
    }

    /**
     *
     * @param database
     * @return
     * @throws ConversionException
     */
    private Set<String> getTableNames(Database database) throws ConversionException {

        try {
            return database.getTableNames();
        } catch (IOException e) {
            throw new ConversionException("Failed to get the table names of the database", e);
        }
    }

    /**
     *
     * @param file
     * @return
     * @throws ConversionException
     */
    private Database openDatabase(File file) throws ConversionException {
        try {
            Database database = Database.open(file);
            if (database == null) {
                throw new ConversionException("Failed to open database at this file: " + file);
            }
            return database;
        } catch (IOException e) {
            throw new ConversionException("Failed to open database at this file: " + file, e);
        }
    }

    /**
     *
     * @param database
     */
    private void close(Database database) {
        if (database != null) {
            try {
                database.close();
            } catch (IOException e) {
                // Deliberately ignore closing exceptions
            }
        }
    }
}
