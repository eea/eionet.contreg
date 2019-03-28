package eionet.repgen;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import eionet.repgen.datasources.SPARQLDataSource;

public class SPARQLQueryExecuter implements JRQueryExecuter {

    private String sparql;

    public SPARQLQueryExecuter(String sparql) {
        this.sparql = sparql;
    }

    @Override
    public JRDataSource createDatasource() throws JRException {
        SPARQLDataSource ds = new SPARQLDataSource();
        try {
            ds.init(sparql);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            throw new JRException("SPARQL init failed " + e);
        }
        return ds;
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean cancelQuery() throws JRException {
        // TODO Auto-generated method stub
        return false;
    }

}
