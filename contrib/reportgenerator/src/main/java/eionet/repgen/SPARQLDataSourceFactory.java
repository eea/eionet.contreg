package eionet.repgen;

import net.sf.jasperreports.engine.JRDataSource;
import eionet.repgen.datasources.SPARQLDataSource;

public class SPARQLDataSourceFactory {
    public static JRDataSource createDataSource() throws Exception {
        SPARQLDataSource ds = new SPARQLDataSource();

        return ds;
    }
}
