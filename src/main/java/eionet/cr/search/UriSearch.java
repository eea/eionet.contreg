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
	private String uri;
	private Long uriHash;
	
	/**
	 * 
	 * @param uri
	 */
	public UriSearch(String uri){
		this.uri = uri;
		if (uri!=null){
			uriHash = new Long(Hashes.spoHash(uri));
		}
	}

	/**
	 * 
	 * @param uriHash
	 */
	public UriSearch(Long uriHash){
		this.uriHash = uriHash;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (uriHash==null)
			return null;
		
		inParameters.add(uriHash);
		return "select SUBJECT as SUBJECT_HASH from SPO where SUBJECT=?";
	}
}
