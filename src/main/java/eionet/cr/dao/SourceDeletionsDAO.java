package eionet.cr.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import eionet.cr.util.Pair;
import eionet.cr.util.pagination.PagingRequest;

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
     * @throws DAOException If database access error occurs.
     */
    int markForDeletion(Collection<String> sourceUrls) throws DAOException;

    /**
     * Marks for deletion the sources returned in the first result set column of the given SPARQL query. Other columns and all
     * non-URL values are simply ignored.
     *
     * @param sparqlQuery The SPARQL query.
     * @return Number of sources successfully marked.
     * @throws DAOException If database access error occurs.
     */
    int markForDeletionSparql(String sparqlQuery) throws DAOException;

    /**
     * Gets source deletion queue from database.
     *
     * @param filterStr Sub-string of URLs to match. Blank = all URLs.
     * @param pagingRequest Result-set page to return.
     * @return The requested result-set page.
     * @throws DAOException If database access error occurs.
     */
    List<Pair<String, Date>> getDeletionQueue(String filterStr, PagingRequest pagingRequest) throws DAOException;
}
