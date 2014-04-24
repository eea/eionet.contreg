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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dao.readers.HarvestQueueItemDTOReader;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.util.sql.SQLUtil;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class VirtuosoUrgentHarvestQueueDAO extends VirtuosoBaseDAO implements UrgentHarvestQueueDAO {

    /** */
    private static final String ADD_PULL_HARVEST_SQL =
            "insert into URGENT_HARVEST_QUEUE (URL,\"TIMESTAMP\", USERNAME) VALUES (?,NOW(),?)";
    /** */
    private static final String ADD_PUSH_HARVEST_SQL =
            "insert into URGENT_HARVEST_QUEUE (URL,\"TIMESTAMP\",PUSHED_CONTENT, USERNAME) VALUES (?,NOW(),?,?)";
    /** */
    private static final String GET_URGENT_HARVEST_QUEUE_SQL = "select * from URGENT_HARVEST_QUEUE order by \"TIMESTAMP\" asc";
    /** */
    private static final String PEEK_SQL = "select top 1 * from URGENT_HARVEST_QUEUE order by \"TIMESTAMP\" asc";
    /** */
    private static final String DELETE_QUEUE_ITEM_SQL = "delete from URGENT_HARVEST_QUEUE where URL=? and \"TIMESTAMP\"=?";

    /** SQL for removing occurrences of a given URL from urgent harvest queue table. */
    private static final String REMOVE_URL_SQL = "delete from URGENT_HARVEST_QUEUE where URL=?";

    /** SQL for removing a harvest queue item with the given id. */
    private static final String REMOVE_ITEM_SQL = "delete from URGENT_HARVEST_QUEUE where ITEM_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#addPullHarvests(java.util.List, java.lang.String)
     */
    @Override
    public void addPullHarvests(List<UrgentHarvestQueueItemDTO> queueItems, String userName) throws DAOException {

        if (CollectionUtils.isEmpty(queueItems)) {
            return;
        }

        if (userName != null && userName.trim().length() == 0) {
            userName = null;
        }

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(ADD_PULL_HARVEST_SQL);

            boolean atLeastOneAdded = false;
            for (int i = 0; i < queueItems.size(); i++) {

                UrgentHarvestQueueItemDTO dto = queueItems.get(i);
                String url = dto.getUrl();
                if (StringUtils.isNotBlank(url)) {
                    url = StringUtils.substringBefore(url, "#");
                    ps.setString(1, url);
                    ps.setString(2, userName);
                    ps.addBatch();
                    atLeastOneAdded = true;
                }
            }

            if (atLeastOneAdded) {
                ps.executeBatch();
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(ps);
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestQueueDAO#addPushHarvest(eionet.cr.dto.HarvestQueueItemDTO)
     */
    @Override
    public void addPushHarvest(UrgentHarvestQueueItemDTO queueItem) throws DAOException {

        List<Object> values = new ArrayList<Object>();

        String url = queueItem.getUrl();
        if (url != null) {
            url = StringUtils.substringBefore(url, "#");
        }
        values.add(url);
        values.add(queueItem.getPushedContent());
        values.add(CRUser.PUSH_HARVEST.getUserName());

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(ADD_PUSH_HARVEST_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestQueueDAO#getUrgentHarvestQueue()
     */
    @Override
    public List<UrgentHarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException {
        return executeSQL(GET_URGENT_HARVEST_QUEUE_SQL, new ArrayList<Object>(), new HarvestQueueItemDTOReader());
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#poll()
     */
    @Override
    public UrgentHarvestQueueItemDTO poll() throws DAOException {

        Connection conn = null;
        try {
            conn = getSQLConnection();
            UrgentHarvestQueueItemDTO queueItem = peek(conn);
            if (queueItem != null) {
                deleteQueueItem(queueItem, conn);
            }
            return queueItem;

        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /**
     * Returns top-most item in queue, but does not remove it.
     *
     * @param conn Db connection.
     * @return The item.
     * @throws SQLException if SQL error
     */
    private static UrgentHarvestQueueItemDTO peek(Connection conn) throws Exception {

        List<Object> values = new ArrayList<Object>();

        HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
        SQLUtil.executeQuery(PEEK_SQL, values, rsReader, conn);
        List<UrgentHarvestQueueItemDTO> list = rsReader.getResultList();

        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    /**
     * Helper method for deleting given queue item.
     *
     * @param queueItem The queue item to delete.
     * @throws SQLException if SQL error
     */
    private static void deleteQueueItem(UrgentHarvestQueueItemDTO queueItem, Connection conn) throws SQLException {

        List<Object> values = new ArrayList<Object>();
        values.add(queueItem.getUrl());
        values.add(queueItem.getTimeAdded());

        SQLUtil.executeUpdate(DELETE_QUEUE_ITEM_SQL, values, conn);
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#isInQueue(java.lang.String)
     */
    @Override
    public boolean isInQueue(String url) throws DAOException {

        boolean ret = false;
        String sql = "select top 1 * from URGENT_HARVEST_QUEUE where URL = ?";
        PreparedStatement ps = null;
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, url);
            rs = ps.executeQuery();
            while (rs.next()) {
                ret = true;
            }
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(rs);
            SQLUtil.close(ps);
            SQLUtil.close(conn);
        }
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#isInQueue(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isInQueue(String url, String userName) throws DAOException {

        String sql = "select top 1 URL from URGENT_HARVEST_QUEUE where URL = ? and USERNAME = ?";

        Connection conn = null;
        try {
            conn = getSQLConnection();
            Object urlObject = SQLUtil.executeSingleReturnValueQuery(sql, Arrays.asList(url, userName), conn);
            return urlObject != null && urlObject.toString().equals(url);
        } catch (Exception e) {
            throw new DAOException(e.toString(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#removeUrl(java.lang.String)
     */
    @Override
    public void removeUrl(String url) throws DAOException {

        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("The given URL must not be blank!");
        }

        executeSQL(REMOVE_URL_SQL, Arrays.asList(url));
    }

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#removeItems(java.util.List)
     */
    @Override
    public void removeItems(List<Integer> itemIds) throws DAOException {

        if (CollectionUtils.isEmpty(itemIds)) {
            throw new IllegalArgumentException("The given list of itemd ids must not be empty!");
        }

        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = getSQLConnection();
            conn.setAutoCommit(false);
            statement = conn.prepareStatement(REMOVE_ITEM_SQL);
            for (Integer itemId : itemIds) {
                statement.setInt(1, itemId);
                statement.addBatch();
            }
            statement.executeBatch();
            conn.commit();
        } catch (Exception e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(statement);
            SQLUtil.close(conn);
        }
    }
}
