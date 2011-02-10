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
package eionet.cr.util.sql;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.openrdf.query.BindingSet;

import eionet.cr.dao.readers.TupleResultSetReader;

/**
 * 
 * @author heinljab
 *
 */
public abstract class ResultSetBaseReader implements SQLResultSetReader, TupleResultSetReader{
	
	/** */
	protected ResultSetMetaData sqlResultSetMetadata = null;
	protected List<String> tupleResultSetBindingNames = null;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.SQLResultSetReader#setResultSetMetaData(java.sql.ResultSetMetaData)
	 */
	@Override
	public void setResultSetMetaData(ResultSetMetaData rsMd){
		this.sqlResultSetMetadata = rsMd;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.SQLResultSetReader#readRow(java.sql.ResultSet)
	 */
	@Override
	public abstract void readRow(ResultSet rs) throws SQLException;

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.readers.TupleResultSetReader#setResultSetBindingNames()
	 */
	@Override
	public void setResultSetBindingNames(){
	}
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.dao.readers.TupleResultSetReader#readTuple(org.openrdf.query.BindingSet)
	 */
	@Override
	public abstract void readTuple(BindingSet bindingSet);
}
