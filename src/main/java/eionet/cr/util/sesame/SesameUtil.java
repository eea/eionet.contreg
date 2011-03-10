package eionet.cr.util.sesame;

import org.openrdf.OpenRDFException;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import eionet.cr.dao.readers.ResultSetReaderException;

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
    public static RepositoryConnection getConnection() throws RepositoryException{

        return SesameConnectionProvider.getConnection();
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
    public static <T> void executeQuery(String sparql, SPARQLResultSetReader<T> reader, RepositoryConnection conn) throws OpenRDFException, ResultSetReaderException{

        TupleQueryResult queryResult = null;
        try{
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparql);
            queryResult = tupleQuery.evaluate();
            if (queryResult!=null){

                boolean isFirstRow = true;
                while (queryResult.hasNext()){

                    if (isFirstRow){
                        reader.startResultSet(queryResult.getBindingNames());
                        isFirstRow = false;
                    }
                    reader.readRow(queryResult.next());
                }
                reader.endResultSet();
            }
        }
        finally{
            SesameUtil.close(queryResult);
        }
    }

    /**
     *
     * @param queryResult
     */
    public static void close(TupleQueryResult queryResult){

        if (queryResult!=null){
            try{
                queryResult.close();
            }
            catch (QueryEvaluationException e){}
        }
    }

    /**
     *
     * @param queryResult
     */
    public static void close(GraphQueryResult queryResult){

        if (queryResult!=null){
            try{
                queryResult.close();
            }
            catch (QueryEvaluationException e){}
        }
    }

    /**
     *
     * @param repo
     */
    public static void shutdown(Repository repo){

        if (repo!=null){
            try{
                repo.shutDown();
            }
            catch (RepositoryException e){}
        }
    }

    /**
     *
     * @param conn
     */
    public static void close(RepositoryConnection conn){

        if (conn!=null){
            try{
                conn.close();
            }
            catch (RepositoryException e){}
        }
    }

}
