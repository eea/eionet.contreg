/*
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code is Content Registry 2.0.
 *
 * The Initial Owner of the Original Code is European Environment
 * Agency.  Portions created by Tieto Eesti are Copyright
 * (C) European Environment Agency.  All Rights Reserved.
 *
 * Contributor(s):
 * Jaanus Heinlaid, Tieto Eesti
 */
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.BindingSet;

import eionet.cr.dao.util.PredicateLabels;
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

	@Override
	public void readTuple(BindingSet bindingSet) {
		// TODO Auto-generated method stub
		
	}
}
