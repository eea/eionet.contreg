package eionet.cr.search;

import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.search.util.SearchExpression;
import eionet.cr.search.util.SubjectDataReader;
import eionet.cr.util.Hashes;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UriSearch extends AbstractSubjectSearch{
	
	/** */
	private SearchExpression uri;
	
	/**
	 * 
	 * @param uri
	 */
	public UriSearch(SearchExpression uri){
		this.uri = uri;
	}

	/**
	 * 
	 * @param uri
	 */
	public UriSearch(String uri){
		this(new SearchExpression(uri));
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (uri==null || uri.isEmpty())
			return null;
		
		inParameters.add(Hashes.spoHash(uri.toString()));
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
