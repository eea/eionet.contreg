package eionet.cr.web.action.home;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
import eionet.cr.dao.DAOFactory;
import eionet.cr.dao.HelperDAO;
import eionet.cr.dto.ReviewDTO;

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
	
	private List<String> reviewSubjectUrls;
	private int reviewId = 0;
	private boolean reviewView;
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {

		loadReview();
		
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_REVIEWS);
		if (this.getContext().getRequest().getParameter("save") != null){
			save();
		}
		
		if (this.getContext().getRequest().getParameter("delete") != null){
			deleteReviews();
		}
		
		return new ForwardResolution("/pages/home/reviews.jsp");
	}
	
	private void loadReview(){
		String reviewString="";
		try {
			reviewString = this.getContext().getRequest().getRequestURL().toString().split("reviews/")[1];
			try {
				reviewId = Integer.parseInt(reviewString);
				if (reviewId > 0){
					this.setHomeContext(false); // Do not show tabs and headers.
					reviewView = true;
				}
			} catch (Exception ex){
				addCautionMessage("Not correct review ID. Only numerical values allowed after /reviews/.");
				this.setHomeContext(false);
				reviewId = 0;
			}
		} catch (Exception ex) {
			// Meaning that the split didn't succeed because there is no ID following.
			reviewId = 0;
		}
		
		
	}
	
	public void save() {
		try {
			factory.getDao(HelperDAO.class).addReview(review, this.getUser());
			addSystemMessage("Review successfully saved.");
		} catch (DAOException ex){
			addWarningMessage("Error while saving a review. The review was not saved.");
		}
		review.setTitle("Saved");
	}
	
	public void deleteReviews() throws DAOException{
		if (this.getContext().getRequest().getParameter("delete") != null){
			if (reviewSubjectUrls != null && !reviewSubjectUrls.isEmpty()) {
				try {
					for (int i=0; i<reviewSubjectUrls.size(); i++){
						DAOFactory.get().getDao(HelperDAO.class).deleteReview(reviewSubjectUrls.get(i));
					}
					addSystemMessage("Selected reviews were deleted.");
				} catch (DAOException ex){
					addWarningMessage("Error occured during review deletion.");
				}
			} else {
				addCautionMessage("No reviews selected for deletion.");
			}
		}
	}
	
	
	public List<String> getRaportsListing() {
		return raportsListing;
	}

	public void setRaportsListing(List<String> raportsListing) {
		this.raportsListing = raportsListing;
	}

	public ReviewDTO getReview() {
		if (reviewView){
			try {
				review = DAOFactory.get().getDao(HelperDAO.class).getReview(this.getUser(), reviewId);
			} catch (Exception ex){
			}
		} else {
		}
		return review;
	}

	public void setReview(ReviewDTO review) {
		this.review = review;
	}

	public boolean isTestvar() {
		return testvar;
	}

	public void setTestvar(boolean testvar) {
		this.testvar = testvar;
	}

	public List<ReviewDTO> getReviews() {
		try {
			return DAOFactory.get().getDao(HelperDAO.class).getReviewList(this.getUser());
		} catch (Exception ex){
			return null;
		}
	}

	public void setReviews(List<ReviewDTO> reviews) {
		this.reviews = reviews;
	}

	public List<String> getReviewSubjectUrls() {
		return reviewSubjectUrls;
	}

	public void setReviewSubjectUrls(List<String> reviewSubjectUrls) {
		this.reviewSubjectUrls = reviewSubjectUrls;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public boolean isReviewView() {
		return reviewView;
	}

	public void setReviewView(boolean reviewView) {
		this.reviewView = reviewView;
	}
	

}
