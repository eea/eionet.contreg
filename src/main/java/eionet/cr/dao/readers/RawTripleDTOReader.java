/*
* The contents of this file are subject to the Mozilla Public
* 
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
* Agency. Portions created by Tieto Eesti are Copyright
* (C) European Environment Agency. All Rights Reserved.
* 
* Contributor(s):
* Jaanus Heinlaid, Tieto Eesti*/
package eionet.cr.dao.readers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import eionet.cr.dto.RawTripleDTO;
import eionet.cr.util.sql.ResultSetListReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class RawTripleDTOReader extends ResultSetListReader<RawTripleDTO>{

	/** */
	private List<RawTripleDTO> resultList = new LinkedList<RawTripleDTO>();
	
	/** */
	private HashSet<String> distinctHashes = new HashSet<String>();
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetListReader#getResultList()
	 */
	@Override
	public List<RawTripleDTO> getResultList() {
		return resultList;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	@Override
	public void readRow(ResultSet rs) throws SQLException {
		
		RawTripleDTO dto = new RawTripleDTO(rs.getString("SUBJECT"),rs.getString("PREDICATE"),
							rs.getString("OBJECT"), rs.getString("OBJ_DERIV_SOURCE"));
		resultList.add(dto);
		
		distinctHashes.add(dto.getSubject());
		distinctHashes.add(dto.getPredicate());
		distinctHashes.add(dto.getObjectDerivSource());
	}

	/**
	 * @return the distinctHashes
	 */
	public HashSet<String> getDistinctHashes() {
		return distinctHashes;
	}
}
