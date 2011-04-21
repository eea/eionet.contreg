/*
* The contents of this file are subject to the Mozilla Public
*
* License Version 1.1 (the "License"); you may not use this file
* except in compliance with the License. You may obtain a copy of
* the License at http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS
* IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
* implied. See the License for the specific language governing
* rights and limitations under the License.
*
* The Original Code is Content Registry 2.0.
*
* The Initial Owner of the Original Code is European Environment
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
*
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.postgre;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.SQLBaseDAO;
import eionet.cr.dao.readers.MinimalSubjectReader;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Util;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public abstract class PostgreSQLBaseDAO extends SQLBaseDAO{

    /** */
    protected Logger logger = Logger.getLogger(PostgreSQLBaseDAO.class);

    /**
     *
     * @param subjectHashes
     * @return
     */
    private String getSubjectsDataQuery(Collection<Long> subjectHashes, String predicateHashesCommaSeparated) {
        return getSubjectsDataQuery(Util.toCSV(subjectHashes), predicateHashesCommaSeparated);
    }
    /**
     * Get subjects data SQL query - joins SPO table 4 times with RESOURCE table
     * @param subjectsSubQuery - limits the subjects to be selected
     * @param predicateHashesCommaSeparated - limits the predicate values to be selected
     * @return
     */
    private String getSubjectsDataQuery(String subjectsSubQuery, String predicateHashesCommaSeparated) {

        StringBuffer buf = new StringBuffer().
        append("select distinct ").
        append("SUBJECT as SUBJECT_HASH, SUBJ_RESOURCE.URI as SUBJECT_URI, SUBJ_RESOURCE.LASTMODIFIED_TIME as SUBJECT_MODIFIED, ").
        append("PREDICATE as PREDICATE_HASH, PRED_RESOURCE.URI as PREDICATE_URI, ").
        append("OBJECT, OBJECT_HASH, ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_SOURCE_OBJECT, OBJ_DERIV_SOURCE, SOURCE, ").
        append("SRC_RESOURCE.URI as SOURCE_URI, DSRC_RESOURCE.URI as DERIV_SOURCE_URI ").
        append("from SPO ").
        append("left join RESOURCE as SUBJ_RESOURCE on (SUBJECT=SUBJ_RESOURCE.URI_HASH) ").
        append("left join RESOURCE as PRED_RESOURCE on (PREDICATE=PRED_RESOURCE.URI_HASH) ").
        append("left join RESOURCE as SRC_RESOURCE on (SOURCE=SRC_RESOURCE.URI_HASH) ").
        append("left join RESOURCE as DSRC_RESOURCE on (OBJ_DERIV_SOURCE=DSRC_RESOURCE.URI_HASH) ").
        append("where ").
        append("SUBJECT in (").append(subjectsSubQuery).append(") ").
        append( predicateHashesCommaSeparated != null && predicateHashesCommaSeparated.length()>0 ?
                "AND PREDICATE IN (".concat(predicateHashesCommaSeparated).concat(") ") : "").
        append("order by ").
        append("SUBJECT, PREDICATE, OBJECT");
        return buf.toString();
    }


    /**
     *
     * @param subjectsMap
     * @return
     * @throws DAOException
     */
    protected List<SubjectDTO> getSubjectsData(Map<Long,SubjectDTO> subjectsMap) throws DAOException{
        return getSubjectsData(new SubjectDataReader(subjectsMap));
    }

    /**
     * The method performs the subjects data query based on the list of subjects IDs.
     * The query reads subjects and predicates filter from the given reader.
     *
     * @param reader
     * @return
     * @throws DAOException
     */
    protected List<SubjectDTO> getSubjectsData(SubjectDataReader reader) throws DAOException{

        Map<Long,SubjectDTO> subjectsMap = reader.getSubjectsMap();
        String predicateHashes = reader.getPredicateHashesCommaSeparated();
        if (subjectsMap == null || subjectsMap.isEmpty()) {
            throw new IllegalArgumentException("Subjects collection must not be null or empty");
        }

        // The idea below is that PostgreSQL hangs when the length of the executed query is
        // more than 4096. This might happen if we execute the below query for too many subjects.
        // So instead, we execute the query by every 150 subjects. That should be safe enough.

        int size = subjectsMap.size();
        int max = 150;
        int len = size / max;
        if (size % max > 0) {
            len++;
        }

        Connection conn = null;
        try {
            List<Long> list = new ArrayList<Long>(subjectsMap.keySet());
            long startTime = System.currentTimeMillis();

            for (int i=0; i<len; i++) {

                int from = Math.min(i*max, size);
                int to = Math.min(from + max, size);
                String query = getSubjectsDataQuery(list.subList(from, to), predicateHashes);

                if (conn == null) {
                    conn = getSQLConnection();
                }

                logger.trace("Goint to execute subjects data query:" + query);
                long startTime1 = System.currentTimeMillis();
                SQLUtil.executeQuery(query, null, reader, conn);
                logger.trace("getSubjectsData, sub query time " + Util.durationSince(startTime1));
            }

            // By now the data of matched subjects should be present as SubjectDTO values in
            // subjectsMap (where keys denote subject hashes). If any value in subjectsMap
            // is null (which might happen if they don't have any triples with the predicates that
            // were asked for), then create them "artificially" now
            Set<Long> unfilledSubjects = Util.getNullValueKeys(subjectsMap);
            if (unfilledSubjects != null && !unfilledSubjects.isEmpty()) {

                StringBuffer buf = new StringBuffer().
                append("select distinct SUBJECT,URI,ANON_SUBJ from SPO,RESOURCE where SUBJECT in (").
                append(Util.toCSV(unfilledSubjects)).append(") and SUBJECT=URI_HASH");
                SQLUtil.executeQuery(buf.toString(),null,new MinimalSubjectReader(subjectsMap),conn);
            }

            logger.debug("getSubjectsData, total query time " + Util.durationSince(startTime));
        }
        catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            SQLUtil.close(conn);
        }

        return reader.getResultList();
    }

    /**
     * Queries the subjects using sub query and not the list of subject hashes.
     * The intention is to use cache tables in sub query.
     *
     * @param reader
     * @param subjectsSubQuery
     * @return
     * @throws DAOException
     */
    protected List<SubjectDTO> getSubjectsData(SubjectDataReader reader, String subjectsSubQuery) throws DAOException{

        String predicateHashes = reader.getPredicateHashesCommaSeparated();
        if (subjectsSubQuery == null || subjectsSubQuery.length() == 0)
            throw new IllegalArgumentException("Subjects sub query must not be null or empty");

        long startTime = System.currentTimeMillis();
        String query = getSubjectsDataQuery(subjectsSubQuery, predicateHashes);
        logger.trace("Goint to execute subjects data query:" + query);

        executeSQL(query, reader);
        logger.debug("getSubjectsData, total query time " + Util.durationSince(startTime));
        return reader.getResultList();
    }


}
