package eionet.cr.search.util;

import java.sql.ResultSet;
import java.sql.SQLException;

import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class SubPropertiesReader extends ResultSetBaseReader{

	/** */
	private SubProperties subProperties;
	
	/**
	 * 
	 * @param subProperties
	 */
	public SubPropertiesReader(SubProperties subProperties) {
		
		if (subProperties==null)
			throw new IllegalArgumentException();

		this.subProperties = subProperties;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		subProperties.add(rs.getString("PREDICATE"), rs.getString("SUB_PROPERTY"));
	}
}
