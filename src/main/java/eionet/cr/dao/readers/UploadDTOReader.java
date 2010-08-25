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
import java.util.Collection;
import java.util.LinkedHashMap;

import eionet.cr.common.Predicates;
import eionet.cr.dto.UploadDTO;
import eionet.cr.util.Hashes;
import eionet.cr.util.sql.ResultSetBaseReader;

/**
 * 
 * @author <a href="mailto:jaanus.heinlaid@tietoenator.com">Jaanus Heinlaid</a>
 *
 */
public class UploadDTOReader extends ResultSetBaseReader{
	
	/** */
	private static final long labelHash = Hashes.spoHash(Predicates.RDFS_LABEL);
	private static final long lastModifiedHash = Hashes.spoHash(Predicates.CR_LAST_MODIFIED);

	/** */
	private LinkedHashMap<String,UploadDTO> uploadsMap = new LinkedHashMap<String,UploadDTO>();
	
	/*
	 * (non-Javadoc)
	 * @see eionet.cr.util.sql.ResultSetBaseReader#readRow(java.sql.ResultSet)
	 */
	public void readRow(ResultSet rs) throws SQLException {
		
		String subjectUri = rs.getString("URI");
		UploadDTO uploadDTO = uploadsMap.get(subjectUri);
		if (uploadDTO==null){
			uploadDTO = new UploadDTO(subjectUri);
			uploadsMap.put(subjectUri, uploadDTO);
		}
		
		long predicateHash = rs.getLong("PREDICATE");
		String objectValue = rs.getString("OBJECT");
		
		if (predicateHash==labelHash){
			uploadDTO.setLabel(objectValue);
		}
		else if (predicateHash==lastModifiedHash){
			uploadDTO.setDateModified(objectValue);
		}
	}

	/**
	 * 
	 * @return
	 */
	public Collection<UploadDTO> getResultList(){
		return uploadsMap.values();
	}
}
