package eionet.cr.search;

import java.util.List;

import eionet.cr.search.util.SearchExpression;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UriSearch extends RDFSubjectSearch{
	
	/** */
	private SearchExpression uri;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.RDFSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List<Object> inParameters) {
		
		if (uri==null || uri.isEmpty())
			return null;
		
		inParameters.add(uri.toString());
		return "select sql_calc_found_rows distinct SUBJECT from SPO where SUBJECT=?";
	}

	/**
	 * @return the uri
	 */
	public SearchExpression getUri() {
		return uri;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(SearchExpression uri) {
		this.uri = uri;
	}

}
