package eionet.cr.web.util.search;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PredicateBasedColumn extends SearchResultColumn{

	/** */
	private String predicateUri;
	
	/**
	 * @return the predicateUri
	 */
	public String getPredicateUri() {
		return predicateUri;
	}
	/**
	 * @param predicateUri the predicateUri to set
	 */
	public void setPredicateUri(String predicateUri) {
		this.predicateUri = predicateUri;
	}
}
