package eionet.repgen;

import java.util.Map;

import net.sf.jasperreports.engine.JRDataset;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRValueParameter;
import net.sf.jasperreports.engine.JasperReportsContext;
import net.sf.jasperreports.engine.query.JRQueryExecuter;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;

/**
 * Factory to be used in iReports for SPARQL.
 *
 * @author Kaido Laine
 */
public class SPARQLQueryExecuterFactory implements QueryExecuterFactory {

    @Override
    public Object[] getBuiltinParameters() {
        return new Object[] {};
    }

    @Override
    public JRQueryExecuter createQueryExecuter(JasperReportsContext paramJasperReportsContext, JRDataset paramJRDataset,
            Map<String, ? extends JRValueParameter> paramMap) throws JRException {

        Logger.log("Going to execute SPARQL");
        String sparql = paramJRDataset.getQuery().getText();
        Logger.log("SPARQL from query " + sparql);

        Object countryCodeParam = paramMap.get("COUNTRY_CODE").getValue();

        if (countryCodeParam != null) {
            String countryCode = countryCodeParam.toString();
            sparql = SPARQLUtil.replaceJasperParam(sparql, "COUNTRY_CODE", countryCode);
            Logger.log("Country code replaced in SPARQL " + countryCode);
        }

        Logger.log("createQueryExecuter() Going to execute SPARQL " + " " + sparql);
        return new SPARQLQueryExecuter(sparql);
    }

    @Override
    public boolean supportsQueryParameterType(String paramString) {
        if (paramString.equals("java.lang.String")) {
            return true;
        }
        return false;
    }

    @Override
    public JRQueryExecuter createQueryExecuter(JRDataset paramJRDataset, Map<String, ? extends JRValueParameter> paramMap)
            throws JRException {

        String sparql = paramJRDataset.getQuery().getText();
        Logger.log("createQueryExecuter() SPARQL " + sparql);
        return new SPARQLQueryExecuter(sparql);
    }

}
