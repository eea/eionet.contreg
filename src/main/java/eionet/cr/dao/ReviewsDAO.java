package eionet.cr.dao;

import java.util.List;

import eionet.cr.dto.ReviewDTO;
import eionet.cr.web.security.CRUser;

/**
 * DAO for reviews functionality.
 *
 * @author Risto Alt
 */
public interface ReviewsDAO extends DAO {

    /**
     * @param user
     * @return int
     * @throws DAOException if query fails
     */
    int generateNewReviewId(CRUser user) throws DAOException;

    /**
     * @param user
     * @return int
     * @throws DAOException if query fails
     */
    int getLastReviewId(CRUser user) throws DAOException;

    /**
     * @param review
     * @param user
     * @return int
     * @throws DAOException if query fails
     */
    int addReview(ReviewDTO review, CRUser user) throws DAOException;

    /**
     * @param reviewId
     * @param review
     * @param user
     * @return
     * @throws DAOException if query fails
     */
    void saveReview(int reviewId, ReviewDTO review, CRUser user) throws DAOException;

    /**
     * @param user
     * @return List<ReviewDTO
     * @throws DAOException if query fails
     */
    List<ReviewDTO> getReviewList(CRUser user) throws DAOException;

    /**
     * @param user
     * @param reviewId
     * @return List<ReviewDTO
     * @throws DAOException if query fails
     */
    ReviewDTO getReview(CRUser user, int reviewId) throws DAOException;

    /**
     * Returns TRUE if last modified date for objectUri is later than last modified date for reviewUri
     *
     * @param reviewUri
     * @param objectUri
     * @return boolean
     * @throws DAOException - if query fails
     */
    boolean isReviewObsolete(String reviewUri, String objectUri) throws DAOException;

    /**
     * @param user
     * @param reviewId
     * @return List<String>
     * @throws DAOException if query fails
     */
    List<String> getReviewAttachmentList(CRUser user, int reviewId) throws DAOException;

    /**
     * @param user
     * @param reviewId
     * @param deleteAttachments
     * @return
     * @throws DAOException if query fails
     */
    void deleteReview(CRUser user, int reviewId, boolean deleteAttachments) throws DAOException;

    /**
     * @param user
     * @param reviewId
     * @param attachmentUri
     * @return
     * @throws DAOException if query fails
     */
    void deleteAttachment(CRUser user, int reviewId, String attachmentUri) throws DAOException;
}
