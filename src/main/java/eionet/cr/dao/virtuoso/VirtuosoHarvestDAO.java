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
package eionet.cr.dao.virtuoso;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.readers.HarvestDTOReader;
import eionet.cr.dao.readers.HarvestStatReader;
import eionet.cr.dao.readers.HarvestWithMessageTypesReader;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.dto.HarvestStatDTO;
import eionet.cr.harvest.BaseHarvest;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.sql.SQLUtil;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class VirtuosoHarvestDAO extends VirtuosoBaseDAO implements HarvestDAO {

    /** SQL for fetching a harvest by its ID. */
    private static final String GET_HARVEST_BY_ID_SQL = "select *, USERNAME as \"USER\" from HARVEST where HARVEST_ID=?";

    /** SQL for marking all abandoned harvests. */
    private static final String MARK_ABANDONED_HARVESTS_SQL = "UPDATE harvest SET status = ? WHERE status = ? AND finished IS NULL";

    /**
     * {@inheritDoc}
     */
    @Override
    public HarvestDTO getHarvestById(Integer harvestId) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(harvestId);
        List<HarvestDTO> list = executeSQL(GET_HARVEST_BY_ID_SQL, values, new HarvestDTOReader());
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HarvestDTO> getHarvestsBySourceId(Integer harvestSourceId) throws DAOException {

        int maxDistinctHarvests = 20;

        String getHarvestsBySourceIdSQL =
                "select distinct top " + HarvestMessageType.values().length * maxDistinctHarvests + " H.HARVEST_ID as HARVEST_ID,"
                        + " H.HARVEST_SOURCE_ID as SOURCE_ID, H.TYPE as HARVEST_TYPE, H.USERNAME as HARVEST_USER,"
                        + " H.STATUS as STATUS, H.STARTED as STARTED, H.FINISHED as FINISHED,"
                        + " H.ENC_SCHEMES as ENC_SCHEMES, H.TOT_STATEMENTS as TOT_STATEMENTS,"
                        + " H.HTTP_CODE as HTTP_CODE,"
                        + " H.LIT_STATEMENTS as LIT_STATEMENTS, M.TYPE as MESSAGE_TYPE"
                        + " from HARVEST AS H left join HARVEST_MESSAGE AS M on H.HARVEST_ID=M.HARVEST_ID"
                        + " where H.HARVEST_SOURCE_ID=? order by H.STARTED desc";

        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceId);
        return executeSQL(getHarvestsBySourceIdSQL, values, new HarvestWithMessageTypesReader(maxDistinctHarvests));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HarvestStatDTO> getLastHarvestStats(Integer limit) throws DAOException {

        String getHarvestStatsSQL =
                "SELECT TOP " + limit + " h.harvest_id, h.started, h.finished, h.tot_statements, hs.url"
                        + " FROM HARVEST AS h LEFT JOIN HARVEST_SOURCE AS hs ON h.harvest_source_id = hs.harvest_source_id"
                        + " WHERE h.status = ? ORDER BY h.finished DESC";

        List<Object> values = new ArrayList<Object>();
        values.add(BaseHarvest.STATUS_FINISHED);
        return executeSQL(getHarvestStatsSQL, values, new HarvestStatReader());
    }

    /** */
    private static final String GET_LAST_HARVEST_BY_SOURCE_ID_SQL = "select top 1 *, USERNAME as \"USER\""
            + " from HARVEST where HARVEST_SOURCE_ID=? order by HARVEST.STARTED desc";

    /**
     * SQL for last harvest record while harvest has not returned Source not modified http code.
     */
    private static final String GET_LAST_REAL_HARVEST_BY_SOURCE_ID_SQL = "select top 1 *, USERNAME as \"USER\""
            + " from HARVEST where HARVEST_SOURCE_ID=? AND http_code = 200 order by HARVEST.STARTED desc";

    /**
     * {@inheritDoc}
     */
    @Override
    public HarvestDTO getLastHarvestBySourceId(Integer harvestSourceId) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceId);
        List<HarvestDTO> list = executeSQL(GET_LAST_HARVEST_BY_SOURCE_ID_SQL, values, new HarvestDTOReader());
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /** */
    private static final String INSERT_STARTED_HARVEST_SQL =
            "insert into HARVEST (HARVEST_SOURCE_ID, TYPE, USERNAME, STATUS, STARTED) values (?, ?, ?, ?, ?)";

    /**
     * {@inheritDoc}
     */
    @Override
    public int insertStartedHarvest(int harvestSourceId, String harvestType, String user, String status) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(harvestSourceId));
        values.add(harvestType);
        values.add(user);
        values.add(status);
        values.add(new java.util.Date());

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdateReturnAutoID(INSERT_STARTED_HARVEST_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String UPDATE_FINISHED_HARVEST_SQL =
            "update HARVEST set STATUS=?, FINISHED=?, TOT_STATEMENTS=?, HTTP_CODE=? where HARVEST_ID=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateFinishedHarvest(int harvestId, int noOfTriples, int httpCode) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(BaseHarvest.STATUS_FINISHED);
        values.add(new java.util.Date());
        values.add(new Integer(noOfTriples));
        values.add(new Integer(httpCode));

        values.add(new Integer(harvestId));

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(UPDATE_FINISHED_HARVEST_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteOldHarvests(int harvestId, int preserveRecent) throws DAOException {

        // Get harvest source id and maximum started time.
        // Because "TOP N" cannot be used in sub-select, we must get the highest started value from the last element
        // in the resultset.

        List<Object> selectParams = new ArrayList<Object>();
        selectParams.add(harvestId);

        StringBuffer selectSql = new StringBuffer();
        selectSql.append("SELECT TOP " + preserveRecent + " *, USERNAME as \"USER\" ");
        selectSql.append("FROM harvest AS h ");
        selectSql.append("WHERE h.harvest_source_id = ( ");
        selectSql.append("    SELECT h.harvest_source_id ");
        selectSql.append("    FROM harvest AS h ");
        selectSql.append("    WHERE h.harvest_id = ? ) ");
        selectSql.append("ORDER BY h.harvest_id DESC");

        List<HarvestDTO> list = executeSQL(selectSql.toString(), selectParams, new HarvestDTOReader());
        if (list == null || list.size() == 0 || list.size() <= preserveRecent) {
            // If list smaller than preserveRecent, we have nothing more to do herLast 10 harvestse.
            return;
        }

        HarvestDTO dto = list.get(list.size() - 1);
        if (dto == null) {
            return;
        }

        // Delete older harvests with same harvest source.
        List<Object> deleteParams = new ArrayList<Object>();
        deleteParams.add(dto.getHarvestSourceId());
        deleteParams.add(dto.getHarvestId());

        StringBuffer deleteSql = new StringBuffer();
        deleteSql.append("DELETE FROM harvest AS h WHERE h.harvest_source_id = ? AND h.harvest_id < ?");

        executeSQL(deleteSql.toString(), deleteParams);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HarvestDTO getLastRealHarvestBySourceId(Integer harvestSourceId) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceId);
        List<HarvestDTO> list = executeSQL(GET_LAST_REAL_HARVEST_BY_SOURCE_ID_SQL, values, new HarvestDTOReader());
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestDAO#markAbandonedHarvests()
     */
    @Override
    public int markAbandonedHarvests() throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(BaseHarvest.STATUS_ABANDONED);
        values.add(BaseHarvest.STATUS_STARTED);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdate(MARK_ABANDONED_HARVESTS_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }
}
