/*
 * The contents of this file are subject to the Mozilla Public
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
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao.mysql;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.readers.HarvestMessageDTOReader;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.util.sql.DbConnectionProvider;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author heinljab
 *
 */
public class MySQLHarvestMessageDAO extends MySQLBaseDAO implements HarvestMessageDAO {


    MySQLHarvestMessageDAO() {
        //reducing visibility
    }

    /** */
    private static final String q_HarvestMessageByHarvestID = "select * from HARVEST_MESSAGE where HARVEST_ID=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestMessageDAO#findHarvestMessagesByHarvestID(java.lang.String)
     */
    public List<HarvestMessageDTO> findHarvestMessagesByHarvestID(int harvestID) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(harvestID));
        return executeQuery(q_HarvestMessageByHarvestID, values, new HarvestMessageDTOReader());
    }

    /** */
    private static final String q_HarvestMessageByMessageID = "select * from HARVEST_MESSAGE where HARVEST_MESSAGE_ID=?";

    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestMessageDAO#findHarvestMessageByMessageID(java.lang.String)
     */
    public HarvestMessageDTO findHarvestMessageByMessageID(int messageID) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(messageID));
        List<HarvestMessageDTO> list = executeQuery(q_HarvestMessageByMessageID, values, new HarvestMessageDTOReader());
        return list != null && list.size()>0 ? list.get(0) : null;
    }

    private static final String q_insertHarvestMessage =
        "insert into HARVEST_MESSAGE (HARVEST_ID, TYPE, MESSAGE, STACK_TRACE) values (?, ?, ?, ?)";
    /*
     * (non-Javadoc)
     * @see eionet.cr.dao.HarvestMessageDAO#insertHarvestMessage(eionet.cr.dto.HarvestMessageDTO)
     */
    public Integer insertHarvestMessage(HarvestMessageDTO harvestMessageDTO) throws DAOException {

        Integer harvestMessageID = null;

        if (harvestMessageDTO == null)
            return null;

        List<Object> values = new ArrayList<Object>();
        values.add(harvestMessageDTO.getHarvestId());
        values.add(harvestMessageDTO.getType());
        values.add(harvestMessageDTO.getMessage());
        values.add(harvestMessageDTO.getStackTrace());

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(q_insertHarvestMessage, values, conn);
            harvestMessageID = getLastInsertID(conn);

            return harvestMessageID;
        }
        catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String deleteHarvestMessageSQL = "delete from HARVEST_MESSAGE where HARVEST_MESSAGE_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestMessageDao#deleteMessage()
     */
    public void deleteMessage(Integer messageId) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(messageId);

        Connection conn = null;
        try {
            conn = getConnection();
            SQLUtil.executeUpdate(deleteHarvestMessageSQL, values, conn);
        }
        catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        finally {
            SQLUtil.close(conn);
        }
    }
}
