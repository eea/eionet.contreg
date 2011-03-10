package eionet.cr.dao.postgre;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.ExporterDAO;
import eionet.cr.dao.postgre.helpers.PostgreFilteredTypeSearchHelper;
import eionet.cr.util.Hashes;
import eionet.cr.util.Util;
import eionet.cr.util.sql.ResultSetExportReader;
import eionet.cr.util.sql.SQLUtil;

public class PostgreSQLExporterDAO extends PostgreSQLBaseDAO implements ExporterDAO {

    /**
     *
     */
    public void exportByTypeAndFilters(
            Map<String, String> filters,
            List<String> selectedPredicates,
            ResultSetExportReader reader) throws DAOException {

        // create query helper
        PostgreFilteredTypeSearchHelper helper = new PostgreFilteredTypeSearchHelper(filters, null,
                null, null);

        // create the list of IN parameters of the query
        ArrayList<Object> inParams = new ArrayList<Object>();

        long startTime = System.currentTimeMillis();

        //limit predicates
        List<Long> predicateHashes = new ArrayList<Long>();
        if(selectedPredicates!=null && !selectedPredicates.isEmpty()){
            for(String predicate : selectedPredicates){
                if(!predicateHashes.contains(predicate)){
                    predicateHashes.add(Hashes.spoHash(predicate));
                }
            }
        }
        String predicateHashesCommaSeparated = Util.toCSV(predicateHashes);

        getSubjectsDataAndWriteItIntoExportOutput(reader, helper.getUnorderedQueryWithoutDistinct(inParams),
                predicateHashesCommaSeparated);

        logger.debug("Export by type and filters, total time " + Util.durationSince(startTime));

    }

    protected void getSubjectsDataAndWriteItIntoExportOutput(ResultSetExportReader reader, String subjectsSubQuery,
            String predicateHashes) throws DAOException{

        if (subjectsSubQuery==null || subjectsSubQuery.length()==0)
            throw new IllegalArgumentException("Subjects sub query must not be null or empty");

        Connection conn = null;
        try{
            if (conn==null){
                conn = getSQLConnection();
            }

            long startTime = System.currentTimeMillis();

            //create temporary table for subjects hashes
            String createTmpTableQuery = getCreateTempSubjectsTablesQuery(subjectsSubQuery, predicateHashes);
            String dropTmpTableQuery = getDropTempTableQuery();
            logger.trace("create temporary table query:" + createTmpTableQuery);

            SQLUtil.executeUpdate(createTmpTableQuery, conn);
            //conn.commit();
            logger.debug("createTmpTableQuery, total query time " + Util.durationSince(startTime));

            String query = getSubjectsDataFromTmpTablesQuery();
            logger.trace("Goint to execute subjects data query:" + query);

            //read subjects and resources from the temp tables
            long startTime2 = System.currentTimeMillis();
            SQLUtil.executeQuery(query, null, reader, conn);
            logger.debug("getSubjectsData, total query time " + Util.durationSince(startTime2));

            //drop temporary tables
            SQLUtil.executeUpdate(dropTmpTableQuery, conn);
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
    }

    /**
     * Creates temporary tables for selected subjects and corresponding resources
     *
     * @param subjectsSubQuery
     * @param predicateHashesCommaSeparated
     * @return
     */
    protected String getCreateTempSubjectsTablesQuery(String subjectsSubQuery, String predicateHashesCommaSeparated) {

        StringBuffer buf = new StringBuffer().
        append("drop table if exists TMP_SUBJECTS; drop table if exists TMP_RESOURCES; ").
        append("create temp table TMP_SUBJECTS ").
        append("as select SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, ANON_OBJ, ANON_SUBJ, LIT_OBJ, OBJ_LANG from SPO where ").
        append("SUBJECT in (").append(subjectsSubQuery).append(") ").
        append( predicateHashesCommaSeparated!=null && predicateHashesCommaSeparated.length()>0 ?
                "AND PREDICATE IN (".concat(predicateHashesCommaSeparated).concat(") ") : "").
        append(";").
        append("create temp table TMP_RESOURCES as ").
        append("select URI, URI_HASH from RESOURCE where URI_HASH in (select SUBJECT from TMP_SUBJECTS) ").
        append("union select URI, URI_HASH from RESOURCE where URI_HASH in (select PREDICATE from TMP_SUBJECTS) ");

        return buf.toString();
    }
    /**
     * Query creates the actual query joining temporary subjects and resources tables instead of joining SPO and RESOURCE
     * @return
     */
    protected String getSubjectsDataFromTmpTablesQuery() {

        StringBuffer buf = new StringBuffer().
        append("select distinct ").
        append("SUBJECT as SUBJECT_HASH, SUBJ_RESOURCE.URI as SUBJECT_URI, ").
        append("PREDICATE as PREDICATE_HASH, PRED_RESOURCE.URI as PREDICATE_URI, ").
        append("OBJECT, OBJECT_HASH, ANON_OBJ, ANON_SUBJ, LIT_OBJ, OBJ_LANG ").
        append("from TMP_SUBJECTS as SPO ").
        append("left join TMP_RESOURCES as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
        append("left join TMP_RESOURCES as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
        append("order by ").
        append("SUBJECT, PREDICATE, OBJECT");
        return buf.toString();
    }
    /**
     * Query drops temprary tables
     * @return
     */
    protected String getDropTempTableQuery(){
        return "drop table if exists TMP_SUBJECTS; drop table if exists TMP_RESOURCES";
    }
}
