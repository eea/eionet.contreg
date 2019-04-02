package eionet.repgen;

import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import eionet.repgen.datasources.Art17DeliveriesDS;
import eionet.repgen.datasources.SPARQLDataSource;

/**
 * standalone report generator.
 *
 * @author Kaido Laine
 */
public class QARepGenerator {

    /** sample call that can be used in local machine. */

    /*
    public static void main(String [] args) {
        QARepGenerator gen = new QARepGenerator();
        try {
            gen.fill("EE");
            gen.pdf();

        } catch (JRException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
     */

    /*public void compile() {
        JasperCompileManager.compileReportToFile(
                "ExpertQA.jrxml"" +
                        "
                our_compiled_template.jasper");//the path and name we want to save the compiled file to
    } */

    /**
     * fills report template.
     * @param countryCode
     * @throws JRException
     */
    public void fill(String countryCode) throws JRException {
        long start = System.currentTimeMillis();
        //Preparing parameters
        Map parameters = new HashMap();

        try {
            parameters.put("Art17Table1DataSource", new Art17DeliveriesDS(countryCode));
            SPARQLDataSource table6DS  = new SPARQLDataSource();
            String tbl6Sparql = SPARQLUtil.getSparqlBookmarkByName("Art17 table 6");
            tbl6Sparql = SPARQLUtil.replaceSparqlParam(tbl6Sparql, "countryCode", countryCode);
            table6DS.init(tbl6Sparql);

            parameters.put("Art17Table6DataSource", table6DS);
        } catch (Exception e1) {
            // TODO Auto-generated catch block
            throw new JRException("Error " + e1);
        }



        try {
            JasperFillManager.fillReportToFile("ExpertQA.jasper", parameters, new Art17DeliveriesDS(countryCode));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //TODO Logger
        System.err.println("Filling time : " + (System.currentTimeMillis() - start));
    }


    /**
    *
    */
   public void pdf() throws JRException {
       long start = System.currentTimeMillis();
       JasperExportManager.exportReportToPdfFile("ExpertQA.jrprint");
       //TODO Logger
       System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
   }
}
