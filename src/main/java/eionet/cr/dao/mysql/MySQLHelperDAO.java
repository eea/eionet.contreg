package eionet.cr.dao.mysql;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.readers.DataflowPicklistReader;
import eionet.cr.dao.readers.PredicateLabelsReader;
import eionet.cr.dao.readers.RDFExporter;
import eionet.cr.dao.readers.SubjectDataReader;
import eionet.cr.dao.util.PredicateLabels;
import eionet.cr.dao.util.SubProperties;
import eionet.cr.dao.util.UriLabelPair;
import eionet.cr.dto.DownloadFileDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.PredicateDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.dto.TripleDTO;
import eionet.cr.dto.UploadDTO;
import eionet.cr.dto.UserBookmarkDTO;
import eionet.cr.dto.UserHistoryDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.ObjectLabelPair;
import eionet.cr.util.Pair;
import eionet.cr.util.URIUtil;
import eionet.cr.util.Util;
import eionet.cr.util.YesNoBoolean;
import eionet.cr.util.pagination.PagingRequest;
import eionet.cr.util.sql.PairReader;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.util.sql.SingleObjectReader;
import eionet.cr.web.security.CRUser;


/**
 *  Mysql implementation of {@link HelperDAO}.
 *
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class MySQLHelperDAO extends MySQLBaseDAO implements HelperDAO {

    /**
     *
     */
    MySQLHelperDAO() {
        //reducing visibility
    }

    /**
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getLatestFiles(int)
     */
    public List<Pair<String, String>> getLatestFiles(int limit) throws DAOException {

        /* Get the hashes and URIs of recent subjects of type=cr:file
         * (we need URIs, because we might need to derive labels from them).
         */

        String sql = "SELECT DISTINCT RESOURCE.URI_HASH, RESOURCE.URI FROM RESOURCE INNER JOIN SPO ON RESOURCE.URI_HASH = SPO.SUBJECT"
                + " WHERE SPO.PREDICATE= ? AND OBJECT_HASH= ? ORDER BY FIRSTSEEN_TIME DESC LIMIT ?;";
        List<Long> params = new LinkedList<Long>();
        params.add(Hashes.spoHash(Predicates.RDF_TYPE));
        params.add( Hashes.spoHash(Subjects.CR_FILE));
        params.add(new Long(limit));

        Map<String, String> labelMap = new LinkedHashMap<String, String>();
        Map<String, String> uriMap = new LinkedHashMap<String, String>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, Hashes.spoHash(Predicates.RDF_TYPE));
            pstmt.setLong(2, Hashes.spoHash(Subjects.CR_FILE));
            pstmt.setLong(3, limit);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                uriMap.put(rs.getString(1), rs.getString(2));
                labelMap.put(rs.getString(1), "");
            }

            /* if any subjects were found, let's find their labels */

            if (!labelMap.isEmpty()) {

                sql = "SELECT DISTINCT SPO.SUBJECT as id, SPO.OBJECT as value FROM SPO WHERE SPO.PREDICATE=? "
                        + "AND SPO.SUBJECT IN (" + Util.toCSV(labelMap.keySet()) + ")";
                pstmt = conn.prepareStatement(sql);
                pstmt.setLong(1, Hashes.spoHash(Predicates.RDFS_LABEL));
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    labelMap.put(rs.getString(1), rs.getString(2));
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }

        /* Loop through labels and if a label was not found for a particular subject,
         * then derive the label from the subject's URI.
         */
        ArrayList<Pair<String, String>> result = new ArrayList<Pair<String, String>>();
        for (String uriHash : labelMap.keySet()) {
            if (StringUtils.isBlank(labelMap.get(uriHash))) {
                String uri = uriMap.get(uriHash);
                result.add(new Pair<String, String>(uriHash, URIUtil.extractURILabel(uri, uri)));
            } else {
                result.add(new Pair<String, String>(uriHash, labelMap.get(uriHash)));
            }
        }

        return result;
    }

    /** */
    private static final String sqlPicklist = "select distinct OBJECT from SPO where PREDICATE=? and LIT_OBJ='Y' order by OBJECT asc";
    /**
     * @see eionet.cr.dao.HelperDAO#getPicklistForPredicate(java.lang.String)
     * {@inheritDoc}
     */
    public Collection<ObjectLabelPair> getPicklistForPredicate(String predicateUri, boolean extractLabels) throws DAOException {
        return null;
//        if (StringUtils.isBlank(predicateUri)) {
//            return Collections.emptyList();
//        }
//
//        List<String> resultList = executeQuery(
//                sqlPicklist,
//                Collections.singletonList((Object)Hashes.spoHash(predicateUri)),
//                new SingleObjectReader<String>());
//        return resultList;
    }

    /** */
    private static final String tripleInsertSQL = "insert into SPO (SUBJECT, PREDICATE, OBJECT, OBJECT_HASH, OBJECT_DOUBLE,"
            + " ANON_SUBJ, ANON_OBJ, LIT_OBJ, OBJ_LANG, OBJ_DERIV_SOURCE, OBJ_DERIV_SOURCE_GEN_TIME, OBJ_SOURCE_OBJECT, SOURCE,"
            + " GEN_TIME) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoHelperDao#addTriples(eionet.cr.dto.SubjectDTO)
     */
    public void addTriples(SubjectDTO subjectDTO) throws DAOException {

        if (subjectDTO == null || subjectDTO.getPredicateCount() == 0)
            return;

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(tripleInsertSQL);

            boolean doExecuteBatch = false;
            long subjectHash = subjectDTO.getUriHash();
            for (String predicateUri : subjectDTO.getPredicateUris()) {

                Collection<ObjectDTO> objects = subjectDTO.getObjects(predicateUri);
                if (objects != null && !objects.isEmpty()) {

                    long predicateHash = Hashes.spoHash(predicateUri);
                    for (ObjectDTO object : objects) {

                        pstmt.setLong(1, subjectHash);
                        pstmt.setLong(2, predicateHash);
                        pstmt.setString(3, object.getValue());
                        pstmt.setLong(4, object.getHash());
                        pstmt.setObject(5, Util.toDouble(object.getValue()));
                        pstmt.setString(6, YesNoBoolean.format(subjectDTO.isAnonymous()));
                        pstmt.setString(7, YesNoBoolean.format(object.isAnonymous()));
                        pstmt.setString(8, YesNoBoolean.format(object.isLiteral()));
                        pstmt.setString(9, StringUtils.trimToEmpty(object.getLanguage()));
                        pstmt.setLong(10, object.getDerivSourceUri() == null ? 0 : Hashes.spoHash(object.getDerivSourceUri()));
                        pstmt.setLong(11, object.getDerivSourceGenTime());
                        pstmt.setLong(12, object.getSourceObjectHash());
                        pstmt.setLong(13, Hashes.spoHash(object.getSourceUri()));
                        pstmt.setLong(14, System.currentTimeMillis());

                        pstmt.addBatch();
                        if (doExecuteBatch == false) {
                            doExecuteBatch = true;
                        }
                    }
                }
            }

            if (doExecuteBatch == true) {

                // insert triples
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(pstmt);
            SQLUtil.close(conn);
        }
    }

    /** */
    public static final String INSERT_RESOURCE_SQL = "insert ignore into RESOURCE"
            + " (URI, URI_HASH, FIRSTSEEN_SOURCE, FIRSTSEEN_TIME) values (?, ?, ?, ?)";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.SpoHelperDao#addResource(java.lang.String, java.lang.String)
     */
    public void addResource(String uri, String sourceUri) throws DAOException {

        ArrayList values = new ArrayList();
        values.add(uri);
        values.add(Long.valueOf(Hashes.spoHash(uri)));
        if (StringUtils.isBlank(sourceUri)) {
            values.add(Long.valueOf(0));
            values.add(Long.valueOf(0));
        } else {
            values.add(Long.valueOf(Hashes.spoHash(sourceUri)));
            values.add(Long.valueOf(System.currentTimeMillis()));
        }

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(INSERT_RESOURCE_SQL, values, conn);
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String GET_DC_PROPERTIES_SQL = "select distinct SUBJECT from SPO"
            + " where SOURCE=" + Hashes.spoHash(Subjects.DUBLIN_CORE_SOURCE_URL)
            + " and PREDICATE=" + Hashes.spoHash(Predicates.RDF_TYPE)
            + " and OBJECT_HASH=" + Hashes.spoHash(Subjects.RDF_PROPERTY);
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getAddibleProperties(java.util.Collection)
     */
    public HashMap<String, String> getAddibleProperties(Collection<String> subjectTypes) throws DAOException {

        HashMap<String, String> result = new HashMap<String, String>();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            /* get the DublinCore properties */

            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(GET_DC_PROPERTIES_SQL);
            List<String> dcPropertiesHashes = new ArrayList<String>();
            while (rs.next()) {
                dcPropertiesHashes.add(rs.getString(1));
            }
            SQLUtil.close(rs);

            if (!dcPropertiesHashes.isEmpty()) {
                result.putAll(getSubjectLabels(dcPropertiesHashes, conn));
            }

            /* get the properties for given subject types */

            if (subjectTypes != null && !subjectTypes.isEmpty()) {

                StringBuilder buf = new StringBuilder("select distinct SUBJECT from SPO where PREDICATE=").
                append(Hashes.spoHash(Predicates.RDFS_DOMAIN)).append(" and OBJECT_HASH in (").
                append(Util.toCSV(subjectTypes)).append(")");

                rs = stmt.executeQuery(buf.toString());
                List<String> otherPropertiesHashes = new ArrayList<String>();
                while (rs.next()) {
                    otherPropertiesHashes.add(rs.getString(1));
                }

                if (!otherPropertiesHashes.isEmpty()) {
                    result.putAll(getSubjectLabels(otherPropertiesHashes, conn));
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return result;
    }

    /**
     *
     * @param subjectHashes
     * @param conn
     * @return
     * @throws SQLException
     */
    private HashMap<String, String> getSubjectLabels(Collection<String> subjectHashes, Connection conn) throws SQLException {

        HashMap<String, String> result = new HashMap<String, String>();
        boolean closeConnection = false;

        Statement stmt = null;
        ResultSet rs = null;
        try {
            if (conn == null) {
                conn = getConnection();
                closeConnection = true;
            }
            stmt = conn.createStatement();

            StringBuilder buf =
                new StringBuilder("select distinct RESOURCE.URI as SUBJECT_URI, OBJECT as SUBJECT_LABEL").
                append(" from SPO inner join RESOURCE on SPO.SUBJECT=RESOURCE.URI_HASH where SUBJECT in (").
                append(Util.toCSV(subjectHashes)).append(") and PREDICATE=").
                append(Hashes.spoHash(Predicates.RDFS_LABEL)).append(" and LIT_OBJ='Y'");

            rs = stmt.executeQuery(buf.toString());
            while (rs.next()) {
                result.put(rs.getString("SUBJECT_URI"), rs.getString("SUBJECT_LABEL"));
            }
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            if (closeConnection) {
                SQLUtil.close(conn);
            }
        }

        return result;
    }

    /** */
    private static final String getSubjectSchemaUriSQL =
        "select OBJECT from SPO where SUBJECT=? and PREDICATE="
        + Hashes.spoHash(Predicates.CR_SCHEMA) + " limit 1";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getSubjectSchemaUri(java.lang.String)
     */
    public String getSubjectSchemaUri(String subjectUri) throws DAOException {

        if (StringUtils.isBlank(subjectUri))
            return null;

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(getSubjectSchemaUriSQL);
            stmt.setLong(1, Hashes.spoHash(subjectUri));
            rs = stmt.executeQuery();
            return rs.next() ? rs.getString(1) : null;
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String getPredicatesUsedForType_SQL = "select distinct SPOPRED.PREDICATE from SPO as SPOTYPE"
            + ", SPO as SPOPRED where SPOTYPE.PREDICATE=" + Hashes.spoHash(Predicates.RDF_TYPE) + " and SPOTYPE.OBJECT_HASH=?"
            + " and SPOTYPE.SUBJECT=SPOPRED.SUBJECT";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getPredicatesUsedForType(java.lang.String)
     */
    public List<SubjectDTO> getPredicatesUsedForType(String typeUri) throws DAOException {

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Long.valueOf(Hashes.spoHash(typeUri)));

        List<Long> predicateUris = executeQuery(getPredicatesUsedForType_SQL, values, new SingleObjectReader<Long>());
        if (predicateUris == null || predicateUris.isEmpty()) {
            return new ArrayList<SubjectDTO>();
        } else {
            // get the SubjectDTO objects of the found predicates
            Map<Long, SubjectDTO> subjectsMap = new HashMap<Long, SubjectDTO>();
            for (Long hash : predicateUris) {
                subjectsMap.put(hash, null);
            }
            executeQuery(getSubjectsDataQuery(subjectsMap.keySet()), null, new SubjectDataReader(subjectsMap));

            // since a used predicate may not appear as a subject in SPO, there might unfound SubjectDTO objects
            HashSet<Long> unfoundSubjects = new HashSet<Long>();
            for (Entry<Long, SubjectDTO> entry : subjectsMap.entrySet()) {
                if (entry.getValue() == null) {
                    unfoundSubjects.add(entry.getKey());
                }
            }

            // if there were indeed any unfound SubjectDTO objects, find URIs for those predicates
            // and create dummy SubjectDTO objects from those URIs
            if (!unfoundSubjects.isEmpty()) {
                Map<Long, String> resourceUris = getResourceUris(unfoundSubjects);
                for (Entry<Long, SubjectDTO> entry : subjectsMap.entrySet()) {
                    if (entry.getValue() == null) {
                        String uri = resourceUris.get(entry.getKey());
                        if (!StringUtils.isBlank(uri)) {
                            unfoundSubjects.remove(entry.getKey());
                            entry.setValue(new SubjectDTO(uri, false));
                        }
                    }
                }
            }

            // clean the subjectsMap of unfound subjects
            for (Long hash : unfoundSubjects) {
                subjectsMap.remove(hash);
            }

            return new LinkedList<SubjectDTO>( subjectsMap.values());
        }
    }

    /**
     *
     * @param resourceHashes
     * @return
     * @throws DAOException
     */
    private Map<Long, String> getResourceUris(HashSet<Long> resourceHashes) throws DAOException {

        StringBuffer buf = new StringBuffer().
        append("select URI_HASH, URI from RESOURCE where URI_HASH in (").append(Util.toCSV(resourceHashes)).append(")");

        HashMap<Long, String> result = new HashMap<Long, String>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(buf.toString());
            while (rs.next()) {
                String uri = rs.getString("URI");
                if (!StringUtils.isBlank(uri)) {
                    result.put(Long.valueOf(rs.getLong("URI_HASH")), uri);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(stmt);
            SQLUtil.close(conn);
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#isAllowLiteralSearch(java.lang.String)
     */
    public boolean isAllowLiteralSearch(String predicateUri) throws DAOException {

        //sanity checks
        if (StringUtils.isBlank(predicateUri)) {
            return false;
        }
        String allowLiteralSearchQuery = "select distinct OBJECT from SPO where SUBJECT=? and PREDICATE=? and LIT_OBJ='N' and ANON_OBJ='N'";

        ArrayList<Object> values = new ArrayList<Object>();
        values.add(Long.valueOf(Hashes.spoHash(predicateUri)));
        values.add(Long.valueOf((Hashes.spoHash(Predicates.RDFS_RANGE))));

        List<String> resultList = executeQuery(allowLiteralSearchQuery, values, new SingleObjectReader<String>());
        if (resultList == null || resultList.isEmpty()) {
            return true; // if not rdfs:domain specified at all, then lets allow literal search
        }

        for (String result : resultList) {
            if (Subjects.RDFS_LITERAL.equals(result)) {
                return true; // rdfs:Literal is present in the specified rdfs:domain
            }
        }

        return false;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getSpatialSources()
     */
    public List<String> getSpatialSources() throws DAOException {

        String sql = "select distinct URI from SPO,RESOURCE where "
                + "PREDICATE= ? and OBJECT_HASH= ? and SOURCE=RESOURCE.URI_HASH";

        List<Long> params = new LinkedList<Long>();
        params.add(Hashes.spoHash(Predicates.RDF_TYPE));
        params.add(Hashes.spoHash(Subjects.WGS_POINT));

        return executeQuery(sql, params, new SingleObjectReader<String>());
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getSubject(java.lang.String)
     */
    public SubjectDTO getSubject(Long subjectHash) throws DAOException {

        if (subjectHash == null) {
            return null;
        }

        Map<Long, SubjectDTO> map = new LinkedHashMap<Long, SubjectDTO>();
        map.put(subjectHash, null);

        List<SubjectDTO> subjects = executeQuery(getSubjectsDataQuery(map.keySet()),
                null, new SubjectDataReader(map));

        return subjects == null || subjects.isEmpty() ? null : subjects.get(0);
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getPredicateLabels(java.util.Set)
     */
    public PredicateLabels getPredicateLabels(Set<Long> subjectHashes) throws DAOException {

        PredicateLabels predLabels = new PredicateLabels();
        if (subjectHashes != null && !subjectHashes.isEmpty()) {

            StringBuffer sqlBuf = new StringBuffer().
            append("select RESOURCE.URI as PREDICATE_URI, SPO.OBJECT as LABEL, SPO.OBJ_LANG as LANG").
            append(" from SPO, RESOURCE").
            append(" where SPO.SUBJECT in (").append(Util.toCSV(subjectHashes)).append(")").
            append(" and SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_LABEL)).
            append(" and SPO.LIT_OBJ='Y'").
            append(" and SPO.SUBJECT=RESOURCE.URI_HASH");

            executeQuery(sqlBuf.toString(), new PredicateLabelsReader(predLabels));
        }

        return predLabels;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getSubProperties(java.util.Set)
     */
    public SubProperties getSubProperties(Collection<String> subjectHashes) throws DAOException {
        throw new DAOException("Method not implemented");
        /*
        SubProperties subProperties = new SubProperties();
        if (subjectHashes != null && !subjectHashes.isEmpty()) {

            StringBuffer sqlBuf = new StringBuffer().
            append("select distinct SPO.OBJECT as PREDICATE, RESOURCE.URI as SUB_PROPERTY").
            append(" from SPO, RESOURCE").
            append(" where SPO.OBJECT_HASH in (").append(Util.toCSV(subjectHashes)).append(")").
            append(" and SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDFS_SUBPROPERTY_OF)).
            append(" and SPO.LIT_OBJ='N' and SPO.ANON_OBJ='N'").
            append(" and SPO.SUBJECT=RESOURCE.URI_HASH");

            executeQuery(sqlBuf.toString(), new SubPropertiesReader(subProperties));
        }

        return subProperties;
        */
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getLatestSubjects(java.lang.String, int)
     */
    public Collection<SubjectDTO> getLatestSubjects(String rdfType, int limit) throws DAOException {

        // validate arguments
        if (StringUtils.isBlank(rdfType))
            throw new IllegalArgumentException("rdfType must not be blank!");
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0!");

        // build SQL query
        StringBuffer sqlBuf = new StringBuffer().
        append("select RESOURCE.URI_HASH as ").append(PairReader.LEFTCOL).
        append(", RESOURCE.FIRSTSEEN_TIME as ").append(PairReader.RIGHTCOL).
        append(" from RESOURCE where URI_HASH in (select SUBJECT from SPO where ").
        append(" SPO.PREDICATE=").append(Hashes.spoHash(Predicates.RDF_TYPE)).
        append(" and SPO.OBJECT_HASH=").append(Hashes.spoHash(rdfType)).
        append(" ) order by RESOURCE.FIRSTSEEN_TIME desc limit ").append(Math.max(1, limit));

        // execute SQL query
        PairReader<Long, Long> pairReader = new PairReader<Long, Long>();
        executeQuery(sqlBuf.toString(), pairReader);
        List<Pair<Long, Long>> resultList = pairReader.getResultList();

        executeQuery(sqlBuf.toString(), pairReader);
        List<Pair<Long, Long>> pairList = pairReader.getResultList();

        Collection<SubjectDTO> result = new LinkedList<SubjectDTO>();

        // if result list not empty, get the subjects data and set their first-seen times
        if (pairList != null && !pairList.isEmpty()) {

            // create helper objects
            Map<Long, SubjectDTO> subjectsMap = new LinkedHashMap<Long, SubjectDTO>();
            Map<Long, Date> firstSeenTimes = new HashMap<Long, Date>();
            for (Pair<Long, Long> p : resultList) {
                subjectsMap.put(p.getLeft(), null);
                firstSeenTimes.put(p.getLeft(), new Date(p.getRight()));
            }

            // get subjects data
            executeQuery(getSubjectsDataQuery(subjectsMap.keySet()),
                    null, new SubjectDataReader(subjectsMap));

            // set firstseen-times of found subjects
            for (SubjectDTO subject : subjectsMap.values()) {
                subject.setDcDate(
                        firstSeenTimes.get(new Long(subject.getUriHash())));
            }

            result = subjectsMap.values();
        }

        return result;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getSubjectsNewerThan(java.util.Date, int)
     */
    public List<SubjectDTO> getSubjectsNewerThan(Date timestamp, int limit) throws DAOException {

        // validate arguments
        if (timestamp == null || timestamp.after(new Date()))
            throw new IllegalArgumentException("timestamp must not be null or after current time!");
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0!");

        // build SQL query
        StringBuffer sqlBuf = new StringBuffer().
        append("select SPO.SUBJECT as SUBJECT_HASH from SPO, RESOURCE").
        append(" where SPO.PREDICATE=? and SPO.OBJECT_HASH=? and SPO.SUBJECT=RESOURCE.URI_HASH ").
        append(" and RESOURCE.FIRSTSEEN_TIME>?").
        append(" order by RESOURCE.FIRSTSEEN_TIME desc").
        append(" limit ").append(limit);

        ArrayList<Object> inParameters = new ArrayList<Object>();
        inParameters.add(Hashes.spoHash(Predicates.RDF_TYPE));
        inParameters.add(Hashes.spoHash(Subjects.CR_FILE));
        inParameters.add(Long.valueOf(timestamp.getTime()));

        // execute SQL query
        SingleObjectReader<Long> reader = new SingleObjectReader<Long>();
        executeQuery(sqlBuf.toString(), inParameters, reader);
        List<Long> resultList = reader.getResultList();

        // if result list null or empty, return
        if (resultList == null || resultList.isEmpty()) {
            return new LinkedList<SubjectDTO>();
        }

        // create helper objects
        Map<Long, SubjectDTO> subjectsMap = new HashMap<Long, SubjectDTO>();
        for (Long subjectHash : resultList) {
            subjectsMap.put(subjectHash, null);
        }

        // get subjects data
        List<SubjectDTO> result = executeQuery(getSubjectsDataQuery(subjectsMap.keySet()),
                null, new SubjectDataReader(subjectsMap));
        return result;
    }

    /** */
    private static final String dataflowPicklistSQL = new StringBuffer().
        append("select distinct ").
            append("INSTRUMENT_TITLE.OBJECT as INSTRUMENT_TITLE, ").
            append("OBLIGATION_TITLE.OBJECT as OBLIGATION_TITLE, ").
            append("OBLIGATION_URI.URI as OBLIGATION_URI ").
        append("from ").
            append("SPO as OBLIGATION_TITLE ").
            append("left join RESOURCE as OBLIGATION_URI on OBLIGATION_TITLE.SUBJECT=OBLIGATION_URI.URI_HASH ").
            append("left join SPO as OBLIGATION_INSTR on OBLIGATION_TITLE.SUBJECT=OBLIGATION_INSTR.SUBJECT ").
            append("left join SPO as INSTRUMENT_TITLE on OBLIGATION_INSTR.OBJECT_HASH=INSTRUMENT_TITLE.SUBJECT ").
        append("where ").
            append("OBLIGATION_TITLE.PREDICATE=").append(Hashes.spoHash(Predicates.DC_TITLE)).
            append(" and OBLIGATION_TITLE.LIT_OBJ='Y' and OBLIGATION_INSTR.PREDICATE=").
            append(Hashes.spoHash(Predicates.ROD_INSTRUMENT_PROPERTY)).
            append(" and OBLIGATION_INSTR.LIT_OBJ='N' and OBLIGATION_INSTR.ANON_OBJ='N'").
            append(" and INSTRUMENT_TITLE.PREDICATE=").append(Hashes.spoHash(Predicates.DCTERMS_ALTERNATIVE)).
            append(" and INSTRUMENT_TITLE.LIT_OBJ='Y' ").
        append("order by ").
            append("INSTRUMENT_TITLE.OBJECT, OBLIGATION_TITLE.OBJECT ").toString();

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getDataflowSearchPicklist()
     */
    public HashMap<String, ArrayList<UriLabelPair>> getDataflowSearchPicklist()
                                                                        throws DAOException {

        DataflowPicklistReader reader = new DataflowPicklistReader();
        executeQuery(dataflowPicklistSQL, reader);
        return reader.getResultMap();
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getDistinctOrderedTypes()
     */
    public ArrayList<Pair<String, String>> getDistinctOrderedTypes() throws DAOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getSubjectCountInSource(long)
     */
    public int getSubjectCountInSource(long sourceHash) throws DAOException {

        Connection conn = null;
        try {
            conn = getConnection();
            Object o = SQLUtil.executeSingleReturnValueQuery(
                    "select count(distinct SUBJECT) from SPO where SOURCE=" + sourceHash, conn);
            return (o == null || StringUtils.isBlank(o.toString()))
                    ? 0 : Integer.parseInt(o.toString());
        } catch (SQLException e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }




    public void updateTypeDataCache() throws DAOException {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void registerUserUrl(CRUser user, String url, boolean isBookmark) throws DAOException {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void addUserBookmark(CRUser user, String url) throws DAOException {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteUserBookmark(CRUser user, String url) throws DAOException {

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }
    @Override
    public List<UserBookmarkDTO> getUserBookmarks(CRUser user) throws DAOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public boolean isSubjectUserBookmark(CRUser user, String subject)  throws DAOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void updateUserHistory(CRUser user, String url) throws DAOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<UserHistoryDTO> getUserHistory(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<TripleDTO> getSampleTriplesInSource(String sourceUrl,
            PagingRequest pagingRequest) throws DAOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public int getLastReviewId(CRUser user) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public int generateNewReviewId(CRUser user) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public int addReview(ReviewDTO review, CRUser user) {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<ReviewDTO> getReviewList(CRUser user)  throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public ReviewDTO getReview(CRUser user, int reviewId)  throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteReview(CRUser user, int reviewId, boolean deleteAttachments)  throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }



    @Override
    public void deleteTriples(Collection<TripleDTO> triples) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteTriplesOfSource(long sourceHash) throws DAOException {

        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public List<String> getReviewAttachmentList(CRUser user, int reviewId)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public void deleteAttachment(CRUser user, int reviewId, String attachmentUri)
            throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#loadAttachment(java.lang.String)
     */
    public DownloadFileDTO loadAttachment(String attachmentUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#getUserUploads(eionet.cr.web.security.CRUser)
     */
    public Collection<UploadDTO> getUserUploads(CRUser crUser) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#isExistingSubject(long)
     */
    public boolean isExistingSubject(String subjectUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#deleteSubjects(java.util.List)
     */
    public void deleteSubjects(List<String> subjectUris) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#renameSubjects(java.util.Map)
     */
    public void renameSubjects(Map<Long, String> newUrisByOldHashes) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#readDistinctPredicates(Long)
     */
    public List<PredicateDTO> readDistinctPredicates(Long sourceHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#readDistinctSubjectUrls(java.lang.Long)
     */
    public List<String> readDistinctSubjectUrls(Long sourceHash) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#outputSourceTriples(eionet.cr.dao.readers.RDFExporter)
     */
    public void outputSourceTriples(RDFExporter reader) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#deleteTriples(java.lang.String, java.lang.String, java.lang.String)
     */
    public void deleteTriples(String subjectUri, String predicateUri, String sourceUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public SubjectDTO getSubject(String subjectUri) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public long getTriplesCount() throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    @Override
    public LinkedHashMap<URI, String> getSparqlBookmarks(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    /* (non-Javadoc)
     * @see eionet.cr.dao.HelperDAO#registerUserFolderInCrHomeContext(eionet.cr.web.security.CRUser)
     */
    @Override
    public void registerUserFolderInCrHomeContext(CRUser user) throws DAOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
}


