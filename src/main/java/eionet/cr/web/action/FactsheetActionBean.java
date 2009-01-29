package eionet.cr.web.action;

import java.util.Collection;
import java.util.List;

import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.UrlBinding;
import eionet.cr.dto.SubjectDTO;
import eionet.cr.search.SearchException;
import eionet.cr.search.Searcher;
import eionet.cr.search.UriSearch;
import eionet.cr.search.util.SearchExpression;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
@UrlBinding("/factsheet.action")
public class FactsheetActionBean extends AbstractActionBean{

	/** */
	private String uri;
	private SubjectDTO subject;

	/**
	 * 
	 * @return
	 * @throws SearchException
	 */
	@DefaultHandler
	public Resolution view() throws SearchException{
		
		UriSearch uriSearch = new UriSearch(new SearchExpression(uri));
		uriSearch.execute();
		Collection<SubjectDTO> coll = uriSearch.getResultList();
		if (coll!=null && !coll.isEmpty())
			subject = coll.iterator().next();

		return new ForwardResolution("/pages/factsheet.jsp");
	}

	/**
	 * @return the resourceUri
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * @param resourceUri the resourceUri to set
	 */
	public void setUri(String resourceUri) {
		this.uri = resourceUri;
	}

	/**
	 * @return the resource
	 */
	public SubjectDTO getSubject() {
		return subject;
	}
}
