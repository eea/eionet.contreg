package eionet.cr.web.action.home;

import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.HandlesEvent;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dao.DAOException;
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
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@DefaultHandler
	public Resolution view() throws DAOException {
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_REVIEWS);
		return new ForwardResolution("/pages/home/reviews.jsp");
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.web.action.AbstractSearchActionBean#search()
	 */
	@HandlesEvent("add")
	public Resolution add() throws DAOException {
		setEnvironmentParams(this.getContext(), AbstractHomeActionBean.TYPE_REVIEWS);
		return new ForwardResolution("/pages/home/reviews.jsp");
	}
	
	public List<String> getRaportsListing() {
		return raportsListing;
	}

	public void setRaportsListing(List<String> raportsListing) {
		this.raportsListing = raportsListing;
	}

	public ReviewDTO getReview() {
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

}
