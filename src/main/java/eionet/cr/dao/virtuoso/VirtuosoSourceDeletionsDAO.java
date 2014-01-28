package eionet.cr.dao.virtuoso;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;

import eionet.cr.config.GeneralConfig;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.SourceDeletionsDAO;
import eionet.cr.util.Hashes;
import eionet.cr.util.sesame.SesameUtil;
import eionet.cr.util.sql.SQLUtil;

/**
 * Virtuoso-specific implementation of {@link SourceDeletionsDAO}.
 *
 * @author Jaanus
 */
public class VirtuosoSourceDeletionsDAO extends VirtuosoBaseDAO implements SourceDeletionsDAO {

    /** Marking will be done in batches, this is the batch size. */
    private static final int MARKING_BATCH_SIZE = 5000;

    /** SQL for marking a harvest source for background deletion. */
    private static final String MARK_FOR_DELETION_SQL = "UPDATE harvest_source SET delete_requested=now() where URL_HASH=?";

    /** SQL for "softly" inserting a harvest source. "Softly" means that if source already exists, no errors thrown. */
    private static final String SOFT_INSERT_SOURCE =
            "INSERT SOFT harvest_source (url,url_hash,time_created,interval_minutes) VALUES (?,?,NOW(),"
                    + GeneralConfig.getDefaultHarvestIntervalMinutes() + ")";

    /*
     * (non-Javadoc)
     *
     * @see eionet.cr.dao.SourceDeletionsDAO#markForDeletion(java.util.Collection)
     */
    @Override
    public int markForDeletion(Collection<String> sourceUrls) throws DAOException {

        if (CollectionUtils.isEmpty(sourceUrls)) {
            return 0;
        }

        Connection conn = null;
        PreparedStatement pstmtInsert = null;
        PreparedStatement pstmtMark = null;
        try {
            conn = SesameUtil.getSQLConnection();
            conn.setAutoCommit(false);

            // We shall require to ensure the record exists (i.e. by doing soft insert) and that it gets updated, hence 2 steps.
            pstmtInsert = conn.prepareStatement(SOFT_INSERT_SOURCE);
            pstmtMark = conn.prepareStatement(MARK_FOR_DELETION_SQL);

            int batchCounter = 0;
            int updateCounter = 0;

            for (String sourceUrl : sourceUrls) {

                pstmtInsert.setString(1, sourceUrl);
                pstmtInsert.setLong(2, Hashes.spoHash(sourceUrl));
                pstmtInsert.addBatch();

                pstmtMark.setLong(1, Hashes.spoHash(sourceUrl));
                pstmtMark.addBatch();

                if (++batchCounter % MARKING_BATCH_SIZE == 0) {

                    pstmtInsert.executeBatch();
                    int[] updateCounts = pstmtMark.executeBatch();
                    for (int i = 0; i < updateCounts.length; i++) {
                        updateCounter += updateCounts[i];
                    }
                }
            }

            if (batchCounter % MARKING_BATCH_SIZE != 0) {
                pstmtInsert.executeBatch();
                int[] updateCounts = pstmtMark.executeBatch();
                for (int i = 0; i < updateCounts.length; i++) {
                    updateCounter += updateCounts[i];
                }
            }

            conn.commit();
            return updateCounter;
        } catch (SQLException e) {
            SQLUtil.rollback(conn);
            throw new DAOException(e.getMessage(), e);
        } finally {
            SQLUtil.close(pstmtInsert);
            SQLUtil.close(pstmtMark);
            SQLUtil.close(conn);
        }
    }
}
