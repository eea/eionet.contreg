package eionet.cr.documentation;

import eionet.doc.dal.sql.SqlConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Documentation Connection Provider.
 *
 */
@Component
public final class DocumentationConnectionProvider implements SqlConnectionProvider {

    private final DataSource dataSource;

    @Autowired
    public DocumentationConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Connection createConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
    
}
