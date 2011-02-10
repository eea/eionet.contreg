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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.BindingSet;

/**
 * 
 * @author heinljab
 *
 */
public class SQLValueReader extends ResultSetListReader<Map<String,SQLValue>>{
	
	/** */
	private List<Map<String,SQLValue>> result = new ArrayList<Map<String,SQLValue>>();
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
		int colCount = sqlResultSetMetadata.getColumnCount();
		Map<String,SQLValue> rowMap = new HashMap<String,SQLValue>();
		for (int i=1; i<=colCount; i++){
			String colName = sqlResultSetMetadata.getColumnName(i);
			int colSQLType = sqlResultSetMetadata.getColumnType(i);
			rowMap.put(colName, new SQLValue(rs.getObject(i), colSQLType));
		}
		if (rowMap.size()>0)
			result.add(rowMap);
	}

	/**
	 * 
	 * @return
	 */
	public List<Map<String,SQLValue>> getResultList(){
		return result.size()==0 ? null : result;
	}

	@Override
	public void readTuple(BindingSet bindingSet) {
		// TODO Auto-generated method stub
		
	}
}
