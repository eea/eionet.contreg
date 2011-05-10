package eionet.cr.web.action.home;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.IOUtils;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.SpoBinaryDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SpoBinaryDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.util.Hashes;
import eionet.cr.web.security.CRUser;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 *
 */

@UrlBinding("/home/{username}/reviews")
public class ReviewsActionBean extends AbstractHomeActionBean {

    private List<String> raportsListing;

    private ReviewDTO review;
    private boolean testvar;

    private List<ReviewDTO> reviews;

    private List<Integer> reviewIds;
    private List<String> attachmentList;
    private int reviewId = 0;
    private boolean reviewView;

    private FileBean attachment;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        loadReview();

        setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_REVIEWS, true);

        if (this.getContext().getRequest().getParameter("addSave") != null) {
            if (isUserAuthorized()) {
                add();
            } else {
                addWarningMessage("User must be logged in to add a review.");
            }
        }

        if (this.getContext().getRequest().getParameter("editSave") != null) {
            if (isUserAuthorized()) {
                save();
            } else {
                addWarningMessage("User must be logged in to save a review.");
            }
        }

        if (this.getContext().getRequest().getParameter("delete") != null) {
            if (isUserAuthorized()) {
                deleteReviews();
            } else {
                addWarningMessage("User must be logged in to delete reviews.");
            }
        }

        if (this.getContext().getRequest().getParameter("deleteReview") != null) {
            if (isUserAuthorized()) {
                deleteSingleReview();
            } else {
                addWarningMessage("User must be logged in to delete a review.");
            }
        }

        if (this.getContext().getRequest().getParameter("upload") != null) {
            if (isUserAuthorized()) {
                upload();
            } else {
                addWarningMessage("User must be logged in to upload attachments.");
            }
        }

        if (this.getContext().getRequest().getParameter("deleteAttachments") != null) {
            if (isUserAuthorized()) {
                deleteAttachments();
            } else {
                addWarningMessage("User must be logged in to delete attachments.");
            }
        }


        return new ForwardResolution("/pages/home/reviews.jsp");
    }

    /**
     *
     */
    private void loadReview() {

        if (reviewId != 0) {
            return;
        }

        String[] splits = getContext().getRequest().getRequestURL().toString().split("reviews/");
        if (splits == null || splits.length < 2) {
            reviewId = 0;
        } else {
            String reviewString = splits[1];
            if (reviewString.contains("?")) {
                reviewString = reviewString.split("?")[0];
            }

            try {
                reviewId = Integer.parseInt(reviewString);
                if (reviewId > 0) {
                    setHomeContext(false); // Do not show tabs and headers.
                    reviewView = true;
                }
            } catch (Exception ex) {
                reviewId = 0;
                reviewView = true;
                setHomeContext(false);
                addCautionMessage("Not correct review ID. Only numerical values allowed after /reviews/.");
            }
        }
    }

    /**
     *
     */
    public void add() {
        if (isUserAuthorized()) {
            try {
                reviewId = factory.getDao(HelperDAO.class).addReview(review, this.getUser());
                addSystemMessage("Review successfully added.");
            } catch (DAOException ex) {
                logger.error(ex);
                addWarningMessage("System error while adding a review. The review was not added.");
            }
        } else {
            addWarningMessage("Only the owner of this home space can add reviews.");
        }
    }

    /**
     *
     */
    public void save() {
        if (isUserAuthorized()) {
            try {
                factory.getDao(HelperDAO.class).saveReview(reviewId, review, this.getUser());
                addSystemMessage("Review successfully saved.");
            } catch (DAOException ex) {
                logger.error(ex);
                addWarningMessage("System error while saving the review. The review was not saved.");
            }
        } else {
            addWarningMessage("Only the owner of this home space can save reviews.");
        }
    }

    /**
     * @throws DAOException
     */
    public void deleteReviews() throws DAOException {
        if (isUserAuthorized()) {
            if (this.getContext().getRequest().getParameter("delete") != null) {
                if (reviewIds != null && !reviewIds.isEmpty()) {
                    try {
                        for (int i = 0; i < reviewIds.size(); i++) {
                            DAOFactory.get().getDao(HelperDAO.class).deleteReview(this.getUser(), reviewIds.get(i), true);
                        }
                        addSystemMessage("Selected reviews were deleted.");
                    } catch (DAOException ex) {
                        logger.error(ex);
                        addWarningMessage("System error occured during review deletion.");
                    }
                } else {
                    addCautionMessage("No reviews selected for deletion.");
                }
            }
        } else {
            addWarningMessage("Only the owner of this home space can delete reviews.");
        }
    }

    /**
     * @throws DAOException
     */
    public void deleteSingleReview() throws DAOException {
        if (isUserAuthorized()) {
            if (this.getContext().getRequest().getParameter("deleteReview") != null) {
                try {
                    reviewId = Integer.parseInt(this.getContext().getRequest().getParameter("deleteReview"));
                    DAOFactory.get().getDao(HelperDAO.class).deleteReview(this.getUser(), reviewId, true);
                    addSystemMessage("Review #" + reviewId + " was deleted.");
                    reviewId = 0;
                } catch (DAOException ex) {
                    logger.error(ex);
                    addWarningMessage("System error occured during review deletion.");
                }
            }
        } else {
            addWarningMessage("Only the owner of this home space can delete reviews.");
        }
    }

    /**
     */
    public void upload() {

        logger.debug("Storing uploaded review attachment, file bean = " + attachment);

        if (attachment == null) {
            return;
        }

        // construct attachment uri
        String attachmentUri = getUser().getReviewAttachmentUri(
                reviewId, attachment.getFileName());

        InputStream attachmentContentStream = null;
        try {
            // save attachment contents into database
            attachmentContentStream = attachment.getInputStream();
            SpoBinaryDTO dto = new SpoBinaryDTO(
                    Hashes.spoHash(attachmentUri), attachmentContentStream);
            DAOFactory.get().getDao(SpoBinaryDAO.class).add(dto, attachment.getSize());

            // construct review uri
            String reviewUri = getUser().getReviewUri(reviewId);

            // construct review SubjectDTO
            SubjectDTO subjectDTO = new SubjectDTO(reviewUri, false);

            // add cr:hasAttachment triple to review SubjectDTO
            ObjectDTO objectDTO = new ObjectDTO(attachmentUri, false);
            objectDTO.setSourceUri(reviewUri);
            subjectDTO.addObject(Predicates.CR_HAS_ATTACHMENT, objectDTO);

            HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);

            // persist review SubjectDTO
            helperDAO.addTriples(subjectDTO);

            // make sure both attachment uri and cr:hasAttachment are present in RESOURCE table
            helperDAO.addResource(attachmentUri, attachmentUri);
            helperDAO.addResource(Predicates.CR_HAS_ATTACHMENT, reviewUri);

			// since the review URI was used above as triple source, add it to HARVEST_SOURCE too
			// (but set interval minutes to 0, to avoid it being background-harvested)
			DAOFactory.get().getDao(HarvestSourceDAO.class).addSourceIgnoreDuplicate(
					HarvestSourceDTO.create(reviewUri, true, 0, getUser().getUserName()));
			
            // finally, attempt to harvest the uploaded file's contents
            harvestUploadedFile(attachmentUri, attachment, null, getUserName());
            
        } catch (DAOException daoe) {
            logger.error("Error when storing attachment", daoe);
            addSystemMessage("Error when storing attachment");
        } catch (IOException ioe) {
            logger.error("File could not be successfully uploaded", ioe);
            addSystemMessage("File could not be successfully uploaded");
        } finally {
            IOUtils.closeQuietly(attachmentContentStream);
            deleteUploadedFile(attachment);
        }
    }

    /**
     *
     */
    public void deleteAttachments() {
        if (isUserAuthorized()) {
            try {
                if (attachmentList != null && attachmentList.size() > 0) {
                    for (int i = 0; i < attachmentList.size(); i++) {
                        DAOFactory.get().getDao(HelperDAO.class).deleteAttachment(this.getUser(), 0, attachmentList.get(i));
                    }
                }
            } catch (DAOException ex) {
                logger.error(ex);
                addCautionMessage(ex.getMessage());
            }
        } else {
            addWarningMessage("Only the owner of this review can delete attachments.");
        }
    }

    /**
     * @return
     */
    public List<String> getRaportsListing() {
        return raportsListing;
    }

    /**
     * @param raportsListing
     */
    public void setRaportsListing(List<String> raportsListing) {
        this.raportsListing = raportsListing;
    }

    /**
     * @return
     */
    public ReviewDTO getReview() {

        if (reviewView) {
            try {
                review = DAOFactory.get().getDao(HelperDAO.class).getReview(new CRUser(getAttemptedUserName()), reviewId);
                if (review.getReviewID() == 0) {
                    addCautionMessage("Review with this ID is not found.");
                    review = null;
                } else {
                    // Load attachments list only when it is needed - viewing a review.
                    review.setAttachments(DAOFactory.get().getDao(HelperDAO.class).getReviewAttachmentList(new CRUser(getAttemptedUserName()), reviewId));
                }
            } catch (DAOException ex) {
                logger.error("Error when getting review", ex);
            }
        }

        return review;
    }

    /**
     * @return
     */
    public String getReviewContentHTML() {
        if (review.getReviewContent() != null) {
            return review.getReviewContent().replace("&", "&amp;").replace("<", "&lt;").replace("\r\n", "<br/>").replace("\n", "<br/>");
        } else {
            return "";
        }
    }

    /**
     * @return
     */
    public String getReviewContentForm() {
        if (review.getReviewContent() != null) {
            return review.getReviewContent();
        } else {
            return "";
        }
    }

    /**
     * @return
     */
    public boolean isReviewContentPresent() {
        if (review.getReviewContent() != null && review.getReviewContent().length() > 0) {
            if (review.getReviewContent().replace(" ", "").length() > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * @param review
     */
    public void setReview(ReviewDTO review) {
        review.setReviewContentType("text/plain");
        this.review = review;
    }

    /**
     * @return
     */
    public boolean isTestvar() {
        return testvar;
    }

    /**
     * @param testvar
     */
    public void setTestvar(boolean testvar) {
        this.testvar = testvar;
    }

    /**
     * @return
     */
    public List<ReviewDTO> getReviews() {
        try {
            if (this.isUserAuthorized() && this.getUser() != null) {
                return DAOFactory.get().getDao(HelperDAO.class).getReviewList(this.getUser());
            } else {
                return DAOFactory.get().getDao(HelperDAO.class).getReviewList(new CRUser(this.getAttemptedUserName()));
            }
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * @param reviews
     */
    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    /**
     * @return
     */
    public List<Integer> getReviewIds() {
        return reviewIds;
    }

    /**
     * @param reviewIds
     */
    public void setReviewIds(List<Integer> reviewIds) {
        this.reviewIds = reviewIds;
    }

    /**
     * @return
     */
    public int getReviewId() {
        return reviewId;
    }

    /**
     * @param reviewId
     */
    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    /**
     * @return
     */
    public boolean isReviewView() {
        return reviewView;
    }

    /**
     * @param reviewView
     */
    public void setReviewView(boolean reviewView) {
        this.reviewView = reviewView;
    }

    /**
     * @return
     */
    public FileBean getAttachment() {
        return attachment;
    }

    /**
     * @param attachment
     */
    public void setAttachment(FileBean attachment) {
        this.attachment = attachment;
    }

    /**
     * @return
     */
    public List<String> getAttachmentList() {
        return attachmentList;
    }

    /**
     * @param attachmentList
     */
    public void setAttachmentList(List<String> attachmentList) {
        this.attachmentList = attachmentList;
    }

}
