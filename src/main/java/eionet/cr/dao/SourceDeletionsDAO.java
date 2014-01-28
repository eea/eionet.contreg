package eionet.cr.dao;

import java.util.Collection;

/**
 * DAO for operations relating to the background deletion of marked harevst sources.
 *
 * @author Jaanus
 */
public interface SourceDeletionsDAO extends DAO {

    /**
     * Marks given sources for deletion.
     *
     * @param sourceUrls The sources given by URLs.
     * @return Number of sources successfully marked.
     * @throws DAOException Id database access error occurs.
     */
    int markForDeletion(Collection<String> sourceUrls) throws DAOException;
}
