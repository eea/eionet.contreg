package eionet.cr.web.action;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.repository.RepositoryConnection;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.sesame.SPARQLResultSetBaseReader;
import eionet.cr.util.sesame.SesameUtil;

/**
 * 
 * @author jaanus
 * 
 */
@UrlBinding("/virtuosoQuery.action")
public class VirtuosoQueryActionBean extends AbstractActionBean {

    private String query;

    /**
     * 
     * @return
     */
    @DefaultHandler
    public Resolution view() {
        return new ForwardResolution("/pages/virtuosoQuery.jsp");
    }

    /**
     * 
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public StreamingResolution query() throws DAOException {

        RepositoryConnection conn = null;
        try {
            conn = SesameUtil.getRepositoryConnection();
            ResultReader reader = new ResultReader();

            long startTime = System.currentTimeMillis();
            SesameUtil.executeQuery(query, reader, conn);
            long execTime = System.currentTimeMillis() - startTime;

            String result = reader.getResult();
            if (result == null || result.trim().length() == 0) {
                result = "The query gave no results!";
            } else {
                result = result + "<br/> Done, -- " + execTime + " ms.";
            }

            return new StreamingResolution("text/html", result);
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SesameUtil.close(conn);
        }
    }

    /**
     * @return
     */
    public String getQuery() {
        return query;
    }

    /**
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * 
     * @author jaanus
     * 
     */
    private class ResultReader extends SPARQLResultSetBaseReader {

        /** */
        private StringBuffer result = new StringBuffer();

        /** */
        private int rowCounter = 0;

        /*
         * (non-Javadoc)
         * 
         * @see eionet.cr.util.sesame.SPARQLResultSetBaseReader#startResultSet()
         */
        protected void startResultSet() {

            if (bindingNames == null || bindingNames.isEmpty()) {
                return;
            }

            result.append("<table class=\"datatable\">");
            result.append("<thead>");

            for (Object bindingName : bindingNames) {
                result.append("<th>").append(bindingName).append("</th>");
            }

            result.append("</thead>");
            result.append("<tbody>");
        }

        /*
         * (non-Javadoc)
         * 
         * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
         */
        @Override
        public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

            if (rowCounter % 2 == 0) {
                result.append("<tr class=\"odd\">");
            } else {
                result.append("<tr class=\"even\">");
            }

            for (Object bindingName : bindingNames) {

                Value value = bindingSet.getValue(bindingName.toString());
                result.append("<td>").append(value.stringValue()).append("</td>");
            }

            result.append("<tr>");
            rowCounter++;
        }

        /*
         * (non-Javadoc)
         * 
         * @see eionet.cr.util.sesame.SPARQLResultSetBaseReader#endResultSet()
         */
        @Override
        public void endResultSet() {

            if (rowCounter > 0 && result.length() > 0) {

                result.append("</tbody>");
                result.append("</table>");
            }
        }

        /**
         *
         */
        public String getResult() {
            return result.toString();
        }
    }
}
