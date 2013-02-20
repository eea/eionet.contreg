package eionet.cr.web.action.factsheet;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import eionet.cr.common.Predicates;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HarvestSourceDAO;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dao.ReviewsDAO;
import eionet.cr.dto.HarvestSourceDTO;
import eionet.cr.dto.ObjectDTO;
import eionet.cr.dto.ReviewDTO;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.filestore.FileStore;
import eionet.cr.harvest.CurrentHarvests;
import eionet.cr.harvest.HarvestException;
import eionet.cr.harvest.UploadHarvest;
import eionet.cr.util.FolderUtil;
import eionet.cr.web.action.AbstractActionBean;
import eionet.cr.web.security.CRUser;
import eionet.cr.web.util.tabs.FactsheetTabMenuHelper;
import eionet.cr.web.util.tabs.TabElement;

/**
 *
 * @author <a href="mailto:jaak.kapten@tieto.com">Jaak Kapten</a>
 * @author Risto Alt
 *
 */

@UrlBinding("/reviews.action")
public class ReviewsActionBean extends AbstractActionBean {

    /** URI by which the factsheet has been requested. */
    private String uri;
    private List<TabElement> tabs;

    private List<String> raportsListing;

    private ReviewDTO review;
    private boolean obsolete;

    private List<ReviewDTO> reviews;

    private List<Integer> reviewIds;
    private List<String> attachmentList;
    private int reviewId = 0;

    private FileBean attachment;

    /**
     *
     * @return
     * @throws DAOException
     */
    @DefaultHandler
    public Resolution view() throws DAOException {

        if (StringUtils.isBlank(uri) && isUserLoggedIn()) {
            uri = getUser().getReviewsUri();
        }

        HelperDAO helperDAO = DAOFactory.get().getDao(HelperDAO.class);
        SubjectDTO subject = helperDAO.getFactsheet(uri, null, null);

        FactsheetTabMenuHelper helper = new FactsheetTabMenuHelper(uri, subject, factory.getDao(HarvestSourceDAO.class));
        tabs = helper.getTabs(FactsheetTabMenuHelper.TabTitle.REVIEW_FOLDER);

        if (isReviewView()) {
            initReview();
        }

        return new ForwardResolution("/pages/factsheet/reviews.jsp");
    }

    private void initReview() throws DAOException {
        try {
            review = DAOFactory.get().getDao(ReviewsDAO.class).getReview(new CRUser(getAttemptedUserName()), reviewId);
            if (review.getReviewID() == 0) {
                addCautionMessage("Review with this ID is not found.");
                review = null;
            } else {
                review.setReviewContentType("text/plain");

                // Check if review is obsolete
                obsolete =
                    DAOFactory.get().getDao(ReviewsDAO.class)
                    .isReviewObsolete(review.getReviewSubjectUri(), review.getObjectUrl());

                // Load review content from file.
                try {
                    File f = FileStore.getInstance(getAttemptedUserName()).getFile("reviews/review" + reviewId);
                    if (f != null) {
                        String content = FileUtils.readFileToString(f, "UTF-8");
                        review.setReviewContent(content);
                    }
                } catch (IOException e) {
                    addWarningMessage("Error loading content from file.");
                    e.printStackTrace();
                }

                // Load attachments list only when it is needed - viewing a review.
                review.setAttachments(DAOFactory.get().getDao(ReviewsDAO.class)
                        .getReviewAttachmentList(new CRUser(getAttemptedUserName()), reviewId));
            }
        } catch (DAOException ex) {
            logger.error("Error when getting review", ex);
        }
    }

    /**
     *
     */
    public Resolution add() {
        if (isUsersReview()) {
            try {
                reviewId = factory.getDao(ReviewsDAO.class).addReview(review, getUser());
                try {
                    insertFile();
                } catch (IOException e) {
                    // delete triples for this review if storing file has failed
                    factory.getDao(ReviewsDAO.class).deleteReview(getUser(), reviewId, true);
                    e.printStackTrace();
                    addWarningMessage("System error while storing review content to file. The review was not added.");
                }
                addSystemMessage("Review successfully added.");
            } catch (DAOException ex) {
                logger.error(ex);
                ex.printStackTrace();
                addWarningMessage("System error while adding a review. The review was not added.");
            }
        } else {
            addWarningMessage("Only the owner of this home space can add reviews.");
        }
        return new RedirectResolution(this.getClass()).addParameter("uri", uri).addParameter("reviewId", reviewId);
    }

    // Save review content as file under userhome/reviews folder
    private void insertFile() throws IOException {
        if (review != null) {
            String content = review.getReviewContent();
            if (content == null) {
                content = "";
            }
            byte[] bytes = content.getBytes("UTF-8");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            FileStore.getInstance(getUserName()).add("reviews/review" + reviewId, true, byteArrayInputStream);
            byteArrayInputStream.close();
        }
    }

    /**
     *
     */
    public Resolution save() {
        if (isUsersReview()) {
            try {
                factory.getDao(ReviewsDAO.class).saveReview(reviewId, review, getUser());
                insertFile();
                addSystemMessage("Review successfully saved.");
            } catch (IOException ex) {
                logger.error(ex);
                addWarningMessage("System error while saving the review content. The review was not saved.");
            } catch (DAOException ex) {
                logger.error(ex);
                addWarningMessage("System error while saving the review. The review was not saved.");
            }
        } else {
            addWarningMessage("Only the owner of this home space can save reviews.");
        }
        return new RedirectResolution(this.getClass()).addParameter("uri", uri).addParameter("reviewId", reviewId);
    }

    /**
     * @throws DAOException
     */
    public Resolution deleteReviews() throws DAOException {
        if (isUsersReview()) {
            if (reviewIds != null && !reviewIds.isEmpty()) {
                try {
                    List<String> reviewUris = new ArrayList<String>();
                    for (int i = 0; i < reviewIds.size(); i++) {
                        DAOFactory.get().getDao(ReviewsDAO.class).deleteReview(getUser(), reviewIds.get(i), true);
                        // Delete review folder and files
                        FileStore.getInstance(getUserName()).delete("reviews/review" + reviewIds.get(i));
                        FileStore.getInstance(getUserName()).deleteFolder("reviews/" + reviewIds.get(i));

                        reviewUris.add(getUser().getReviewUri(reviewIds.get(i)));
                    }
                    // Delete review harvest sources also
                    if (reviewUris != null && reviewUris.size() > 0) {
                        DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(reviewUris);
                    }
                    addSystemMessage("Selected reviews were deleted.");
                } catch (DAOException ex) {
                    logger.error(ex);
                    addWarningMessage("System error occured during review deletion.");
                }
            } else {
                addCautionMessage("No reviews selected for deletion.");
            }
        } else {
            addWarningMessage("Only the owner of this home space can delete reviews.");
        }
        return new RedirectResolution(this.getClass());
    }

    /**
     * @throws DAOException
     */
    public Resolution deleteSingleReview() throws DAOException {
        if (isUsersReview()) {
            if (reviewId != 0) {
                try {
                    DAOFactory.get().getDao(ReviewsDAO.class).deleteReview(getUser(), reviewId, true);

                    // Delete review folder and files
                    FileStore.getInstance(getUserName()).delete("reviews/review" + reviewId);
                    FileStore.getInstance(getUserName()).deleteFolder("reviews/" + reviewId, true);

                    // Delete review harvest source
                    List<String> reviewUris = new ArrayList<String>();
                    reviewUris.add(getUser().getReviewUri(reviewId));
                    DAOFactory.get().getDao(HarvestSourceDAO.class).removeHarvestSources(reviewUris);

                    addSystemMessage("Review #" + reviewId + " was deleted.");
                    reviewId = 0;
                } catch (DAOException ex) {
                    logger.error(ex);
                    addWarningMessage("System error occured during review deletion.");
                }
            } else {
                addWarningMessage("Review ID is missing!");
            }
        } else {
            addWarningMessage("Only the owner of this home space can delete reviews.");
        }
        return new RedirectResolution(this.getClass());
    }

    /**
     */
    public Resolution upload() {

        if (isUsersReview()) {
            logger.debug("Storing uploaded review attachment, file bean = " + attachment);

            if (attachment != null) {

                String fileName = StringUtils.replace(attachment.getFileName(), " ", "%20");
                // construct attachment uri
                String attachmentUri = getUser().getReviewAttachmentUri(reviewId, fileName);

                InputStream attachmentContentStream = null;
                try {
                    // save attachment into filesystem under user folder
                    String filePath = "reviews/" + reviewId + "/" + fileName;
                    attachmentContentStream = attachment.getInputStream();
                    File file = FileStore.getInstance(getUserName()).add(filePath, true, attachmentContentStream);

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

                    // since the review URI was used above as triple source, add it to HARVEST_SOURCE too
                    // (but set interval minutes to 0, to avoid it being background-harvested)
                    DAOFactory.get().getDao(HarvestSourceDAO.class)
                    .addSourceIgnoreDuplicate(HarvestSourceDTO.create(reviewUri, true, 0, getUser().getUserName()));

                    // finally, attempt to harvest the uploaded file's contents
                    harvestUploadedFile(attachmentUri, file, null, getUser().getUserName(), attachment.getContentType());

                } catch (DAOException daoe) {
                    logger.error("Error when storing attachment", daoe);
                    addSystemMessage("Error when storing attachment");
                } catch (IOException ioe) {
                    logger.error("File could not be successfully uploaded", ioe);
                    addSystemMessage("File could not be successfully uploaded");
                } finally {
                    IOUtils.closeQuietly(attachmentContentStream);
                }
            }
        } else {
            addWarningMessage("Only the owner of this home space can add attachments.");
        }
        return new RedirectResolution(this.getClass()).addParameter("uri", uri).addParameter("reviewId", reviewId);
    }

    /**
     *
     * @param sourceUrl
     * @param uploadedFile
     * @param dcTitle
     */
    private void harvestUploadedFile(String sourceUrl, File file, String dcTitle, String userName, String contentType) {

        // create and store harvest source for the above source url,
        // don't throw exceptions, as an uploaded file does not have to be
        // harvestable
        HarvestSourceDTO harvestSourceDTO = null;
        try {
            logger.debug("Creating and storing harvest source");
            HarvestSourceDAO dao = DAOFactory.get().getDao(HarvestSourceDAO.class);

            HarvestSourceDTO source = new HarvestSourceDTO();
            source.setUrl(sourceUrl);
            source.setIntervalMinutes(0);

            dao.addSourceIgnoreDuplicate(source);
            harvestSourceDTO = dao.getHarvestSourceByUrl(sourceUrl);
        } catch (DAOException e) {
            logger.info("Exception when trying to create harvest source for the uploaded file content", e);
        }

        // perform harvest,
        // don't throw exceptions, as an uploaded file does not HAVE to be
        // harvestable
        try {
            if (harvestSourceDTO != null) {
                UploadHarvest uploadHarvest = new UploadHarvest(harvestSourceDTO, file, dcTitle, contentType);
                CurrentHarvests.addOnDemandHarvest(harvestSourceDTO.getUrl(), userName);
                try {
                    uploadHarvest.execute();
                } finally {
                    CurrentHarvests.removeOnDemandHarvest(harvestSourceDTO.getUrl());
                }
            } else {
                logger.debug("Harvest source was not created, so skipping harvest");
            }
        } catch (HarvestException e) {
            logger.info("Exception when trying to harvest uploaded file content", e);
        }
    }

    /**
     *
     */
    public Resolution deleteAttachments() {
        if (isUsersReview()) {
            try {
                if (attachmentList != null && attachmentList.size() > 0) {
                    for (int i = 0; i < attachmentList.size(); i++) {
                        DAOFactory.get().getDao(ReviewsDAO.class).deleteAttachment(getUser(), reviewId, attachmentList.get(i));
                        String fileName = StringUtils.substringAfterLast(attachmentList.get(i), "/");
                        FileStore.getInstance(getUserName()).delete("reviews/" + reviewId + "/" + fileName);
                    }
                }
            } catch (DAOException ex) {
                logger.error(ex);
                addCautionMessage(ex.getMessage());
            }
        } else {
            addWarningMessage("Only the owner of this review can delete attachments.");
        }
        return new RedirectResolution(this.getClass()).addParameter("uri", uri).addParameter("reviewId", reviewId);
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
     * Extracts username from review uri
     *
     * @return username
     */
    public String getAttemptedUserName() {
        return FolderUtil.extractUserName(uri);
    }

    /**
     * @return
     */
    public ReviewDTO getReview() {
        return review;
    }

    /**
     * @return
     */
    public String getReviewContentHTML() {
        if (review.getReviewContent() != null) {
            return review.getReviewContent().replace("&", "&amp;").replace("<", "&lt;").replace("\r\n", "<br/>")
            .replace("\n", "<br/>");
        } else {
            return "";
        }
    }

    /**
     * @return
     */
    public boolean isReviewContentPresent() {

        if (review.getReviewContent() != null && review.getReviewContent().length() > 0) {
            return review.getReviewContent().replace(" ", "").length() > 0;
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
     * @throws DAOException
     */
    public List<ReviewDTO> getReviews() throws DAOException {

        if (CollectionUtils.isEmpty(reviews)) {
            if (getUser() != null) {
                reviews = DAOFactory.get().getDao(ReviewsDAO.class).getReviewList(getUser());
            } else {
                reviews = DAOFactory.get().getDao(ReviewsDAO.class).getReviewList(new CRUser(getAttemptedUserName()));
            }
        }

        return reviews;
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
        boolean ret = false;
        if (reviewId != 0) {
            ret = true;
        }
        return ret;
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

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<TabElement> getTabs() {
        return tabs;
    }

    /**
     * True, if the review with given uri belongs to the currently logged in user.
     *
     * @return
     */
    public boolean isUsersReview() {
        boolean ret = false;
        if (!StringUtils.isBlank(uri) && getUser() != null) {
            ret = uri.startsWith(getUser().getHomeUri());
        }
        return ret;
    }

    public boolean isObsolete() {
        return obsolete;
    }
}
