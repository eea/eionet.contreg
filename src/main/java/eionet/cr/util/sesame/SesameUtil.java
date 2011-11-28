package eionet.cr.util.sesame;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.openrdf.OpenRDFException;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import virtuoso.sesame2.driver.VirtuosoRepositoryConnection;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.Bindings;
import eionet.cr.util.Util;

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
    public static RepositoryConnection getRepositoryConnection() throws RepositoryException {

        return SesameConnectionProvider.getRepositoryConnection();
    }

    /**
     *
     * @return Connection
     * @throws SQLException
     */
    public static Connection getSQLConnection() throws SQLException {

        return SesameConnectionProvider.getSQLConnection();
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
    public static <T> void executeQuery(String sparql, Bindings bindings, SPARQLResultSetReader<T> reader,
            RepositoryConnection conn) throws OpenRDFException, ResultSetReaderException {
        executeQuery(sparql, bindings, reader, null, conn);
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
    public static <T> void executeQuery(String sparql, Bindings bindings, SPARQLResultSetReader<T> reader, String defaultGraphUri,
            RepositoryConnection conn) throws OpenRDFException, ResultSetReaderException {

        TupleQueryResult queryResult = null;
        try {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);

            ValueFactory valueFactory = conn.getValueFactory();
            if (bindings != null) {
                bindings.applyTo(tupleQuery, valueFactory);
            }

            if (!StringUtils.isBlank(defaultGraphUri)) {
                DatasetImpl dataset = new DatasetImpl();
                dataset.addDefaultGraph(valueFactory.createURI(defaultGraphUri));
                tupleQuery.setDataset(dataset);
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
    public static <T> void executeQuery(String sparql, SPARQLResultSetReader<T> reader, RepositoryConnection conn)
    throws OpenRDFException, ResultSetReaderException {

        executeQuery(sparql, null, reader, conn);
    }

    /**
     *
     * @param <T>
     * @param sparql
     * @param reader
     * @param defaultGraphUri
     * @param conn
     * @throws OpenRDFException
     * @throws ResultSetReaderException
     */
    public static <T> void executeQuery(String sparql, SPARQLResultSetReader<T> reader, String defaultGraphUri,
            RepositoryConnection conn) throws OpenRDFException, ResultSetReaderException {

        executeQuery(sparql, null, reader, defaultGraphUri, conn);
    }

    /**
     * Executes a SPARQL/Update (SPARUL) query (i.e. one that performs modifications on data). Throws
     * {@link UnsupportedOperationException} if the given connection is not a {@link VirtuosoRepositoryConnection}, because it uses
     * {@link VirtuosoRepositoryConnection#executeSPARUL(String)} to execute the given SPARQL/Update statement. The latter supports
     * not variable bindings, hence no bindings given in the method's input too.
     *
     * @param sparul The SPARUL statement to execute.
     * @param conn The repository connection to operate on.
     * @param defaultGraphUri
     * @return The number triples inserted/updated/deleted
     * @throws OpenRDFException When a repository access error occurs.
     */
    public static int executeSPARUL(String sparul, RepositoryConnection conn, String... defaultGraphUri) throws OpenRDFException {

        if (!(conn instanceof VirtuosoRepositoryConnection)) {
            throw new UnsupportedOperationException("Method implemented only for "
                    + VirtuosoRepositoryConnection.class.getSimpleName());
        }

        if (defaultGraphUri != null && defaultGraphUri.length > 0) {
            for (int i = 0; i < defaultGraphUri.length; i++) {
                sparul = "define input:default-graph-uri <" + defaultGraphUri[i] + "> " + sparul;
            }
        }

        VirtuosoRepositoryConnection virtConn = (VirtuosoRepositoryConnection) conn;
        return virtConn.executeSPARUL(sparul);
    }

    /**
     * Executes the given SPARQL/Update (aka SPARUL) statement. Rollback is NOT made if query does not succeed.
     *
     * @param sparul
     * @param conn repository connection
     * @param bindings Query bindings
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     */
    public static void executeSPARUL(String sparul, Bindings bindings, RepositoryConnection conn) throws RepositoryException,
    QueryEvaluationException, MalformedQueryException {

        BooleanQuery query = conn.prepareBooleanQuery(QueryLanguage.SPARQL, sparul);
        if (bindings != null) {
            bindings.applyTo(query, conn.getValueFactory());
        }
        query.evaluate();
    }

    /**
     * Executes SPARQL Query producing RDF and exports to the passed RDF handler.
     *
     * @param sparql SPARQL for (CONSTRUCT) query
     * @param rdfHandler RDF handler for output RDF format
     * @param conn RepositoryConnection
     * @param bindings Query Bindings
     * @throws QueryEvaluationException if query evaluation fails
     * @throws RDFHandlerException if RDF handler fails
     * @throws MalformedQueryException if query is not formed correctly
     * @throws RepositoryException if Repository API call fails
     */
    public static void exportGraphQuery(final String sparql, final RDFHandler rdfHandler, final RepositoryConnection conn,
            final Bindings bindings) throws QueryEvaluationException, RDFHandlerException, MalformedQueryException,
            RepositoryException {

        GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
        if (bindings != null) {
            bindings.applyTo(graphQuery, conn.getValueFactory());
        }

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
            } catch (QueryEvaluationException e) {
            }
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
                // ignoring repository closing exceptions
            }
        }
    }

    /**
     *
     * @param conn
     */
    public static void rollback(RepositoryConnection conn) {

        if (conn != null) {
            try {
                conn.rollback();
            } catch (RepositoryException e) {
                // ignoring rollback exceptions
            }
        }
    }

    /**
     *
     * @param sparulQuery
     * @return
     */
    public static boolean isWellFormedSPARUL(String sparulQuery) {

        if (StringUtils.isBlank(sparulQuery)) {
            return false;
        }

        try {
            new SPARQLParser().parseUpdate(sparulQuery, null);
            return true;
        } catch (MalformedQueryException e) {
            return false;
        }
    }

    /**
     *
     * @param sparqlQuery
     * @return
     */
    public static boolean isWellFormedSPARQL(String sparqlQuery) {

        if (StringUtils.isBlank(sparqlQuery)) {
            return false;
        }

        try {
            new SPARQLParser().parseQuery(sparqlQuery, null);
            return true;
        } catch (MalformedQueryException e) {
            return false;
        }
    }

    /**
     *
     * @param query
     * @return
     */
    public static boolean isSelectQuery(String query) {

        return isWellFormedSPARQL(query) && Util.containsTokenIgnoreCase(query, "SELECT");
    }

    /**
     *
     * @param query
     * @return
     */
    public static boolean isConstructQuery(String query) {

        return isWellFormedSPARQL(query) && Util.containsTokenIgnoreCase(query, "CONSTRUCT");
    }

    /**
     *
     * @param query
     * @return
     */
    public static boolean isDescribeQuery(String query) {

        return isWellFormedSPARQL(query) && Util.containsTokenIgnoreCase(query, "DESCRIBE");
    }

    /**
     *
     * @param query
     * @return
     */
    public static boolean isaSKQuery(String query) {

        return isWellFormedSPARQL(query) && Util.containsTokenIgnoreCase(query, "ASK");
    }
}
