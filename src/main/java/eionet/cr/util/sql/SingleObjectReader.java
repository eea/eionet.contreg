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
 * Aleksandr Ivanov, Tieto Eesti
 */
package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.openrdf.query.BindingSet;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class SingleObjectReader<T> extends ResultSetListReader<T> {
	
	private List<T> resultList = new LinkedList<T>();

	/** 
	 * @see eionet.cr.util.sql.ResultSetListReader#getResultList()
	 * {@inheritDoc}
	 */
	@Override
	public List<T> getResultList() {
		return resultList;
	}

	/** 
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void readRow(ResultSet rs) throws SQLException {
		resultList.add((T) rs.getObject(1));
	}

	@Override
	public void readTuple(BindingSet bindingSet) {
		// TODO Auto-generated method stub
		
	}

}
