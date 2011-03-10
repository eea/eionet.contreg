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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.UrgentHarvestQueueDAO;
import eionet.cr.dao.readers.HarvestQueueItemDTOReader;
import eionet.cr.dto.UrgentHarvestQueueItemDTO;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLUrgentHarvestQueueDAO extends PostgreSQLBaseDAO implements UrgentHarvestQueueDAO{

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestQueueDAO#addQueueItem(eionet.cr.dto.HarvestQueueItemDTO)
     */
    public void addPullHarvests(List<UrgentHarvestQueueItemDTO> queueItems) throws DAOException {

        String valueStr = "(?,NOW())";
        List<Object> values = new ArrayList<Object>();
        StringBuffer buf = new StringBuffer("insert into URGENT_HARVEST_QUEUE (URL,TIMESTAMP) VALUES ");
        for (int i=0; i<queueItems.size(); i++){
            UrgentHarvestQueueItemDTO dto = queueItems.get(i);
            buf.append(i>0 ? "," : "").append(valueStr);

            String url = dto.getUrl();
            if (url!=null){
                url = StringUtils.substringBefore(url, "#");
            }
            values.add(url);
        }

        Connection conn = null;
        try{
            conn = getSQLConnection();
            SQLUtil.executeUpdate(buf.toString(), values, conn);
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String addPushHarvestSQL = "insert into URGENT_HARVEST_QUEUE (URL,TIMESTAMP,PUSHED_CONTENT) VALUES (?,NOW(),?)";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestQueueDAO#addPushHarvest(eionet.cr.dto.HarvestQueueItemDTO)
     */
    public void addPushHarvest(UrgentHarvestQueueItemDTO queueItem) throws DAOException {

        List<Object> values = new ArrayList<Object>();

        String url = queueItem.getUrl();
        if (url!=null){
            url = StringUtils.substringBefore(url, "#");
        }
        values.add(url);
        values.add(queueItem.getPushedContent());

        Connection conn = null;
        try{
            conn = getSQLConnection();
            SQLUtil.executeUpdate(addPushHarvestSQL, values, conn);
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String getUrgentHarvestQueueSQL = "select * from URGENT_HARVEST_QUEUE order by TIMESTAMP asc";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestQueueDAO#getUrgentHarvestQueue()
     */
    public List<UrgentHarvestQueueItemDTO> getUrgentHarvestQueue() throws DAOException{
        return executeSQL(getUrgentHarvestQueueSQL, new ArrayList<Object>(), new HarvestQueueItemDTOReader());
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.UrgentHarvestQueueDAO#poll()
     */
    public UrgentHarvestQueueItemDTO poll() throws DAOException {

        Connection conn = null;
        try{
            conn = getSQLConnection();
            UrgentHarvestQueueItemDTO queueItem = peek(conn);
            if (queueItem!=null)
                deleteQueueItem(queueItem, conn);

            return queueItem;
        }
        catch (Exception e){
            throw new DAOException(e.getMessage(), e);
        }
        finally{
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String peekSQL = "select * from URGENT_HARVEST_QUEUE order by TIMESTAMP asc limit 1";
    /**
     *
     * @param conn
     * @return
     * @throws SQLException
     */
    private static UrgentHarvestQueueItemDTO peek(Connection conn) throws Exception{

        List<Object> values = new ArrayList<Object>();

        HarvestQueueItemDTOReader rsReader = new HarvestQueueItemDTOReader();
        SQLUtil.executeQuery(peekSQL, values, rsReader, conn);
        List<UrgentHarvestQueueItemDTO> list = rsReader.getResultList();

        return (list!=null && !list.isEmpty()) ? list.get(0) : null;
    }

    /** */
    private static final String deleteQueueItemSQL = "delete from URGENT_HARVEST_QUEUE where URL=? and TIMESTAMP=?";
    /**
     *
     * @param queueItem
     * @throws SQLException
     */
    private static void deleteQueueItem(UrgentHarvestQueueItemDTO queueItem, Connection conn) throws SQLException{

        List<Object> values = new ArrayList<Object>();
        values.add(queueItem.getUrl());
        values.add(queueItem.getTimeAdded());

        SQLUtil.executeUpdate(deleteQueueItemSQL, values, conn);
    }
}
