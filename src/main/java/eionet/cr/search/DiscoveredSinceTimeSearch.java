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
package eionet.cr.search;

import java.util.Date;
import java.util.List;

import eionet.cr.common.Predicates;
import eionet.cr.common.Subjects;
import eionet.cr.util.Hashes;
import eionet.cr.util.pagination.Pagination;

public class DiscoveredSinceTimeSearch extends AbstractSubjectSearch {
	
	/** */
	private static final int DEFAULT_LIMIT = 300;
	
	/** */
	private Date time;
	
	/**
	 * 
	 * @param time
	 */
	public DiscoveredSinceTimeSearch(Date time){
		this.time = time;
	}

	/*
	 * (non-Javadoc)
	 * @see eionet.cr.search.AbstractSubjectSearch#getSubjectSelectSQL(java.util.List)
	 */
	protected String getSubjectSelectSQL(List inParameters) {
		
		if (time==null)
			return null;
		
		StringBuffer sqlBuf = new StringBuffer().
		append("select SPO.SUBJECT as SUBJECT_HASH from SPO, RESOURCE").
		append(" where SPO.PREDICATE=? and SPO.OBJECT_HASH=? and SPO.SUBJECT=RESOURCE.URI_HASH ").
		append(" and RESOURCE.FIRSTSEEN_TIME>?").
		append(" order by RESOURCE.FIRSTSEEN_TIME desc");
		
		inParameters.add(Hashes.spoHash(Predicates.RDF_TYPE));
		inParameters.add(Hashes.spoHash(Subjects.CR_FILE));
		inParameters.add(Long.valueOf(time.getTime()));
		
		if (pageLength>0){
			sqlBuf.append(" limit ");
			if (pageNumber>0){
				sqlBuf.append("?,");
				inParameters.add(new Integer((pageNumber-1)*pageLength));
			}
			sqlBuf.append(pageLength);
		}
		else
			sqlBuf.append(" limit ").append(DEFAULT_LIMIT);
		
		return sqlBuf.toString();
	}
}
