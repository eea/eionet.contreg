package eionet.cr.util.sesame;

import java.sql.Connection;
import java.sql.SQLException;

import org.openrdf.OpenRDFException;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.Bindings;

/**
 *
 * @author jaanus
 *
 */
public class SesameUtil {

    /**
     *
     * @return RepositoryConnection
     * @throws RepositoryException
     */
    public static RepositoryConnection getRepositoryConnection()
            throws RepositoryException {

        return SesameConnectionProvider.getRepositoryConnection();
    }

    /**
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection getConnection() throws SQLException {

        return SesameConnectionProvider.getSimpleConnection();
    }

    /**
     * 
     * @param <T>
     * @param sparql
     * @param bindings
     * @param reader
     * @param conn
     * @throws OpenRDFException
     * @throws ResultSetReaderException
     */
    public static <T> void executeQuery(String sparql, Bindings bindings,
            SPARQLResultSetReader<T> reader, RepositoryConnection conn)
            throws OpenRDFException, ResultSetReaderException {

        TupleQueryResult queryResult = null;
        try {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
            
            if (bindings!=null){
                bindings.applyTo(tupleQuery, conn.getValueFactory());
            }
            
            queryResult = tupleQuery.evaluate();
            if (queryResult != null) {

                boolean isFirstRow = true;
                while (queryResult.hasNext()) {

                    if (isFirstRow) {
                        reader.startResultSet(queryResult.getBindingNames());
                        isFirstRow = false;
                    }
                    reader.readRow(queryResult.next());
                }
                reader.endResultSet();
            }
        } finally {
            SesameUtil.close(queryResult);
        }
    }

    /**
     * 
     * @param <T>
     * @param sparql
     * @param reader
     * @param conn
     * @throws OpenRDFException
     * @throws ResultSetReaderException
     */
    public static <T> void executeQuery(String sparql,
            SPARQLResultSetReader<T> reader, RepositoryConnection conn)
            throws OpenRDFException, ResultSetReaderException {
        
        executeQuery(sparql, null, reader, conn);
    }

    /**
     * @param sparql
     * @param conn
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     *
     *             Executes SPARQL query that changes RDF data. Rollback is NOT
     *             made if query does not succeed
     */
    public static void executeUpdateQuery(String sparql,
            RepositoryConnection conn) throws RepositoryException,
            QueryEvaluationException, MalformedQueryException {

        BooleanQuery query = conn.prepareBooleanQuery(QueryLanguage.SPARQL,
                sparql);
        query.evaluate();

    }

    /**
     * Executes SPARQL Query producing RDF and exports to the passed RDF handler.
     * @param sparql SPARQL for (CONSTRUCT) query
     * @param rdfHandler RDF handler for output RDF format
     * @param conn RepositoryConnection
     * @throws QueryEvaluationException if query evaluation fails
     * @throws RDFHandlerException if RDF handler fails
     * @throws MalformedQueryException if query is not formed correctly
     * @throws RepositoryException if Repository API call fails
     */
    public static void exportGraphQuery(final String sparql, final RDFHandler rdfHandler, final RepositoryConnection conn)
        throws QueryEvaluationException, RDFHandlerException, MalformedQueryException, RepositoryException  {

        GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
        graphQuery.evaluate(rdfHandler);
    }
    /**
     *
     * @param queryResult Query Result
     */
    public static void close(final TupleQueryResult queryResult) {

        if (queryResult != null) {
            try {
                queryResult.close();
            } catch (QueryEvaluationException e) {}
        }
    }

    /**
     *
     * @param queryResult
     */
    public static void close(GraphQueryResult queryResult) {

        if (queryResult != null) {
            try {
                queryResult.close();
            } catch (QueryEvaluationException e) {
            }
        }
    }

    /**
     *
     * @param repo
     */
    public static void shutdown(Repository repo) {

        if (repo != null) {
            try {
                repo.shutDown();
            } catch (RepositoryException e) {
            }
        }
    }

    /**
     *
     * @param conn
     */
    public static void close(RepositoryConnection conn) {

        if (conn != null) {
            try {
                conn.close();
            } catch (RepositoryException e) {
            }
        }
    }

}
