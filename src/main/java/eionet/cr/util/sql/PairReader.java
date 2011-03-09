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
import java.util.List;

import eionet.cr.dao.readers.ResultSetReaderException;
import eionet.cr.util.Pair;

/**
 * @author Aleksandr Ivanov
 * <a href="mailto:aleksandr.ivanov@tietoenator.com">contact</a>
 */
public class PairReader<T, T1> extends SQLResultSetBaseReader<Pair<T, T1>> {

	/** */
	public static final String LEFTCOL = "LCOL";
	public static final String RIGHTCOL = "RCOL";
	
	/** 
	 * @see eionet.cr.util.sql.ResultSetListReader#getResultList()
	 * {@inheritDoc}
	 */
	@Override
	public List<Pair<T, T1>> getResultList() {
		return resultList;
	}

	/** 
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void readRow(ResultSet rs) throws SQLException, ResultSetReaderException {
		resultList.add(new Pair<T, T1>((T)rs.getObject(LEFTCOL), (T1)rs.getObject(RIGHTCOL)));
	}
	
}
