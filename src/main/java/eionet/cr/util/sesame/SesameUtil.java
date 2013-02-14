package eionet.cr.util.sesame;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

import virtuoso.sesame2.driver.VirtuosoRepositoryConnection;
import eionet.cr.common.CRRuntimeException;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.Bindings;
import eionet.cr.util.Util;

/**
 *
 * @author jaanus
 *
 */
public final class SesameUtil {

    /** Static logger. */
    protected static final Logger LOGGER = Logger.getLogger(SesameUtil.class);

    /**
     * Hide utility class constructor.
     */
    private SesameUtil() {
        // Just an empty private constructor to avoid instantiating this utility class.
    }

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
     * @param dbName
     * @return
     * @throws SQLException
     */
    public static Connection getSQLConnection(String dbName) throws SQLException {

        return SesameConnectionProvider.getSQLConnection(dbName);
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
     * @param sparul
     *            The SPARUL statement to execute.
     * @param conn
     *            The repository connection to operate on.
     * @param defaultGraphUri
     * @return The number triples inserted/updated/deleted
     * @throws OpenRDFException
     *             When a repository access error occurs.
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
     * Executes a SPARQL/Update (SPARUL) query (i.e. one that performs modifications on data).
     *
     * @param sparul
     * @param conn
     *            repository connection
     * @param bindings
     *            Query bindings
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
     * @param sparql
     *            SPARQL for (CONSTRUCT) query
     * @param rdfHandler
     *            RDF handler for output RDF format
     * @param conn
     *            RepositoryConnection
     * @param bindings
     *            Query Bindings
     * @throws QueryEvaluationException
     *             if query evaluation fails
     * @throws RDFHandlerException
     *             if RDF handler fails
     * @throws MalformedQueryException
     *             if query is not formed correctly
     * @throws RepositoryException
     *             if Repository API call fails
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
     * @param queryResult
     *            Query Result
     */
    public static void close(TupleQueryResult queryResult) {

        if (queryResult != null) {
            try {
                queryResult.close();
            } catch (Exception e) {
                // Ignore closing exceptions.
                LOGGER.warn("Exception when closing TupleQueryResult.", e);
            }
        }
    }

    /**
     *
     * @param repositoryResult
     */
    public static <T> void close(RepositoryResult<T> repositoryResult) {

        if (repositoryResult != null) {
            try {
                repositoryResult.close();
            } catch (Exception e) {
                // Ignore closing exceptions.
                LOGGER.warn("Exception when closing RepositoryResult.", e);
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
            } catch (Exception e) {
                // Ignore closing exceptions.
                LOGGER.warn("Exception when closing GraphQueryResult.", e);
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
            } catch (Exception e) {
                // Ignore shutdown exceptions.
                LOGGER.warn("Exception when shutting down the repository.", e);
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
            } catch (Exception e) {
                // Ignoring closing exceptions.
                LOGGER.warn("Exception when closing connection", e);
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
                // Ignore rollback exceptions.
                LOGGER.warn("Exception when rollbacking connection", e);
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

    /**
     * Sets dataset to query, if default-graph-uri or named-graph-uri parameters are used.
     *
     * @param query
     * @param connection
     * @param defaultGraphUris
     * @param namedGraphUris
     */
    public static void setDatasetParameters(Query query, RepositoryConnection connection, String[] defaultGraphUris,
            String[] namedGraphUris) {
        if (defaultGraphUris != null || namedGraphUris != null) {
            DatasetImpl dataset = new DatasetImpl();
            if (defaultGraphUris != null) {
                for (String uriStr : defaultGraphUris) {
                    dataset.addDefaultGraph(connection.getValueFactory().createURI(uriStr));
                }
            }
            if (namedGraphUris != null) {
                for (String uriStr : namedGraphUris) {
                    dataset.addNamedGraph(connection.getValueFactory().createURI(uriStr));
                }
            }
            query.setDataset(dataset);
        }
    }

    /**
     *
     * @param uriString
     * @param valueFactory
     * @return
     */
    public static boolean isValidURI(String uriString, ValueFactory valueFactory) {

        if (StringUtils.isBlank(uriString)) {
            return false;
        }

        try {
            valueFactory.createURI(uriString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Creates and returns an implementation of {@link Literal} from the given object, using the given {@link ValueFactory}. The
     * implementation (and hence also the datatype) of the literal is chosen based on the class of the given object. A
     * java.lang.Integer object will return a literal with integer datatype, a java.util.Date object will return a literal with
     * dateTime datatype, etc.
     *
     * Note that when the given object is a String, its language cannot be detected from the method inputs, so you have to use
     * {@link ValueFactory#createLiteral(String, String)} explicitly.
     *
     * Also note that a stirng object is trimmed before creating a literal from it.
     *
     * If the given object is null, the returned Literal is also null. If the given object's datatype could not be detected for some
     * reason, a literal of object.toString() is returned.
     *
     * @param object
     * @param valueFactory
     * @return
     */
    public static Literal createLiteral(Object object, ValueFactory valueFactory) {

        if (object == null) {
            return null;
        }

        Literal literal = null;
        if (object instanceof Date) {
            GregorianCalendar gregCalendar = new GregorianCalendar();
            gregCalendar.setTime((Date) object);
            try {
                XMLGregorianCalendar xmlGregCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregCalendar);
                literal = valueFactory.createLiteral(xmlGregCalendar);
            } catch (DatatypeConfigurationException e) {
                throw new CRRuntimeException("Failed to instantiate XML datatype factory implementation", e);
            }
        } else if (object instanceof Boolean) {
            literal = valueFactory.createLiteral(((Boolean) object).booleanValue());
        } else if (object instanceof Integer) {
            literal = valueFactory.createLiteral(((Integer) object).intValue());
        } else if (object instanceof Long) {
            literal = valueFactory.createLiteral(((Long) object).longValue());
        } else if (object instanceof Byte) {
            literal = valueFactory.createLiteral(((Byte) object).byteValue());
        } else if (object instanceof Double) {
            literal = valueFactory.createLiteral(((Double) object).doubleValue());
        } else if (object instanceof Float) {
            literal = valueFactory.createLiteral(((Float) object).floatValue());
        } else if (object instanceof Short) {
            literal = valueFactory.createLiteral(((Short) object).shortValue());
        } else {
            literal = valueFactory.createLiteral(object.toString().trim());
        }

        return literal;
    }

    /**
     * For input values of "name" and 5 returns "?name1, ?name2, ?name3, ?name4, ?name5".
     * For input values of "title" and 6 returns "?title1, ?title2, ?title3, ?title4, ?title5, ?title6".
     * And so forth.
     *
     * @param varName
     * @param times
     * @return
     */
    public static String createSPARQLVariablesCSV(String varName, int times){

        StringBuilder sb = new StringBuilder();
        for (int i=1; i <= times; i++){
            if (i > 1){
                sb.append(", ");
            }
            sb.append("?").append(varName).append(i);
        }

        return sb.toString();
    }
}
