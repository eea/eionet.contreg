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
import java.util.ArrayList;
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestMessageDAO;
import eionet.cr.dao.readers.HarvestMessageDTOReader;
import eionet.cr.dto.HarvestMessageDTO;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class VirtuosoHarvestMessageDAO extends VirtuosoBaseDAO implements HarvestMessageDAO {

    /** */
    private static final String GET_HARVEST_MESSAGE_BY_HARVEST_ID_SQL = "select * from HARVEST_MESSAGE where HARVEST_ID=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HarvestMessageDTO> findHarvestMessagesByHarvestID(int harvestID) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(harvestID));
        return executeSQL(GET_HARVEST_MESSAGE_BY_HARVEST_ID_SQL, values, new HarvestMessageDTOReader());
    }

    /** */
    private static final String GET_HARVEST_MESSAGE_BY_MESSAGE_ID_SQL = "select * from HARVEST_MESSAGE where HARVEST_MESSAGE_ID=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public HarvestMessageDTO findHarvestMessageByMessageID(int messageID) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(messageID));
        List<HarvestMessageDTO> list = executeSQL(GET_HARVEST_MESSAGE_BY_MESSAGE_ID_SQL, values, new HarvestMessageDTOReader());
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    /** */
    private static final String INSERT_HARVEST_MESSAGE_SQL =
            "insert into HARVEST_MESSAGE (HARVEST_ID, TYPE, MESSAGE, STACK_TRACE) values (?, ?, ?, ?)";

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer insertHarvestMessage(HarvestMessageDTO harvestMessageDTO) throws DAOException {

        if (harvestMessageDTO == null)
            return null;

        List<Object> values = new ArrayList<Object>();
        values.add(harvestMessageDTO.getHarvestId());
        values.add(harvestMessageDTO.getType());
        values.add(harvestMessageDTO.getMessage());
        values.add(harvestMessageDTO.getStackTrace());

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdateReturnAutoID(INSERT_HARVEST_MESSAGE_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String DELETE_HARVEST_MESSAGE_SQL = "delete from HARVEST_MESSAGE where HARVEST_MESSAGE_ID=?";

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMessage(Integer messageId) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(messageId);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(DELETE_HARVEST_MESSAGE_SQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

}
