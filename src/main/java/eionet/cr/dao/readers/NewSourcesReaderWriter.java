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
 * Agency. Portions created by Zero Technologies are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 *        Jaanus Heinlaid
 */

package eionet.cr.dao.readers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.openrdf.model.BNode;
import org.openrdf.model.Value;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.SQLUtil;

// TODO: Auto-generated Javadoc
/**
 * Reads harvest source URLs from the given binding set, and inserts them into HARVEST_SOURCE table, using {@link PreparedStatement}
 * and execution by batches (to save performance).
 *
 * @author Jaanus Heinlaid
 */
public class NewSourcesReaderWriter extends ResultSetMixedReader {

    /** */
    private static final Logger LOGGER = Logger.getLogger(NewSourcesReaderWriter.class);

    /** */
    private static final String INSERT_SQL = "insert soft HARVEST_SOURCE "
            + "(URL,URL_HASH,TIME_CREATED,INTERVAL_MINUTES) VALUES (?,?,NOW(),"
            + Integer.parseInt(GeneralConfig.getProperty(GeneralConfig.HARVESTER_REFERRALS_INTERVAL,
                    String.valueOf(HarvestSourceDTO.DEFAULT_REFERRALS_INTERVAL))) + ")";

    /** */
    private static final int BATCH_LIMIT = 1000;

    /** */
    private Connection sqlConn = null;

    /** */
    private PreparedStatement preparedStatement;

    /** */
    private int sourceCount;

    /**
     *
     */
    private int currentBatchSize;

    /**
     * Class constructor.
     *
     * @param sqlConn
     */
    public NewSourcesReaderWriter(Connection sqlConn) {

        if (sqlConn == null) {
            throw new IllegalArgumentException("Connection must not be null!");
        }
        this.sqlConn = sqlConn;
    }

    /*
     * (non-Javadoc)
     * @see eionet.cr.util.sesame.SPARQLResultSetReader#readRow(org.openrdf.query.BindingSet)
     */
    @Override
    public void readRow(BindingSet bindingSet) throws ResultSetReaderException {

        String sourceUrl = getFirstBindingStringValue(bindingSet);
        if (!StringUtils.isBlank(sourceUrl)) {

            // Replace spaces from url
            sourceUrl = StringUtils.replace(sourceUrl.trim(), " ", "%20");

            sourceCount++;
            try {
                getPreparedStatement().setString(1, sourceUrl);
                getPreparedStatement().setLong(2, Hashes.spoHash(sourceUrl));
                getPreparedStatement().addBatch();
                currentBatchSize++;

                if (currentBatchSize == BATCH_LIMIT) {

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace("Inserting " + currentBatchSize + " sources");
                    }

                    getPreparedStatement().executeBatch();
                    getPreparedStatement().clearParameters();
                    currentBatchSize = 0;

                    if (LOGGER.isTraceEnabled()) {
                        LOGGER.trace(sourceCount + " sources inserted so far");
                    }
                }

            } catch (SQLException e) {
                throw new ResultSetReaderException(e.getMessage(), e);
            }
        }
    }

    /**
     * @throws ResultSetReaderException
     */
    public void finish() throws ResultSetReaderException {

        try {
            if (currentBatchSize > 0) {

                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace("Inserting " + currentBatchSize + " sources");
                }
                getPreparedStatement().executeBatch();
                if (LOGGER.isTraceEnabled()) {
                    LOGGER.trace(sourceCount + " sources inserted in total");
                }
            }
        } catch (SQLException e) {
            throw new ResultSetReaderException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    public void closeResources() {
        SQLUtil.close(preparedStatement);
    }

    /**
     *
     *
     * @param bindingSet
     * @return
     */
    private String getFirstBindingStringValue(BindingSet bindingSet) {

        if (bindingSet != null && bindingSet.size() > 0) {

            Binding binding = bindingSet.iterator().next();
            if (binding != null) {

                Value value = binding.getValue();
                String stringValue = value.stringValue();
                if (!StringUtils.isBlank(stringValue)) {

                    if (value instanceof BNode) {
                        if (blankNodeUriPrefix != null && !stringValue.startsWith(blankNodeUriPrefix)) {
                            stringValue = blankNodeUriPrefix + stringValue;
                        }
                    }

                    return stringValue;
                }
            }
        }

        return null;
    }

    /**
     * @return the preparedStatement
     * @throws SQLException
     */
    public PreparedStatement getPreparedStatement() throws SQLException {

        // Lazy loading.
        if (preparedStatement == null) {
            preparedStatement = sqlConn.prepareStatement(INSERT_SQL);
        }

        return preparedStatement;
    }

    /**
     * @return the sourceCount
     */
    public int getSourceCount() {
        return sourceCount;
    }

    /* (non-Javadoc)
     * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
     */
    @Override
    public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
        throw new UnsupportedOperationException("Method not implemented!");
    }
}
