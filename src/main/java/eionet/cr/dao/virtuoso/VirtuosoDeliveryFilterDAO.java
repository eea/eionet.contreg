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
 * The Original Code is Content Registry 3
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency. Portions created by TripleDev or Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Juhan Voolaid
 */

package eionet.cr.dao.virtuoso;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dao.DAOException;
import eionet.cr.dao.DeliveryFilterDAO;
import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.dto.DeliveryFilterDTO;
import eionet.cr.util.sql.SQLResultSetBaseReader;

/**
 * Delivery filter DAO.
 *
 * @author Juhan Voolaid
 */
public class VirtuosoDeliveryFilterDAO extends VirtuosoBaseDAO implements DeliveryFilterDAO {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DeliveryFilterDTO> getDeliveryFilters(String username) throws DAOException {
        List<Object> params = new ArrayList<Object>();
        params.add(username);

        StringBuffer selectSql = new StringBuffer();
        selectSql.append("SELECT * ");
        selectSql.append("FROM delivery_filter ");
        selectSql.append("WHERE username = ? ");
        selectSql.append("ORDER BY delivery_filter_id DESC");

        List<DeliveryFilterDTO> result = executeSQL(selectSql.toString(), params, new SQLResultSetBaseReader<DeliveryFilterDTO>() {
            @Override
            public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
                DeliveryFilterDTO dto = new DeliveryFilterDTO();
                dto.setId(rs.getLong("delivery_filter_id"));
                dto.setLocality(rs.getString("locality"));
                dto.setLocalityLabel(rs.getString("locality_label"));
                dto.setObligation(rs.getString("obligation"));
                dto.setObligationLabel(rs.getString("obligation_label"));
                dto.setUsername(rs.getString("username"));
                dto.setYear(rs.getString("year"));

                resultList.add(dto);
            }
        });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DeliveryFilterDTO getDeliveryFilte(long id) throws DAOException {

        List<Object> params = new ArrayList<Object>();
        params.add(id);

        StringBuffer selectSql = new StringBuffer();
        selectSql.append("SELECT * ");
        selectSql.append("FROM delivery_filter ");
        selectSql.append("WHERE delivery_filter_id = ? ");

        DeliveryFilterDTO result =
            executeUniqueResultSQL(selectSql.toString(), params, new SQLResultSetBaseReader<DeliveryFilterDTO>() {
                @Override
                public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
                    DeliveryFilterDTO dto = new DeliveryFilterDTO();
                    dto.setId(rs.getLong("delivery_filter_id"));
                    dto.setLocality(rs.getString("locality"));
                    dto.setLocalityLabel(rs.getString("locality_label"));
                    dto.setObligation(rs.getString("obligation"));
                    dto.setObligationLabel(rs.getString("obligation_label"));
                    dto.setUsername(rs.getString("username"));
                    dto.setYear(rs.getString("year"));

                    resultList.add(dto);
                }
            });

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveDeliveryFilter(DeliveryFilterDTO deliveryFilter) throws DAOException {
        String sql =
            "insert into delivery_filter (obligation, obligation_label, locality, locality_label, year, username) " +
            "values (?, ?, ?, ?, ?, ?)";

        List<Object> params = new ArrayList<Object>();
        params.add(deliveryFilter.getObligation());
        params.add(deliveryFilter.getObligationLabel());
        params.add(deliveryFilter.getLocality());
        params.add(deliveryFilter.getLocalityLabel());
        params.add(deliveryFilter.getYear());
        params.add(deliveryFilter.getUsername());

        executeSQL(sql, params);
        deleteOldestFilter(deliveryFilter.getUsername(), 10);
    }

    private void deleteOldestFilter(String username, int preserveRecent) throws DAOException {

        // Getting the id's of latest delivery filters
        List<Object> selectParams = new ArrayList<Object>();
        selectParams.add(username);

        StringBuffer selectSql = new StringBuffer();
        selectSql.append("SELECT TOP " + preserveRecent + " delivery_filter_id ");
        selectSql.append("FROM delivery_filter ");
        selectSql.append("WHERE username = ? ");
        selectSql.append("ORDER BY delivery_filter_id DESC");

        List<Long> list = executeSQL(selectSql.toString(), selectParams, new SQLResultSetBaseReader<Long>() {

            @Override
            public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
                Long id = rs.getLong("delivery_filter_id");
                resultList.add(id);
            }
        });

        // Deleting all the user filters except the latest ones
        List<Object> deleteParams = new ArrayList<Object>();
        deleteParams.add(username);

        StringBuffer deleteSql = new StringBuffer();
        deleteSql.append("DELETE FROM delivery_filter ");
        deleteSql.append("WHERE delivery_filter_id not in (" + StringUtils.join(list, ",") + ") ");
        deleteSql.append("AND username = ?");

        executeSQL(deleteSql.toString(), deleteParams);
    }

}
