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
import java.util.List;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.HarvestDAO;
import eionet.cr.dao.readers.HarvestDTOReader;
import eionet.cr.dao.readers.HarvestWithMessageTypesReader;
import eionet.cr.dto.HarvestDTO;
import eionet.cr.harvest.util.HarvestMessageType;
import eionet.cr.util.sql.SQLUtil;

/**
 *
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PostgreSQLHarvestDAO extends PostgreSQLBaseDAO implements
        HarvestDAO {

    /** */
    private static final String getHarvestByIdSQL = "select *, USERNAME as USER from HARVEST where HARVEST_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestDAO#getHarvestById(java.lang.Integer)
     */
    public HarvestDTO getHarvestById(Integer harvestId) throws DAOException {
        List<Object> values = new ArrayList<Object>();
        values.add(harvestId);
        List<HarvestDTO> list = executeSQL(getHarvestByIdSQL, values,
                new HarvestDTOReader());
        return list != null && list.size() > 0 ? list.get(0) : null;
    }

    /** */
    private static final String getHarvestsBySourceIdSQL = "select distinct HARVEST.HARVEST_ID as HARVEST_ID,"
            + " HARVEST.HARVEST_SOURCE_ID as SOURCE_ID,"
            + " HARVEST.TYPE as HARVEST_TYPE,"
            + " HARVEST.USERNAME as HARVEST_USER,"
            + " HARVEST.STATUS as STATUS,"
            + " HARVEST.STARTED as STARTED,"
            + " HARVEST.FINISHED as FINISHED,"
            + " HARVEST.ENC_SCHEMES as ENC_SCHEMES,"
            + " HARVEST.TOT_STATEMENTS as TOT_STATEMENTS,"
            + " HARVEST.LIT_STATEMENTS as LIT_STATEMENTS,"
            + " HARVEST_MESSAGE.TYPE as MESSAGE_TYPE"
            + " from HARVEST left join HARVEST_MESSAGE on HARVEST.HARVEST_ID=HARVEST_MESSAGE.HARVEST_ID"
            + " where HARVEST.HARVEST_SOURCE_ID=? order by HARVEST.STARTED desc limit ?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestDAO#getHarvestsBySourceId()
     */
    public List<HarvestDTO> getHarvestsBySourceId(Integer harvestSourceId)
            throws DAOException {

        int maxDistinctHarvests = 10;

        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceId);
        values.add(HarvestMessageType.values().length * maxDistinctHarvests);
        return executeSQL(getHarvestsBySourceIdSQL, values,
                new HarvestWithMessageTypesReader(maxDistinctHarvests));
    }

    /** */
    private static final String getLastHarvestBySourceIdSQL = "select *, USERNAME as USER"
            + " from HARVEST where HARVEST_SOURCE_ID=? order by HARVEST.STARTED desc limit 1";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestDAO#getLastHarvest(java.lang.Integer)
     */
    public HarvestDTO getLastHarvestBySourceId(Integer harvestSourceId)
            throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(harvestSourceId);
        List<HarvestDTO> list = executeSQL(getLastHarvestBySourceIdSQL, values,
                new HarvestDTOReader());
        return list != null && !list.isEmpty() ? list.get(0) : null;
    }

    /** */
    private static final String insertStartedHarvestSQL = "insert into HARVEST (HARVEST_SOURCE_ID, TYPE, USERNAME, STATUS, STARTED) values (?, ?, ?, ?, now())";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestDAO#insertHarvest(int, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public int insertStartedHarvest(int harvestSourceId, String harvestType,
            String user, String status) throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(new Integer(harvestSourceId));
        values.add(harvestType);
        values.add(user);
        values.add(status);

        Connection conn = null;
        try {
            conn = getSQLConnection();
            return SQLUtil.executeUpdateReturnAutoID(insertStartedHarvestSQL,
                    values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }

    /** */
    private static final String updateFinishedHarvestSQL = "update HARVEST set STATUS=?, FINISHED=now(), TOT_STATEMENTS=?, LIT_STATEMENTS=?, RES_STATEMENTS=?, ENC_SCHEMES=? "
            + "where HARVEST_ID=?";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.HarvestDAO#updateFinishedHarvest(int, int, int, int,
     * java.lang.String)
     */
    public void updateFinishedHarvest(int harvestId, String status,
            int totStatements, int litStatements, int encSchemes)
            throws DAOException {

        List<Object> values = new ArrayList<Object>();
        values.add(status);
        values.add(new Integer(totStatements));
        values.add(new Integer(litStatements));
        values.add(new Integer(totStatements - litStatements));
        values.add(new Integer(encSchemes));
        values.add(new Integer(harvestId));

        Connection conn = null;
        try {
            conn = getSQLConnection();
            SQLUtil.executeUpdate(updateFinishedHarvestSQL, values, conn);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(conn);
        }
    }
}
