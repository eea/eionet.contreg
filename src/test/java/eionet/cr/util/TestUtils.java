package eionet.cr.util;

import eionet.cr.config.GeneralConfig;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mysql.MySqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;

/**
 *
 *
 */
public class TestUtils {

    public static void setUpDatabase(DataSource db, String dataset) throws Exception {
        Connection dsConnection = db.getConnection();
        IDatabaseConnection connection = new DatabaseConnection(dsConnection);
        DatabaseConfig config = connection.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MySqlDataTypeFactory());
        config.setProperty(DatabaseConfig.FEATURE_ALLOW_EMPTY_FIELDS, Boolean.TRUE);
        File file = new File(db.getClass().getClassLoader().getResource(dataset).getFile());
        IDataSet dataSet = new FlatXmlDataSetBuilder().build(file);
        DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);
    }

    public static String getFileUrl(String seedName) {
        return GeneralConfig.getProperty("test.httpd.url").concat(seedName);
    }
}
