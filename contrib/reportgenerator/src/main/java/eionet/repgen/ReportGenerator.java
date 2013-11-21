package eionet.repgen;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import eionet.repgen.datasources.Art17DeliveriesDS;
import eionet.repgen.datasources.SPARQLDataSource;

/**
 * servlet for testing reports.
 *
 * @author Kaido Laine
 */
public class ReportGenerator extends HttpServlet {

    private String art17Sparql;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleSubmit(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        handleSubmit(request, response);
    }

    /**
     * Handles form submissions for <code>#doGet</code> and <code>#doPost</code>.
     *
     * @param request
     *            The <code>HttpServletRequest</code> wrapping the HTTP request that triggered this method.
     * @param response
     *            The <code>HttpServletResponse</code> that gives this method access to the HTTP response the servlet container will
     *            send back to the user agent.
     * @throws IOException
     *             when an error occurs while trying to access the output stream to notify the user of an error.
     */
    protected void handleSubmit(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Declare the printwriter (which we'll use if an error occurs), but
        // don't instantiate it yet because instantiating it will prevent us
        // from streaming the PDF back to the client if everything else works.
        PrintWriter out = null;
        File destFile = null;

        try {

            // Get report file name from params
            String rptfilename = "ExpertQA"; // request.getParameter("rptfilename");
            String countryCode = request.getParameter("countrycode");
            String format = request.getParameter("format");

            // get the name filter
            // String namefilter = request.getParameter("NameFilter");

            // Add filter value to a hashtable of report parameters
            // Preparing parameters
            Map parameters = new HashMap();
            // parameters.put("ReportTitle", "Address Report");
            parameters.put("DataFile", "Art17DeliveriesDS.java");
            // parameters.put("Art17Table1", "Art17DeliveriesDS.java");

            // fill the report
            JasperPrint jasperPrint = null;
            try {

                parameters.put("Art17Table1DataSource", new Art17DeliveriesDS(countryCode));

                SPARQLDataSource table6DS = new SPARQLDataSource();
                String tbl6Sparql = SPARQLUtil.getSparqlBookmarkByName("Art17 table 6");
                tbl6Sparql = SPARQLUtil.replaceSparqlParam(tbl6Sparql, "countryCode", countryCode);
                table6DS.init(tbl6Sparql);

                parameters.put("Art17Table6DataSource", table6DS);

                jasperPrint =
                        JasperFillManager.fillReport(getServletContext().getRealPath("/") + rptfilename + ".jasper", parameters,
                                new Art17DeliveriesDS(countryCode));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String jrprintName = getServletContext().getRealPath("/") + rptfilename + ".jrprint";
            // export report to pdf and stream back to browser
            byte[] retrunBytes = null;
            String contentType = "unknown";
            String contentHeader = "";

            if (jasperPrint != null) {

                if (format.equalsIgnoreCase("pdf")) {
                    retrunBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                    contentType = "application/pdf";
                    contentHeader = "inline; filename=\"ExpertQA.pdf\"";
                } else if (format.equalsIgnoreCase("doc")) {
                    // TODO
                    String fileName = getServletContext().getRealPath("/") + jasperPrint.getName() + ".docx";
                    destFile = new File(fileName);
                    //
                    JRDocxExporter exporter = new JRDocxExporter();
                    //
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());

                    exporter.exportReport();
                    //
                    //
                    retrunBytes = read(fileName);
                    contentType = "application/ms-word";
                    contentHeader = "inline; filename=\"ExpertQA.docx\"";
                }
                ServletOutputStream outstream = response.getOutputStream();
                response.setContentType(contentType);
                response.setContentLength(retrunBytes.length);

                response.setHeader("Content-disposition", contentHeader);
                outstream.write(retrunBytes);
            } else {
                throw new JRException("Error gettting data ");
            }

        } catch (JRException jre) {
            // Get the writer from the response so we can output markup
            out = response.getWriter();

            // Jasper had an internal error when filling the report. Give the
            // user the lowdown.
            out.println("<html>");
            out.println("\t<body>");
            out.println("\t\t<br /><br />");
            out.println("\t\tJasper encountered a problem when attempting" + "to populate the report.");
            out.println("\t\t<br /><br />");
            out.println("\t\tError Message ==> " + jre.getLocalizedMessage());
            out.println("\t\t<br />");
            out.println("\t\tCause of Error ==> " + jre.getCause());
            out.println("\t</body>");
            out.println("</html>");
        } catch (IOException ioe) {
            // Get the writer from the response so we can output markup
            out = response.getWriter();

            // This was an IO error, so it quite possibly resulted from an error
            // creating the print writer, but we're still gonna give it the old
            // college try and attempt to send back useful info to the user
            out.println("<html>");
            out.println("\t<body>");
            out.println("\t\t<br /><br />");
            out.println("\t\tDue to a naming problem with this servlet's" + "initial context, the system is unable to display"
                    + "the report at this time.");
            out.println("\t\t<br /><br />");
            out.println("\t\tError Message ==> " + ioe.getLocalizedMessage());
            out.println("\t\t<br />");
            out.println("\t\tCause of Error ==> " + ioe.getCause());
            out.println("\t</body>");
            out.println("</html>");
        } finally {

            if (destFile != null) {
                destFile.delete();
            }
            // close streams

        }
    }

    private byte[] read(String aInputFileName) {
        log("Reading in binary file named : " + aInputFileName);
        File file = new File(aInputFileName);
        log("File size: " + file.length());
        byte[] result = new byte[(int) file.length()];
        try {
            InputStream input = null;
            try {
                int totalBytesRead = 0;
                input = new BufferedInputStream(new FileInputStream(file));
                while (totalBytesRead < result.length) {
                    int bytesRemaining = result.length - totalBytesRead;
                    // input.read() returns -1, 0, or more :
                    int bytesRead = input.read(result, totalBytesRead, bytesRemaining);
                    if (bytesRead > 0) {
                        totalBytesRead = totalBytesRead + bytesRead;
                    }
                }
                /*
                 * the above style is a bit tricky: it places bytes into the 'result' array; 'result' is an output parameter; the
                 * while loop usually has a single iteration only.
                 */
                log("Num bytes read: " + totalBytesRead);
            } finally {
                log("Closing input stream.");
                input.close();
            }
        } catch (FileNotFoundException ex) {
            log("File not found.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
