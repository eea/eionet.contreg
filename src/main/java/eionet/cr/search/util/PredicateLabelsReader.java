package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import eionet.cr.dto.ObjectDTO;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class PredicateLabelsReader extends ResultSetBaseReader{
	
	/** */
	private PredicateLabels predicateLabels;
	
	/**
	 * 
	 * @param predicateLabels
	 */
	public PredicateLabelsReader(PredicateLabels predicateLabels){
		
		if (predicateLabels==null)
			throw new IllegalArgumentException();
		
		this.predicateLabels = predicateLabels;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		predicateLabels.add(rs.getString("PREDICATE_URI"), rs.getString("LABEL"), rs.getString("LANG"));
	}
}
